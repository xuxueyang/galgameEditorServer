package com.fengyun.cube.workflow.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.fengyun.cube.workflow")
public class FeignConfiguration {

}
