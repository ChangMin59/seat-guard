## 프로젝트 개요

정원 제한이 있는 수강 신청 시스템에서는 동시에 여러 사용자가 요청을 보내는 상황에서  
정원 초과 처리, 취소 이후 좌석 재배치, 동시에 여러 사용자가 신청할 경우 정원 수보다 많은 인원이 등록되거나
취소 이후 좌석이 제대로 반영되지 않는 문제가 발생할 수 있습니다.

또한 강의 상태(DRAFT, OPEN, CLOSED)와 수강 기간에 따라 신청 및 취소 가능 여부가 달라지기 때문에  
단순한 CRUD 방식으로는 이러한 흐름을 안정적으로 제어하기 어렵습니다.

이 프로젝트는 이러한 문제를 해결하기 위해  
**상태 기반 설계와 대기열(Waiting Queue) 구조를 적용한 수강 신청 시스템**입니다.

- 강의는 상태와 시간 조건을 기준으로 신청 가능 여부를 판단합니다.  
- 정원이 초과된 경우 WAITING 상태로 관리하여 대기열을 구성합니다.  
- 취소가 발생하면 FIFO 방식으로 자동 승격되도록 설계했습니다.

이를 통해 좌석 재배치를 시스템 내부에서 일관되게 처리하도록 구현했습니다.
또한 수강 신청 상태를 다음과 같이 분리했습니다.

- **PENDING** : 신청 완료 (결제 전)  
- **CONFIRMED** : 결제 완료  
- **CANCELLED** : 취소된 상태  

그리고 실제 서비스 환경을 반영하여 다음과 같은 제약 조건을 적용했습니다.
- 결제 이후 **7일 이내에만 취소 가능**  
- **강의 시작 이후 취소 불가**  

사용자 경험 측면에서도 개선을 진행했습니다.
- 상태에 따른 메시지를 함께 제공  
- 대기열 인원 조회 기능 추가  
이를 통해 사용자가 현재 시스템 상태를 직관적으로 이해할 수 있도록 했습니다.

마지막으로 동시 요청 상황에서도 데이터 정합성을 유지하기 위해  
**버전 기반 동시성 제어(@Version)**를 적용했습니다.

이를 통해 정원 관리, 상태 전이, 시간 기반 제약, 동시성 제어까지 포함한  
**흐름 중심의 수강 신청 시스템**을 구현했습니다.

---

## 기술 스택

### Backend
- **Java 17**  
  → 객체지향 기반으로 도메인 로직 구현
  
- **Spring Boot**  
  → REST API 기반 서버 구성
  
- **Spring Data JPA**  
  → 데이터 접근 로직을 간단하게 처리  

### Database
- **H2 Database**  
  → 테스트 및 개발용 인메모리 DB  

### Test
- **JUnit5**  
  → 테스트 코드 작성
  
- **SpringBootTest**  
  → 실제 환경 기반 통합 테스트  

### 개발 도구
- **IntelliJ IDEA**  
- **Postman**  

### 기타
- **Lombok**  
  → 반복 코드 감소
  
- **Gradle**  
  → 빌드 및 의존성 관리

  ---

### 1. 프로젝트 실행

git clone https://github.com/ChangMin59/seat-guard.git  
cd seat-guard  
./gradlew clean build  
./gradlew bootRun  


### 2. 서버 확인

http://localhost:8080

※ UI 없는 REST API 서버이므로 화면은 표시되지 않습니다.

### 3. H2 Database 접속

http://localhost:8080/h2-console  

- JDBC URL: jdbc:h2:mem:testdb  
- Username: sa  
- Password: (비어 있음)

### 4. API 기능 확인

#### 1) 강의 생성

POST http://localhost:8080/classes

json
{
  "title": "테스트 강의",
  "description": "설명",
  "price": 10000,
  "capacity": 2,
  "currentCount": 0,
  "status": "OPEN",
  "startDate": "2030-01-01T00:00:00",
  "endDate": "2031-01-01T00:00:00"
}

  ---

  ## 요구사항 해석 및 가정

### 1. 정원 초과 처리 방식

요구사항에서는 정원 초과 시 신청을 거부하도록 되어 있었지만,
단순 거부는 사용자 입장에서 불편하게 느껴질 수 있다고 판단했습니다.

따라서 정원 초과 시에도 신청을 완전히 차단하지 않고
WAITING 상태를 추가로 정의하여 대기열에 등록할 수 있도록 확장했습니다.

이를 통해 취소 발생 시 자동으로 수강 기회를 제공할 수 있도록 했습니다.

### 2. 강의 상태와 신청 가능 조건

