package com.atguigu.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.atguigu.gmall")		//web模块必扫web-util模块
@SpringBootApplication
public class Gmall_Order_Web_Application {

	public static void main(String[] args) {
		SpringApplication.run(Gmall_Order_Web_Application.class, args);
	}

}
