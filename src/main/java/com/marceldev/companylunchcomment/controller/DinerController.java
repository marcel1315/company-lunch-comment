package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.diner.AddDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.CreateDinerDto;
import com.marceldev.companylunchcomment.dto.diner.RemoveDinerTagsDto;
import com.marceldev.companylunchcomment.dto.diner.UpdateDinerDto;
import com.marceldev.companylunchcomment.response.CustomResponse;
import com.marceldev.companylunchcomment.service.DinerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "Diner", description = "식당 관련")
public class DinerController {

  private final DinerService dinerService;

  @Operation(
      summary = "식당 생성",
      description = "식당 이름, 식당 웹사이트 링크, 위도, 경도를 입력한다.<br>"
          + "식당 태그도 입력 가능하다. (#한식, #양식, #깔끔, #간단, #매움, #양많음 등 사용자가 임의 등록 가능)"
  )
  @PostMapping("/diner")
  public CustomResponse<?> createDiner(@Validated @RequestBody CreateDinerDto createDinerDto) {
    dinerService.createDiner(createDinerDto);
    return CustomResponse.success();
  }

  @Operation(
      summary = "식당 정보 수정",
      description = "사용자는 식당 웹사이트 링크, 위도, 경도 정보를 수정할 수 있다. 자신이 작성하지 않은 식당도 수정할 수 있다."
  )
  @PostMapping("/diner/{id}")
  public CustomResponse<?> updateDiner(
      @PathVariable long id,
      @Validated @RequestBody UpdateDinerDto updateDinerDto
  ) {
    dinerService.updateDiner(id, updateDinerDto);
    return CustomResponse.success();
  }

  @Operation(
      summary = "식당 태그 추가"
  )
  @PutMapping("/diner/{id}/tags")
  public CustomResponse<?> addDinerTags(
      @PathVariable long id,
      @Validated @RequestBody AddDinerTagsDto addDinerTagsDto
  ) {
    dinerService.addDinerTag(id, addDinerTagsDto);
    return CustomResponse.success();
  }

  @Operation(
      summary = "식당 태그 제거"
  )
  @DeleteMapping("/diner/{id}/tags")
  public CustomResponse<?> removeDinerTags(
      @PathVariable long id,
      @Validated @RequestBody RemoveDinerTagsDto removeDinerTagsDto
  ) {
    dinerService.removeDinerTag(id, removeDinerTagsDto);
    return CustomResponse.success();
  }

  @Operation(
      summary = "식당 사진 추가"
  )
  @PostMapping("/diner/{id}/images")
  public CustomResponse<?> addDinerImage(
      @PathVariable long id,
      @RequestParam("file") MultipartFile file
  ) {
    dinerService.addDinerImage(id, file);
    return CustomResponse.success();
  }
}
