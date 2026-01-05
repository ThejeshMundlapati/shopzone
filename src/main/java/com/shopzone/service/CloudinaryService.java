package com.shopzone.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shopzone.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

  private final Cloudinary cloudinary;

  @Value("${cloudinary.folder:shopzone/products}")
  private String folder;

  private static final List<String> ALLOWED_FORMATS = Arrays.asList(
      "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
  );

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  /**
   * Upload a single image to Cloudinary
   */
  public String uploadImage(MultipartFile file) {
    validateFile(file);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> uploadResult = cloudinary.uploader().upload(
          file.getBytes(),
          ObjectUtils.asMap(
              "folder", folder,
              "resource_type", "image"
          )
      );

      String secureUrl = (String) uploadResult.get("secure_url");
      log.info("Image uploaded successfully: {}", secureUrl);
      return secureUrl;

    } catch (IOException e) {
      log.error("Failed to upload image to Cloudinary", e);
      throw new BadRequestException("Failed to upload image: " + e.getMessage());
    }
  }

  /**
   * Upload multiple images to Cloudinary
   */
  public List<String> uploadImages(MultipartFile[] files) {
    List<String> urls = new ArrayList<>();

    for (MultipartFile file : files) {
      if (!file.isEmpty()) {
        urls.add(uploadImage(file));
      }
    }

    return urls;
  }

  /**
   * Delete an image from Cloudinary
   */
  public boolean deleteImage(String imageUrl) {
    String publicId = extractPublicId(imageUrl);

    if (publicId == null) {
      log.warn("Could not extract public ID from URL: {}", imageUrl);
      return false;
    }

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> result = cloudinary.uploader().destroy(
          publicId,
          ObjectUtils.emptyMap()
      );

      String deleteResult = (String) result.get("result");
      boolean success = "ok".equals(deleteResult);

      if (success) {
        log.info("Image deleted successfully: {}", publicId);
      } else {
        log.warn("Failed to delete image: {} - Result: {}", publicId, deleteResult);
      }

      return success;

    } catch (IOException e) {
      log.error("Failed to delete image from Cloudinary: {}", publicId, e);
      return false;
    }
  }

  /**
   * Delete multiple images from Cloudinary
   */
  public void deleteImages(List<String> imageUrls) {
    for (String url : imageUrls) {
      deleteImage(url);
    }
  }

  /**
   * Generate a transformed image URL
   */
  public String getTransformedUrl(String imageUrl, int width, int height) {
    String publicId = extractPublicId(imageUrl);

    if (publicId == null) {
      return imageUrl;
    }

    return cloudinary.url()
        .transformation(new com.cloudinary.Transformation()
            .width(width)
            .height(height)
            .crop("fill")
            .quality("auto")
            .fetchFormat("auto"))
        .generate(publicId);
  }

  /**
   * Generate thumbnail URL
   */
  public String getThumbnailUrl(String imageUrl) {
    return getTransformedUrl(imageUrl, 150, 150);
  }

  /**
   * Validate file before upload
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("File is empty");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BadRequestException("File size exceeds maximum limit of 10MB");
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_FORMATS.contains(contentType.toLowerCase())) {
      throw new BadRequestException("Invalid file format. Allowed: JPEG, PNG, WebP, GIF");
    }
  }

  /**
   * Extract public ID from Cloudinary URL
   */
  private String extractPublicId(String imageUrl) {
    try {
      int uploadIndex = imageUrl.indexOf("/upload/");
      if (uploadIndex == -1) {
        return null;
      }

      String path = imageUrl.substring(uploadIndex + 8);

      if (path.startsWith("v") && path.contains("/")) {
        int versionEnd = path.indexOf("/");
        path = path.substring(versionEnd + 1);
      }

      int lastDot = path.lastIndexOf(".");
      if (lastDot > 0) {
        path = path.substring(0, lastDot);
      }

      return path;

    } catch (Exception e) {
      log.error("Failed to extract public ID from URL: {}", imageUrl, e);
      return null;
    }
  }
}