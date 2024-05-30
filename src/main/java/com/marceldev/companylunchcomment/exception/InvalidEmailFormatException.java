package com.marceldev.companylunchcomment.exception;

public class InvalidEmailFormatException extends CustomException {

  public InvalidEmailFormatException(String email) {
    // 항상 외부 응답으로부터 온 에러는 아닐 수 있으므로, HttpStatus를 null로 놓았음
    // TODO: 내부에 잘못된 흐름으로 에러가 나는 에러타입들은 어떻게 할지 생각해보기
    super(null, String.format("Invalid email format: %s", email));
  }
}
