package com.example.app.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GptReformat {
    private static final Logger log = LoggerFactory.getLogger(GptReformat.class);
    private S3LoggingService s3LoggingService;


    public GptReformat(S3LoggingService s3LoggingService){
        this.s3LoggingService = s3LoggingService;
    }

    
    public String removeUnwantedSpaces(String message){
        if (message == null){
            log.warn("The Contents of GPT file was null, Unable to read file contents from the GPT File");
            //Add in email error handling here
            s3LoggingService.logMessageToS3("Error: The Contents of GPT file was null, Unable to read file contents from the GPT File - line 24 on GptReformat.java: " + LocalDate.now() + " On: youtube-service-3" + ",");
            return "Error: Message contents are blank. Line 19 in GptReformat.java";
        }

        //Delete line breaks
        String noLineBreaks = message.replaceAll("\\r?\\n", " ");
        //Remove double spaces
        String cleanedString = noLineBreaks.replaceAll(" {2,}", " ");

        log.info("The Message after being cleaned is: " + cleanedString);

        return cleanedString;
    }
}