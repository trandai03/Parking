package com.project.parking.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadFile(MultipartFile file, String folderName) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName
                ));
    }

    public Map uploadVideo(MultipartFile file, String folderName) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "folder", folderName
                ));
    }
    public String storeFile(MultipartFile file) throws IOException {
        if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
            throw new IOException("Kích thước file quá lớn (>10MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File không phải là hình ảnh");
        }
        try {
            String folderName = "image";
            Map<String, Object> uploadResult = uploadFile(file, folderName);
            return uploadResult.get("url").toString();
        } catch (Exception e) {
            throw new IOException("Lỗi khi tải lên file: " + e.getMessage(), e);
        }
    }
}
