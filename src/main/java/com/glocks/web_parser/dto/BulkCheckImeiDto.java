package com.glocks.web_parser.dto;


import lombok.Data;
@Data
public class BulkCheckImeiDto {

    String imei;
    public BulkCheckImeiDto(String[] record) {
        this.imei = record[0].trim();
    }
}
