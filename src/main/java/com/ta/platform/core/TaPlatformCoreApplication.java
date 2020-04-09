package com.ta.platform.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@EnableSwagger2
@EnableDiscoveryClient
@ComponentScan("com.ta.platform")
@SpringBootApplication
public class TaPlatformCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaPlatformCoreApplication.class, args);
    }

}
