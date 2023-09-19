package cn.iris.cloud.boot.simple;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DubboProviderApplication {
	public static void main(String[] args) {
		//starter
		SpringApplication.run(DubboProviderApplication.class, args);
	}
}
