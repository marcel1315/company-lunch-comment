package com.marceldev.companylunchcomment.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
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

  private final S3Client s3Client;

  public String uploadFile(long dinerId, MultipartFile file) throws IOException {
    String key = genDinerImageKey(dinerId, UUID.randomUUID().toString());

    try (InputStream is = file.getInputStream()) {
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

  private String genDinerImageKey(long dinerId, String filename) {
    return "diner/" + dinerId + "/images/" + filename;
  }
}