강의 상태(DRAFT, OPEN, CLOSED)와 수강 기간을 함께 고려하여
신청 가능 여부를 판단하도록 해석했습니다.

- DRAFT 상태: 신청 불가
- OPEN 상태: 신청 가능
- CLOSED 상태: 모집 마감 상태

기본적으로 CLOSED 상태에서는 신청이 불가능하지만,
정원 초과 상황에서는 WAITING 상태로 대기열 등록을 허용하도록 확장했습니다.

또한 수강 기간을 반영하여 강의 시작 이후에는 신규 신청이 불가능하도록 제한했습니다.

### 3. 취소 정책 해석

수강 취소는 단순 상태 변경이 아니라 일정 조건을 만족해야 가능하도록 해석했습니다.

- 결제 완료(CONFIRMED) 이후 7일 이내에만 취소 가능
- 강의 시작 이후에는 취소 불가

이를 통해 실제 서비스에서 적용될 수 있는 취소 정책을 반영했습니다.

### 4. 대기열 처리 기준

대기열은 순서를 보장해야 한다고 판단하여
가장 먼저 신청한 사용자부터 처리되도록 FIFO 방식으로 설계했습니다.

이를 위해 createdAt 기준으로 가장 먼저 대기한 사용자를
취소 발생 시 자동으로 승격하도록 구현했습니다.

### 5. 사용자 응답 처리 및 상태 가시성

단순 상태 반환만으로는 사용자가 현재 상황을 이해하기 어렵다고 판단했습니다.

따라서 각 상태에 맞는 메시지를 함께 제공하고
대기열 인원 수를 조회할 수 있는 기능을 추가하여
사용자가 현재 대기 상황을 확인할 수 있도록 했습니다.

  ---

  ## 미구현 / 제약사항

### 1. 결제 시스템 미연동

현재 구조에서는 `PENDING → CONFIRMED` 상태 전이를 통해 결제 흐름을 표현하고 있으나,  
실제 결제 API(외부 PG사)와의 연동은 구현되어 있지 않습니다.

→ 향후 결제 성공/실패에 따른 상태 분기 처리 및 트랜잭션 보완 필요

### 2. 동시성 처리 한계

`@Version` 기반의 낙관적 락을 적용하여 기본적인 동시성 제어는 가능하지만,  
대량 트래픽 상황에서의 충돌 처리 및 재시도 전략은 구현되어 있지 않습니다.

→ Redis 기반 분산 락 또는 Queue 기반 처리로 확장 가능

### 3. 사용자 인증/인가 미구현

현재는 `userId`를 파라미터로 전달받아 처리하는 구조로,  
실제 서비스에서 필요한 인증/인가(JWT, 세션 등)는 적용되어 있지 않습니다.

→ Spring Security + JWT 기반 인증 구조로 확장 가능

### 4. 강의 상태 자동 관리 단순화

강의 상태(`OPEN`, `CLOSED`)는 시간 기반으로 갱신되지만,  
배치 처리 또는 스케줄러 기반 자동 전환은 적용되어 있지 않습니다.

→ Scheduler 또는 이벤트 기반 상태 관리로 개선 가능

### 5. 예외 처리 구조 단순화

현재는 `RuntimeException` 기반으로 예외를 처리하고 있으며,  
에러 코드 및 응답 포맷 표준화는 적용되어 있지 않습니다.

→ Global Exception Handler를 통한 공통 응답 구조 설계 필요

---

## AI 활용 범위

본 프로젝트는 생성형 AI를 활용해 초기 코드 구조를 구성하고,  
이후 요구사항과 실제 동작 흐름에 맞게 직접 수정·검증하며 완성했습니다.

### 1. 활용 방식

- 수강 신청, 대기열, 상태 전이 로직의 기본 구조를 AI를 통해 초기 작성
- 구현 방향 및 문제 해결 아이디어 참고

### 2. 검증 중심 개발

AI가 생성한 코드를 무작정 사용하는 것이 아니라,  
실제 API 요청과 DB 상태(H2)를 기준으로 동작을 직접 확인하며 검증했습니다.

- 수강 신청 → 상태(PENDING / WAITING) 정상 분기 여부 확인
- 정원 초과 시 WAITING 상태 생성 여부 검증
- 취소 시 상태 변화 및 대기열 승격 동작 확인
- currentCount 값 변화와 상태 간 일치 여부 검증

→ 모든 기능은 테스트 요청과 DB 조회를 통해 의도한 결과가 나오는지 확인 후 적용

### 3. 직접 수정 및 보완

검증 과정에서 발견된 문제는 직접 수정하여 로직을 개선했습니다.

