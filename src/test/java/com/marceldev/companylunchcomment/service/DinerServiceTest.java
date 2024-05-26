package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.component.S3Manager;
import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.DinerDetailOutputDto;
import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.InternalServerError;
import com.marceldev.companylunchcomment.repository.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.type.DinerSort;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DinerServiceTest {

  @Mock
  private DinerRepository dinerRepository;

  @Mock
  private S3Manager s3Manager;

  @InjectMocks
  private DinerService dinerService;

  @Test
  @DisplayName("식당 생성 - 성공")
  void test_create_diner() {
    //given
    LinkedHashSet<String> tags = new LinkedHashSet<>();
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
    assertEquals(diner.getTags().getLast(), "분위기좋은");
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
                .tags(new LinkedHashSet<>())
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
                .tags(new LinkedHashSet<>(List.of("태그1")))
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
  @DisplayName("식당 태그 추가 - 성공(동일한 이름의 태그가 이미 존재하더라도 없는 것만 추가하고, 에러는 내지 않음)")
  void test_update_diner_add_tag_already_exist_tag() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("태그1", "태그2"));

    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .tags(new LinkedHashSet<>(List.of("태그2")))
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
                .tags(new LinkedHashSet<>(List.of("태그1", "태그2", "태그3")))
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
  @DisplayName("식당 목록 불러오기 - 성공")
  void test_get_diner_list() {
    //given
    GetDinerListDto dto = GetDinerListDto.builder()
        .page(1)
        .pageSize(10)
        .dinerSort(DinerSort.DINER_NAME_ASC)
        .build();

    Diner diner1 = Diner.builder()
        .id(1L)
        .name("감성타코")
        .link("diner.com")
        .build();
    Diner diner2 = Diner.builder()
        .id(2L)
        .name("감성타코2")
        .link("diner2.com")
        .build();

    Page<Diner> pages = new PageImpl<>(List.of(diner1, diner2));
    when(dinerRepository.findAll(any(PageRequest.class)))
        .thenReturn(pages);

    //when
    Page<DinerOutputDto> page = dinerService.getDinerList(dto);

    //then
    assertEquals(page.getContent().size(), 2);
    assertEquals(page.getContent().getFirst().getName(), "감성타코");
  }

  @Test
  @DisplayName("식당 상세 불러오기 - 성공")
  void test_get_diner_detail() {
    //given
    Diner diner = Diner.builder()
        .id(1L)
        .name("감성타코")
        .link("diner.com")
        .dinerImages(List.of())
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(diner));
    when(s3Manager.getPresignedUrls(any()))
        .thenReturn(List.of("https://s3.example.com/1", "https://s3.example.com/2"));

    //when
    DinerDetailOutputDto dinerDetail = dinerService.getDinerDetail(1L);

    //then
    assertEquals(dinerDetail.getName(), "감성타코");
    assertEquals(dinerDetail.getImageUrls().getFirst(), "https://s3.example.com/1");
  }

  @Test
  @DisplayName("식당 상세 불러오기 - 성공(S3 저장소에서 url만들기가 실패하더라도 나머지 정보로 성공 돌려주기")
  void test_get_diner_detail_even_if_s3_fail() {
    //given
    Diner diner = Diner.builder()
        .id(1L)
        .name("감성타코")
        .link("diner.com")
        .dinerImages(List.of())
        .build();

    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(diner));
    when(s3Manager.getPresignedUrls(any()))
        .thenThrow(RuntimeException.class);

    //when
    DinerDetailOutputDto dinerDetail = dinerService.getDinerDetail(1L);

    //then
    assertEquals(dinerDetail.getName(), "감성타코");
  }
}