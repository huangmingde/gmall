package com.atguigu.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan("com.atguigu.gmall.cart.mapper")  //扫描mapper包下mapper接口
@ComponentScan("com.atguigu.gmall")	 //扫描redis配置类、工具类所在的包（不管在哪个模块）
@SpringBootApplication
public class GmallCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartServiceApplication.class, args);
	}
}
