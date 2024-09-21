package com.marceldev.ourcompanylunch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.marceldev.ourcompanylunch.exception.diner.ImageReadFailException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

class MakeThumbnailUtilTest {

  @Test
  @DisplayName("Create thumbnail - Success(jpg)")
  public void make_thumbnail_success_jpg() throws IOException {
    //given
    ClassPathResource imageFile = new ClassPathResource("food.jpg");
    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        imageFile.getFilename(),
        "image/jpeg", // MIME type is jpeg, not jpg.
        imageFile.getInputStream()
    );

    //when
    ByteArrayOutputStream outputStream = MakeThumbnailUtil.resizeFile(
        multipartFile.getInputStream(), "jpg");

    //then
    assertNotNull(multipartFile);
    assertEquals(76150, multipartFile.getSize());
    assertNotNull(outputStream);
  }

  @Test
  @DisplayName("Create thumbnail - Success(png)")
  public void make_thumbnail_success_png() throws IOException {
    //given
    ClassPathResource imageFile = new ClassPathResource("food-salad.png");
    MockMultipartFile multipartFile = new MockMultipartFile(
        "food-salad.png",
        imageFile.getFilename(),
        "image/png",
        imageFile.getInputStream()
    );

    //when
    ByteArrayOutputStream outputStream = MakeThumbnailUtil.resizeFile(
        multipartFile.getInputStream(), "png");

    //then
    assertNotNull(multipartFile);
    assertEquals(43777, multipartFile.getSize());
    assertNotNull(outputStream);
  }

  @Test
  @DisplayName("Create thumbnail - Fail(File format is not image)")
  public void make_thumbnail_fail_no_extension() throws IOException {
    //given
    ClassPathResource textFile = new ClassPathResource("food.txt");
    MockMultipartFile multipartFile = new MockMultipartFile(
        "file",
        textFile.getFilename(),
        "image/jpeg",
        textFile.getInputStream()
    );

    //when
    //then
    assertThrows(
        ImageReadFailException.class,
        () -> MakeThumbnailUtil.resizeFile(multipartFile.getInputStream(), "txt")
    );
  }
}