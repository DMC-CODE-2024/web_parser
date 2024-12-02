package com.glocks.web_parser.service.parser.moi.utility;

import com.glocks.web_parser.dto.EmailDto;
import com.glocks.web_parser.dto.SmsNotificationDto;
import com.glocks.web_parser.model.app.EirsResponseParam;
import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.EirsResponseParamRepository;
import com.glocks.web_parser.repository.app.SysParamRepository;
import com.glocks.web_parser.service.email.EmailService;
import com.glocks.web_parser.service.parser.BulkIMEI.UtilFunctions;
import com.glocks.web_parser.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Notification {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final SmsService smsService;
    private final EmailService emailService;
    private final UtilFunctions utilFunctions;
    private final EirsResponseParamRepository eirsResponseParamRepository;
    private final SysParamRepository sysParamRepository;
    private final MOIService moiService;

    public void sendNotification(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt, String channel, String uploadedFilePath, String tag) {
        String requestId = stolenDeviceMgmt.getRequestId();
        String subFeature = webActionDb.getSubFeature();
        String feature = webActionDb.getFeature();
        String email = stolenDeviceMgmt.getEmailForOtp();
        EirsResponseParam eirsResponseParam;
        String language = stolenDeviceMgmt.getLanguage() == null ? sysParamRepository.getValueFromTag("systemDefaultLanguage") : stolenDeviceMgmt.getLanguage();

        switch (channel) {
            case "SMS" -> {
                SmsNotificationDto smsNotificationDto = new SmsNotificationDto();
                smsNotificationDto.setMsgLang(language);
                smsNotificationDto.setSubFeature(subFeature);
                smsNotificationDto.setFeatureName(feature);
                smsNotificationDto.setFeatureTxnId(requestId);
                smsNotificationDto.setEmail(email);
                smsNotificationDto.setChannelType("SMS");
                smsNotificationDto.setMsisdn(stolenDeviceMgmt.getContactNumber());
                try {
                    eirsResponseParam = utilFunctions.replaceParameter(eirsResponseParamRepository.getByTagAndLanguage(tag, language), requestId, stolenDeviceMgmt.getContactNumberForOtp(), channel);
                    smsNotificationDto.setMessage(eirsResponseParam.getValue());
                } catch (Exception e) {
                    ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(stolenDeviceMgmt.getRequestId()).subFeature(webActionDb.getSubFeature()).build();
                    moiService.exception(exceptionModel);
                }
                logger.info("SMS notification sent {}", smsNotificationDto);
                smsService.callSmsNotificationApi(smsNotificationDto);
            }
            case "EMAIL" -> {
                EmailDto emailDto = new EmailDto();
                emailDto.setEmail(email);
                emailDto.setTxn_id(requestId);
                emailDto.setLanguage(language);
                emailDto.setFile(uploadedFilePath);
                try {
                    eirsResponseParam = utilFunctions.replaceParameter(eirsResponseParamRepository.getByTagAndLanguage(tag, language), requestId, stolenDeviceMgmt.getContactNumberForOtp(), channel);
                    emailDto.setSubject(eirsResponseParam.getDescription());
                    emailDto.setMessage(eirsResponseParam.getValue());
                } catch (Exception e) {
                    ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(stolenDeviceMgmt.getRequestId()).subFeature(webActionDb.getSubFeature()).build();
                    moiService.exception(exceptionModel);
                }
                logger.info("EMAIL notification sent {}", emailDto);
                emailService.callEmailApi(emailDto);
            }
        }

    }
}
