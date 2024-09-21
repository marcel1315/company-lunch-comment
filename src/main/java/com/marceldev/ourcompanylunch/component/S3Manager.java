package com.marceldev.ourcompanylunch.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Manager {

  @Value("${s3.bucket-name}")
  private String bucketName;

  @Value("${cloudfront-url}")
  private String cloudFrontUrl;

  private final S3Client s3Client;

  /**
   * Upload file to an S3 bucket, receiving key and file inputstream.
   */
  public void uploadFile(String key, InputStream inputStream, long size) throws IOException {
    // key: diner/1/images/567a3d2b-94db-4709-93b4-69771d8fdc54.png, filesize: 76120
    log.debug("key: {}, filesize: {}", key, size);

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    PutObjectResponse response = s3Client.putObject(
        putObjectRequest,
        RequestBody.fromInputStream(inputStream, size)
    );

    log.info(response.toString());
  }

  public void removeFile(String key) {
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    DeleteObjectResponse response = s3Client.deleteObject(
        deleteObjectRequest
    );

    log.info(response.toString());
  }

  public List<String> getUrls(List<String> keys) {
    return keys.stream()
        .map(key -> cloudFrontUrl + key)
        .toList();
  }
}
