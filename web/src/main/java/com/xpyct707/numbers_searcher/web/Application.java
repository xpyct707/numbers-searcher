package com.xpyct707.numbers_searcher.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@ComponentScan(basePackages={"com.xpyct707.numbers_searcher.web"})
@EntityScan(basePackages={"com.xpyct707.numbers_searcher.model"})
@EnableJpaRepositories(basePackages={"com.xpyct707.numbers_searcher.repository"})
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
