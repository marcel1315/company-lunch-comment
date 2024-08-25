package com.marceldev.companylunchcomment.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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
   * key 와 파일을 받아 S3 버킷에 업로드
   */
  public String uploadFile(String key, MultipartFile file) throws IOException {
    try (InputStream is = file.getInputStream()) {
      log.debug(key); // diner/1/images/567a3d2b-94db-4709-93b4-69771d8fdc54.png

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      PutObjectResponse response = s3Client.putObject(
          putObjectRequest,
          RequestBody.fromInputStream(is, file.getSize())
      );

      log.info(response.toString());

      return key;
    }
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
