# 프로젝트 설명

## 직장인 점심 기록 서비스 백엔드

- 회사 이메일로 서비스를 가입하고, 직접 주변에 있는 식당을 등록합니다.
- 자신이 먹은 점심과 식당에 대한 코멘트를 기록하고, 사내에서 공유합니다.

## 서비스 기대효과

- 자신이 먹었던 점심 코멘트들을 확인하여, 다음번 점심을 선택할 때 참고합니다.
- 사내 아는 사람들에 의한 코멘트이므로 포털 리뷰보다 믿을 수 있습니다.

# 프로젝트 기능

- 회원가입 기능
  - 사용자는 회원가입을 할 수 있다. 모든 사용자는 회원가입 시 USER 권한(일반 권한)을 지닌다.
  - 회원가입시 이메일, 이름과 비밀번호를 입력받는다. 이메일이 아이디가 되며 unique 해야한다.
  - 구글, 네이버, 카카오, 다음, 한메일, 야후 등 이메일 공급자로부터 받은 이메일은 가입할 수 없다. 회사 도메인을 사용해야 한다.
  - 회원가입 중 이메일을 통한 번호인증을 한다.

- 로그인 기능
  - 사용자는 로그인을 할 수 있다. 로그인시 회원가입때 사용한 아이디(이메일)와 패스워드가 일치해야 한다.

- 회원정보 수정 기능
  - 사용자는 자신의 이름을 수정할 수 있다.

- 회원탈퇴 기능
  - 아이디(이메일)과 비밀번호로 탈퇴할 수 있다.

- 회사 등록 기능
  - 사용자는 회사를 등록할 수 있다. 가입한 이메일 도메인으로 회사가 등록된다.
  - 회사 등록시 이름, 주소, 위도, 경도를 입력한다.
  - 같은 회사라도 여러 주소가 있을 수 있으므로, 회사 등록은 여러번 가능한다.

- 회사 조회 및 선택 기능
  - 사용자는 가입한 이메일 도메인으로 등록된 회사들을 조회할 수 있다.
  - 사용자는 조회된 회사들 중 하나를 선택한다.

- 회사 정보 수정 기능
  - 사용자는 회사 정보를 수정할 수 있다. 주소, 위도, 경도를 수정할 수 있다.
  - 회사 정보 수정을 위해 이메일을 통한 번호 인증을 해야한다.

- 식당 등록 기능
  - 사용자는 식당을 등록할 수 있다.
  - 식당 이름, 식당 웹사이트 링크, 위도, 경도 정보를 입력한다.
  - 식당 태그도 입력 가능하다. (#한식, #양식, #깔끔, #간단, #매움, #양많음 등 사용자가 임의 등록 가능)

- 식당 수정 기능
  - 사용자는 식당 웹사이트 링크, 위도, 경도 정보를 수정할 수 있다. 자신이 작성하지 않은 식당도 수정할 수 있다.
  - 식당 태그도 추가 등록, 삭제가 가능하다.
  - 식당 메뉴 등을 위한 사진을 업로드 및 삭제할 수 있다. 사진 업로드 시 이미지 파일과 함께 사진이 나타날 순서도 함께 입력한다. 사진 순서도 수정할 수 있다.

- 식당 목록 조회 기능
  - 사용자는 식당의 목록을 조회할 수 있다.
  - 식당 이름, 태그, 거리, 코멘트 갯수를 목록으로 볼 수 있다.
  - 회사의 위도와 경도, 식당의 위도와 경도를 사용해 회사로부터 식당의 거리를 표시한다.
  - 식당은 식당 이름, 거리, 코멘트 갯수로 정렬할 수 있다.

- 식당 상세 조회 기능
  - 사용자는 식당을 상세 조회할 수 있다.
  - 이름, 태그, 거리, 코멘트 갯수, 코멘트 목록, 사진 썸네일 목록을 볼 수 있다.
  - 필요한 경우 원본 사진을 가져올 수 있다.

- 식당 제거 기능
  - 사용자는 식당을 제거할 수 있다. 자신이 작성하지 않은 식당도 삭제 할 수 있다.

- 코멘트 작성 기능
  - 사용자는 등록된 식당에 대해 코멘트를 작성할 수 있다.
  - 식당, 코멘트 내용, 사내 공유 여부를 입력한다.

- 코멘트 조회 기능
  - 사용자는 사내 공유된 코멘트 목록을 조회할 수 있다.
  - 본인 여부, 작성자 이름, 코멘트 내용, 식당으로 목록을 조회할 수 있다.

- 코멘트 수정 기능
  - 사용자는 자신이 작성한 코멘트를 수정할 수 있다.
  - 코멘트 내용, 사내 공유 여부를 수정할 수 있다.

- 코멘트 삭제 기능
  - 사용자는 자신이 작성한 코멘트를 삭제할 수 있다.

- 댓글 작성, 수정 및 제거 기능
  - 사용자는 코멘트에 댓글 작성, 수정 및 제거가 가능하다.
  - 댓글은 모든 코멘트에 대해 작성 가능하다.
  - 수정 및 제거는 자신이 작성한 댓글만 가능하다.

# ERD

![ERD](doc/img/company-lunch-comment-erd.png)

# Tech Stack

- Java 21

- Spring Boot 3.2.5

- Spring Web

- Spring Security

- Spring Validation

- Spring Data JPA

- MariaDB

- Java Mail Sender

- Mockito

- Lombok

- AWS S3

# Glossary

- 코멘트 : 짧은 글인 것을 강조하기 위해 “글” 또는 “리뷰”보다 “코멘트”라는 용어를 사용합니다. "글"은 제목과 본문을 가지고 길게 써야할 것 같습니다. "리뷰"는 평점이
  들어가야 할 것 같습니다.

# Troubleshooting

- [Go to the troubleshooting section](doc/TROUBLESHOOTING)