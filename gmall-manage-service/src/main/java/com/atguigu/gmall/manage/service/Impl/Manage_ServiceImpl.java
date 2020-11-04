package com.atguigu.gmall.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.constant.ManageConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.Manage_Service;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service    //dubbo，注册服务到ZK注册中心
public class Manage_ServiceImpl implements Manage_Service {

    //注入mapper
    @Autowired
    private BaseCatalog1_Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2_Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3_Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfo_Mapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValue_Mapper baseAttrValueMapper;

    @Autowired
    private SpuInfo_Mapper spuInfoMapper;

    @Autowired
    private BaseSaleAttr_Mapper baseSaleAttrMapper;

    @Autowired
    private SpuImage_Mapper spuImageMapper;

    @Autowired
    private SpuSaleAttr_Mapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValue_Mapper spuSaleAttrValueMapper;

    // 查询所有一级分类数据
    @Override
    public List<BaseCatalog1> getBaseCatalog1() {
        return this.baseCatalog1Mapper.selectAll();
    }

    // 根据一级分类id，查询所有二级分类数据
    //select * from BaseCatalog2 where catalog1_id = ?
    @Override
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id) {
        BaseCatalog2 catalog2 = new BaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);
        return this.baseCatalog2Mapper.select(catalog2);
    }

    // 根据二级分类id，查询所有三级分类数据
    @Override
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id) {
        BaseCatalog3 catalog3 = new BaseCatalog3();
        catalog3.setCatalog2Id(catalog2Id);
        return this.baseCatalog3Mapper.select(catalog3);
    }

    // 根据三级分类id，查询平台属性集合
    @Override
    public List<BaseAttrInfo> getBaseAttrInfo_list(String catalog3Id) {
        /*BaseAttrInfo attrInfo = new BaseAttrInfo();
        attrInfo.setCatalog3Id(catalog3Id);
        return this.baseAttrInfoMapper.select(attrInfo);*/
        return this.baseAttrInfoMapper.getBaseAttrInfoList_ByCatalog3Id(catalog3Id);
    }

    /*
      插入平台属性（包含平台属性值）
      修改平台属性值
    */
    @Transactional  //事务控制
    @Override
    public void saveAttrInfo_AttrValue(BaseAttrInfo baseAttrInfo) {
        //  修改数据：BaseAttrInfo （页面有修改数据-平台属性值）
        //if(baseAttrInfo.getId()!=null || baseAttrInfo.getId().length()>0){    //老师写错了
        if(baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){
            this.baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            //保存数据：BaseAttrInfo
            this.baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }


        /*
            可以先删除数据（平台属性值），再保存数据
            delete from BaseAttrValue where attrId = baseAttrInfo.getId()
        */
        BaseAttrValue attrValue_del = new BaseAttrValue();
        attrValue_del.setAttrId(baseAttrInfo.getId());
        this.baseAttrValueMapper.delete(attrValue_del);


        //保存数据：BaseAttrValue
        List<BaseAttrValue> attrValue_list = baseAttrInfo.getAttrValueList();
        //注意：这么写会空指针异常：if(attrValue_list.size()>0 && attrValue_list != null)。
        if(attrValue_list != null && attrValue_list.size()>0){
            //循环插入多个BaseAttrValue
            for (BaseAttrValue baseAttrValue : attrValue_list) {
                /*
                baseAttrValue的属性
                    private String id;    //从数据库中观察到该id自增
                    private String valueName;   //该值可以从页面中获取
                    private String attrId;      //attrId=baseAttrInfo.getId() ,前提是baseAttrInfo对象的主键必须自增
                */
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                this.baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }


    //  根据平台属性id，查询平台属性值的集合（点击修改平台属性后，回显平台属性值）
    //  select * from BaseAttrValue where attrId = ?
    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue attrValue = new BaseAttrValue();
        attrValue.setAttrId(attrId);

        return this.baseAttrValueMapper.select(attrValue);
    }


    @Override
    public BaseAttrInfo getBaseAttrInfo_ById(String attrId) {
        //1、先查平台属性（根据attrId）
        //select * from  BaseAttrInfo  where  id  =  attrId
        BaseAttrInfo baseAttrInfo = this.baseAttrInfoMapper.selectByPrimaryKey(attrId);

        //2、把平台属性值集合，设置进平台属性
                // 查询平台属性值集合（有“attrId”的）  select * from BaseAttrValue where attrId = ?
                BaseAttrValue attrValue = new BaseAttrValue();
                attrValue.setAttrId(attrId);
                List<BaseAttrValue> attrValueList = this.baseAttrValueMapper.select(attrValue);
        baseAttrInfo.setAttrValueList(attrValueList);
        return baseAttrInfo;
    }

    //  ===============  SPU   ====================================
    //  根据三级id，查询所有SPU信息
    @Override
    public List<SpuInfo> getSpuList(SpuInfo spuInfo) {
        return this.spuInfoMapper.select(spuInfo);
    }

    /**
     * 查询基本的销售属性 。
     * 控制器：http://localhost:8082/baseSaleAttrList
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttr_List = this.baseSaleAttrMapper.selectAll();
        return baseSaleAttr_List;
    }

    /**
     * 保存SpuInfo信息【SpuImage、SpuSaleAttr（SpuSaleAttrValue）】
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存SpuInfo
        this.spuInfoMapper.insertSelective(spuInfo);

        //保存SpuImage
        List<SpuImage> spuImage_list = spuInfo.getSpuImageList();
        if (spuImage_list!=null && spuImage_list.size()>0){
            //循环插入多张图片
            for (SpuImage spuImage : spuImage_list) {
                //为spuImage设置spu_id。
                spuImage.setSpuId(spuInfo.getId());
                this.spuImageMapper.insertSelective(spuImage);
            }
        }

        //保存SpuSaleAttr
        List<SpuSaleAttr> spuSaleAttr_list = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttr_list!=null && spuSaleAttr_list.size()>0){
            //循环插入多个商品销售属性
            for (SpuSaleAttr spuSaleAttr : spuSaleAttr_list) {
                //为spuSaleAttr设置spu_id
                spuSaleAttr.setSpuId(spuInfo.getId());
                this.spuSaleAttrMapper.insertSelective(spuSaleAttr);

                    //保存SpuSaleAttrValue
                    List<SpuSaleAttrValue> spuSaleAttrValue_list = spuSaleAttr.getSpuSaleAttrValueList();
                    if (spuSaleAttrValue_list!=null && spuSaleAttrValue_list.size()>0){
                        //循环插入商品销售属性值
                        for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValue_list) {
                            //为spuSaleAttrValue设置spu_id
                            spuSaleAttrValue.setSpuId(spuInfo.getId());
                            this.spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                        }
                    }
            }

        }
    }

    // =========================    保存SKU    ===================================

    /**
     *  加载SPU图片
     *  控制器：http://localhost:8082/spuImageList?spuId=58
     *  sql:  select * from SpuImage where spuId = SpuImage.getSpuId
     */
    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        List<SpuImage> spuImage_list = this.spuImageMapper.select(spuImage);
        return spuImage_list;
    }

    /**
     * 功能：加载销售属性和销售属性值
     * 控制器：http://localhost:8082/spuSaleAttrList?spuId=76
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        //没有什么其他的操作，直接调用mapper接口就好了
        return this.spuSaleAttrMapper.getSpuSaleAttrList(spuId);
    }


    @Autowired
    private SkuInfo_Mapper skuInfoMapper;

    @Autowired
    private SkuAttrValue_Mapper skuAttrValueMapper;

    @Autowired
    private  SkuSaleAttrValue_Mapper skuSaleAttrValueMapper;

    @Autowired
    private SkuImage_Mapper skuImageMapper;

    /**
     * 保存SkuInfo【SkuAttrValue、SkuSaleAttrValue、SkuImage】
     * 控制器：http://localhost:8082/saveSkuInfo
     */
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
//        skuInfo
        this.skuInfoMapper.insertSelective(skuInfo);

