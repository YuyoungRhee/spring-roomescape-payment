# 방탈출 서비스(Room Escape)

이 프로젝트는 방탈출 서비스의 예약 시스템을 구현합니다.

## 배포 URL
[http://3.36.69.170:8080/login](http://3.36.69.170:8080/login)

### 테스트 계정

| 역할    | ID                | Password  |
|-------|-------------------|-----------|
| ADMIN | admin@email.com   | 1234  |
| USER  | alice@example.com | 1234  |
| USER  | bob@example.com   | 1234  |


## ERD

![Untitled](https://github.com/user-attachments/assets/f8633e4d-9889-4e69-8e89-04fac164dcab)
[https://dbdiagram.io/d/6842dc26a845fcbe14aa2c82](https://dbdiagram.io/d/6842dc26a845fcbe14aa2c82)

## API 문서

[http://3.36.69.170:8080/docs/index.html](http://3.36.69.170:8080/docs/index.html)
- 업데이트는 `.gradlew build`


## 📚 도메인 개념
도메인에서 사용되는 개념들을 정리합니다. 실제 도메인으로 나타나지 않은 개념도 포함합니다.

- **Registration**: 등록됨. 예약된 상태(Reservation)와 대기 상태(Waiting)를 모두 포함하는 개념
- **RegistrationSlot**: 테마-날짜-시간의 조합으로, 예약 가능한 하나의 슬롯을 의미
- **Reservation**: 예약됨. RegistrationSlot(테마-날짜-시간)을 선택하여 예약이 확정된 상태
- **WaitingReservation**: 예약 대기. RegistrationSlot에 대해 예약이 승인되지 않은 상태
- **Theme**: 방탈출 테마
- **ReservationTime**: 예약 시간
- **Date**: 예약 날짜. `LocalDate`를 사용하여 표현
- **MyPage**: 사용자가 자신의 예약 및 대기 목록을 확인하는 페이지
- **Auth**: 사용자 인증 및 인가를 모두 의미
- **Payment**: 결제. 예약에 대한 결제를 의미.(예약과 1:1 관계)

## 🧩️ 구현 기능

### Theme(테마)

- 기능
    - 관리자가 테마를 추가/삭제/조회하는 API
    - 사용자가 인기 테마를 조회하는 API

### ReservationTime(예약 시간)

- 기능
    - 관리자가 시간을 추가/삭제/조회하는 API
    - 사용자가 테마,날짜에 따른 예약 가능한 시간을 조회하는 API

### Reservation(예약됨)

- 기능
    - 관리자가 예약을 조회(+필터링)/추가/삭제하는 API
    - 사용자가 RegistrationSlot(테마-날짜-시간)을 선택하여 예약을 등록하는 API
- 기능 세부
    - 예약 삭제 시 첫 번째 대기자(Waiter)가 자동으로 예약 승인되도록 구현

### Payment(결제)

- 기능
  - 예약의 전 단계에서 결제를 승인하는 API
  - 승인된 대기(->예약으로 변경됨)에 대한 결제를 승인하는 API
  - 결제를 취소하는 API
- 기능 세부
  - 토스페이먼츠 결제 API를 통해 구현
  - 대기 승인 이벤트가 발행되었을 때 수신하여 NOT_PAID 상태의 결제 생성
  - 예약 취소 이벤트가 발행되었을 때 수신하여 결제 취소

### WaitingReservation(예약 대기)

- 기능
    - 관리자가 예약 대기를 조회/삭제하는 API
    - 사용자가 RegistrationSlot(테마-날짜-시간)에 대해 예약 대기를 요청/삭제하는 API
- 기능 세부
    - 한 사용자가 동일한 RegistrationSlot에 대한 중복 대기 요청 방지
    - 관리자는 모든 Waiting 삭제 가능, 사용자는 본인의 Waiting만 삭제할 수 있도록 권한 제한
    - 대기 우선 순위는 대기를 시작한 시간(waitingStartedAt) 순서대로 계산

### MyPage(마이페이지)

- 기능: 사용자의 booking(Reservation + Waiting)을 모두 조회하는 API
- 기능 세부:
    - 예약+대기 목록 조회 시 date, ReservationTime 순대로 정렬
    - 결제 정보도 함께 조회

### Auth(인증, 인가)

- 기능
    - 사용자 로그인, 로그아웃 API
    - 현재 로그인 되어 있는 사용자 정보를 체크하는 API
- 기능 세부
    - 로그인은 email과 password를 통하여 진행
    - 로그인 성공 시 JWT 토큰을 발급하고, 이를 쿠키에 저장
    - 로그인 체크 시 쿠키에 저장된 JWT 토큰을 검증하여 현재 로그인된 사용자 정보를 반환
