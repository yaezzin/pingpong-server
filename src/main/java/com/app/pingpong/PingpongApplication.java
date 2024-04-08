package com.app.pingpong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableAspectJAutoProxy
@EnableJpaAuditing
@EnableMongoAuditing
@SpringBootApplication
@ComponentScan(basePackages = {"com.app.pingpong"})
public class PingpongApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingpongApplication.class, args);
    }

}
