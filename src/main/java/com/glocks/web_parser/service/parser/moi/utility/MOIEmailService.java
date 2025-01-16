/*
package com.glocks.web_parser.service.parser.moi.utility;

import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.dto.SmsNotificationDto;
import com.glocks.web_parser.service.parser.moi.pendingverification.NotificationModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class MOIEmailService {
    @Autowired
    AppConfig appConfig;
    private final Logger logger = LogManager.getLogger(this.getClass());
    private RestTemplate restTemplate = null;

    public void callEmailNotificationApi(NotificationModel notificationModel) {
        logger.info("Setting for calling the API");

        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(100000);
            clientHttpRequestFactory.setReadTimeout(100000);
            HttpEntity<NotificationModel> request = new HttpEntity<NotificationModel>(notificationModel, headers);
            restTemplate = new RestTemplate(clientHttpRequestFactory);
            String url = appConfig.getNotificationUrl();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
                logger.info("Notification api called successfully for {}", notificationModel.getChannelType());
            }
        } catch (ResourceAccessException resourceAccessException) {
            logger.error("Error while Sending email notification resourceAccessException:{} Request:{}", resourceAccessException.getMessage(), "", resourceAccessException);
        } catch (Exception e) {
            logger.error("Error while Sending email notification Error:{} Request:{}", e.getMessage(), e);
        }

    }
}

*/
