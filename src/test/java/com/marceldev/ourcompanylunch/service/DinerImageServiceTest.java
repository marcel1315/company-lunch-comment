package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerImage;
import com.marceldev.ourcompanylunch.exception.diner.DinerImageNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerMaxImageCountExceedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.ImageDeleteFailException;
import com.marceldev.ourcompanylunch.exception.diner.ImageReadFailException;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
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
        .name("Eat Diner")
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
  @DisplayName("Add diner image - Success")
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
  @DisplayName("Add diner image - Fail(Diner not found)")
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
  @DisplayName("Add diner image - Fail(More than 10 images exist)")
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
  @DisplayName("Add diner image - Fail(Image file can't be read)")
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
  @DisplayName("Remove diner image - Success")
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
  @DisplayName("Remove diner image - Fail(Image not found)")
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
  @DisplayName("Remove diner image - Fail(Fail to remove in S3)")
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