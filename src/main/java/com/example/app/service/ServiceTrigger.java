package com.example.app.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ServiceTrigger {
    private static final Logger log = LoggerFactory.getLogger(ServiceTrigger.class);
    private ReadFile readFile;
    private GptReformat gptReformat;
    private ModelAWSPostRequest modelAWSPostRequest;

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${aws.s3.bucket.landing}")
    private String landingBucket;

    @Value("${aws.s3.key.gpt}")
    private String gptBucketKey;

    @Value("${api.aws.url.polly}")
    private String pollyUrl;

    @Value("${aws.key.access}")
    private String awsAccessKey;

    @Value("${aws.key.secret}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${api.name}")
    private String apiName;


    public ServiceTrigger(ReadFile readFile, GptReformat gptReformat, ModelAWSPostRequest modelAWSPostRequest){
        this.readFile = readFile;
        this.gptReformat = gptReformat;
        this.modelAWSPostRequest = modelAWSPostRequest;
    }

    public void TriggerService(){
        //Initialization Logs
        log.info("The Active Environment is set to: " + environment);
        log.info("Begining to Collect Contents of Fun Fact form S3 Bucket");

        //Trigger Services
        //Service 1: read contents from the AWS S3 bucket save to string variable
        String gptContents = readFile.getBasicFileContents(landingBucket, gptBucketKey);
        //Service 2: Clean up contents of that string, Anywhere we have two spaces in a row we need to delete the double spaces in the string, save to string
        String MessageForApi = gptReformat.removeUnwantedSpaces(gptContents);
        //Service 3: Prepare the Post request to aws POLLY
        Map<String,Object> postRequestBody = modelAWSPostRequest.getPostRequest(pollyUrl, MessageForApi,awsAccessKey,awsSecretKey,awsRegion,apiName);

        //Service 4: make the request to aws polly save response to mp3 to variable here.
        //Service 5: Save the audio file to the aws s3 bucket
        //Service 6 : add in email error handling
        //Task 7: implement unit testing


    }
}