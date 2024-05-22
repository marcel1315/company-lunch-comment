package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.dto.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.DuplicateDinerTagException;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DinerServiceTest {

  @Mock
  private DinerRepository dinerRepository;

  @InjectMocks
  private DinerService dinerService;

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
}