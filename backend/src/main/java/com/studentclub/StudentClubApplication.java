package com.studentclub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 学生社团管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.studentclub.mapper")
public class StudentClubApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentClubApplication.class, args);
        System.out.println("========================================");
        System.out.println("  学生社团管理系统启动成功！");
        System.out.println("  后端地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
