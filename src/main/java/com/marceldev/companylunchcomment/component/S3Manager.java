package com.marceldev.companylunchcomment.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Component
public class S3Manager {

  @Value("${s3.bucket-name}")
  private String bucketName;

  private final S3Client s3Client;

  public S3Manager(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  public String uploadFile(long dinerId, MultipartFile file) throws IOException {
    String key = makeDinerImageKey(dinerId);

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

  private String makeDinerImageKey(long dinerId) {
    return "diner/" + dinerId + "/images/" + UUID.randomUUID();
  }
}
