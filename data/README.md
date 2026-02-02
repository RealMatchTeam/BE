# RealMatch 더미 데이터 생성 도구

이 도구는 RealMatch 프로젝트를 위한 더미 데이터를 생성합니다.



## 설치
```bash
./setup.sh
```

## 환경 변수

프로젝트 루트의 `.env` 파일에서 다음 환경 변수를 읽습니다:

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password
MYSQL_DATABASE=realmatch

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password
```

## 주의사항

- 스크립트 실행 전에 데이터베이스가 실행 중이어야 합니다
- 기존 데이터와 중복되지 않도록 주의하세요
- 대량의 데이터 생성 시 시간이 소요될 수 있습니다
- 기본 설정으로 DB가 초기화 되고 데이터를 넣는 구조입니다

## 문제 해결

### 데이터베이스 연결 실패
- `.env` 파일의 데이터베이스 설정을 확인하세요
- 데이터베이스가 실행 중인지 확인하세요

### 외래 키 오류
- 사용자를 먼저 생성한 후 브랜드와 캠페인을 생성하세요

## 예시

### 개발 환경 초기 데이터
```bash
python main.py --users 50 --brands 20 --campaigns 30 --rooms 20 --messages 10 --applies 10
```

### 대량 테스트 데이터
메시지 생성시 10개만 생성되지 않습니다.
```bash
python main.py --users 500 --brands 20000 --campaigns 30000 --rooms 2000 --messages 10 --applies 10
```

### 빠른 테스트
```bash
python main.py --all 10
```


## 옵션

- `--users N`: 생성할 사용자 수 (기본값: 50)
- `--brands N`: 생성할 브랜드 수 (기본값: 20)
- `--campaigns N`: 생성할 캠페인 수 (기본값: 30)
- `--tags N`: 생성할 태그 수 (기본값: 50)
- `--rooms N`: 생성할 채팅방 수 (기본값: 50)
- `--messages N`: 생성할 메시지 수 (기본값: 10)
- `--applies N`: 생성할 지원 수 (기본값: 20)
- `--all N`: 모든 항목에 동일한 개수 적용 
