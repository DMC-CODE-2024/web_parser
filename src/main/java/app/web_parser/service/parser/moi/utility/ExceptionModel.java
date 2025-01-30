package app.web_parser.service.parser.moi.utility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionModel {
    private String transactionId;
    private String subFeature;
    private String error;
}
