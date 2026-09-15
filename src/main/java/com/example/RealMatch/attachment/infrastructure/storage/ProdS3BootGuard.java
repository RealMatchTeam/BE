package com.example.RealMatch.attachment.infrastructure.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.application.port.AttachmentStorage;

@Component
@Profile("prod")
public class ProdS3BootGuard implements ApplicationRunner {

    @Autowired(required = false)
    private AttachmentStorage s3FileUploadService;

    @Override
    public void run(ApplicationArguments args) {
        if (s3FileUploadService == null || !s3FileUploadService.isAvailable()) {
            throw new IllegalStateException(
                    "Prod profile requires S3 to be configured (app.s3.access-key-id, app.s3.secret-access-key).");
        }
    }
}
