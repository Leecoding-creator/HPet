package com.hpet.vision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Phase 5 - 5-3 보완. OpenAI Vision 토큰/비용 절감을 위한 이미지 축소.
 *
 * 배경(팀 피드백): 원본(3~4MB급) 그대로 보내면 이미지 1장에 토큰이 9천~1만 정도 소모됨.
 * 판정 정확도에 지장 없는 선(512~1024px)까지 줄여서 보내면 토큰/비용/Rate Limit 부담이 크게 줄어든다.
 *
 * - 긴 변 기준 maxDimension을 넘지 않도록 축소한다 (이미 작으면 확대하지 않고 그대로 둔다).
 * - 항상 JPEG로 재인코딩한다 (PNG 등 다른 포맷이 들어와도 통일 - 알파 채널은 흰 배경으로 합성).
 * - 압축 품질(quality)은 설정값으로 조절 가능하게 뒀다.
 */
@Component
public class ImageResizer {

    private static final Logger log = LoggerFactory.getLogger(ImageResizer.class);

    public record Resized(byte[] bytes, String mimeType) {
    }

    /**
     * @param originalBytes 원본 이미지 바이트
     * @param maxDimension  긴 변 최대 길이(px). 예: 1024
     * @param quality       JPEG 압축 품질 0.0~1.0. 예: 0.85
     * @return 축소된 JPEG 바이트. 읽기/축소에 실패하면 원본을 그대로 반환한다(전체 흐름이 깨지지 않도록).
     */
    public Resized resize(byte[] originalBytes, int maxDimension, float quality) {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (original == null) {
                log.warn("이미지를 읽지 못해 원본을 그대로 사용합니다 (크기={}bytes)", originalBytes.length);
                return new Resized(originalBytes, "image/jpeg");
            }

            int width = original.getWidth();
            int height = original.getHeight();
            int longerSide = Math.max(width, height);

            BufferedImage toEncode;
            if (longerSide <= maxDimension) {
                // 이미 충분히 작으면 확대하지 않고, 포맷만 JPEG로 통일해서 재인코딩한다.
                toEncode = toRgb(original);
            } else {
                double scale = (double) maxDimension / longerSide;
                int newWidth = Math.max(1, (int) Math.round(width * scale));
                int newHeight = Math.max(1, (int) Math.round(height * scale));

                BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaled.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, newWidth, newHeight); // 알파 채널 있는 원본 대비 흰 배경 깔기
                g.drawImage(original, 0, 0, newWidth, newHeight, null);
                g.dispose();
                toEncode = scaled;
            }

            byte[] jpegBytes = encodeJpeg(toEncode, quality);
            log.info("이미지 리사이즈 완료: {}x{} -> {}bytes (원본 {}bytes)",
                    toEncode.getWidth(), toEncode.getHeight(), jpegBytes.length, originalBytes.length);
            return new Resized(jpegBytes, "image/jpeg");

        } catch (Exception e) {
            log.warn("이미지 리사이즈 실패, 원본으로 대체합니다: {}", e.getMessage());
            return new Resized(originalBytes, "image/jpeg");
        }
    }

    private BufferedImage toRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, source.getWidth(), source.getHeight());
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("JPEG ImageWriter를 찾을 수 없습니다.");
        }
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(Math.max(0f, Math.min(1f, quality)));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            ios.flush();
            return baos.toByteArray();
        }
    }
}
