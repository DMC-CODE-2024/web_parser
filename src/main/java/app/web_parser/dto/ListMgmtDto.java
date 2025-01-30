package app.web_parser.dto;

import lombok.Data;

@Data
public class ListMgmtDto {

    String msisdn;
    String imei;
    String imsi;

    public ListMgmtDto(String[] record) {
        this.msisdn = record[0].trim();
        this.imsi = record[1].trim();
        this.imei = record[2].trim();
    }

}
