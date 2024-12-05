package com.example.app.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MakeAWSPollyRequest {
    private static final Logger log = LoggerFactory.getLogger(MakeAWSPollyRequest.class);


    public File getAudioFile(Map<String,Object> requestDetails, String url){
        Map<String, String> headers = (Map<String, String>) requestDetails.get("headers");
        String body = (String) requestDetails.get("body");

        File audioFile = null;

        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost postRequest = new HttpPost(url);

            //Add headers
            for (Map.Entry<String, String> header : headers.entrySet()){
                postRequest.addHeader(header.getKey(),header.getValue());
            }

            //Add Body
            StringEntity entity = new StringEntity(body);
            postRequest.setEntity(entity);

            //Execute Request
            log.warn("Authorization header: {}", headers.get("Authorization"));
            log.warn("X-Amz-Date header: {}", headers.get("X-Amz-Date"));
            log.warn("Request body: {}", body);
            log.warn("Request URL: {}", url);
            log.info("Sending Post Request to AWS POLLY!");
            log.debug("Authorization header: {}", headers.get("Authorization"));
            log.debug("X-Amz-Date header: {}", headers.get("X-Amz-Date"));
            log.debug("Request body: {}", body);
            log.debug("Request URL: {}", url);
            try (CloseableHttpResponse response = httpClient.execute(postRequest)){//problem here
                //Check Response Status
                int statusCode = response.getStatusLine().getStatusCode();
                log.info("The returned status code of the request is: " + statusCode);
                if (statusCode == 200){
                    log.info("Aws Polly Requested Successfully");
                    HttpEntity responsEntity = response.getEntity();
                    if (responsEntity != null){
                        InputStream inputStream = responsEntity.getContent();

                        //Create temp file to save audio
                        audioFile = File.createTempFile("audio-file", ".mp3");
                        try (FileOutputStream outputStream = new FileOutputStream(audioFile)){
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        log.info("Audio file saved to {}", audioFile.getAbsolutePath());
                    }
                } else {
                    log.error("AWS Polly request failed with status code: {}", statusCode);
                    //TODO Add in email for error handling here
                    throw new RuntimeException("AWS Polly request failed with status code: " + statusCode);
                }
            }
        } catch (Exception e) {
            log.error("Error while making AWS Polly request", e);
            //TODO Add in email for error handling here
            throw new RuntimeException("Error while making AWS Polly request", e);
        }
        return audioFile;
    }
}