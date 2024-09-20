package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.error.ErrorResponse;
import com.marceldev.ourcompanylunch.dto.member.SignUpDto;
import com.marceldev.ourcompanylunch.dto.member.UpdateMemberDto;
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
@Tag(name = "1 Member", description = "회원 관련")
public class MemberController {

  private final MemberService memberService;

  @Operation(
      summary = "회원정보 수정",
      description = "사용자는 자신의 이름을 수정할 수 있다."
  )
  @PutMapping("/members/{id}")
  public ResponseEntity<Void> updateMember(
      @PathVariable long id,
      @Validated @RequestBody UpdateMemberDto updateMemberDto
  ) {
    memberService.updateMember(id, updateMemberDto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Sign up from auth server(Do not call from client)"
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
