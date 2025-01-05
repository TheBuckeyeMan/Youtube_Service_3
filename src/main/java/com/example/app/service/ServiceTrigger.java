package com.example.app.service;

import java.io.File;
import java.time.LocalDateTime;
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
    private MakeAWSPollyRequest makeAWSPollyRequest;
    private PostFileToS3 postFileToS3;
    private S3LoggingService s3LoggingService;
    private SpeechMarks speechMarks;

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

    @Value("${aws.s3.key.audio}")
    private String audioBucketKey;

    @Value("${aws.s3.key.speech}")
    private String speechBucketKey;

    public ServiceTrigger(ReadFile readFile, GptReformat gptReformat, MakeAWSPollyRequest makeAWSPollyRequest, PostFileToS3 postFileToS3, S3LoggingService s3LoggingService, SpeechMarks speechMarks){
        this.readFile = readFile;
        this.gptReformat = gptReformat;
        this.makeAWSPollyRequest = makeAWSPollyRequest;
        this.postFileToS3 = postFileToS3;
        this.s3LoggingService = s3LoggingService;
        this.speechMarks = speechMarks;
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

        //Service3: Get the speech-marks response from aws polly and save to json file
        log.info("Attempting to get Speech Marks");
        File speechMarksFile = speechMarks.getSpeechMarksFile(MessageForApi, "Matthew");
        log.info("Speech marks successfully retrieved");

        //Service4: save the response form aws polly speeech-marks to s3 bucket
        log.info("Attempting to save Speech-Marks to S3");
        postFileToS3.PostFileToS3Bucket(speechMarksFile,landingBucket ,speechBucketKey);
        log.info("Speech-Marks saved to S3");

        //Service 3: make the request to aws polly save response to mp3 to variable here.
        File audioFile = makeAWSPollyRequest.getAudioFile(MessageForApi,"Matthew");

        //Service 4: Save the audio file to the aws s3 bucket
        postFileToS3.PostFileToS3Bucket(audioFile,landingBucket ,audioBucketKey);
        log.info("The Service has successfully complete and the audio file is saved in the " + audioBucketKey + " Directory of the " + landingBucket + " Bucket!");
        s3LoggingService.logMessageToS3("Succcess: Success occured at: " + LocalDateTime.now() + " On: youtube-service-3" + ",");
        log.info("Final: The Lambda has triggered successfully and the audio file is now saved in the S3 Bucket: " + landingBucket);

        //TODO Task 6: implement unit testing
    }
}