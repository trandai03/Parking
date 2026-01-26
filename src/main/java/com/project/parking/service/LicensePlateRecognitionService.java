package com.project.parking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
@Service
@Slf4j
public class LicensePlateRecognitionService {
    @Value("${platerecognizer.api.url}")
    private String apiUrl;

    @Value("${platerecognizer.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LicensePlateRecognitionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, String> recognizeLicensePlate(MultipartFile image) throws IOException {
        // Chuẩn bị request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Token " + apiKey);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("upload", image.getResource());
        body.add("regions", "vn"); // Đặt vùng là Việt Nam
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        Map<String, String> res = new HashMap<>();
        // Gọi API
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.CREATED) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode results = jsonNode.get("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode result = results.get(0);
                res.put("plate", result.get("plate").asText().toUpperCase()); // Lấy biển số: "12B16888"
                log.info("Biển số: " + res.get("plate"));
            }
        }
        return res;
    }
}
