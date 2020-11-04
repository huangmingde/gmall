package com.atguigu.gmall.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallUserManageApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void test01(){
		HashMap<String, Integer> map = new HashMap<>();
		map.put("黄",16);
		map.put("明",18);
		map.put("德",20);
		map.put("黄明德",18);
		//1. 调用map集合方法entrySet()将集合中的映射关系对象（Entry）,存储到Set集合
		Set<Map.Entry<String, Integer>> entry_Set = map.entrySet();

		//2.迭代Set集合（entry_Set）,获得映射关系对象entry
		//2.1、获取entry_Set的迭代器
		Iterator<Map.Entry<String, Integer>> entry_iterator = entry_Set.iterator();
		//2.2、while循环遍历获取entry
		while (entry_iterator.hasNext()){
			Map.Entry<String, Integer> entry = entry_iterator.next();
			//3.调用entry对象的方法：getKet，getValue获取键值对
			String key = entry.getKey();
			Integer value = entry.getValue();
			System.out.println(key+"："+value);
		}
	}

}
