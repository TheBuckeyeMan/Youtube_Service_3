package com.example.app.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SpeechMarkType;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;

@Service
public class SpeechMarks {
    private static final Logger log = LoggerFactory.getLogger(MakeAWSPollyRequest.class);
    private S3LoggingService s3LoggingService;

    public SpeechMarks(S3LoggingService s3LoggingService){
        this.s3LoggingService = s3LoggingService;
    }

    public File getSpeechMarksFile(String message, String voiceid) {
        File speechMarksFile = null;

        try (PollyClient pollyClient = PollyClient.builder()
                .region(Region.US_EAST_2) // Set your AWS Region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            // Build the request
            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .text(message)
                    .voiceId(voiceid)
                    .outputFormat(OutputFormat.JSON)
                    .speechMarkTypes(SpeechMarkType.WORD)
                    .build();

            // Call Polly
            log.info("Sending request to AWS Polly...");
            ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(request);

            // Write response to file
            speechMarksFile = new File("/tmp/speechMarks.json");
            try (FileOutputStream fos = new FileOutputStream(speechMarksFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                InputStream inputStream = response;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            log.info("SpeechMarks file saved to: ", speechMarksFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Error while getting speech marks from  AWS Polly", e);
            s3LoggingService.logMessageToS3("Error: Error while interacting with AWS Polly - line 62 on SpeechMarks.java: " + LocalDate.now() + " On: youtube-service-3" + ",");
            throw new RuntimeException("Error while getting speech marks from  AWS Polly", e);
        }

        return speechMarksFile;
    }
}
