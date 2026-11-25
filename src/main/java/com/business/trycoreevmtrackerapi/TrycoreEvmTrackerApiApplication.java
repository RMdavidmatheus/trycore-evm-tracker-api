package com.business.trycoreevmtrackerapi;

import com.business.trycoreevmtrackerapi.infrastructure.config.DotenvEnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TrycoreEvmTrackerApiApplication {

    static void main(String[] args) {
        DotenvEnvironmentPostProcessor.loadIntoSystemProperties();
        SpringApplication.run(TrycoreEvmTrackerApiApplication.class, args);
    }

}
