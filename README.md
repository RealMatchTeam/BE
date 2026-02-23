![img.png](img.png)

![img_2.png](img_2.png)

# 📖 프로젝트 소개

> **Real Match**는 브랜드와 마이크로 인플루언서를 연결하는  
> **매칭 및 협업 관리 플랫폼**입니다.

최근 마이크로 인플루언서의 영향력이 확대되면서 협업 수요는 증가하고 있지만,  
여전히 많은 협업이 **DM·이메일 중심의 비정형적인 방식**으로 이루어지고 있습니다.

---

## 🎯 Real Match가 제공하는 핵심 기능

- 📌 **매칭 알고리즘 기반 추천**
- 📊 **매칭 진단 대시보드**
- 📅 **협업 상태 추적 및 일정 관리 로직**
- 💬 **실시간 커뮤니케이션 및 알림 시스템**

---

Real Match는 위 기능을 통합 제공하여 브랜드와 인플루언서 간 협업 전 과정을  
**하나의 플랫폼에서 체계적으로 관리할 수 있도록 설계되었습니다.**

단순 매칭을 넘어,

> 👉 **데이터 기반 분석 + 협업 프로세스 통합 관리**

를 통해 보다 정교하고 효율적인 브랜드 마케팅 환경을 구축하는 것을 목표로 합니다.

## 🧩 주요 기능

| 기능               | 설명                          |
|------------------|-----------------------------|
| 📝 **매칭 검사**     | 매칭 알고리즘 기반 브랜드 추천           |
| 🔗 **매칭 진단 대시보드** | 브랜드 · 인플루언서간의 매칭 현황 관리      |
| 📆 **협업 일정 관리**  | 일정 및 협업 관리 캘린더              |
| 📆 **실시간 채팅 기능** | 브랜드·인플루언서 간 실시간 메시지 및 파일 공유     |
| 🔍 **브랜드 · 캠페인 검색** | 키워드 기반 브랜드 · 캠페인 검색         |
| 🔔 **알림 기능**     | 매칭, 일정 변경, 협업 상태 업데이트에 대한 실시간 알림 (푸시/이메일) |
| 👤 **회원 관리**     | JWT 기반 회원가입 / 소셜 로그인        |
| 🖼️ **프로필 관리**   | 마이페이지에서 프로필 및  관심사 수정       |

---

## 🏛 아키텍쳐 구조

![img_3.png](img_3.png)


---

## 👩‍💻 RealMatch Spring Developers

<div >

