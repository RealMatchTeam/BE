# 첨부 파일 소유권 검증 필요성 설명

## 🔍 현재 검증 로직

```java
// 첨부 파일 존재 및 소유권 검증
if (command.attachmentId() != null) {
    ChatAttachment attachment = chatAttachmentRepository.findById(command.attachmentId())
            .orElseThrow(() -> new ChatException(ChatErrorCode.ATTACHMENT_NOT_FOUND));
    if (!attachment.getUploaderId().equals(senderId)) {
        throw new ChatException(ChatErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
    }
}
```

---

## ❓ 왜 이 검증이 필요한가?

### 1. 보안 취약점 방지

**공격 시나리오**:
```
1. 사용자 A가 파일을 업로드 → attachmentId=9001 반환
2. 사용자 B가 네트워크 스니핑/브라우저 개발자 도구/API 응답 로깅 등을 통해 
   attachmentId=9001을 알게 됨
3. 사용자 B가 WebSocket으로 메시지 전송:
   {
     "roomId": 3001,
     "messageType": "IMAGE",
     "attachmentId": 9001,  // ← 사용자 A가 업로드한 파일 ID
     "clientMessageId": "..."
   }
4. 검증이 없다면 → 사용자 B가 사용자 A의 파일을 사용해서 메시지를 보낼 수 있음
```

**문제점**:
- 다른 사용자가 업로드한 파일을 무단으로 사용 가능
- 메시지에 "사용자 B가 보냄"이라고 표시되지만 실제 파일은 사용자 A의 것
- 데이터 정합성 깨짐

---

### 2. 데이터 정합성 보장

**데이터 불일치 시나리오**:
```
메시지 테이블:
- messageId: 7001
- senderId: 202 (사용자 B)
- attachmentId: 9001

첨부 파일 테이블:
- attachmentId: 9001
- uploaderId: 101 (사용자 A)  ← 불일치!

결과:
- 메시지에는 "사용자 B가 보냄"으로 표시
- 하지만 실제 파일은 사용자 A가 업로드한 것
- UI에서 혼란 발생 가능
```

---

### 3. PRD 명시 사항

`PRD/validation.md`에 명시되어 있음:
```
### 2.3 Service 검증 — "이 요청이 지금 가능한가?"

**적합한 검증**
- 소유권: attachment가 sender 소유인지
```

---

## 🤔 그런데 실제로 일어날 수 있나?

### 가능한 시나리오들:

1. **네트워크 스니핑**
   - HTTP 요청/응답을 가로채서 다른 사용자의 `attachmentId` 확인 가능

2. **브라우저 개발자 도구**
   - 네트워크 탭에서 API 응답 확인
   - 다른 사용자의 업로드 응답에서 `attachmentId` 확인 가능

3. **API 응답 로깅**
   - 클라이언트 앱의 로그에서 다른 사용자의 `attachmentId` 노출 가능

4. **버그로 인한 노출**
   - API 버그로 다른 사용자의 첨부 파일 목록이 노출될 수 있음
   - 또는 메시지 조회 API에서 다른 사용자의 첨부 파일 정보가 노출될 수 있음

5. **예측 가능한 ID**
   - `attachmentId`가 순차적으로 증가한다면, 다른 사용자의 ID를 추측 가능

---

## 💡 검증이 없다면?

### 문제점:

1. **보안 취약점**
   - 다른 사용자의 파일을 무단으로 사용 가능
   - 민감한 파일(계약서, 개인정보 등)을 다른 사용자가 사용할 수 있음

2. **데이터 정합성 깨짐**
   - 메시지의 `senderId`와 첨부 파일의 `uploaderId`가 불일치
   - 감사(audit) 추적 불가능

3. **법적 문제**
   - 파일 소유권이 불명확해져 법적 분쟁 가능
   - "누가 이 파일을 업로드했는가?" 추적 불가

---

## ✅ 검증이 있다면?

### 보호되는 것:

1. **보안 강화**
   - 다른 사용자의 파일을 사용하는 것을 방지
   - 파일 소유권 명확화

2. **데이터 정합성**
   - 메시지의 `senderId`와 첨부 파일의 `uploaderId`가 항상 일치
   - 감사 추적 가능

3. **명확한 에러 메시지**
   - `ATTACHMENT_OWNERSHIP_MISMATCH` 에러로 문제를 명확히 알림
   - 디버깅 용이

---

## 🎯 결론

**첨부 파일 소유권 검증은 필수입니다.**

이유:
1. ✅ PRD에 명시되어 있음 (`PRD/validation.md`)
2. ✅ 보안 취약점 방지 (다른 사용자의 파일 무단 사용 방지)
3. ✅ 데이터 정합성 보장 (senderId와 uploaderId 일치)
4. ✅ 실제로 발생 가능한 시나리오 (네트워크 스니핑, 버그 등)

**현재 구현은 올바릅니다.** ✅

---

## 📝 추가 고려사항

### 만약 검증을 제거한다면?

1. **대안 1: 업로드 시점에 roomId 연결**
   ```java
   // 첨부 파일 업로드 시 roomId도 함께 저장
   ChatAttachment {
       Long uploaderId;
       Long roomId;  // 추가
   }
   
   // 메시지 전송 시 roomId 일치 확인
   if (!attachment.getRoomId().equals(command.roomId())) {
       throw new ChatException(...);
   }
   ```
   - 하지만 이건 소유권 검증과는 다른 목적 (방별 접근 제어)

2. **대안 2: 첨부 파일을 메시지 전송 시점에만 생성**
   - 업로드와 메시지 전송을 분리하지 않고 동시에 처리
   - 하지만 현재 설계는 "업로드 → 메시지 전송" 분리 구조

**결론**: 현재의 소유권 검증이 가장 명확하고 안전합니다.
