package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.diner.AddDinerTagsRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.dto.diner.DinerDetailOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.DinerOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.GetDinerListRequest;
import com.marceldev.ourcompanylunch.dto.diner.RemoveDinerTagsRequest;
import com.marceldev.ourcompanylunch.dto.diner.UpdateDinerRequest;
import com.marceldev.ourcompanylunch.dto.error.ErrorResponse;
import com.marceldev.ourcompanylunch.exception.diner.DinerMaxImageCountExceedException;
import com.marceldev.ourcompanylunch.exception.diner.DuplicateDinerTagException;
import com.marceldev.ourcompanylunch.exception.diner.ImageWithNoExtensionException;
import com.marceldev.ourcompanylunch.service.DinerImageService;
import com.marceldev.ourcompanylunch.service.DinerService;
import com.marceldev.ourcompanylunch.service.DinerSubscribeService;
import com.marceldev.ourcompanylunch.service.DinerTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "3 Diner")
public class DinerController {

  private final DinerService dinerService;

  private final DinerImageService dinerImageService;

  private final DinerSubscribeService dinerSubscribeService;

  private final DinerTagService dinerTagService;

  @Operation(
      summary = "Register a diner"
  )
  @PostMapping("/diners")
  public ResponseEntity<CreateDinerResponse> createDiner(
      @Validated @RequestBody CreateDinerRequest dto
  ) {
    CreateDinerResponse response = dinerService.createDiner(dto);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get a list of diner",
      description = "Distance is between the company's location and the diner's location.<br>"
          + "Sort by diner name, distance, comments' count."
  )
  @GetMapping("/diners")
  public ResponseEntity<Page<DinerOutputDto>> getDinerList(
      @Validated @ModelAttribute GetDinerListRequest getDinerListRequest
  ) {
    Page<DinerOutputDto> diners = dinerService.getDinerList(getDinerListRequest);
    return ResponseEntity.ok(diners);
  }

  @Operation(
      summary = "Get details of diner"
  )
  @GetMapping("/diners/{id}")
  public ResponseEntity<DinerDetailOutputDto> getDinerDetail(
      @PathVariable long id
  ) {
    DinerDetailOutputDto diner = dinerService.getDinerDetail(id);
    return ResponseEntity.ok(diner);
  }

  @Operation(
      summary = "Update diner information",
      description = "A member can change link, latitude and longitude of diner<br>"
          + ",even the diner is not created by him/herself."
  )
  @PutMapping("/diners/{id}")
  public ResponseEntity<Void> updateDiner(
      @PathVariable long id,
      @Validated @RequestBody UpdateDinerRequest updateDinerRequest
  ) {
    dinerService.updateDiner(id, updateDinerRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Remove a diner"
  )
  @DeleteMapping("/diners/{id}")
  public ResponseEntity<Void> removeDiner(
      @PathVariable long id
  ) {
    dinerService.removeDiner(id);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Add diner tags"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 3002 - Duplicate tag")
  })
  @PutMapping("/diners/{id}/tags")
  public ResponseEntity<Void> addDinerTags(
      @PathVariable long id,
      @Validated @RequestBody AddDinerTagsRequest addDinerTagsRequest
  ) {
    dinerTagService.addDinerTag(id, addDinerTagsRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Remove diner tags"
  )
  @DeleteMapping("/diners/{id}/tags")
  public ResponseEntity<Void> removeDinerTags(
      @PathVariable long id,
      @Validated @RequestBody RemoveDinerTagsRequest removeDinerTagsRequest
  ) {
    dinerTagService.removeDinerTag(id, removeDinerTagsRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Add a diner image"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description =
          "errorCode: 3001 - Max image count exceeded<br>"
              + "errorCode: 3003 - No extension in file")
  })
  @PostMapping(value = "/diners/{id}/images", consumes = "multipart/form-data")
  public ResponseEntity<Void> addDinerImage(
      @PathVariable long id,
      @RequestParam("image") MultipartFile image
  ) {
    dinerImageService.addDinerImage(id, image);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Remove the diner image"
  )
  @DeleteMapping("/diners/images/{id}")
  public ResponseEntity<Void> removeDinerImage(
      @PathVariable long id
  ) {
    dinerImageService.removeDinerImage(id);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Subscribe the diner"
  )
  @PostMapping("/diners/{id}/subscribe")
  public ResponseEntity<Void> subscribeDiner(
      @PathVariable long id
  ) {
    dinerSubscribeService.subscribeDiner(id);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Unsubscribe the diner"
  )
  @PostMapping("/diners/{id}/unsubscribe")
  public ResponseEntity<Void> unsubscribeDiner(
      @PathVariable long id
  ) {
    dinerSubscribeService.unsubscribeDiner(id);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(DinerMaxImageCountExceedException e) {
    return ErrorResponse.badRequest(3001, e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(DuplicateDinerTagException e) {
    return ErrorResponse.badRequest(3002, e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(ImageWithNoExtensionException e) {
    return ErrorResponse.badRequest(3003, e.getMessage());
  }
}
