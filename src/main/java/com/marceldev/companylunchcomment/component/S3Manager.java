package com.marceldev.companylunchcomment.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Manager {

  @Value("${s3.bucket-name}")
  private String bucketName;

  private final S3Client s3Client;

  public String uploadFile(String key, MultipartFile file) throws IOException {
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

  public List<String> getPresignedUrls(List<String> keys) {
    List<String> presignedUrls = new ArrayList<>();

    try (S3Presigner presigner = S3Presigner.create()) {
      for (String key : keys) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(getRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        log.info("Presigned URL: [{}]", presignedRequest.url().toString());
        presignedUrls.add(presignedRequest.url().toExternalForm());
      }
      return presignedUrls;
    }
  }
}