- WAITING 상태가 생성되지 않는 문제 원인 분석 및 수정
- currentCount 기준과 상태 흐름 간 불일치 해결
- 요청 흐름에 맞게 상태 전이 로직 재구성

→ 최종 결과물은 단순 생성된 코드가 아니라,  
동작을 기준으로 직접 검증하고 수정한 코드입니다.

---

## API 목록 및 예시

### 1. 강의 생성

POST http://localhost:8080/classes

```json
{
  "title": "테스트 강의",
  "description": "설명",
  "price": 10000,
  "capacity": 2,
  "currentCount": 0,
  "status": "OPEN",
  "startDate": "2030-01-01T00:00:00",
  "endDate": "2031-01-01T00:00:00"
}

2. 강의 목록 조회

GET http://localhost:8080/classes

→ 전체 강의 목록 조회 (상태 필터 가능)

3. 강의 상세 조회

GET /classes/{id}

→ 특정 강의 상세 정보 조회

4. 수강 신청

POST http://localhost:8080/enrollments?classId={classId}&userId={userId}

예시
POST http://localhost:8080/enrollments?classId=1&userId=1

→ 정원 내: PENDING
→ 정원 초과: WAITING

5. 수강 취소

PATCH http://localhost:8080/enrollments/{enrollmentId}/cancel

예시
PATCH http://localhost:8080/enrollments/1/cancel

→ 상태: CANCELLED
→ 대기열 존재 시 WAITING → CONFIRMED 자동 승격

6. 수강 확정 (결제 완료)

PATCH http://localhost:8080/enrollments/{enrollmentId}/confirm

예시
PATCH http://localhost:8080/enrollments/1/confirm

→ 상태: PENDING → CONFIRMED

7. 내 수강 신청 목록 조회

GET http://localhost:8080/enrollments/my?userId={userId}

예시
GET http://localhost:8080/enrollments/my?userId=1

→ 해당 사용자의 수강 신청 목록 조회

8. 대기열 인원 조회

GET http://localhost:8080/enrollments/{classId}/waiting-count

예시
GET http://localhost:8080/enrollments/1/waiting-count

→ WAITING 상태 인원 수 반환


## 데이터 모델 설명

본 시스템은 **강의(Class)** 와 **수강 신청(Enrollment)** 두 개의 핵심 엔티티를 중심으로 구성됩니다.  
수강 신청은 강의와 N:1 관계를 가지며, 상태 기반으로 동작합니다.

### 1. Class (강의)

강의 정보와 수강 가능 상태를 관리하는 엔티티입니다.

| 필드명 | 설명 |
|--------|------|
| id | 강의 ID |
| title | 강의 제목 |
| description | 강의 설명 |
| price | 강의 가격 |
| capacity | 최대 수강 인원 |
| currentCount | 현재 수강 신청 인원 |
| status | 강의 상태 (DRAFT, OPEN, CLOSED) |
| startDate | 강의 시작일 |
| endDate | 강의 종료일 |

👉 역할  
- 수강 가능 여부 판단 (정원, 기간, 상태)  
- 수강 신청 시 인원 제한 관리  

### 2. Enrollment (수강 신청)

사용자의 수강 신청 정보와 상태를 관리하는 엔티티입니다.

| 필드명 | 설명 |
|--------|------|
| id | 수강 신청 ID |
| userId | 사용자 ID |
| class_id | 강의 ID (외래키) |
| status | 수강 상태 |
| createdAt | 신청 시간 |

👉 상태 정의  

- **PENDING**  
  → 수강 신청 완료 상태 (정원 내)  

- **CONFIRMED**  
  → 수강 확정 상태 (결제 완료)  

- **WAITING**  
  → 정원 초과로 대기열에 등록된 상태  

- **CANCELLED**  
  → 수강 취소 상태  

### 3. 관계 구조

Class (1) ─── (N) Enrollment
하나의 강의에는 여러 수강 신청이 존재
수강 신청은 하나의 강의에만 속함

4. 핵심 동작 구조
수강 신청 → PENDING / WAITING
결제 완료 → CONFIRMED
수강 취소 → CANCELLED
정원 초과 → WAITING
취소 발생 → WAITING → CONFIRMED 자동 승격

---

## 테스트 실행 방법

### 1. 자동 테스트 실행

./gradlew test

→ 주요 비즈니스 로직 검증
- 수강 신청 (정원 / 대기열)
- 중복 신청 방지
- 수강 취소 및 상태 변화

### 2. 수동 테스트 (API)

Postman 또는 브라우저를 통해 API를 호출하여  
대기열 및 상태 변화 흐름을 직접 확인할 수 있습니다.
