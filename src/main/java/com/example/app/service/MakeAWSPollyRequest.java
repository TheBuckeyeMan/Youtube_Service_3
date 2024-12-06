package com.example.app.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;

@Service
public class MakeAWSPollyRequest {
    private static final Logger log = LoggerFactory.getLogger(MakeAWSPollyRequest.class);

    public File getAudioFile(String message, String voiceid) {
        File audioFile = null;

        try (PollyClient pollyClient = PollyClient.builder()
                .region(Region.US_EAST_2) // Set your AWS Region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            // Build the request
            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .text(message)
                    .voiceId(voiceid)
                    .outputFormat(OutputFormat.MP3)
                    .build();

            // Call Polly
            log.info("Sending request to AWS Polly...");
            ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(request);

            // Write response to file
            audioFile = new File("/tmp/audio-file.mp3");
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                InputStream inputStream = response;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            log.info("Audio file saved to: {}", audioFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Error while interacting with AWS Polly", e);
            //TODO Add in email for error handling here
            throw new RuntimeException("Error while interacting with AWS Polly", e);
        }

        return audioFile;
    }
}
