package com.example.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GptReformat {
    private static final Logger log = LoggerFactory.getLogger(GptReformat.class);

    public String removeUnwantedSpaces(String message){
        if (message == null){
            log.warn("The Contents of GPT file was null, Unable to read file contents from the GPT File");
            //Add in email error handling here
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