package com.project.parking.utils;

import com.cloudinary.Cloudinary;
import com.project.parking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageUtils {

    private final CloudinaryService cloudinaryService;
    
    /**
     * Tải hình ảnh lên Cloudinary từ MultipartFile
     */
    public String uploadToCloudinary(MultipartFile file, String folderName) throws IOException {
        try {
            Map uploadResult = cloudinaryService.uploadFile(file, folderName);
            return uploadResult.get("url").toString();
        } catch (Exception e) {
            log.error("Error uploading to Cloudinary: {}", e.getMessage());
            throw new IOException("Error uploading to Cloudinary: " + e.getMessage());
        }
    }
    
    /**
     * Tải hình ảnh lên Cloudinary từ Base64
     */
    public String uploadToCloudinaryFromBase64(String base64Image, String folderName) throws IOException {
        // Xử lý base64 string, loại bỏ phần "data:image/jpeg;base64," nếu có
        String base64Content = base64Image;
        if (base64Image.contains(",")) {
            base64Content = base64Image.split(",")[1];
        }
        
        // Chuyển đổi base64 thành dữ liệu nhị phân
        byte[] imageBytes = Base64.getDecoder().decode(base64Content);
        
        // Tạo file tạm thời để tải lên Cloudinary
        File tempFile = createTempFileFromBytes(imageBytes);
        
        try {
            MultipartFile multipartFile = createMultipartFileFromFile(tempFile, "image.jpg");
            String imageUrl = uploadToCloudinary(multipartFile, folderName);
            
            // Xóa file tạm sau khi tải lên
            tempFile.delete();
            
            return imageUrl;
        } catch (Exception e) {
            // Xóa file tạm nếu có lỗi
            tempFile.delete();
            log.error("Error uploading base64 image to Cloudinary: {}", e.getMessage());
            throw new IOException("Error uploading base64 image to Cloudinary: " + e.getMessage());
        }
    }
    
    /**
     * Tải hình ảnh lên Cloudinary từ BufferedImage
     */
    public String uploadToCloudinaryFromBufferedImage(BufferedImage image, String format, String folderName) throws IOException {
        // Tạo file tạm thời
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), "." + format);
        
        try {
            // Lưu BufferedImage vào file tạm
            ImageIO.write(image, format, tempFile);
            
            // Tải file lên Cloudinary
            MultipartFile multipartFile = createMultipartFileFromFile(tempFile, "image." + format);
            String imageUrl = uploadToCloudinary(multipartFile, folderName);
            
            // Xóa file tạm sau khi tải lên
            tempFile.delete();
            
            return imageUrl;
        } catch (Exception e) {
            // Xóa file tạm nếu có lỗi
            tempFile.delete();
            log.error("Error uploading BufferedImage to Cloudinary: {}", e.getMessage());
            throw new IOException("Error uploading BufferedImage to Cloudinary: " + e.getMessage());
        }
    }
    
    /**
     * Tạo MultipartFile từ File
     */
    private MultipartFile createMultipartFileFromFile(final File file, String filename) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return "image/jpeg";
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(file.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(file.toPath(), dest.toPath());
            }
        };
    }
    
    /**
     * Tạo file tạm từ mảng byte
     */
    private File createTempFileFromBytes(byte[] imageBytes) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imageBytes);
        }
        return tempFile;
    }

    /**
     * Chuyển đổi MultipartFile thành BufferedImage
     */
    public static BufferedImage convertMultipartFileToBufferedImage(MultipartFile file) throws IOException {
        return ImageIO.read(file.getInputStream());
    }

    /**
     * Chuyển đổi image Base64 thành BufferedImage
     */
    public static BufferedImage convertBase64ToBufferedImage(String base64Image) throws IOException {
        // Xử lý base64 string, loại bỏ phần "data:image/jpeg;base64," nếu có
        String base64Content = base64Image;
        if (base64Image.contains(",")) {
            base64Content = base64Image.split(",")[1];
        }
        
        byte[] imageBytes = Base64.getDecoder().decode(base64Content);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * Chuyển đổi BufferedImage thành Base64
     */
    public static String convertBufferedImageToBase64(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Thay đổi kích thước hình ảnh
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(resultingImage, 0, 0, null);
        graphics2D.dispose();
        return resizedImage;
    }

    /**
     * Điều chỉnh độ sáng và độ tương phản
     */
    public static BufferedImage adjustBrightnessContrast(BufferedImage image, float brightness, float contrast) {
        BufferedImage adjustedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        // Điều chỉnh độ sáng và độ tương phản
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                
                int a = (pixel >> 24) & 0xff;
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                
                // Điều chỉnh độ sáng
                r = (int) (r * brightness);
                g = (int) (g * brightness);
                b = (int) (b * brightness);
                
                // Giới hạn giá trị trong khoảng 0-255
                r = Math.min(Math.max(0, r), 255);
                g = Math.min(Math.max(0, g), 255);
                b = Math.min(Math.max(0, b), 255);
                
                // Điều chỉnh độ tương phản
                r = (int) (((r / 255.0 - 0.5) * contrast + 0.5) * 255.0);
                g = (int) (((g / 255.0 - 0.5) * contrast + 0.5) * 255.0);
                b = (int) (((b / 255.0 - 0.5) * contrast + 0.5) * 255.0);
                
                // Giới hạn giá trị trong khoảng 0-255
                r = Math.min(Math.max(0, r), 255);
                g = Math.min(Math.max(0, g), 255);
                b = Math.min(Math.max(0, b), 255);
                
                pixel = (a << 24) | (r << 16) | (g << 8) | b;
                adjustedImage.setRGB(x, y, pixel);
            }
        }
        
        return adjustedImage;
    }

    /**
     * Chuyển đổi sang ảnh grayscale (thang độ xám)
     */
    public static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return grayscale;
    }

    /**
     * Lưu BufferedImage vào file
     */
    public static void saveImageToFile(BufferedImage image, String format, File outputFile) throws IOException {
        ImageIO.write(image, format, outputFile);
    }

    /**
     * Thêm watermark (đóng dấu) vào hình ảnh
     */
    public static BufferedImage addWatermark(BufferedImage image, String text, int x, int y) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(text, x, y);
        g2d.dispose();
        return image;
    }

    /**
     * Cắt một vùng từ hình ảnh
     */
    public static BufferedImage crop(BufferedImage src, int x, int y, int width, int height) {
        return src.getSubimage(x, y, width, height);
    }
}