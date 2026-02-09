package com.example.RealMatch.user.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermName {

    AGE("만 14세 이상입니다"),
    SERVICE_TERMS("서비스 이용약관에 동의합니다"),
    PRIVACY_COLLECTION("개인정보를 수집하고 이용하는 것에 동의합니다"),
    PRIVACY_THIRD_PARTY("개인정보를 제3자에게 제공하는 것에 동의합니다"),
    MARKETING_CONSENT("이벤트 혜택과 광고성 정보 수신에 동의합니다"),
    MARKETING_PRIVACY_COLLECTION("마케팅 목적의 개인정보 수집과 이용에 동의합니다"),
    MARKETING_NOTIFICATION("이메일과 앱 푸시 알림 수신에 동의합니다");

    private final String displayName;
}
