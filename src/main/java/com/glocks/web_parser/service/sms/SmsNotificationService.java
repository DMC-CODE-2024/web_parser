package com.glocks.web_parser.service.sms;


import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.dto.SmsNotificationDto;
import com.glocks.web_parser.service.parser.moi.utility.ExceptionModel;
import com.glocks.web_parser.service.parser.moi.utility.MOIService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Class to implement sms notification in web parser
 */
@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    @Autowired
    AppConfig appConfig;
    private final Logger logger = LogManager.getLogger(this.getClass());
    private RestTemplate restTemplate = null;
    private final MOIService moiService;

    public void callSmsNotificationApi(SmsNotificationDto smsNotificationDto) {
        logger.info("Setting for calling the API");

        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(100000);
            clientHttpRequestFactory.setReadTimeout(100000);
            HttpEntity<SmsNotificationDto> request = new HttpEntity<SmsNotificationDto>(smsNotificationDto, headers);
            restTemplate = new RestTemplate(clientHttpRequestFactory);
            String url = appConfig.getNotificationUrl();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
                logger.info("Sms Notification api called successfully");
            }
        } /*catch (ResourceAccessException resourceAccessException) {
            logger.error("Error while Sending sms notification resourceAccessException:{} Request:{}", resourceAccessException.getMessage(), "", resourceAccessException);
        } */ catch (Exception e) {
            logger.error("Error while Sending sms notification Error:{} Request:{}", e.getMessage(), e);
            ExceptionModel exceptionModel = ExceptionModel.builder().error(smsNotificationDto.getMessage()).transactionId(smsNotificationDto.getFeatureTxnId()).subFeature(smsNotificationDto.getSubFeature()).build();
            moiService.exception(exceptionModel);
        }

    }
}
