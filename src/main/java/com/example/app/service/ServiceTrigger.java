package com.example.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ServiceTrigger {
    private static final Logger log = LoggerFactory.getLogger(ServiceTrigger.class);
    private Testing testing;

    @Value("${spring.profiles.active}")
    private String environment;

    public ServiceTrigger(Testing testing){
        this.testing = testing;
    }

    public void TriggerService(){
        //Initialization Logs
        log.info("The Active Environment is set to: " + environment);
        log.info("Begining to Collect Contents of Fun Fact form S3 Bucket");

        //Trigger Services
        String test = testing.hello();
        log.info("Our Test data is: " + test);
    }
}