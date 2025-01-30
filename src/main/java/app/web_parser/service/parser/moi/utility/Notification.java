package app.web_parser.service.parser.moi.utility;

import app.web_parser.config.AppConfig;
import app.web_parser.dto.EmailDto;
import app.web_parser.dto.SmsNotificationDto;
import app.web_parser.model.app.EirsResponseParam;
import app.web_parser.model.app.StolenDeviceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.EirsResponseParamRepository;
import app.web_parser.repository.app.SysParamRepository;
import app.web_parser.service.email.EmailService;
import app.web_parser.service.parser.BulkIMEI.UtilFunctions;
import app.web_parser.service.sms.SmsNotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Notification {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final SmsNotificationService smsNotificationService;
    private final EmailService emailService;
    private final UtilFunctions utilFunctions;
    private final EirsResponseParamRepository eirsResponseParamRepository;
    private final SysParamRepository sysParamRepository;
    private final MOIService moiService;
    private final AppConfig appConfig;

    public void sendNotification(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt, String channel, String uploadedFilePath, String tag) {
        String requestId = stolenDeviceMgmt.getRequestId();
        String subFeature = webActionDb.getSubFeature();
        String feature = appConfig.getStolenFeatureName();
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
                smsNotificationService.callSmsNotificationApi(smsNotificationDto);
            }
            case "EMAIL" -> {
                EmailDto emailDto = new EmailDto();
                emailDto.setEmail(email);
                emailDto.setTxn_id(requestId);
                emailDto.setLanguage(language);
                emailDto.setFile(uploadedFilePath);
                emailDto.setFeatureName(feature);
                try {
                    eirsResponseParam = utilFunctions.replaceParameter(eirsResponseParamRepository.getByTagAndLanguage(tag, language), requestId, stolenDeviceMgmt.getContactNumberForOtp(), channel);
                    emailDto.setSubject(eirsResponseParam.getSubject());
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
