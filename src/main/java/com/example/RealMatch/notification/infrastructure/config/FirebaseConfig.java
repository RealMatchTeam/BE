package com.example.RealMatch.notification.infrastructure.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;

/**
 * Firebase Admin SDK 초기화 설정
 * FCM 서비스 계정 키 파일이 없으면 FCM 기능은 비활성화된다.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${fcm.credentials-path:}")
    private String credentialsPath;

    @Value("${fcm.project-id:}")
    private String projectId;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void init() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            LOG.warn("[FCM] Firebase credentials path is not configured. FCM push will be disabled.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = new FileInputStream(credentialsPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();
                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                LOG.info("[FCM] Firebase Admin SDK initialized. projectId={}", projectId);
            } catch (IOException e) {
                LOG.error("[FCM] Failed to initialize Firebase Admin SDK. FCM push will be disabled.", e);
            }
        } else {
            firebaseInitialized = true;
            LOG.info("[FCM] Firebase Admin SDK already initialized.");
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (!firebaseInitialized || FirebaseApp.getApps().isEmpty()) {
            LOG.warn("[FCM] FirebaseMessaging bean is null (Firebase not initialized).");
            return null;
        }
        return FirebaseMessaging.getInstance();
    }

    public boolean isFirebaseInitialized() {
        return firebaseInitialized;
    }
}
