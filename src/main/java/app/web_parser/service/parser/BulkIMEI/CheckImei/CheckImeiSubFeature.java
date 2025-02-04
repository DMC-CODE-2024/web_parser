package app.web_parser.service.parser.BulkIMEI.CheckImei;

import app.web_parser.alert.AlertService;
import app.web_parser.builder.CheckImeiReqDetailBuilder;
import app.web_parser.config.AppConfig;
import app.web_parser.config.AppDbConfig;
import app.web_parser.config.DbConfigService;
import app.web_parser.constants.FileType;
import app.web_parser.constants.ListType;
import app.web_parser.dto.*;
import app.web_parser.repository.app.*;

import app.web_parser.model.app.BulkCheckImeiMgmt;
import app.web_parser.model.app.CheckImeiReqDetail;
import app.web_parser.model.app.WebActionDb;

import app.web_parser.service.email.EmailService;
import app.web_parser.service.fileCopy.ListFileManagementService;
import app.web_parser.service.fileOperations.FileOperations;
import app.web_parser.service.parser.BulkIMEI.UtilFunctions;
import app.web_parser.service.rule.Rules;
import app.web_parser.service.sms.SmsNotificationService;
import app.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static app.web_parser.config.AppConfig.bulkCheckIMEIFeatureNameStatic;
import static app.web_parser.constants.BulkCheckImeiConstants.*;

@Service
public class CheckImeiSubFeature {

    @Autowired
    WebActionDbRepository webActionDbRepository;
    @Autowired
    FileOperations fileOperations;
    @Autowired
    AppConfig appConfig;
    @Autowired
    SysParamRepository sysParamRepository;
    @Autowired
    BulkCheckImeiMgmtRepository bulkCheckImeiMgmtRepository;
    @Autowired
    UtilFunctions utilFunctions;
    @Autowired
    RuleRepository ruleRepository;
    @Autowired
    Rules rules;
    @Autowired
    AppDbConfig appDbConfig;
    @Autowired
    Validation validation;
    @Autowired
    CheckImeiReqDetailRepository checkImeiReqDetailRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    EirsResponseParamRepository eirsResponseParamRepository;
    @Autowired
    ListFileManagementService listFileManagementService;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    AlertService alertService;
    @Autowired
    SmsNotificationService smsNotificationService;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public void executeInitProcess(WebActionDb webActionDb, BulkCheckImeiMgmt bulkCheckImeiMgmt) {
        logger.info("Starting the init process for bulk check IMEI for transaction id {}", webActionDb.getTxnId());

        webActionDbRepository.updateWebActionStatus(2, webActionDb.getId());
        executeValidateProcess(webActionDb, bulkCheckImeiMgmt);

    }

