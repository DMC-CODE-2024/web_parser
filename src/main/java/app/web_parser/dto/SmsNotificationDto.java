package app.web_parser.dto;

import lombok.Data;

@Data
public class SmsNotificationDto {

    private Long id;
    private String channelType;
    private String message;
    private String msisdn;
    private String msgLang;
    private String email;
    private String featureTxnId;
    private String subFeature;
    private String featureName;

}
