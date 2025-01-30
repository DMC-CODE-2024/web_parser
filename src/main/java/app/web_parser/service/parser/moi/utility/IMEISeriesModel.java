package app.web_parser.service.parser.moi.utility;

import app.web_parser.validator.Validation;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Data
public class IMEISeriesModel {
    private String imei1;
    private String imei2;
    private String imei3;
    private String imei4;
    private String brand;
    private String model;
    private String contactNumber;
    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Validation validation;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public IMEISeriesModel setImeiSeries(String[] record, String feature) {
        this.imei1=null;
        this.imei2=null;
        this.imei3=null;
        this.imei4=null;
        switch (feature) {
            case "STOLEN" -> {
                for (int i = 0; i < record.length; i++) {
                    switch (i) {
                        case 0 -> {
                            this.contactNumber = recordIndexWise(record[i]);
                        }
                        case 1 -> {
                            this.imei1 = recordIndexWise(record[i]);
                        }
                        case 2 -> {
                            this.imei2 = recordIndexWise(record[i]);
                        }
                        case 3 -> {
                            this.imei3 = recordIndexWise(record[i]);
                        }
                        case 4 -> {
                            this.imei4 = recordIndexWise(record[i]);
                        }
                        case 6 -> {
                            this.brand = recordIndexWise(record[i]);
                        }
                        case 7 -> {
                            this.model = recordIndexWise(record[i]);
                        }
                    }
                }
            }
            case "DEFAULT" -> {

                for (int i = 0; i < record.length; i++) {
                    switch (i) {
                        case 0 -> {
                            this.imei1 = recordIndexWise(record[i]);
                        }
                        case 1 -> {
                            this.imei2 = recordIndexWise(record[i]);
                        }
                        case 2 -> {
                            this.imei3 = recordIndexWise(record[i]);
                        }
                        case 3 -> {
                            this.imei4 = recordIndexWise(record[i]);
                        }
                    }
                }
            }
        }
        logger.info("IMEISeriesModel {}", this);
        return this;
    }

    public String recordIndexWise(String input) {
        return Objects.nonNull(input) ? input.strip() : null;
    }
}
