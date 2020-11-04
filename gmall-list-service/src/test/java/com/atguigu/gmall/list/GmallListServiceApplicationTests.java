package com.atguigu.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {
/*
举例：
	GET /movie_chn/movie/_search
	{
	  "query": {
		"term": {
		  "actorList.name.keyword": "张国立"
		}
	  }
	}
*/
	@Autowired
	private JestClient jestClient;

	@Test
	public void testES() throws IOException {
		//1、定义dsl语句
		String query = "{\n" +
						"  \"query\": {\n" +
						"    \"term\": {\n" +
						"      \"actorList.name.keyword\": \"张国立\"\n" +
						"    }\n" +
						"  }\n" +
						"}";

		//2、查询GET
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

		//3、执行查询
		SearchResult searchResult = jestClient.execute(search);

		//4、获取数据	//没有对象接收，因此使用map
		List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);

		//5、遍历取数据
		for (SearchResult.Hit<Map, Void> hit : hits) {
			Map map = hit.source;
			System.out.println(map.get("name"));	//红海事件
		}

	}

	public void contextLoads() {
	}

}