//        skuAttrValue（平台属性值）
        List<SkuAttrValue> skuAttrValue_List = skuInfo.getSkuAttrValueList();
        if (skuAttrValue_List!=null && skuAttrValue_List.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValue_List) {
                //由于页面没有传递sku_id，因此，需要我自行设置
                skuAttrValue.setSkuId(skuInfo.getId());
                this.skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

//        skuSaleAttrValue（销售属性值）
        List<SkuSaleAttrValue> skuSaleAttrValue_List = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValue_List!=null && skuSaleAttrValue_List.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValue_List) {
                //由于页面没有传递sku_id，因此，需要我自行设置
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                this.skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }

//        skuImage（图片）
        List<SkuImage> skuImage_List = skuInfo.getSkuImageList();
        if (skuImage_List!=null && skuImage_List.size()>0){
            for (SkuImage skuImage : skuImage_List) {
                //由于页面没有传递sku_id，因此，需要我自行设置
                skuImage.setSkuId(skuInfo.getId());
                this.skuImageMapper.insertSelective(skuImage);
            }
        }
    }
    /**
     * 知识点回顾：
     * 集合长度：.size()
     * 字符串长度：.length()
     * 数组长度：.length
     * 文件长度：new File().length()
     *      方式一：byte[] bytes = new byte[1024];
     *      方式二：byte[] bytes = new byte[file.length()];
     */

