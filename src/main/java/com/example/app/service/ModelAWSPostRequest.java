package com.example.app.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ModelAWSPostRequest {
    private static final Logger log = LoggerFactory.getLogger(ModelAWSPostRequest.class);
    //Makes aws post request for aws services -> This is the equivelant of the RestTemplate for Aws Services
    public Map<String,Object> getPostRequest(String url, String message, String awsAccessKey, String awsSecretKey, String awsRegion, String apiName){
        //Define body of the request
        try {
        // Build the body using Jackson
        Map<String, String> payload = new HashMap<>();
        payload.put("Text", message);
        payload.put("OutputFormat", "mp3");
        payload.put("VoiceId", "Matthew");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(payload);
        
        //Generate the current timestamp
        Instant now = Instant.now();
        String amzDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                                            .withZone(ZoneOffset.UTC)
                                            .format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd")
                                            .withZone(ZoneOffset.UTC)
                                            .format(now);

        //Define Headders for Request
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Host", "polly." + awsRegion + ".amazonaws.com");
        headers.put("X-Amz-Date", amzDate);

        //Create the canonical Request
        String canonicalUri = "/v1/speech";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json\n" + "host:polly." + awsRegion + ".amazonaws.com\n" + "x-amz-date:" + amzDate + "\n";
        String signedHeaders = "content-type;host;x-amz-date";
        String payloadHash = hash(body);
        String canonicalRequest = String.format("%s\n%s\n%s\n%s\n%s\n%s","POST", canonicalUri, canonicalQueryString, canonicalHeaders, signedHeaders, payloadHash);

        //Create the String to sign
        String algorithm = "AWS4-HMAC-SHA256";
        String credentialScope = dateStamp + "/" + awsRegion + "/" + apiName + "/aws4_request";
        String stringToSign = String.format("%s\n%s\n%s\n%s",algorithm,amzDate,credentialScope, hash(canonicalRequest));

        //Calculate the signing signature
        byte[] signingKey = getSignatureKey(awsSecretKey, dateStamp, awsRegion, apiName);
        String signature = hmacHex(signingKey, stringToSign);

        //Create the Authorization HEader
        String authorizationHeader = String.format("%s Credential=%s/%s, SignedHeaders=%s, Signature=%s",algorithm, awsAccessKey, credentialScope, signedHeaders, signature);

        //Assemble the final headers
        headers.put("Authorization", authorizationHeader);

        //Prepare the Request
        Map<String, Object> request = new HashMap<>();
        request.put("url", url);
        request.put("headers", headers);
        request.put("body", body);

        log.info("The Post Request before sending Looks Like: " + request);
        return request;

    } catch (Exception e){
        //TODO Add in Email could be due to temp file being to big in size, message contains over 3k charictor or 
        throw new RuntimeException("Error creating AWS Polly Request", e);
    }
}
    private String hash(String text) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hashBytes);
    }

    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kDate = hmacSha256(("AWS4" + key).getBytes(StandardCharsets.UTF_8), dateStamp);
        byte[] kRegion = hmacSha256(kDate, regionName);
        byte[] kService = hmacSha256(kRegion, serviceName);
        return hmacSha256(kService, "aws4_request");
    }

    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        sha256Hmac.init(new SecretKeySpec(key, "HmacSHA256"));
        return sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacHex(byte[] key, String data) throws Exception {
        return bytesToHex(hmacSha256(key, data));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}