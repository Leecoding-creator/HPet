package com.hpet.common.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.UUID;

/**
 * 인증 사진(영양제 복용 / 자세 교정)을 로컬 디스크에 저장한다.
 *
 * ⚠️ 해커톤 스코프: 로컬 디스크 저장이라 서버를 재배포하면 파일이 사라진다.
 * 실서비스로 갈 때는 이 클래스만 S3 업로드 로직으로 바꾸면 되고, 나머지 코드는 그대로 재사용 가능
 * (AiVisionClient/EmailSender와 동일한 "인터페이스로 갈아끼우기" 패턴을 위해 클래스를 여기 한 곳으로 모아둠).
 */
@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    private final Path storageRoot;
    private final String publicBaseUrl;

    public ImageStorageService(
            @Value("${storage.local.root:./uploads}") String storageRootPath,
            @Value("${storage.public-base-url:/files}") String publicBaseUrl) {
        this.storageRoot = Paths.get(storageRootPath).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl;
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장 폴더를 만들지 못했습니다: " + this.storageRoot, e);
        }
    }

    /**
     * @param category  "dose" 또는 "posture" 처럼 하위 폴더로 쓸 카테고리
     * @param imageBytes 저장할 이미지 바이트
     * @return          프론트에서 <img src="...">로 바로 쓸 수 있는 URL 경로 (예: /files/dose/xxxx.jpg)
     */
    public String save(String category, byte[] imageBytes) {
        try {
            Path categoryDir = storageRoot.resolve(category);
            Files.createDirectories(categoryDir);

            String filename = UUID.randomUUID() + ".jpg";
            Path targetPath = categoryDir.resolve(filename);
            Files.write(targetPath, imageBytes);

            String url = publicBaseUrl + "/" + category + "/" + filename;
            log.info("이미지 저장 완료: {} ({}bytes)", url, imageBytes.length);
            return url;
        } catch (IOException e) {
            log.error("이미지 저장 실패", e);
            throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
        }
    }

    public Path resolveStorageRoot() {
        return storageRoot;
    }
}
