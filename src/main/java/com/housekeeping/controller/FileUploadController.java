package com.housekeeping.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final AmazonS3 amazonS3;

    @Value("${ncp.bucket.name}")
    private String bucketName;

    @Value("${rembg.server.url}")
    private String rembgServerUrl;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString(); // 랜덤 UUID 생성
        String fileExtension = "";

        // 파일 확장자 추출
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 새 파일명 생성
        String newFileName = fileName + fileExtension;

        try {
            // Call the Rembg server to remove the background
            RestTemplate restTemplate = new RestTemplate();
            byte[] fileBytes = file.getBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return originalFileName;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(rembgServerUrl + "/remove-bg", requestEntity, byte[].class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return new ResponseEntity<>("Failed to remove background", response.getStatusCode());
            }

            byte[] resultBytes = response.getBody();

            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(resultBytes.length);
            metadata.setContentType(file.getContentType());

            // PutObjectRequest 생성 및 PublicRead 권한 설정
            PutObjectRequest request = new PutObjectRequest(bucketName, newFileName, new ByteArrayInputStream(resultBytes), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            // 파일 업로드
            amazonS3.putObject(request);

            // 업로드된 파일의 URL 생성
            String fileUrl = amazonS3.getUrl(bucketName, newFileName).toString();
            return ResponseEntity.ok(fileUrl); // URL 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + originalFileName);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName) {
        try {
            // 파일 이름을 URL 인코딩
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            encodedFileName = encodedFileName.replaceAll("\\+", "%20"); // 공백 처리

            amazonS3.deleteObject(bucketName, encodedFileName);
            return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다: " + fileName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 이름 인코딩 실패: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 삭제 실패: " + fileName);
        }
    }
}
