package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.component.S3Manager;
import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import com.marceldev.companylunchcomment.exception.DinerImageNotFoundException;
import com.marceldev.companylunchcomment.exception.DinerMaxImageCountExceedException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.DuplicateDinerTagException;
import com.marceldev.companylunchcomment.exception.ImageDeleteFail;
import com.marceldev.companylunchcomment.exception.ImageUploadFail;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.repository.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
class DinerServiceTest {

  @Mock
  private DinerRepository dinerRepository;

  @Mock
  private DinerImageRepository dinerImageRepository;

  @Mock
  private S3Manager s3Manager;

  @InjectMocks
  private DinerService dinerService;

  MultipartFile mockImageFile = new MockMultipartFile(
      "file",
      "test.png",
      "image/png",
      "test".getBytes()
  );

  @BeforeEach
  public void setUp() throws Exception {
    setPrivateField(dinerService, "dinerMaxImageCount", 10);
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  @DisplayName("식당 생성 - 성공")
  void test_create_diner() {
    //given
    List<String> tags = new ArrayList<>();
    tags.add("멕시코");
    tags.add("분위기좋은");

    CreateDinerDto dto = CreateDinerDto.builder()
        .name("감성타코")
        .link("diner.com")
        .latitude("37.29283882")
        .longitude("127.39232323")
        .tags(tags)
        .build();

    //when
    ArgumentCaptor<Diner> captor = ArgumentCaptor.forClass(Diner.class);
    dinerService.createDiner(dto);

    //then
    verify(dinerRepository).save(captor.capture());
    Diner diner = captor.getValue();
    assertEquals(diner.getName(), "감성타코");
    assertEquals(diner.getLink(), "diner.com");
    assertEquals(diner.getLatitude(), "37.29283882");
    assertEquals(diner.getLongitude(), "127.39232323");
    assertEquals(diner.getTags().get(1), "분위기좋은");
  }

  @Test
  @DisplayName("식당 생성 - 실패(DB)")
  void test_create_diner_db_fail() {
    //given
    CreateDinerDto dto = CreateDinerDto.builder()
        .name("감성타코")
        .build();
    when(dinerRepository.save(any()))
        .thenThrow(RuntimeException.class);

    //when
    //then
    assertThrows(
        InternalServerError.class,
        () -> dinerService.createDiner(dto)
    );
  }

  @Test
  @DisplayName("식당 정보 수정 - 성공")
  void test_update_diner() {
    //given
    UpdateDinerDto dto = UpdateDinerDto.builder()
        .link("diner1.com")
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .link("diner.com")
                .latitude("37.11111111")
                .longitude("127.11111111")
                .build()
        ));

    //when
    ArgumentCaptor<Diner> captor = ArgumentCaptor.forClass(Diner.class);
    dinerService.updateDiner(1, dto);

