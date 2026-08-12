package cn.niuma.lingdi000721.startyuanreftain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cn.niuma.lingdi000721.startyuanreftain.mapper")
@SpringBootApplication
public class StartYuanReftainApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartYuanReftainApplication.class, args);
    }

}
