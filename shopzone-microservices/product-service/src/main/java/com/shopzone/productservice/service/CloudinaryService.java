package com.shopzone.productservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shopzone.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;
    @Value("${cloudinary.folder:shopzone/products}") private String folder;
    private static final List<String> ALLOWED = Arrays.asList("image/jpeg","image/jpg","image/png","image/webp","image/gif");

    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is empty");
        if (file.getSize() > 10*1024*1024) throw new BadRequestException("File too large (max 10MB)");
        if (!ALLOWED.contains(file.getContentType())) throw new BadRequestException("Invalid format");
        try {
            Map<String,Object> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folder, "resource_type", "image"));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Upload failed: " + e.getMessage());
        }
    }

    public boolean deleteImage(String url) {
        String publicId = extractPublicId(url);
        if (publicId == null) return false;
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> r = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(r.get("result"));
        } catch (IOException e) { return false; }
    }

    public void deleteImages(List<String> urls) { urls.forEach(this::deleteImage); }

    private String extractPublicId(String url) {
        try {
            int i = url.indexOf("/upload/");
            if (i == -1) return null;
            String path = url.substring(i + 8);
            if (path.startsWith("v") && path.contains("/")) path = path.substring(path.indexOf("/") + 1);
            int dot = path.lastIndexOf(".");
            return dot > 0 ? path.substring(0, dot) : path;
        } catch (Exception e) { return null; }
    }
}