//==========day07==========================================================================================================
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 根据skuId，获取商品详情信息（SkuInfo）
     */
    @Override
    public SkuInfo getSkuInfo_bySkuId(String skuId) {
//        return getSkuInfo_noAdvanted(skuId); //未优化
//        return getSkuInfoJedis(skuId);    //优化：redis
        return getSkuInfo_redisson(skuId);  //优化：redisson
    }

    private SkuInfo getSkuInfo_redisson(String skuId){
        Jedis jedis = null;
        RLock lock = null;
        //redis没有宕机，redis可用（try-catch-finally）
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://192.168.202.129:6379");
            RedissonClient redissonClient = Redisson.create(config);
            lock = redissonClient.getLock("yourLock");
            // 加锁
            lock.lock(10, TimeUnit.SECONDS);
            //业务代码
            //获取jedis
            jedis = redisUtil.getJedis();
            //定义key：（见名知意）【"sku:skuId:info"】
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            //判断缓存中是否有数据，如果有，从缓存中获取；没有，从db获取，并将数据放入缓存!
            if (jedis.exists(skuKey)){
                //有缓存
                String skuInfo_JSON = jedis.get(skuKey);
                SkuInfo skuInfo = JSON.parseObject(skuInfo_JSON, SkuInfo.class);
                System.out.println("redis没有宕机了，有缓存，走redis缓存");
                return skuInfo;
            }else {
                //没有缓存
                System.out.println("redis没有宕机了，没缓存，走数据库");
                SkuInfo skuInfo_byDB = getSkuInfo_byDB(skuId);
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT , JSON.toJSONString(skuInfo_byDB));
                return skuInfo_byDB;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
            if (lock != null){
                //解锁
                lock.unlock();
            }
        }
        //redis宕机了，catch异常，直接走DB
        System.out.println("redis宕机了，redis宕机了，catch异常，直接走DB");
        return getSkuInfo_byDB(skuId);
    }

    private SkuInfo getSkuInfoJedis(String skuId) {
        // 获取jedis
        SkuInfo skuInfo =null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            // 定义key： 见名之意： sku：skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            // 获取数据
            String skuJson = jedis.get(skuKey);

            if (skuJson==null || skuJson.length()==0){
                System.out.println("缓存中没有数据");
                // 定义分布式锁的key=sku:skuId:lock ，并将分布式锁的key设置到缓存
                // 执行set 命令加分布式锁
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                String key_retValue   = jedis.set(skuLockKey, "good", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(key_retValue)){
                    // 此时加锁成功！
                    //从数据库查出数据，转成JSON，放到缓存中
                    skuInfo = getSkuInfo_byDB(skuId);
                    String skuInfo_JSON = JSON.toJSONString(skuInfo);
                    jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,skuInfo_JSON);
                    // 删除锁！
                    jedis.del(skuLockKey);
                    return skuInfo;
                }else {
                    // 其他用户等待
                    Thread.sleep(1000);
                    // 调用getSkuInfo();
                    return getSkuInfo_byDB(skuId);
                }
            }else {
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return getSkuInfo_byDB(skuId);
    }

    private SkuInfo getSkuInfo_noAdvanted(String skuId) {
        //获取jedis
        Jedis jedis = null;
        //redis没有宕机，redis可用（try-catch-finally）
        try {
            jedis = redisUtil.getJedis();
            //定义key：（见名知意）【"sku:skuId:info"】
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            //判断缓存中是否有数据，如果有，从缓存中获取；没有，从db获取，并将数据放入缓存!
            if (jedis.exists(skuKey)){
                //有缓存
                String skuInfo_JSON = jedis.get(skuKey);
                SkuInfo skuInfo = JSON.parseObject(skuInfo_JSON, SkuInfo.class);
                System.out.println("redis没有宕机了，有缓存，走redis缓存");
                return skuInfo;
            }else {
                //没有缓存
                System.out.println("redis没有宕机了，没缓存，走数据库");
                SkuInfo skuInfo_byDB = getSkuInfo_byDB(skuId);
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT , JSON.toJSONString(skuInfo_byDB));
                return skuInfo_byDB;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }

        //redis宕机了，catch异常，直接走DB
        System.out.println("redis宕机了，redis宕机了，catch异常，直接走DB");
        return getSkuInfo_byDB(skuId);
    }

    //从DB，查询skuInfo
    private SkuInfo getSkuInfo_byDB(String skuId){
        SkuInfo skuInfo = this.skuInfoMapper.selectByPrimaryKey(skuId);
        //将skuImageList 放入 skuInfo 中!
        skuInfo.setSkuImageList(getSkuImageList_bySkuId(skuId));
        //将skuAttrValueList 放进 skuInfo中
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        skuInfo.setSkuAttrValueList(this.skuAttrValueMapper.select(skuAttrValue));
        return skuInfo;
    }

