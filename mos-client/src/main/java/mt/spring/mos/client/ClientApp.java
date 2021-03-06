package mt.spring.mos.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Author Martin
 * @Date 2020/5/15
 */
@SpringBootApplication
@EnableEurekaClient
public class ClientApp {
	public static void main(String[] args) {
		SpringApplication.run(ClientApp.class, args);
	}
}