    //then
    verify(dinerRepository).save(captor.capture());
    assertEquals(captor.getValue().getLink(), "diner1.com"); // change
    assertEquals(captor.getValue().getLatitude(), "37.11111111"); // not change
    assertEquals(captor.getValue().getLongitude(), "127.11111111"); // not change
  }

  @Test
  @DisplayName("식당 정보 수정 - 실패(존재하지 않는 식당)")
  void test_update_diner_fail_not_found() {
    //given
    UpdateDinerDto dto = UpdateDinerDto.builder()
        .link("diner1.com")
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(DinerNotFoundException.class,
        () -> dinerService.updateDiner(1, dto));
  }

  @Test
  @DisplayName("식당 정보 수정 - 실패(DB)")
  void test_update_diner_fail_db() {
    //given
    UpdateDinerDto dto = UpdateDinerDto.builder()
        .link("diner1.com")
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .build()
        ));
    when(dinerRepository.save(any()))
        .thenThrow(RuntimeException.class);

    //when
    //then
    assertThrows(InternalServerError.class,
        () -> dinerService.updateDiner(1, dto));
  }

  @Test
  @DisplayName("식당 태그 추가 - 성공(다른 태그가 없을 때)")
  void test_update_diner_add_tag_blank() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("태그1", "태그2"));

    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .tags(new ArrayList<>())
                .build()
        ));

    //when
    ArgumentCaptor<Diner> captor = ArgumentCaptor.forClass(Diner.class);
    dinerService.addDinerTag(1, dto);

    //then
    verify(dinerRepository).save(captor.capture());
    assertEquals(captor.getValue().getTags().size(), 2);
  }

  @Test
  @DisplayName("식당 태그 추가 - 성공(다른 태그가 있을 때, 기존 태그가 없어지면 안됨)")
  void test_update_diner_add_tag_not_blank() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("태그2", "태그3"));

    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .tags(new ArrayList<>(List.of("태그1")))
                .build()
        ));

    //when
    ArgumentCaptor<Diner> captor = ArgumentCaptor.forClass(Diner.class);
    dinerService.addDinerTag(1, dto);

    //then
    verify(dinerRepository).save(captor.capture());
    assertEquals(captor.getValue().getTags().size(), 3);
    assertEquals(captor.getValue().getTags().getFirst(), "태그1");
  }

  @Test
  @DisplayName("식당 태그 추가 - 실패(동일한 이름의 태그가 이미 존재함)")
  void test_update_diner_add_tag_already_exist_tag() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("태그1", "태그2"));

    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .tags(new ArrayList<>(List.of("태그2")))
                .build()
        ));

    //when
    //then
    assertThrows(DuplicateDinerTagException.class,
        () -> dinerService.addDinerTag(1, dto)
    );
  }

  @Test
  @DisplayName("식당 태그 삭제 - 성공")
  void test_update_diner_remove_tag() {
    //given
    RemoveDinerTagsDto dto = new RemoveDinerTagsDto();
    dto.setTags(List.of("태그1", "태그2"));

    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .tags(new ArrayList<>(List.of("태그1", "태그2", "태그3")))
                .build()
        ));

    //when
    ArgumentCaptor<Diner> captor = ArgumentCaptor.forClass(Diner.class);
    dinerService.removeDinerTag(1, dto);

    //then
    verify(dinerRepository).save(captor.capture());
    assertEquals(captor.getValue().getTags().size(), 1);
    assertEquals(captor.getValue().getTags().getFirst(), "태그3");
  }

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
    when(s3Manager.uploadFile(anyLong(), any()))
        .thenReturn(key);

    //when
    dinerService.addDinerImage(1, mockImageFile);
    ArgumentCaptor<DinerImage> captor = ArgumentCaptor.forClass(DinerImage.class);

    //then
    verify(dinerImageRepository).save(captor.capture());
    assertEquals(captor.getValue().getLink(), key);
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
        () -> dinerService.addDinerImage(1L, mockImageFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(이미 등록된 이미지가 10개 이상)")
  void test_update_diner_add_image_fail_max_count() {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDiner(any()))
        .thenReturn(10);

    //when
    //then
    assertThrows(DinerMaxImageCountExceedException.class,
        () -> dinerService.addDinerImage(1L, mockImageFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(S3 저장소에 이미지 업로드 실패)")
  void test_update_diner_add_image_fail_upload() throws Exception {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDiner(any()))
        .thenReturn(0);
    when(s3Manager.uploadFile(anyLong(), any()))
        .thenThrow(new IOException());

    //when
    //then
    assertThrows(ImageUploadFail.class,
        () -> dinerService.addDinerImage(1L, mockImageFile));
  }

  @Test
  @DisplayName("식당 이미지 추가 - 실패(DB에 저장 실패)")
  void test_update_diner_add_image_fail_db_save() throws Exception {
    //given
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(
            Diner.builder().build()
        ));
    when(dinerImageRepository.countByDiner(any()))
        .thenReturn(0);
    when(s3Manager.uploadFile(anyLong(), any()))
        .thenReturn("diner/1/images/" + UUID.randomUUID());
    when(dinerImageRepository.save(any()))
        .thenThrow(new RuntimeException());

    //when
    //then
    assertThrows(InternalServerError.class,
        () -> dinerService.addDinerImage(1L, mockImageFile));
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
                .link(key)
                .build()
        ));

    //when
    dinerService.removeDinerImage(1L, 1L);
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
        () -> dinerService.removeDinerImage(1L, 1L));
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
                .link(key)
                .build()
        ));
    doThrow(new RuntimeException())
        .when(s3Manager).removeFile(any());

    //when
    //then
    assertThrows(ImageDeleteFail.class,
        () -> dinerService.removeDinerImage(1L, 1L));
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
                .link(key)
                .build()
        ));

    //when
    //then
    assertThrows(InternalServerError.class,
        () -> dinerService.removeDinerImage(1L, 1L));
  }
}