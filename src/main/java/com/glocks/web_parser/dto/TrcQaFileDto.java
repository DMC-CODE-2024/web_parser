package com.glocks.web_parser.dto;

import lombok.Data;

@Data
public class TrcQaFileDto {

    String no;
    String companyName;
    String companyId;
    String phoneNumber;
    String email;
    String expiryDate;

    public TrcQaFileDto(String[] headers) {
        this.no = headers[0].trim();
        this.companyName = headers[1].trim();
        this.companyId = headers[2].trim();
        this.phoneNumber = headers[3].trim();
        this.email = headers[4].trim();
        this.expiryDate = headers[5].trim();
    }
}

