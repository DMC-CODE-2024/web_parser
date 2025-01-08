package com.glocks.web_parser.service.email;

import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.dto.EmailDto;
import com.glocks.web_parser.service.parser.moi.utility.ExceptionModel;
import com.glocks.web_parser.service.parser.moi.utility.MOIService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.joining;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    AppConfig appConfig;
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIService moiService;
    private RestTemplate restTemplate = null;

    public void callEmailApi(EmailDto emailDto) {
        logger.info("Setting for calling the API");

        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            List<Charset> acceptCharset = new ArrayList<>();
            List<Locale.LanguageRange> languageRanges = new ArrayList<>();
            acceptCharset.add(StandardCharsets.UTF_8);
//            languageRanges.add()
//            headers.setAcceptLanguage(languageRanges);
            headers.setAcceptCharset(acceptCharset);
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(100000);
            clientHttpRequestFactory.setReadTimeout(100000);
            restTemplate = new RestTemplate();
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            List<MediaType> list = new ArrayList<>();
            list.add(MediaType.ALL);
            converter.setSupportedMediaTypes(list);
            restTemplate.getMessageConverters().add(converter);
            String url = appConfig.getEmailUrl() + "?";

//            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
//                    // Add query parameter
//                    .queryParam("email", emailDto.getEmail())
//                    .queryParam("message", emailDto.getMessage())
//                    .queryParam("msgLang",emailDto.getLanguage())
//                    .queryParam("subject",emailDto.getSubject())
//                    .queryParam("txnId",emailDto.getTxn_id());
//            logger.info(String.valueOf(builder.buildAndExpand().toUri()));


            Map<String, String> requestParams = new HashMap<>();
            requestParams.put("message", emailDto.getMessage());
            requestParams.put("subject", emailDto.getSubject());
            requestParams.put("email", emailDto.getEmail());
            requestParams.put("msgLang", emailDto.getLanguage());
            requestParams.put("txnId", emailDto.getTxn_id());
            requestParams.put("featureName", emailDto.getFeatureName());

            String encodedURL = requestParams.keySet().stream()
                    .map(key -> {
                        return key + "=" + requestParams.get(key);
                    })
                    .collect(joining("&", url, ""));


            logger.info("Url is {}", encodedURL);

            MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
            Resource file1 = new FileSystemResource(new File(emailDto.getFile()));
            multipartBodyBuilder.part("file", file1, MediaType.MULTIPART_FORM_DATA);
            logger.info(multipartBodyBuilder);
            MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();
            HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    encodedURL, HttpMethod.POST, httpEntity, String.class);
//            System.out.println("responseEntity");
            if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
                logger.info("Email api called successfully");
            }
        } /*catch (ResourceAccessException resourceAccessException) {
            logger.error("Error while Sending Alert resourceAccessException:{} Request:{}", resourceAccessException.getMessage(), "", resourceAccessException);
        } */ catch (Exception e) {
            logger.error("Error while Sending Alert Error:{} Request:{}", e.getMessage(), e);
            ExceptionModel exceptionModel = ExceptionModel.builder().error(emailDto.getMessage()).transactionId(emailDto.getFeatureTxnId()).subFeature(emailDto.getSubFeature()).build();
            moiService.exception(exceptionModel);
        }


    }


    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
