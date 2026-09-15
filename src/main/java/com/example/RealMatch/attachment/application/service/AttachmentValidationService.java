package com.example.RealMatch.attachment.application.service;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.policy.AttachmentUploadPolicy;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentValidationService {

    private final AttachmentUploadPolicy uploadPolicy;

    public String validateUploadRequest(
            String originalFilename,
            String contentType,
            long fileSize,
            AttachmentType attachmentType
    ) {
        if (attachmentType == null) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_TYPE);
        }
        String normalizedContentType = normalizeContentType(contentType);

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }
        if (normalizedContentType == null) {
            throw new CustomException(AttachmentErrorCode.INVALID_CONTENT_TYPE);
        }

        validateFileName(originalFilename);

        long maxSize = attachmentType == AttachmentType.IMAGE
                ? uploadPolicy.getMaxImageSizeBytes()
                : uploadPolicy.getMaxFileSizeBytes();
        validateFileSize(fileSize, maxSize);

        if (attachmentType == AttachmentType.IMAGE) {
            validateFileFormat(
                    normalizedContentType,
                    originalFilename,
                    uploadPolicy.getAllowedImageContentTypes(),
                    uploadPolicy.getAllowedImageExtensions(),
                    AttachmentErrorCode.INVALID_IMAGE_TYPE
            );
        } else if (attachmentType == AttachmentType.FILE) {
            validateFileFormat(
                    normalizedContentType,
                    originalFilename,
                    uploadPolicy.getAllowedFileContentTypes(),
                    uploadPolicy.getAllowedFileExtensions(),
                    AttachmentErrorCode.INVALID_FILE_TYPE
            );
        }
        return normalizedContentType;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private void validateFileName(String filename) {
        if (filename.chars().anyMatch(Character::isISOControl)
                || filename.contains("..")
                || filename.contains("/")
                || filename.contains("\\")
                || filename.length() > 255) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }
    }

    private void validateFileSize(long fileSize, long maxSizeBytes) {
        if (fileSize <= 0) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_SIZE);
        }
        if (fileSize > maxSizeBytes) {
            throw new CustomException(AttachmentErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateFileFormat(
            String contentType,
            String filename,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions,
            AttachmentErrorCode errorCode
    ) {
        Set<String> contentTypes = allowedContentTypes == null ? Set.of() : allowedContentTypes;
        Set<String> extensions = allowedExtensions == null ? Set.of() : allowedExtensions;
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!contentTypes.contains(contentType) || !extensions.contains(extension)) {
            throw new CustomException(errorCode);
        }
    }

    public Path stage(InputStream input, String name, String contentType, long size) throws IOException {
        var path = Files.createTempFile("attachment-", ".upload");
        try {
            try (var output = Files.newOutputStream(path)) {
                byte[] buffer = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > size) {
                        throw new CustomException(AttachmentErrorCode.INVALID_FILE_SIZE);
                    }
                    output.write(buffer, 0, count);
                }
                if (total != size) {
                    throw new CustomException(AttachmentErrorCode.INVALID_FILE_SIZE);
                }
            }
            validateContent(path, name, contentType);
            return path;
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException cleanup) {
                ex.addSuppressed(cleanup);
            }
            throw ex;
        }
    }

    private void validateContent(Path path, String name, String contentType) throws IOException {
        String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String expected = switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "";
        };
        if (!expected.equals(contentType.replace("image/jpg", "image/jpeg"))) {
            throw new CustomException(AttachmentErrorCode.INVALID_CONTENT_TYPE);
        }
        byte[] header;
        try (var stream = Files.newInputStream(path)) {
            header = stream.readNBytes(8);
        }
        boolean valid = switch (extension) {
            case "jpg", "jpeg" -> starts(header, new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff});
            case "png" -> starts(header, new byte[] {(byte) 0x89, 80, 78, 71, 13, 10, 26, 10});
            case "pdf" -> starts(header, "%PDF-".getBytes(US_ASCII));
            case "doc" -> starts(header, new byte[] {(byte) 0xd0, (byte) 0xcf, 17, (byte) 0xe0, (byte) 0xa1, (byte) 0xb1, 26, (byte) 0xe1});
            case "docx" -> validDocx(path);
            default -> false;
        };
        if (!valid) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE);
        }
        if (expected.startsWith("image/")) {
            try (var stream = ImageIO.createImageInputStream(path.toFile())) {
                var readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) {
                    throw new CustomException(AttachmentErrorCode.INVALID_IMAGE_TYPE);
                }
                var reader = readers.next();
                try {
                    reader.setInput(stream);
                    if (reader.getWidth(0) > 16384 || reader.getHeight(0) > 16384
                            || (long) reader.getWidth(0) * reader.getHeight(0) > 25_000_000) {
                        throw new CustomException(AttachmentErrorCode.FILE_SIZE_EXCEEDED);
                    }
                    var parameters = reader.getDefaultReadParam();
                    int sample = Math.max(1, (int) Math.ceil(Math.sqrt((double) reader.getWidth(0) * reader.getHeight(0) / 4_000_000)));
                    parameters.setSourceSubsampling(sample, sample, 0, 0);
                    if (reader.read(0, parameters) == null) {
                        throw new CustomException(AttachmentErrorCode.INVALID_FILE);
                    }
                } finally {
                    reader.dispose();
                }
            }
        }
    }

    private boolean validDocx(Path path) throws IOException {
        try (var zip = new ZipFile(path.toFile())) {
            if (zip.size() > 10000 || zip.getEntry("[Content_Types].xml") == null || zip.getEntry("word/document.xml") == null) {
                return false;
            }
            long total = 0;
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.getSize() < 0 || entry.getName().contains("..") || entry.getName().contains("vbaProject")) {
                    return false;
                }
                total += entry.getSize();
                if (total > 100_000_000) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean starts(byte[] value, byte[] prefix) {
        return value.length >= prefix.length && Arrays.equals(value, 0, prefix.length, prefix, 0, prefix.length);
    }
}