    public void executeValidateProcess(WebActionDb webActionDb, BulkCheckImeiMgmt bulkCheckImeiMgmt) {
        logger.info("Starting the validate process for bulk check IMEI for transaction id {}", webActionDb.getTxnId());
        EmailDto emailDto = new EmailDto();
        SmsNotificationDto smsNotificationDto = new SmsNotificationDto();
        emailDto.setEmail(bulkCheckImeiMgmt.getEmail());
        emailDto.setTxn_id(bulkCheckImeiMgmt.getTransactionId());
        String language = bulkCheckImeiMgmt.getLanguage() == null ? sysParamRepository.getValueFromTag("systemDefaultLanguage") : bulkCheckImeiMgmt.getLanguage();
        emailDto.setLanguage(language);
        smsNotificationDto.setMsgLang(language);
        smsNotificationDto.setSubFeature(subFeatureName);
        smsNotificationDto.setFeatureName(bulkCheckIMEIFeatureNameStatic);
        smsNotificationDto.setFeatureTxnId(bulkCheckImeiMgmt.getTransactionId());
        smsNotificationDto.setEmail(bulkCheckImeiMgmt.getEmail());
        smsNotificationDto.setChannelType("SMS");
        smsNotificationDto.setMsisdn(bulkCheckImeiMgmt.getContactNumber());
        //dbConfigService.loadAllConfig();
        try {

            String currentFileName = bulkCheckImeiMgmt.getFileName();
            String transactionId = bulkCheckImeiMgmt.getTransactionId();
            String filePath = appConfig.getBulkCheckImeiFilePath() + "/" + transactionId + "/" + currentFileName;
            FileDto currFile = new FileDto(currentFileName, appConfig.getListMgmtFilePath() + "/" + bulkCheckImeiMgmt.getTransactionId());
            emailDto.setFile(filePath);
            logger.info("File path is {}", filePath);
            if (!fileOperations.checkFileExists(filePath)) {
                logger.error("File does not exists {}", filePath);
                alertService.raiseAnAlert(transactionId, "alert1109", "Bulk Check IMEI", currentFileName + " with transaction id " + transactionId, 0);
                utilFunctions.updateFailStatus(webActionDb, bulkCheckImeiMgmt);
                return;
            }
            int bulkImeiCount = Integer.parseInt(sysParamRepository.getValueFromTag("BULK_CHECK_IMEI_COUNT"));
            if (currFile.getTotalRecords() < 1 || currFile.getTotalRecords() > bulkImeiCount) {
                EirsResponseParamDto emailValue = eirsResponseParamRepository.findValue(language, numberOfRecordsMessage);
                emailDto.setSubject(utilFunctions.replaceString(emailValue.getSubject(), bulkImeiCount, transactionId, currentFileName));
                emailDto.setMessage(utilFunctions.replaceString(emailValue.getValue(), bulkImeiCount, transactionId, currentFileName));

                EirsResponseParamDto smsValue = eirsResponseParamRepository.findValue(language, smsNumberOfRecordsMessage);
                smsNotificationDto.setMessage(utilFunctions.replaceString(smsValue.getValue(), bulkImeiCount, transactionId, currentFileName));
                smsNotificationDto.setFeatureName(bulkCheckIMEIFeatureNameStatic);
//                emailDto.setSubject(dbConfigService.getValue("numberOfRecordsSubject"));
//                emailDto.setSubject(dbConfigService.getValue("numberOfRecordsMessage"));
                emailService.callEmailApi(emailDto);
                smsNotificationService.callSmsNotificationApi(smsNotificationDto);
                utilFunctions.updateFailStatus(webActionDb, bulkCheckImeiMgmt, currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
                return;
            }
            if (!fileValidation(filePath)) {
                // send email as well
                EirsResponseParamDto emailValue = eirsResponseParamRepository.findValue(language, invalidDataFormatMessage);
                emailDto.setSubject(utilFunctions.replaceString(emailValue.getSubject(), bulkImeiCount, transactionId, currentFileName));
                emailDto.setMessage(utilFunctions.replaceString(emailValue.getValue(), bulkImeiCount, transactionId, currentFileName));

                EirsResponseParamDto smsValue = eirsResponseParamRepository.findValue(language, smsInvalidDataFormatMessage);
                smsNotificationDto.setMessage(utilFunctions.replaceString(smsValue.getValue(), bulkImeiCount, transactionId, currentFileName));
                smsNotificationDto.setFeatureName(bulkCheckIMEIFeatureNameStatic);
//                emailDto.setSubject(dbConfigService.getValue("invalidDataFormatSubject"));
//                emailDto.setSubject(dbConfigService.getValue("invalidDataFormatMessage"));
                emailService.callEmailApi(emailDto);
                smsNotificationService.callSmsNotificationApi(smsNotificationDto);
                utilFunctions.updateFailStatus(webActionDb, bulkCheckImeiMgmt, currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
                return;
            }
            logger.info("File is ok will process it now");
            webActionDbRepository.updateWebActionStatus(3, webActionDb.getId());
            executeProcess(webActionDb, bulkCheckImeiMgmt);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        }

    }

    public void executeProcess(WebActionDb webActionDb, BulkCheckImeiMgmt bulkCheckImeiMgmt) {

        logger.info("Starting the execute process for bulk imei check");
        SmsNotificationDto smsNotificationDto = new SmsNotificationDto();
        EmailDto emailDto = new EmailDto();
        emailDto.setEmail(bulkCheckImeiMgmt.getEmail());
        emailDto.setTxn_id(bulkCheckImeiMgmt.getTransactionId());
        String language = bulkCheckImeiMgmt.getLanguage() == null ? sysParamRepository.getValueFromTag("systemDefaultLanguage") : bulkCheckImeiMgmt.getLanguage();
        emailDto.setLanguage(language);
        smsNotificationDto.setMsgLang(language);
        smsNotificationDto.setSubFeature("CHECK_IMEI");
        smsNotificationDto.setFeatureName("Bulk IMEI Check");
        smsNotificationDto.setFeatureTxnId(bulkCheckImeiMgmt.getTransactionId());
        smsNotificationDto.setEmail(bulkCheckImeiMgmt.getEmail());
        smsNotificationDto.setChannelType("SMS");
        smsNotificationDto.setMsisdn(bulkCheckImeiMgmt.getContactNumber());
        try {
            String currentFileName = bulkCheckImeiMgmt.getFileName();
            String transactionId = bulkCheckImeiMgmt.getTransactionId();
//            String filePath = appConfig.getBulkCheckImeiFilePath() + "/" + transactionId + "/" + currentFileName;
            FileDto currFile = new FileDto(currentFileName, appConfig.getBulkCheckImeiFilePath() + "/" + bulkCheckImeiMgmt.getTransactionId());
            File outFile = new File(appConfig.getBulkCheckImeiFilePath() + "/" + transactionId + "/" + transactionId + ".csv");
            PrintWriter writer = new PrintWriter(outFile);
            List<RuleDto> ruleList = ruleRepository.getRuleDetails("BULK_CHECK_IMEI", "Enabled");
            logger.info(ruleList.toString());
            writer.println("IMEI,Status");
            boolean output = fileRead(currFile, ruleList, writer, bulkCheckImeiMgmt);
            writer.close();
            listFileManagementService.saveListManagementEntity(transactionId, ListType.OTHERS, FileType.BULK, appConfig.getBulkCheckImeiFilePath() + "/" + bulkCheckImeiMgmt.getTransactionId() + "/", bulkCheckImeiMgmt.getTransactionId() + ".csv", currFile.getTotalRecords());
            if (!output) {
                logger.error("Updating with fail status");
//                emailService.callEmailApi(emailDto);
                utilFunctions.updateFailStatus(webActionDb, bulkCheckImeiMgmt, currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
                return;
            }
            logger.info("The file processed successfully, updating success status.");
            emailDto.setFile(appConfig.getBulkCheckImeiFilePath() + "/" + transactionId + "/" + transactionId + ".csv");
            EirsResponseParamDto emailValue = eirsResponseParamRepository.findValue(language, fileProcessSuccessMessage);
            emailDto.setSubject(utilFunctions.replaceString(emailValue.getSubject(), currFile.getTotalRecords(), transactionId, currentFileName));
            emailDto.setMessage(utilFunctions.replaceString(emailValue.getValue(), currFile.getTotalRecords(), transactionId, currentFileName));
            emailDto.setFeatureName(bulkCheckIMEIFeatureNameStatic);
            EirsResponseParamDto smsValue = eirsResponseParamRepository.findValue(language, smsFileProcessSuccessMessage);
            smsNotificationDto.setMessage(utilFunctions.replaceString(smsValue.getValue(), 0, transactionId, currentFileName));
            smsNotificationDto.setFeatureName(bulkCheckIMEIFeatureNameStatic);
//            emailDto.setSubject(dbConfigService.getValue("fileProcessSuccessSubject"));
//            emailDto.setSubject(dbConfigService.getValue("fileProcessSuccessMessage"));
            emailService.callEmailApi(emailDto);
            smsNotificationService.callSmsNotificationApi(smsNotificationDto);
            utilFunctions.updateSuccessStatus(webActionDb, bulkCheckImeiMgmt, currFile.getTotalRecords(), currFile.getSuccessRecords(), currFile.getFailedRecords());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception while processing the file: {}", bulkCheckImeiMgmt.getFileName());
        }
    }

    public boolean fileRead(FileDto file, List<RuleDto> ruleList, PrintWriter outFile, BulkCheckImeiMgmt bulkCheckImeiMgmt) throws SQLException {
        int successCount = 0;
        int failureCount = 0;
        boolean luhnAlgoCheck = appConfig.isLuhnAlgoCheck();
        boolean gracePeriod = rules.checkGracePeriod();
        try (Connection conn = appDbConfig.springDataSource().getConnection(); BufferedReader reader = new BufferedReader(new FileReader(file.getFilePath() + "/" + file.getFileName()))) {
            String record = null;
            reader.readLine(); // skipping the header
            try {
                logger.info("luhn Algorithm should check {}", luhnAlgoCheck);
                while ((record = reader.readLine()) != null) {
                    if (!record.isBlank()) {
                        /*  if (record.isEmpty()) continue;*/

                        String[] bulkCheckImeiRecord = record.split(",", -1);
                        BulkCheckImeiDto bulkCheckImeiDto = new BulkCheckImeiDto(bulkCheckImeiRecord);
                        if (!isLuhnAlgorithmPass(bulkCheckImeiDto.getImei(), luhnAlgoCheck)) {
                            outFile.println(record + "," + dbConfigService.getValue("error_invalid_imei"));
                        } else {
                            String ruleOutput = rules.applyRule(ruleList, bulkCheckImeiDto.getImei().trim(), gracePeriod, conn, "BU");
                            CheckImeiReqDetail checkImeiReqDetail = CheckImeiReqDetailBuilder.forInsert(bulkCheckImeiDto.getImei(), bulkCheckImeiMgmt);
                            if (ruleOutput.isEmpty() || ruleOutput.isBlank()) {
                                successCount++;
                                outFile.println(record + "," + dbConfigService.getValue("msgForCompliantBulkImei"));
                                checkImeiReqDetail.setComplianceStatus(dbConfigService.getValue("msgForCompliantBulkImei"));
                                checkImeiReqDetailRepository.save(checkImeiReqDetail);
                            } else if (ruleOutput.startsWith("error")) { // gdce error case handling
                                failureCount++;
                                outFile.println(record + "," + dbConfigService.getValue("error_custom_gdce_bic"));
                                checkImeiReqDetail.setComplianceStatus(dbConfigService.getValue("error_custom_gdce_bic"));
                                checkImeiReqDetail.setFailProcessDescription("Rule failed for custom gdce.");
                                checkImeiReqDetailRepository.save(checkImeiReqDetail);
                            } else {
                                failureCount++;
                                outFile.println(record + "," + dbConfigService.getValue("msgForNonCompliantBulkImei"));
                                checkImeiReqDetail.setComplianceStatus(dbConfigService.getValue("msgForNonCompliantBulkImei"));
                                checkImeiReqDetail.setFailProcessDescription("Rule failed for " + ruleOutput);
                                checkImeiReqDetailRepository.save(checkImeiReqDetail);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Exception in processing the record {}", record);
                outFile.println(record);
            }
            reader.close();
        } catch (Exception ex) {
            logger.error("Exception in processing the file {}", file.getFileName());
            return false;
        }
        logger.info("File summary is {}", file);
        file.setFailedRecords(failureCount);
        file.setSuccessRecords(successCount);
        return true;
    }

    boolean fileValidation(String fileName) {
        File file = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String record;
            record = reader.readLine();
            String[] firstHeader = record.split(",", -1);
            if (firstHeader.length != 1 || !firstHeader[0].trim().equalsIgnoreCase("IMEI")) { // validating the header
                logger.error("The record {} is not in correct format", record);
                return false;
            }
            while ((record = reader.readLine()) != null) {
                if (!record.isBlank()) {
                    String[] imeiRecord = record.split(",", -1);
                    String imei = imeiRecord[0].trim();
                    if (imeiRecord.length != 1 || !validation.isLengthEqual(imei, 15) || !validation.isNumeric(imei)) {
                        logger.error("The record {} is not in correct format ", record);
                        return false;
                    }
                }
            }
            reader.close();
            logger.info("The file is validated and is matches the required format");
            return true;
        } catch (Exception ex) {
            logger.error("Exception while reading the file {} {}", fileName, ex.getMessage());
            return false;
        }
    }


    public boolean isLuhnAlgorithmPass(String imei, boolean isAlgoCheck) {
        if (isAlgoCheck) {
            int sum = 0;
            boolean alternate = false;
            for (int i = imei.length() - 1; i >= 0; i--) {
                char c = imei.charAt(i);
                int digit = Character.getNumericValue(c);
                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }
                sum += digit;
                alternate = !alternate;
            }
            logger.info("isLuhnAlgorithmPass {}", (sum % 10 == 0));
            return (sum % 10 == 0);
        }
        return true;
    }
}


