# RealMatch 더미 데이터 생성 도구

이 도구는 RealMatch 프로젝트를 위한 더미 데이터를 생성합니다.

## 기능

- 사용자 생성: CREATOR 및 BRAND 역할의 사용자 생성
- 브랜드 생성: BEAUTY 및 FASHION 업종의 브랜드 생성
- 캠페인 생성: 실제와 유사한 캠페인 데이터 생성
- 태그 생성: 뷰티, 패션, 콘텐츠 관련 태그 생성

## 설치

1. 가상환경 생성
   ```bash
   python -m venv venv
   ```

2. 가상환경 활성화
   - Linux/Mac:
     ```bash
     source venv/bin/activate
     ```
   - Windows:
     ```bash
     venv\Scripts\activate
     ```

3. 패키지 설치
   ```bash
   pip install -r requirements.txt
   ```

## 사용법

### 기본 사용

기본 개수로 더미 데이터 생성 (사용자 50명, 브랜드 20개, 캠페인 30개):

```bash
python generate_dummy_data.py
```

### 개수 지정

각 항목의 개수를 개별적으로 지정:

```bash
python generate_dummy_data.py --users 100 --brands 30 --campaigns 50
```

### 모든 항목 동일 개수

모든 항목에 동일한 개수 적용:

```bash
python generate_dummy_data.py --all 100
```

## 옵션

- `--users N`: 생성할 사용자 수 (기본값: 50)
- `--brands N`: 생성할 브랜드 수 (기본값: 20)
- `--campaigns N`: 생성할 캠페인 수 (기본값: 30)
- `--tags N`: 생성할 태그 수 (기본값: 50)
- `--all N`: 모든 항목에 동일한 개수 적용

## 환경 변수

프로젝트 루트의 `.env` 파일에서 다음 환경 변수를 읽습니다:

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password
MYSQL_DATABASE=realmatch
```

## 생성되는 데이터

### 사용자 (User)
- 이름, 이메일, 닉네임
- 성별, 생년월일
- 주소, 상세주소
- 역할 (CREATOR, BRAND)
- 프로필 이미지 URL

### 브랜드 (Brand)
- 브랜드명
- 업종 (BEAUTY, FASHION)
- 로고 URL
- 간단 소개, 상세 소개
- 홈페이지 URL
- 매칭률

### 캠페인 (Campaign)
- 제목, 설명
- 선호 스킬, 일정, 영상 스펙
- 협찬 상품, 보상 금액
- 시작/종료 날짜
- 모집 기간
- 모집 인원

### 태그 (Tag)
- 뷰티: 스타일, 기능, 피부 타입
- 패션: 스타일, 카테고리, 브랜드
- 콘텐츠: 유형, 플랫폼

## 주의사항

- 스크립트 실행 전에 데이터베이스가 실행 중이어야 합니다
- 기존 데이터와 중복되지 않도록 주의하세요
- 대량의 데이터 생성 시 시간이 소요될 수 있습니다

## 문제 해결

### 데이터베이스 연결 실패
- `.env` 파일의 데이터베이스 설정을 확인하세요
- 데이터베이스가 실행 중인지 확인하세요

### 외래 키 오류
- 사용자를 먼저 생성한 후 브랜드와 캠페인을 생성하세요

## 예시

### 개발 환경 초기 데이터
```bash
python generate_dummy_data.py --users 30 --brands 10 --campaigns 20
```

### 대량 테스트 데이터
```bash
python generate_dummy_data.py --users 500 --brands 100 --campaigns 200
```

### 빠른 테스트
```bash
python generate_dummy_data.py --all 10
```
