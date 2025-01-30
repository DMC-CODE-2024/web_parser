package app.web_parser.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {

    private String alertId;

    private String alertMessage;

    private String alertProcess;

    private String userId;
    private String serverName;
    private String featureName;
    private String txnId;
}
