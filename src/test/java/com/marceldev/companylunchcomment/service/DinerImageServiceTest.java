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
import com.marceldev.companylunchcomment.exception.diner.DinerImageNotFoundException;
import com.marceldev.companylunchcomment.exception.diner.DinerMaxImageCountExceedException;
import com.marceldev.companylunchcomment.exception.diner.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.diner.ImageDeleteFailException;
import com.marceldev.companylunchcomment.exception.diner.ImageReadFailException;
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
import org.springframework.core.io.ClassPathResource;
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

  private MultipartFile mockImageFile;

  private Diner diner;

  @BeforeEach
  public void setup() throws Exception {
    setPrivateField(dinerImageService, "dinerMaxImageCount", 10);
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private void setupDiner() {
    diner = Diner.builder()
        .id(1L)
        .name("먹자 식당")
        .build();
  }

  private void cleanDiner() {
    diner = null;
  }

  private void setupMockImageFile() {
    ClassPathResource imageFile = new ClassPathResource("food.jpg");

    try {
      mockImageFile = new MockMultipartFile(
          "food.jpg",
          imageFile.getFilename(),
          "image/jpeg",
          imageFile.getInputStream()
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void cleanMockImageFile() {
    mockImageFile = null;
  }

  @Test
  @DisplayName("식당 이미지 추가 - 성공")
  void test_update_diner_add_image() throws Exception {
    //given
    setupDiner();
    setupMockImageFile();

    DinerImage dinerImage = DinerImage.builder()
        .id(10L)
        .orders(100)
        .build();

    //when
    when(dinerRepository.findById(1L))
        .thenReturn(Optional.of(diner));
    when(dinerImageRepository.findTopByDinerOrderByOrdersDesc(diner))
        .thenReturn(Optional.of(dinerImage));
    dinerImageService.addDinerImage(1L, mockImageFile);
    ArgumentCaptor<DinerImage> captor = ArgumentCaptor.forClass(DinerImage.class);

    //then
    verify(dinerImageRepository, times(2)).save(captor.capture());
    assertEquals(200, captor.getValue().getOrders());

    cleanMockImageFile();
    cleanDiner();
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(식당이 존재하지 않음)")
  void test_update_diner_add_image_fail_no_diner() {
    //given
    //when
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(DinerNotFoundException.class,
        () -> dinerImageService.addDinerImage(1L, mockImageFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(이미 등록된 이미지가 10개 이상)")
  void test_update_diner_add_image_fail_max_count() {
    //given
    //when
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDinerAndThumbnail(any(), anyBoolean()))
        .thenReturn(10);

    //then
    assertThrows(DinerMaxImageCountExceedException.class,
        () -> dinerImageService.addDinerImage(1L, mockImageFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(읽을 수 없는 이미지 파일)")
  void test_update_diner_add_image_fail_cant_read_image() throws Exception {
    //given
    setupDiner();

    mockImageFile = new MockMultipartFile(
        "food.jpg",
        "food.jpg",
        "image/jpeg",
        "not_image".getBytes()
    );

    //when
    when(dinerRepository.findById(diner.getId()))
        .thenReturn(Optional.of(diner));
    when(dinerImageRepository.countByDinerAndThumbnail(diner, false))
        .thenReturn(0);

    //then
    assertThrows(ImageReadFailException.class,
        () -> dinerImageService.addDinerImage(diner.getId(), mockImageFile));

    cleanMockImageFile();
    cleanDiner();
  }

  @Test
  @DisplayName("식당 이미지 제거 - 성공")
  void test_update_diner_remove_image() {
    //given
    String key = UUID.randomUUID().toString();

    //when
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            DinerImage.builder()
                .id(1L)
                .s3Key(key)
                .build()
        ));

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
    //when
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(DinerImageNotFoundException.class,
        () -> dinerImageService.removeDinerImage(1L));
  }

  @Test
  @DisplayName("식당 이미지 제거 - 실패(S3에서 제거 실패)")
  void test_update_diner_remove_image_fail_s3_delete_fail() {
    //given
    String key = UUID.randomUUID().toString();

    //when
    when(dinerImageRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            DinerImage.builder()
                .id(1L)
                .s3Key(key)
                .build()
        ));
    doThrow(new RuntimeException())
        .when(s3Manager).removeFile(any());

    //then
    assertThrows(ImageDeleteFailException.class,
        () -> dinerImageService.removeDinerImage(1L));
  }
}