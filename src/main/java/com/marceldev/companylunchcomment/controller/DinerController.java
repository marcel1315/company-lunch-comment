package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.CreateDinerDto;
import com.marceldev.companylunchcomment.service.DinerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DinerController {

  private final DinerService dinerService;

  /**
   * 식당 생성
   *   - 식당 이름, 식당 웹사이트 링크, 위도, 경도를 입력한다.
   *   - 식당 태그도 입력 가능하다. (#한식, #양식, #깔끔, #간단, #매움, #양많음 등 사용자가 임의 등록 가능)
   */
  @PostMapping("/diner")
  public ResponseEntity<?> createDiner(@Validated @RequestBody CreateDinerDto createDinerDto) {
    dinerService.createDiner(createDinerDto);
    return ResponseEntity.ok(null);
  }
}
