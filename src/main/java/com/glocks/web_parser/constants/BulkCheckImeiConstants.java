package com.glocks.web_parser.constants;

import com.glocks.web_parser.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import static com.glocks.web_parser.config.AppConfig.bulkCheckIMEIFeatureNameStatic;

public class BulkCheckImeiConstants {


    //public  static String featureName ="Bulk Check IMEI"; ;
    public static String subFeatureName = "CHECK_IMEI";

    //public static String numberOfRecordsSubject = "numberOfRecordsSubject";
    public static String numberOfRecordsMessage = "numberOfRecordsMessage";
    public static String smsNumberOfRecordsMessage = "smsNumberOfRecordsMessage";

    //public static String invalidDataFormatSubject = "invalidDataFormatSubject";
    public static String invalidDataFormatMessage = "invalidDataFormatMessage";
    public static String smsInvalidDataFormatMessage = "smsInvalidDataFormatMessage";

    //public static String fileProcessSuccessSubject= "fileProcessSuccessSubject";
    public static String fileProcessSuccessMessage = "fileProcessSuccessMessage";
    public static String smsFileProcessSuccessMessage = "smsFileProcessSuccessMessage";
}