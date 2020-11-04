package com.atguigu.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@ComponentScan("com.atguigu.gmall")   //扫描redis工具类、配置类所在的包（service-util模块）
@MapperScan("com.atguigu.gmall.user.mapper")
@SpringBootApplication
public class Gmall_User_Manage_Application {

	public static void main(String[] args) {
		SpringApplication.run(Gmall_User_Manage_Application.class, args);
	}

}
