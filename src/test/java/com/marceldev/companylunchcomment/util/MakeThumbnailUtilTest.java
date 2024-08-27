package com.marceldev.companylunchcomment.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.marceldev.companylunchcomment.exception.diner.ImageReadFailException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("썸네일 생성 유틸")
class MakeThumbnailUtilTest {

  @Test
  @DisplayName("썸네일 생성 - 성공(jpg)")
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
  @DisplayName("썸네일 생성 - 성공(png)")
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
  @DisplayName("썸네일 생성 - 실패(이미지로 읽을 수 없는 파일인 경우)")
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