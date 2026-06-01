package com.onAir.submate.global.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES =
            java.util.Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadPath;
    private final String profileUrlPrefix;

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${file.profile-url-prefix}") String profileUrlPrefix) throws IOException {
        this.uploadPath = Paths.get(uploadDir);
        this.profileUrlPrefix = profileUrlPrefix;
        Files.createDirectories(this.uploadPath);
    }

    public String storeProfileImage(MultipartFile file) {
        validate(file);

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;
        Path destination = uploadPath.resolve(filename);

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        return profileUrlPrefix + "/" + filename;
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(profileUrlPrefix)) return;
        String filename = imageUrl.substring(profileUrlPrefix.length() + 1);
        try {
            Files.deleteIfExists(uploadPath.resolve(filename));
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", filename);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("JPG, PNG, WEBP 파일만 업로드 가능합니다.");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
