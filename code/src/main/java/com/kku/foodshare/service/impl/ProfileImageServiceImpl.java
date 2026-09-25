package com.kku.foodshare.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.domain.entity.UserProfileImage;
import com.kku.foodshare.dto.response.ProfileImageResponse;
import com.kku.foodshare.repository.UserProfileImageRepository;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.ProfileImageService;

@Service
public class ProfileImageServiceImpl implements ProfileImageService {

    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final int MAX_SIDE = 4096;

    private final UserRepository userRepository;
    private final UserProfileImageRepository imageRepository;

    public ProfileImageServiceImpl(
            UserRepository userRepository,
            UserProfileImageRepository imageRepository
    ) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    @Transactional
    public void updateImage(String email, byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("กรุณาเลือกรูปภาพ");
        }

        if (imageBytes.length > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "รูปภาพต้องมีขนาดไม่เกิน 2 MB"
            );
        }

        String contentType = detectImageType(imageBytes);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("ไม่พบบัญชีผู้ใช้")
                );

        UserProfileImage image = imageRepository
                .findById(user.getId())
                .orElseGet(UserProfileImage::new);

        image.setUser(user);
        image.setImageData(imageBytes);
        image.setContentType(contentType);

        imageRepository.save(image);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileImageResponse> getImage(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("ไม่พบบัญชีผู้ใช้")
                );

        return imageRepository.findById(user.getId())
                .map(image -> new ProfileImageResponse(
                        image.getContentType(),
                        image.getImageData()
                ));
    }

    private String detectImageType(byte[] bytes) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes)
        )) {
            if (stream == null) {
                throw new IllegalArgumentException(
                        "ไม่สามารถอ่านไฟล์รูปภาพได้"
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(stream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "รองรับเฉพาะรูป JPG และ PNG"
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(stream);

                String format = reader.getFormatName();
                String contentType;

                if ("JPEG".equalsIgnoreCase(format)
                        || "JPG".equalsIgnoreCase(format)) {
                    contentType = "image/jpeg";
                } else if ("PNG".equalsIgnoreCase(format)) {
                    contentType = "image/png";
                } else {
                    throw new IllegalArgumentException(
                            "รองรับเฉพาะรูป JPG และ PNG"
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width < 1 || height < 1
                        || width > MAX_SIDE || height > MAX_SIDE) {
                    throw new IllegalArgumentException(
                            "รูปภาพต้องมีขนาดไม่เกิน 4096 × 4096 พิกเซล"
                    );
                }

                return contentType;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "ไฟล์รูปภาพเสียหายหรืออ่านไม่ได้",
                    exception
            );
        }
    }
}