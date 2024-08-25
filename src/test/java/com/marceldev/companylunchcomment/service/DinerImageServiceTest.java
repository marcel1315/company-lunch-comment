package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.component.S3Manager;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import com.marceldev.companylunchcomment.exception.DinerImageNotFoundException;
import com.marceldev.companylunchcomment.exception.DinerMaxImageCountExceedException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.ImageDeleteFail;
import com.marceldev.companylunchcomment.exception.ImageUploadFail;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.repository.diner.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class DinerImageServiceTest {

  @Mock
  private DinerRepository dinerRepository;

  @Mock
  private DinerImageRepository dinerImageRepository;

  @Mock
  private S3Manager s3Manager;

  @InjectMocks
  private DinerImageService dinerImageService;

  @BeforeEach
  public void setUp() throws Exception {
    setPrivateField(dinerImageService, "dinerMaxImageCount", 10);
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  MultipartFile mockImageFile = new MockMultipartFile(
      "file",
      "test.png",
      "image/png",
      "test".getBytes()
  );

  MultipartFile mockThumbnailFile = new MockMultipartFile(
      "file",
      "test_thumbnail.png",
      "image/png",
      "test".getBytes()
  );

  @Test
  @DisplayName("식당 이미지 추가 - 성공")
  void test_update_diner_add_image() throws Exception {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.findTopByDinerOrderByOrdersDesc(any()))
        .thenReturn(Optional.of(
            DinerImage.builder().orders(100).build()
        ));
    String key = String.format("diner/%d/images/%s", 1L, UUID.randomUUID());
    when(s3Manager.uploadFile(any(), any()))
        .thenReturn(key);

    //when
    dinerImageService.addDinerImage(1, mockImageFile, mockThumbnailFile);
    ArgumentCaptor<DinerImage> captor = ArgumentCaptor.forClass(DinerImage.class);

    //then
    verify(dinerImageRepository, times(2)).save(captor.capture());
    assertEquals(captor.getValue().getS3Key(), key);
    assertEquals(captor.getValue().getOrders(), 200);
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(식당이 존재하지 않음)")
  void test_update_diner_add_image_fail_no_diner() {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when

    //then
    assertThrows(DinerNotFoundException.class,
        () -> dinerImageService.addDinerImage(1L, mockImageFile, mockThumbnailFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(이미 등록된 이미지가 10개 이상)")
  void test_update_diner_add_image_fail_max_count() {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDinerAndThumbnail(any(), anyBoolean()))
        .thenReturn(10);

    //when
    //then
    assertThrows(DinerMaxImageCountExceedException.class,
        () -> dinerImageService.addDinerImage(1L, mockImageFile, mockThumbnailFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(S3 저장소에 이미지 업로드 실패)")
  void test_update_diner_add_image_fail_upload() throws Exception {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDinerAndThumbnail(any(), anyBoolean()))
        .thenReturn(0);
    when(s3Manager.uploadFile(any(), any()))
        .thenThrow(new IOException());

    //when
    //then
    assertThrows(ImageUploadFail.class,
        () -> dinerImageService.addDinerImage(1L, mockImageFile, mockThumbnailFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(DB에 저장 실패)")
  void test_update_diner_add_image_fail_db_save() throws Exception {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDinerAndThumbnail(any(), anyBoolean()))
        .thenReturn(0);
    when(s3Manager.uploadFile(any(), any()))
        .thenReturn("diner/1/images/" + UUID.randomUUID());
    when(dinerImageRepository.save(any()))
        .thenThrow(new RuntimeException());

    //when
    //then
    assertThrows(InternalServerError.class,
        () -> dinerImageService.addDinerImage(1L, mockImageFile, mockThumbnailFile));
  }

  @Test
  @DisplayName("식당 이미지 제거 - 성공")
  void test_update_diner_remove_image() {
    //given
    String key = UUID.randomUUID().toString();
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            DinerImage.builder()
                .id(1L)
                .s3Key(key)
                .build()
        ));

    //when
    dinerImageService.removeDinerImage(1L);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    //then
    verify(s3Manager).removeFile(captor.capture());
    verify(dinerImageRepository).delete(any());
    assertEquals(captor.getValue(), key);
  }

  @Test
  @DisplayName("식당 이미지 제거 - 실패(이미지가 존재하지 않음)")
  void test_update_diner_remove_image_fail_image_not_found() {
    //given
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(DinerImageNotFoundException.class,
        () -> dinerImageService.removeDinerImage(1L));
  }

  @Test
  @DisplayName("식당 이미지 제거 - 실패(S3에서 제거 실패)")
  void test_update_diner_remove_image_fail_s3_delete_fail() {
    //given
    String key = UUID.randomUUID().toString();
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            DinerImage.builder()
                .id(1L)
                .s3Key(key)
                .build()
        ));
    doThrow(new RuntimeException())
        .when(s3Manager).removeFile(any());

    //when
    //then
    assertThrows(ImageDeleteFail.class,
        () -> dinerImageService.removeDinerImage(1L));
  }

  @Test
  @DisplayName("식당 이미지 제거 - 실패(DB에서 제거 실패)")
  void test_update_diner_remove_image_fail_db_delete() {
    //given
    String key = UUID.randomUUID().toString();
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            DinerImage.builder()
                .id(1L)
                .s3Key(key)
                .build()
        ));

    doThrow(new RuntimeException())
        .when(dinerImageRepository).delete(any());

    //when
    //then
    assertThrows(InternalServerError.class,
        () -> dinerImageService.removeDinerImage(1L));
  }
}