package app.web_parser.dto;


import lombok.Data;
import org.springframework.stereotype.Service;

@Data
public class TrcTaFileDto {

    String no;
    String company;
    String trademark;
    String productName;
    String countryOfManufacture;
    String companyId;
    String commercialName;
    String trcIdentifier;
    String model;
    String approvedDate;


    public TrcTaFileDto(String[] taDataRecord) {
        this.no = taDataRecord[0].trim();
        this.company = taDataRecord[1].trim();
        this.companyId = taDataRecord[2].trim();
        this.trademark = taDataRecord[3].trim();
        this.productName = taDataRecord[4].trim();
        this.commercialName = taDataRecord[5].trim();
        this.model = taDataRecord[6].trim();
        this.countryOfManufacture = taDataRecord[7].trim();
        this.trcIdentifier = taDataRecord[8].trim();
        this.approvedDate = taDataRecord[9].trim();
    }
}
