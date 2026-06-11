package com.mbvg.linomove.MBVGServicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class MBVGRecaptchaService {

    @Value("${google.recaptcha.secret-key:}")
    private String secretKey;

    public boolean validar(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        if (secretKey == null || secretKey.trim().isEmpty()) {
            return false;
        }

        try {
            String url = "https://www.google.com/recaptcha/api/siteverify";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> parametros = new LinkedMultiValueMap<>();
            parametros.add("secret", secretKey);
            parametros.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parametros, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() == null) {
                return false;
            }

            Object success = response.getBody().get("success");

            return Boolean.TRUE.equals(success);

        } catch (Exception e) {
            return false;
        }
    }
}