### version  
- Spring Boot 3.5.9   
- JDK 21   

아래 명령어를 이용하여 convention을 지켜주세요
```bash
git config core.hooksPath .githooks
```

아래 명령어를 통해 쉽게 도커 컨테이너에서 사용할 수 있습니다.

```bash
./gradlew clean build -x test
docker-compose up --build -d# BE
```
