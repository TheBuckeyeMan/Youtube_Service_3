package com.example.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Testing {
    private static final Logger log = LoggerFactory.getLogger(Testing.class);
    
    public String hello(){
        return "Conner is a hoe";
    }
}
