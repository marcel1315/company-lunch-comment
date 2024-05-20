package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.marceldev.companylunchcomment.dto.CreateDinerDto;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import java.util.ArrayList;
import java.util.List;
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
  @DisplayName("식당 생성 성공")
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
}