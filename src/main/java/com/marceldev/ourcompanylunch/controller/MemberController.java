package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.error.ErrorResponse;
import com.marceldev.ourcompanylunch.dto.member.SignUpDto;
import com.marceldev.ourcompanylunch.dto.member.UpdateMemberRequest;
import com.marceldev.ourcompanylunch.exception.member.AlreadyExistMemberException;
import com.marceldev.ourcompanylunch.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "1 Member")
public class MemberController {

  private final MemberService memberService;

  @Operation(
      summary = "Update member info",
      description = "A member can change his/her own name."
  )
  @PutMapping("/members/{id}")
  public ResponseEntity<Void> updateMember(
      @PathVariable long id,
      @Validated @RequestBody UpdateMemberRequest updateMemberRequest
  ) {
    memberService.updateMember(id, updateMemberRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Sign up from auth server(Do not call from web client)"
  )
  @PostMapping("/members/signup")
  public ResponseEntity<Void> signupMember(
      @Validated @RequestBody SignUpDto dto
  ) {
    memberService.signUp(dto);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(AlreadyExistMemberException e) {
    return ErrorResponse.badRequest(1001, e.getMessage());
  }
}