//==================================================================================================================

    /**
     * 功能：显示SkuImage图片列表
     */
    //根据skuId，获取图片集合（SkuImageList），并添加到作用域
    @Override
    public List<SkuImage> getSkuImageList_bySkuId(String skuId) {
        //sql：select * from SkuImage where skuId = ?
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        return this.skuImageMapper.select(skuImage);
    }

    /**
     * 功能：1显示商品销售属性、商品销售属性值;2、根据skuId，锁定商品属性值。
     * 方法参数是skuInfo，为什么？因为skuInfo包含skuId（skuInfo.getId）、spuId（skuInfo.getSpuId）
     * 方法返回值是List<SpuSaleAttr>，为什么？因为SpuSaleAttr包含多个SpuSaleAttrValue（含isChecked）
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList_Value_checked(SkuInfo skuInfo) {
        //查询商品销售属性集合【商品销售属性值集合（isChecked）】
        return this.spuSaleAttrMapper.getSpuSaleAttrList_Value_checked(skuInfo.getId(),skuInfo.getSpuId());
    }

    /**
     * 功能：用户选择不同的商品属性值组合，而跳转不同的skuId页面。
     * 例如：https://item.jd.com/{skuId}.html
     */
    //根据spuId，获取skuSaleAttrValue集合
    @Override
    public List<SkuSaleAttrValue> getskuSaleAttrValueList_BySpuId(String spuId) {
        //直接调用mapper
        return this.skuSaleAttrValueMapper.getskuSaleAttrValueList_BySpuId(spuId);
    }

    /**
     * 根据平台属性值id集合，获取BaseAttrInfoList（含平台属性名称、平台属性值名称）
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoList(List<String> attrValueIdList) {
        /*SELECT *
        FROM base_attr_info bai INNER JOIN base_attr_value bav ON bai.id = bav.attr_id
        WHERE bav.id in (81,82,83);*/
        //将集合变成字符串
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        return this.baseAttrInfoMapper.getBaseAttrInfoList(attrValueIds);
    }


}
