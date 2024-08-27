package com.marceldev.companylunchcomment.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

class MakeThumbnailUtilTest {

  @Test
  public void testImage() throws IOException {
    ClassPathResource imageFile = new ClassPathResource("food.jpg");

    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        imageFile.getFilename(),
        "image/jpeg", // MIME type is jpeg, not jpg.
        imageFile.getInputStream()
    );

    assertNotNull(multipartFile);
    assertEquals(76150, multipartFile.getSize());
  }

  @Test
  public void testImageResize() throws IOException {
    ClassPathResource imageFile = new ClassPathResource("food.jpg");

    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        imageFile.getFilename(),
        "image/jpeg", // MIME type is jpeg, not jpg.
        imageFile.getInputStream()
    );
    assertNotNull(multipartFile);
    assertEquals(76150, multipartFile.getSize());

    String extension = FileUtil.getExtension(multipartFile)
        .orElseThrow(RuntimeException::new);
    ByteArrayOutputStream outputStream = MakeThumbnailUtil.resizeFile(
        multipartFile.getInputStream(), extension);
    assertNotNull(outputStream);
  }
}