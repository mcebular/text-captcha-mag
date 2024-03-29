package com.textcaptcha.textingest.service.annotator;

import com.textcaptcha.textingest.config.TextIngestConfigProvider;
import com.textcaptcha.textingest.dto.ClasslaApiResponse;
import com.textcaptcha.textingest.exception.AnnotatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
public class ClasslaAnnotatorService implements AnnotatorService<String, ClasslaApiResponse> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TextIngestConfigProvider config;
    private final RestTemplate rest;

    public ClasslaAnnotatorService(TextIngestConfigProvider config) {
        this.config = config;

        rest = new RestTemplate();
        rest.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Override
    public ClasslaApiResponse annotate(String text) throws AnnotatorException {
        String url = config.getClasslaUrl() + "/annotate";

        ResponseEntity<ClasslaApiResponse> restResponse = rest.postForEntity(url, text, ClasslaApiResponse.class);
        ClasslaApiResponse response = restResponse.getBody();

        if (response != null) {
            return response;
        } else {
            throw new RestClientException("No response from annotator service (" + url + ").");
        }
    }

}
