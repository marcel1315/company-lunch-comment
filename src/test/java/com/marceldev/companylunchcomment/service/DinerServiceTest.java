package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import com.marceldev.companylunchcomment.dto.member.SecurityMember;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerImage;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.CompanyNotExistException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.repository.diner.DinerImageRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.type.DinerSort;
import com.marceldev.companylunchcomment.type.Role;
import com.marceldev.companylunchcomment.type.SortDirection;
import com.marceldev.companylunchcomment.util.LocationUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DinerServiceTest {

  @Mock
  private DinerRepository dinerRepository;

  @Mock
  private DinerImageRepository dinerImageRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private S3Manager s3Manager;

  @InjectMocks
  private DinerService dinerService;

  // 테스트에서 목으로 사용될 company. diner를 가져올 때, member가 속한 company의 diner가 아니면 가져올 수 없음
  Company company1 = Company.builder()
      .id(1L)
      .name("좋은회사")
      .address("서울특별시 강남구 강남대로 200")
      .location(LocationUtil.createPoint(127.123123, 37.123123))
      .domain("example.com")
      .build();

  // 테스트에서 목으로 사용될 member. diner를 가져올 때, 적절한 member가 아니면 가져올 수 없음
  Member member1 = Member.builder()
      .id(1L)
      .email("kys@example.com")
      .name("김영수")
      .role(Role.USER)
      .password("somehashedvalue")
      .company(company1)
      .build();

  @BeforeEach
  public void setupMember() {
    GrantedAuthority authority = new SimpleGrantedAuthority("USER");
    Collection authorities = Collections.singleton(authority); // Use raw type here

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    SecurityMember securityMember = SecurityMember.builder().member(member1).build();
    lenient().when(authentication.getPrincipal()).thenReturn(securityMember);

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    lenient().when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(member1));
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

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
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(tags)
        .build();

    Company company = Company.builder()
        .id(1L)
        .name("좋은회사")
        .build();

    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder()
                .company(company)
                .build()
        ));

    //when
    ArgumentCaptor<Diner> captor = ArgumentCaptor.forClass(Diner.class);
    dinerService.createDiner(dto);

    //then
    verify(dinerRepository).save(captor.capture());
    Diner diner = captor.getValue();
    assertEquals(diner.getName(), "감성타코");
    assertEquals(diner.getLink(), "diner.com");
    assertEquals(diner.getLocation(), LocationUtil.createPoint(127.39232323, 37.29283882));
    assertEquals(diner.getTags().getLast(), "분위기좋은");
  }

  @Test
  @DisplayName("식당 생성 - 실패(선택된 회사가 없음)")
  void test_create_diner_no_company_chosen() {
    //given
    CreateDinerDto dto = CreateDinerDto.builder()
        .name("감성타코")
        .build();

    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder()
                .company(null)
                .build()
        ));

    //when
    //then
    assertThrows(
        CompanyNotExistException.class,
        () -> dinerService.createDiner(dto)
    );
  }

  @Test
  @DisplayName("식당 정보 수정 - 성공")
  void test_update_diner() {
    //given
    UpdateDinerDto dto = UpdateDinerDto.builder()
        .link("diner1.com")
        .latitude(37.11111111)
        .longitude(127.11111111)
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .link("diner.com")
                .location(LocationUtil.createPoint(127.11111111, 37.11111111))
                .company(company1)
                .build()
        ));

    //when
    //then
    dinerService.updateDiner(1, dto);
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
                .company(company1)
                .tags(new LinkedHashSet<>())
                .build()
        ));

    //when
    //then
    dinerService.addDinerTag(1, dto);
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
                .company(company1)
                .tags(new LinkedHashSet<>(List.of("태그1")))
                .build()
        ));

    //when
    //then
    dinerService.addDinerTag(1, dto);
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
                .company(company1)
                .tags(new LinkedHashSet<>(List.of("태그2")))
                .build()
        ));

    //when
    //then
    dinerService.addDinerTag(1, dto);
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
                .company(company1)
                .tags(new LinkedHashSet<>(List.of("태그1", "태그2", "태그3")))
                .build()
        ));

    //when
    //then
    dinerService.removeDinerTag(1, dto);
  }

  @Test
  @DisplayName("식당 목록 불러오기 - 성공")
  void test_get_diner_list() {
    //given
    GetDinerListDto dto = GetDinerListDto.builder()
        .sortBy(DinerSort.DINER_NAME)
        .sortDirection(SortDirection.ASC)
        .build();

    DinerOutputDto diner1 = DinerOutputDto.builder()
        .id(1L)
        .name("감성타코")
        .link("diner.com")
        .latitude(37.123123)
        .longitude(127.123123)
        .tags(new LinkedHashSet<>(List.of("태그1", "태그2")))
        .build();
    DinerOutputDto diner2 = DinerOutputDto.builder()
        .id(2L)
        .name("감성타코2")
        .link("diner2.com")
        .latitude(37.123123)
        .longitude(127.123123)
        .tags(new LinkedHashSet<>(List.of("태그1", "태그2")))
        .build();

    Page<DinerOutputDto> pages = new PageImpl<>(List.of(diner1, diner2));
    when(dinerRepository.getList(anyLong(), any(), any()))
        .thenReturn(pages);
    PageRequest pageable = PageRequest.of(0, 10);

    //when
    Page<DinerOutputDto> page = dinerService.getDinerList(dto, pageable);

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
        .company(company1)
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
        .company(company1)
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

  @Test
  @DisplayName("식당 제거 - 성공")
  void test_remove_diner() {
    //given
    Diner diner = Diner.builder()
        .id(1L)
        .name("감성타코")
        .company(company1)
        .dinerImages(List.of(
            DinerImage.builder()
                .s3Key("diner/1/images/unrastu-29823-unr0w-w82nu")
                .build(),
            DinerImage.builder()
                .s3Key("diner/1/images/aryusn2-sutnr-238fu-92uss")
                .build()
        ))
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(diner));

    //when
    dinerService.removeDiner(1L);

    //then
    verify(dinerImageRepository).deleteByDinerId(any());
    verify(dinerRepository).delete(any());
    verify(s3Manager, times(2)).removeFile(any());
  }

  @Test
  @DisplayName("식당 제거 - 성공(S3 저장소의 이미지 삭제를 실패해도 식당 제거는 성공을 내보냄")
  void test_remove_diner_even_if_s3_fail() {
    //given
    Diner diner = Diner.builder()
        .id(1L)
        .name("감성타코")
        .company(company1)
        .dinerImages(List.of(
            DinerImage.builder()
                .s3Key("diner/1/images/unrastu-29823-unr0w-w82nu")
                .build(),
            DinerImage.builder()
                .s3Key("diner/1/images/aryusn2-sutnr-238fu-92uss")
                .build()
        ))
        .build();
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.of(diner));
    doThrow(new RuntimeException())
        .when(s3Manager).removeFile(any());

    //when
    dinerService.removeDiner(1L);

    //then
    verify(dinerImageRepository).deleteByDinerId(any());
    verify(dinerRepository).delete(any());
  }
}