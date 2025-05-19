package com.loopy.footballvideoprocessor.video.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.loopy.footballvideoprocessor.common.exception.StorageException;
import com.loopy.footballvideoprocessor.config.CloudflareProperties;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@Slf4j
public class R2StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final CloudflareProperties cloudflareProperties;

    public R2StorageService(CloudflareProperties cloudflareProperties) {
        this.cloudflareProperties = cloudflareProperties;

        CloudflareProperties.R2 r2Props = cloudflareProperties.getR2();
        if (r2Props == null) {
            throw new IllegalStateException("Cloudflare R2 properties (cloudflare.r2) are not configured.");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                r2Props.getAccessKey(),
                r2Props.getSecretKey());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        this.s3Client = S3Client.builder()
                .region(Region.of(r2Props.getRegion()))
                .endpointOverride(URI.create(r2Props.getEndpoint()))
                .credentialsProvider(credentialsProvider)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(r2Props.getRegion()))
                .endpointOverride(URI.create(r2Props.getEndpoint()))
                .credentialsProvider(credentialsProvider)
                .build();

        this.bucketName = r2Props.getBucketName();
        if (this.bucketName == null || this.bucketName.trim().isEmpty()) {
            throw new IllegalStateException("Cloudflare R2 bucket name (cloudflare.r2.bucket-name) is not configured.");
        }
        log.info("R2StorageService initialized with bucket: {}", this.bucketName);
    }

    /**
     * Upload video file to R2 storage
     * 
     * @param file   Video file
     * @param userId User ID
     * @return File key in R2
     */
    public String uploadVideo(MultipartFile file, UUID userId) {
        try {
            String fileName = generateFileName(file.getOriginalFilename(), userId);
            String key = "videos/" + userId + "/" + fileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .cacheControl("max-age=86400, private") // Cache 1 ngày, chỉ client cache
                    .metadata(Map.of(
                            "original-name", file.getOriginalFilename(),
                            "user-id", userId.toString()))
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Uploaded file to R2: {}", key);
            return key;
        } catch (IOException | S3Exception e) {
            log.error("Failed to upload file to R2", e);
            throw new StorageException("Không thể tải file lên bộ lưu trữ", e);
        }
    }

    /**
     * Upload processed video to R2
     * 
     * @param inputStream   Video content
     * @param contentLength Content length
     * @param contentType   Content type
     * @param fileName      File name
     * @param userId        User ID
     * @return File key in R2
     */
    public String uploadProcessedVideo(InputStream inputStream, long contentLength, String contentType,
            String fileName, UUID userId) {
        try {
            String key = "processed/" + userId + "/" + fileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .contentType(contentType)
                    .cacheControl("max-age=86400, private") // Cache 1 ngày, chỉ client cache
                    .metadata(Map.of(
                            "original-name", fileName,
                            "user-id", userId.toString(),
                            "processed", "true"))
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            log.info("Uploaded processed file to R2: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("Failed to upload processed file to R2", e);
            throw new StorageException("Không thể tải file đã xử lý lên bộ lưu trữ", e);
        }
    }

    /**
     * Upload thumbnail to R2
     * 
     * @param inputStream   Thumbnail content
     * @param contentLength Content length
     * @param contentType   Content type
     * @param fileName      File name
     * @param userId        User ID
     * @return File key in R2
     */
    public String uploadThumbnail(InputStream inputStream, long contentLength, String contentType,
            String fileName, UUID userId) {
        try {
            String key = "thumbnails/" + userId + "/" + fileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .contentType(contentType)
                    .metadata(Map.of(
                            "user-id", userId.toString(),
                            "thumbnail", "true"))
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            log.info("Uploaded thumbnail to R2: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("Failed to upload thumbnail to R2", e);
            throw new StorageException("Không thể tải thumbnail lên bộ lưu trữ", e);
        }
    }

    /**
     * Download file from R2
     * 
     * @param key File key in R2
     * @return File bytes
     */
    public ResponseInputStream<GetObjectResponse> downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(request);
        } catch (S3Exception e) {
            log.error("Failed to download file from R2: {}", key, e);
            throw new StorageException("Không thể tải file từ bộ lưu trữ: " + key, e);
        }
    }

    /**
     * Delete file from R2
     * 
     * @param key File key in R2
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Deleted file from R2: {}", key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from R2: {}", key, e);
            throw new StorageException("Không thể xóa file khỏi bộ lưu trữ: " + key, e);
        }
    }

    /**
     * Generate a presigned URL for accessing the file
     * 
     * @param key                 File key in R2
     * @param expirationInMinutes Expiration time in minutes
     * @return Presigned URL
     */
    public String generatePresignedUrl(String key, int expirationInMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            log.info("Generated presigned URL for file: {}, expires in {} minutes", key, expirationInMinutes);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL for file: {}", key, e);
            throw new StorageException("Không thể tạo URL tạm thời cho file: " + key, e);
        }
    }

    /**
     * Generate a unique file name for storage
     * 
     * @param originalFileName Original file name
     * @param userId           User ID
     * @return Generated file name
     */
    private String generateFileName(String originalFileName, UUID userId) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}