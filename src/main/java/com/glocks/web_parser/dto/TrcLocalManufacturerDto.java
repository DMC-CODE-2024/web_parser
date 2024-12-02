package com.glocks.web_parser.dto;

import lombok.Data;

@Data
public class TrcLocalManufacturerDto {


    String imei;
    String serialNumber;
    String manufacturerId;
    String manufacturerName;
    String manufactureringDate;

    public TrcLocalManufacturerDto(String[] headers) {
        this.imei = headers[0].trim();
        this.serialNumber = headers[1].trim();
        this.manufacturerId = headers[2].trim();
        this.manufacturerName = headers[3].trim();
        this.manufactureringDate = headers[4].trim();
    }
}