| <img src="https://github.com/lingard1234.png" width="120" height="120"> | <img src="https://github.com/ParkJiYeoung8297.png" width="120" height="120"> | <img src="https://github.com/1000hyehyang.png" width="120" height="120"> | <img src="https://github.com/yerimi00.png" width="120" height="120"> | <img src="https://github.com/Yoonchulchung.png" width="120" height="120"> |
|:-----------------------------------------------------------------------:|:----------------------------------------------------------------------------:|:------------------------------------------------------------------------:|:--------------------------------------------------------------------:|:-------------------------------------------------------------------------:|
|                [**고경수**](https://github.com/lingard1234)                |                [**박지영**](https://github.com/ParkJiYeoung8297)                |                [**여채현**](https://github.com/1000hyehyang)                |                [**이예림**](https://github.com/yerimi00)                |                [**정윤철**](https://github.com/Yoonchulchung)                |
|                           `Backend Developer`                           |                        `Backend Developer / API Lead`                        |                           `Backend Developer`                            |                         `Backend Developer`                          |                   `Backend Developer / Algorithm Lead`                    |
|                           로그인/회원가입<br/>마이페이지                            |                           비즈니스 (매칭 · 협업)  <br/>캠페인                           |                                채팅 <br>알림                                 |                                 브랜드                                  |                               매칭 검사 / 알고리즘                                |

</div>


## 👥 Detailed Responsibilities
<p align="center">
<sub>🖱️ 좌우로 스크롤해서 내용을 확인해주세요.</sub>
</p>
<div style="display: flex; gap: 20px; overflow-x: auto; padding: 10px 0;">

<div style="min-width: 320px; border: 1px solid #ddd; padding: 18px; border-radius: 12px;">



### 👨‍💻 고경수
**Backend Developer**

- Spring Security 기반 인증/인가 구현
- JWT 토큰 발급 및 검증 로직 개발
- 사용자 정보 관리 및 마이페이지 API 구현
- Role 기반 접근 제어 처리

</div>

<div style="min-width: 320px; border: 1px solid #ddd; padding: 18px; border-radius: 12px;">

### 👩‍💻 박지영
**Backend Developer / API Lead**

- 협업 플로우 API 설계 및 구현 (지원 · 제안 · 수락 · 거절)
- 상태 전이 기반 도메인 로직 설계 (제안 → 수락 → 진행 → 완료)
- QueryDSL 기반 협업 캠페인 검색 및 필터링 처리
- 브랜드-캠페인 조회 및 좋아요 기능 구현
- Docker 기반 CD 구축 및 서버 배포 자동화

</div>

<div style="min-width: 320px; border: 1px solid #ddd; padding: 18px; border-radius: 12px;">

### 👩‍💻 여채현
**Backend Developer**

- WebSocket 기반 실시간 채팅 시스템 구현
- STOMP 프로토콜 기반 메시지 브로커 구성
- RabbitMQ 기반 비동기 알림 이벤트 처리
- Firebase · Email 연동 알림 시스템 구현

</div>

<div style="min-width: 320px; border: 1px solid #ddd; padding: 18px; border-radius: 12px;">

### 👩‍🎨 이예림
**Backend Developer**

- 브랜드 도메인 CRUD API 설계 및 구현
- 브랜드 목록 · 상세 · 요약 조회 API 개발
- 브랜드 좋아요 토글 및 협찬 제품 조회 기능 구현

</div>

<div style="min-width: 320px; border: 1px solid #ddd; padding: 18px; border-radius: 12px;">

### 👨‍💻 정윤철
**Backend Developer / Algorithm Lead**

- 사용자·캠페인 태그 기반 매칭 점수 계산 알고리즘 설계
- 매칭 알고리즘 기반 추천 API 설계 및 구현
- Redis 캐싱을 활용한 조회 성능 개선
- Docker 기반 서버 환경 구성 및 CI 자동화 구축

</div>

</div>

---

## 📂 Project Structure

```yml
BE
├── global (공통 설정, 예외 처리, 유틸 등)
└── user
    ├── presentation (외부와의 접점)
    │   ├── UserController.java
    │   └── dto (해당 도메인 전용 데이터 전송 객체)
    │       ├── request
    │       │     └── UserRequest.java
    │       └── response
    │              └── UserResponse.java        
    ├── application (비즈니스 로직의 흐름 제어)
    │   └── service 
    │          ├── UserService.java (Interface)
    │          └── UserServiceImpl.java
    └── domain (핵심 비즈니스 규칙 및 엔티티)
        ├── entity
        │   └── UserEntity.java
        └── repository
            └── UserRepository.java

```
---

## 🛠 Tech Stack

### Backend
- Spring Boot 3.5.9
- Java 21
- Spring Security (JWT, OAuth2)
- Spring Data JPA
- QueryDSL

### Database
- MySQL
- Redis

### Infra
- Docker / Docker Compose
- AWS EC2
- AWS RDS
- AWS S3 / CloudFront

### Communication
- WebSocket (채팅)
- Firebase / Email (알림)
- RabbitMQ

---

## 📏 코드 컨벤션


아래 명령어를 이용하여 convention을 지켜주세요
```bash
git config core.hooksPath .githooks
```

아래 명령어를 이용해서 코드 스타일을 준수했는지 확인해주세요
```bash
./gradlew checkstyleMain checkstyleTest
```

아래 명령어를 통해 쉽게 도커 컨테이너에서 사용할 수 있습니다.

```bash
./gradlew clean build -x test
docker-compose up --build -d # BE
```
