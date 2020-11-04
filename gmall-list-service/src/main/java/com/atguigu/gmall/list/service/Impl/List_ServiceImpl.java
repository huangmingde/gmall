package com.atguigu.gmall.list.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfo_es;
import com.atguigu.gmall.bean.Sku_SearchParams;
import com.atguigu.gmall.bean.Sku_SearchResult;
import com.atguigu.gmall.service.List_Service;
import com.atguigu.gmall.utils.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service    //暴露服务
public class List_ServiceImpl implements List_Service{

    //注入操作es的jestClient
    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX="gmall";
    public static final String ES_TYPE="SkuInfo";

    //注入redis工具类
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 功能：保存skuInfo到es
     * PUT /gmall/SkuInfo/1
     */
    @Override
    public void saveSkuInfo_es(SkuInfo_es skuInfoEs) throws IOException {
        Index index = new Index.Builder(skuInfoEs).index(ES_INDEX).type(ES_TYPE).id(skuInfoEs.getId()).build();
        DocumentResult docResult = this.jestClient.execute(index);
    }

    /**
     * 功能：搜索skuInfo
     */
    @Override
    public Sku_SearchResult searchSkuInfo_fromES(Sku_SearchParams skuSearchParams) throws IOException {
        //1、定义DSL语句
        String  query=getSkuInfo_dsl(skuSearchParams);
        //2、定义GET命令     GET /gmall/SkuInfo/_search
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        //3、执行搜索,得到DSL命令的搜索结果集
        SearchResult searchResult = this.jestClient.execute(search);
        //4、处理搜索结果集
        Sku_SearchResult skuSearchResult = makeSkuInfoSearchResult(searchResult,skuSearchParams);
        return skuSearchResult;
    }


    /**
     * 记录每个商品的详情页被用户访问的次数
     */
    @Override
    public void incrHotScore(String skuId) throws IOException {
        Jedis jedis = redisUtil.getJedis();
        //给集合“hotScore”的元素“skuId:888”添加增量“1”。并得到增加增量的结果“hotScore”
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        //每访问10次商品详情页，就更新一次es
        if (hotScore%10==0){
            //Math.round()：四舍五入
            updateHotScore(skuId,  Math.round(hotScore));
        }
    }

    /**
     *  达到10次的访问次数，更新es
     */
    private void updateHotScore(String skuId, long hotScore) throws IOException {
        /*
            1、定义dsl语句
            2、定义动作
            3、执行动作
         */
        String upd = "{\n" +
                        "  \"doc\": {\n" +
                        "    \"hotScore\": "+hotScore+"\n" +
                        "  }\n" +
                        "}";

        Update updHotScore = new Update.Builder(upd).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
        jestClient.execute(updHotScore);
    }

    /**
     *定义方法-获取skuInfo的DSL语句
     */
    private String getSkuInfo_dsl(Sku_SearchParams skuSearchParams) {
        //定义查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.1、创建bool
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        //1.2、为bool设置filter
        if (skuSearchParams.getCatalog3Id()!=null && skuSearchParams.getCatalog3Id().length()>0){
            TermQueryBuilder term = new TermQueryBuilder("catalog3Id",skuSearchParams.getCatalog3Id());
            bool.filter(term);
        }
        if (skuSearchParams.getValueId()!=null && skuSearchParams.getValueId().length>0){
            //  遍历valueId的数组，取出valueId
            for (String valueId : skuSearchParams.getValueId()) {
                TermQueryBuilder term = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                bool.filter(term);
            }
        }
        //1.3、为bool设置must
        if (skuSearchParams.getKeyword()!=null && skuSearchParams.getKeyword().length()>0){
            MatchQueryBuilder match = new MatchQueryBuilder("skuName",skuSearchParams.getKeyword());
            bool.must(match);

            //顺便设置高亮
            //获取高亮对象
            HighlightBuilder highlight = searchSourceBuilder.highlight();
            //设置高亮规则
            highlight.field("skuName");
            highlight.preTags("<span style=color:red>");
            highlight.postTags("</span>");
            //把高亮对象设置进查询器
            searchSourceBuilder.highlight(highlight);
        }

        //1、查询前过滤：query-bool-filter-term；query-bool-must-match
        searchSourceBuilder.query(bool);
        //2、分页
        //每页20条数据。  第1页 0 20；第2页 20 20；第3页 40 20
        int from = (skuSearchParams.getPageNo()-1)*skuSearchParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuSearchParams.getPageSize());
        //3、排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);   //热度由高到低
        //4、聚合:aggs-term-field
        TermsBuilder terms = AggregationBuilders.terms("groupby_attr");
        terms.field("skuAttrValueList.valueId");
        //放入查询器
        searchSourceBuilder.aggregation(terms);

        String query = searchSourceBuilder.toString();
        System.out.println("---------------------query="+query);
        return query;
    }

    /**
     * 定义方法-处理搜索结果集
     */
    private Sku_SearchResult makeSkuInfoSearchResult(SearchResult searchResult, Sku_SearchParams skuSearchParams) {
//        声明sku搜索结果集对象
        Sku_SearchResult skuSearchResult = new Sku_SearchResult();


//        List<SkuInfo_es> skuInfoList_es;
        //声明一个集合存储SkuInfo_es
        List<SkuInfo_es> skuInfoEsList = new ArrayList<>();
        //从搜索结果集searchResult中，获取到skuInfoEs、设置高亮，再存储到集合中
        List<SearchResult.Hit<SkuInfo_es, Void>> hits = searchResult.getHits(SkuInfo_es.class);
        for (SearchResult.Hit<SkuInfo_es, Void> hit : hits) {
            //获取skuInfoEs
            SkuInfo_es skuInfoEs = hit.source;
            //设置高亮
            if (hit.highlight!=null && hit.highlight.size()>0){
                Map<String, List<String>> hili_Map = hit.highlight;
                List<String> hili_List = hili_Map.get("skuName");
                String skuName_hili = hili_List.get(0);
                skuInfoEs.setSkuName(skuName_hili);
            }
            skuInfoEsList.add(skuInfoEs);
        }
        skuSearchResult.setSkuInfoList_es(skuInfoEsList);

//        long total;
        skuSearchResult.setTotal(searchResult.getTotal());

//        long totalPages;
        //10条数据，每页显示3条，一共几页？4页。怎么算的？10%3，等于3，余1。3+1=4
        /*long totalPages = searchResult.getTotal()%skuSearchParams.getPageSize()==0?
                searchResult.getTotal()/skuSearchParams.getPageSize():(searchResult.getTotal()/skuSearchParams.getPageSize())+1;*/
        //新公式：（总数+页数-1）/页数
        long totalPages = (searchResult.getTotal()+skuSearchParams.getPageSize()-1)/skuSearchParams.getPageSize();
        skuSearchResult.setTotalPages(totalPages);

//        List<String> attrValueIdList;
        //从搜索结果集中,获得分组后的attrValueId，并放进集合。
        List<String> skuAttrValueIdList = new ArrayList();
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            String skuAttrValueId = bucket.getKey();
            skuAttrValueIdList.add(skuAttrValueId);
        }
        skuSearchResult.setAttrValueIdList(skuAttrValueIdList);

        return skuSearchResult;
    }

}
