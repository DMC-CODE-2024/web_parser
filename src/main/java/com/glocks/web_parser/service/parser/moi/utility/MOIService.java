package com.glocks.web_parser.service.parser.moi.utility;

import com.glocks.web_parser.alert.AlertService;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.validator.Validation;
import jakarta.persistence.NonUniqueResultException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class MOIService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final StolenDeviceDetailRepository stolenDeviceDetailRepository;
    private final SearchImeiByPoliceMgmtRepository searchImeiByPoliceMgmtRepository;
    private final StolenDeviceMgmtRepository stolenDeviceMgmtRepository;
    private final MDRRepository mdrRepository;
    private final BlackListRepository blackListRepository;
    private final BlackListHisRepository blackListHisRepository;
    private final GreyListRepository greyListRepository;
    private final GreyListHisRepository greyListHisRepository;
    private final ImeiPairDetailRepository imeiPairDetailRepository;
    private final ImeiPairDetailHisRepository imeiPairDetailHisRepository;
    private final SysParamRepository sysParamRepository;
    private final AppConfig appConfig;
    private final WebActionDbRepository webActionDbRepository;
    private final AlertService alertService;
    private final DbConfigService dbConfigService;
    public Optional<SearchImeiByPoliceMgmt> findByTxnId(String txnId) {
        Optional<SearchImeiByPoliceMgmt> response = searchImeiByPoliceMgmtRepository.findByTransactionId(txnId);
        logger.info("SearchImeiByPoliceMgmt response : {} based on txn ID :{}", txnId, response);
        return response;
     /*   try {
            Optional<SearchImeiByPoliceMgmt> response = searchImeiByPoliceMgmtRepository.findByTransactionId(txnId);
            logger.info("SearchImeiByPoliceMgmt response : {} based on txn ID :{}", txnId, response);
            return response;
        } catch (NonUniqueResultException e) {
            logger.info("NonUniqueResultException {}", e.getMessage());
        }
        return null;*/
    }

    public boolean isBrandAndModelValid(List<String> list) {
        Map<String, String> map = new HashMap<>();
        for (String tac : list) {
            mdrRepository.findByDeviceId(tac).ifPresentOrElse(x -> {
                map.put(tac, x.stream().map(y -> y.getBrandName() + "---" + y.getModelName()).collect(Collectors.joining()));
            }, () -> {
                map.put(tac, "NA");
            });
        }
        logger.info(" Brand and model name {}", map);
        if (map.isEmpty()) return false;
        Set<String> uniqueKeys = map.keySet().stream().collect(Collectors.toSet());
        Set<String> uniqueValues = map.values().stream().collect(Collectors.toSet());
        return uniqueKeys.size() == 1 && uniqueValues.size() == 1;
    }

    public Optional<StolenDeviceDetail> findByImeiAndStatusIgnoreCaseAndRequestTypeIgnoreCaseIn(String imei) {
        Optional<StolenDeviceDetail> result = stolenDeviceDetailRepository.findByImeiAndStatusIgnoreCaseAndRequestTypeIgnoreCaseIn(imei, "Done", List.of("LOST", "STOLEN"));
        logger.info("findByImeiAndStatusIgnoreCaseAndRequestTypeIgnoreCaseIn response {}", result);
        return result;
    }


    public void updateStatusAndCountFoundInLost(String status, int count, String transactionId, String failReason) {
        searchImeiByPoliceMgmtRepository.updateStatus(status, failReason, count, transactionId);
        logger.info("Record updated in  SearchImeiByPoliceMgmt with status {} and txnId {} and count {} and failReason {}", status, transactionId, count, failReason);
    }

    public void updateCountFoundInLostAndRecordCount(String status, int count, String transactionId, String failReason, Integer recordCount) {
        searchImeiByPoliceMgmtRepository.updateStatusAndRecordCount(status, failReason, count, recordCount, transactionId);
        logger.info("Record updated in SearchImeiByPoliceMgmt with status {}, txnId {},recordCount {},failReason {} and count {} ", status, transactionId, recordCount, failReason, count);
    }


    public boolean isMultipleIMEIExist(IMEISeriesModel imeiSeriesModel) {
        long count = Stream.of(imeiSeriesModel).flatMap(x -> Stream.of(x.getImei2(), x.getImei3(), x.getImei4())).filter(Objects::isNull).count();
        logger.info("No. of IMEI's found empty {}", count);
        return count == 3 ? false : true;
    }

    public List<String> tacList(IMEISeriesModel imeiSeriesModel) {
        String[] arr = {imeiSeriesModel.getImei1(), imeiSeriesModel.getImei2(), imeiSeriesModel.getImei3(), imeiSeriesModel.getImei4()};
        List<String> tacList = Stream.of(arr).filter(Objects::nonNull).filter(x -> x.length() > 8).map(imei -> imei.substring(0, 8)).collect(Collectors.toList());
        logger.info("TAC list {}", tacList);
        return tacList;
    }

    public String greyListDuration() {
        String greyListDuration = sysParamRepository.getValueFromTag("GREY_LIST_DURATION");
        return greyListDuration;
    }

    public <T> T save(T v1, Function<T, T> saveFunction) {
        logger.info("Save operation for :  {}", v1);
        return saveFunction.apply(v1);
    }

    public void updateSource(String source, String imei, String repo) {
        logger.info("Updated {} with source {} for imei {}", repo, source, imei);
        int rowAffected = 0;
        switch (repo) {
            case "BLACK_LIST" -> rowAffected = blackListRepository.updateSource(source, imei);
            case "GREY_LIST" -> rowAffected = greyListRepository.updateSource(source, imei);
        }
        if (rowAffected == 1) {
            logger.info("Removed MOI and updated source value for {}", repo);
        } else {
            logger.info("Failed to update source value for {}", repo);
        }
    }


    public LocalDateTime expiryDate(int daysToAdd) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime expiryDate = currentDateTime.plusDays(daysToAdd);
        logger.info("ExpiryDate {}", expiryDate);
        return expiryDate;
    }

    public DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    public boolean isDateFormatValid(String createdOn) {
        try {
            LocalDateTime.parse(createdOn, dateFormatter());
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder()
                            .error(this.extractTableNameFromMessage(sqlException.getMessage()))
                            .transactionId(null)
                            .subFeature(null)
                            .build();
                    logger.info("{} table not found", this.extractTableNameFromMessage(sqlException.getMessage()));
                    this.exception(exceptionModel);
                }
            }
        } catch (DateTimeParseException e) {
            logger.info("Invalid format received for deviceLostDateTime {} ,Expected format: yyyy-MM-dd HH:mm:ss", createdOn);
            return false;
        }
        return true;
    }

    public void imeiPairDetail(String createdOn, String mode, String imei) {
        String imeiValue = getIMEI(imei);
        imeiPairDetailRepository.findByCreatedOnGreaterThanEqual(createdOn, imeiValue).ifPresentOrElse(imeiPairDetailList -> {
            logger.info("ImeiPairDetail result for created_on {} {}", createdOn, imeiPairDetailList);
            imeiPairDetailList.forEach(list -> {
                ImeiPairDetailHis imeiPairDetailHis = new ImeiPairDetailHis();
                BeanUtils.copyProperties(list, imeiPairDetailHis);
                imeiPairDetailHis.setAction("0");
                imeiPairDetailHis.setActionRemark("MOI");
                ImeiPairDetailHis savedEntity = save(imeiPairDetailHis, imeiPairDetailHisRepository::save);
                if (savedEntity != null) {
                    imeiPairDetailRepository.deleteById(Math.toIntExact(list.getId()));
                    logger.info("Delete operation for IMEI {} from imei_pair_detail ", list.getImei());
                }
            });
        }, () -> logger.info("No result found in ImeiPairDetail for created_on {}", createdOn));
    }

    public String getTacFromIMEI(String imei) {
        return Stream.of(imei).filter(Objects::nonNull).map(x -> imei.substring(0, 8)).collect(Collectors.joining());
    }

    public String getIMEI(String imei) {
        return Stream.of(imei).filter(Objects::nonNull).map(x -> imei.substring(0, 14)).collect(Collectors.joining());
    }


    public void greyListDurationIsZero(String imei, String mode, StolenDeviceMgmt lostDeviceMgmt) {
        String imeiValue = getIMEI(imei);
        findBlackListByImei(imeiValue).ifPresentOrElse(blackList -> {
            String source = remove(blackList.getSource());
            if (Objects.isNull(source)) updateSource("MOI", imeiValue, "BLACK_LIST");
            else {
                if (!source.equalsIgnoreCase("MOI")) updateSource(source + ",MOI", imeiValue, "BLACK_LIST");
            }
        }, () -> {
            BlackList blackList = new BlackList();
            blackList.setImei(imeiValue);
            blackList.setModeType(mode);
            blackList.setSource("MOI");
            blackList.setRequestType("Stolen");
            blackList.setRemarks(lostDeviceMgmt.getRemark());
            blackList.setTxnId(lostDeviceMgmt.getRequestId());
            blackList.setTac(getTacFromIMEI(imei));
            blackList.setActualImei(imei);
            save(blackList, blackListRepository::save);
            BlackListHis blackListHis = new BlackListHis();
            BeanUtils.copyProperties(blackList, blackListHis);
            blackListHis.setOperation(1);
            save(blackListHis, blackListHisRepository::save);
        });
    }

    public void greyListDurationGreaterThanZero(int greyListDuration, String imei, String mode, StolenDeviceMgmt stolenDeviceMgmt) {
        GreyList greyList = GreyList.builder().imei(getIMEI(imei)).msisdn(stolenDeviceMgmt.getContactNumber()).modeType(mode).source("MOI").expiryDate(expiryDate(greyListDuration)).requestType("Stolen").remarks(stolenDeviceMgmt.getRemark()).txnId(stolenDeviceMgmt.getRequestId()).tac(getTacFromIMEI(imei)).actualImei(imei).build();
        save(greyList, greyListRepository::save);
        GreyListHis greyListHis = new GreyListHis();
        BeanUtils.copyProperties(greyList, greyListHis);
        greyListHis.setOperation(1);
        save(greyListHis, greyListHisRepository::save);
    }

    public Optional<StolenDeviceMgmt> findByRequestId(String id) {
        Optional<StolenDeviceMgmt> response = stolenDeviceMgmtRepository.findByRequestId(id);
        logger.info("Based on {} record is {}", id, response);
        return response;
    }

    public Optional<BlackList> findBlackListByImei(String imei) {
        Optional<BlackList> result = Optional.ofNullable(blackListRepository.findBlackListByImei(imei));
        if (result.isEmpty()) {
            logger.info("No record found for IMEI {} in black_list", imei);
        }
        return result;
    }

    public Optional<GreyList> findGreyListByImei(String imei) {
        Optional<GreyList> result = Optional.ofNullable(greyListRepository.findGreyListByImei(imei));
        if (result.isEmpty()) {
            logger.info("No record found for IMEI {} in grey_list", imei);
        }
        return result;
    }

    public void updateStatusInLostDeviceMgmt(String status, String requestId) {
        logger.info("Record marked as {} in stolen_device_mgmt for {}", status, requestId);
        stolenDeviceMgmtRepository.updateStatus(status, requestId);

    }

    public Function<IMEISeriesModel, List<String>> imeiSeries = (imeiSeries) -> {
        List<String> collect = Stream.of(imeiSeries).flatMap(x -> Stream.of(x.getImei1(), x.getImei2(), x.getImei3(), x.getImei4())).filter(imei -> imei != null && !imei.isEmpty()).collect(Collectors.toList());
        logger.info("Non null IMEI's {}", collect);
        return collect;
    };

    public Predicate<String> isIMEILengthAllowed = record -> {
        int length = record.length();
        logger.info("IMEI {} length is {}", record, length);
        return length == 14 || length == 15 || length == 16;
    };

    public Predicate<String> isNumericAndValid = record -> {
        if (new Validation().isNumeric(record)) {
            return isIMEILengthAllowed.test(record);
        }
        logger.info("Invalid IMEI {}", record);
        return false;
    };

    public PrintWriter file(String currFilePath) {
        try {
            File outFile = new File(currFilePath);
            PrintWriter writer = new PrintWriter(outFile);
            return writer;
        } catch (Exception e) {
            logger.info("Exception related to printWriter {}", e.getMessage());
        }
        return null;
    }

    public String joiner(String[] split, String status) {
        return Arrays.stream(split).collect(Collectors.joining(",")) + status;
    }

    public void invalidFile(String header, PrintWriter printWriter) {
        printWriter.println(header + "," + dbConfigService.getValue("invalid_format") + "");
        printWriter.close();
    }

    public void webActionDbOperation(int state, Long id) {
        webActionDbRepository.updateWebActionStatus(state, id);
        logger.info("Updated state as {} in web_action_db ", state);
    }

    public Function<String, Long> sourceCount = (source) -> {
        if (Objects.nonNull(source)) {
            String[] split = source.split(",");
            boolean isMoiExistInSource = Arrays.stream(split).anyMatch(x -> x.equals("MOI"));
            logger.info("is MOI exist in source {}", isMoiExistInSource);
            long count = Arrays.stream(split).count();
            if (isMoiExistInSource && count == 1) {
                if (count == 1) {
                    logger.info("Source value matched with MOI", source);
                    return 1L;
                }
            } else {
                logger.info("Source count {} and source value found {}", count, Arrays.toString(split));
                return 2L;
            }
        }
        logger.info("No source value {} matched with MOI", source);
        return 0L;
    };

    public String remove(String source) {
        if (Objects.nonNull(source)) {
            return Arrays.stream(source.split(",")).filter(element -> !element.equals("MOI")).collect(Collectors.joining(","));
        } else logger.info("No {} found", source);
        return null;
    }


    public boolean areHeadersValid(String filePath, String feature, int length) {
        boolean isHeadersNameValid = false;
        Map<String, String> map = new HashMap<>();
        File file = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headers = reader.readLine();
            String[] header = headers.split(appConfig.getListMgmtFileSeparator(), -1);
            if (header.length != length) {
                logger.info("Invalid header length");
                return false;
            }

            switch (feature) {
                case "STOLEN" -> {
                    map.put("0", "Phone Number");
                    map.put("1", "IMEI1");
                    map.put("2", "IMEI2");
                    map.put("3", "IMEI3");
                    map.put("4", "IMEI4");
                    map.put("5", "Device Type");
                    map.put("6", "Device Brand");
                    map.put("7", "Device Model");
                    map.put("8", "Serial number");
                    isHeadersNameValid = IntStream.range(0, length).allMatch(i -> map.get(String.valueOf(i)).equalsIgnoreCase(header[i].trim()));
                    logger.info("isHeadersNameValid {}", isHeadersNameValid);
                    if (!isHeadersNameValid) {
                        logger.info("Invalid headers found");
                        reader.close();
                    }

                }
                case "RECOVER" -> {
                    map.put("0", "IMEI");
                    isHeadersNameValid = IntStream.range(0, length).allMatch(i -> map.get(String.valueOf(i)).equalsIgnoreCase(header[i].trim()));
                    logger.info("isHeadersNameValid {}", isHeadersNameValid);
                    if (!isHeadersNameValid) {
                        logger.info("Invalid headers found");
                        reader.close();
                    }
                }

                case "DEFAULT" -> {
                    map.put("0", "IMEI1");
                    map.put("1", "IMEI2");
                    map.put("2", "IMEI3");
                    map.put("3", "IMEI4");
                    isHeadersNameValid = IntStream.range(0, length).allMatch(i -> map.get(String.valueOf(i)).equalsIgnoreCase(header[i].trim()));
                    logger.info("isHeadersNameValid {}", isHeadersNameValid);
                    if (!isHeadersNameValid) {
                        logger.info("Invalid headers found");
                        reader.close();
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("Exception while reading the file {} {}", filePath, ex.getMessage());
            return false;
        }
        return isHeadersNameValid;
    }


    public void updateStatusAsFailInLostDeviceMgmt(WebActionDb webActionDb, String transactionId) {
        try {
            this.updateStatusInLostDeviceMgmt("Fail", transactionId);
            webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder()
                            .error(this.extractTableNameFromMessage(sqlException.getMessage()))
                            .transactionId(null)
                            .subFeature(null)
                            .build();
                    logger.info("{} table not found", this.extractTableNameFromMessage(sqlException.getMessage()));
                    this.exception(exceptionModel);
                }
            }
        } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(transactionId).subFeature(webActionDb.getSubFeature()).build();
            this.exception(exceptionModel);
        }
    }

    public void commonStorageAvailable(WebActionDb webActionDb, String txnID) {
        try {
            boolean commonStorage = appConfig.isCommonStorage();
            logger.info("isCommonStorage available : {}", commonStorage);
            if (commonStorage && Objects.nonNull(txnID)) {
                this.updateStatusInLostDeviceMgmt("Fail", txnID);
                this.webActionDbOperation(5, webActionDb.getId());
            }
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder()
                            .error(this.extractTableNameFromMessage(sqlException.getMessage()))
                            .transactionId(null)
                            .subFeature(null)
                            .build();
                    logger.info("{} table not found", this.extractTableNameFromMessage(sqlException.getMessage()));
                    this.exception(exceptionModel);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occured inside isCommonStorage method {}", e.getMessage());
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(txnID).subFeature(webActionDb.getSubFeature()).build();
            this.exception(exceptionModel);
        }
    }


    public void exception(ExceptionModel exceptionModel) {
        logger.info("exception raised {} while processing txnID {}", exceptionModel.getTransactionId(), exceptionModel.getError());
        String errorCode = null;
        String value = null;
        String alertMsg = null;
        if (Objects.isNull(exceptionModel.getTransactionId()) && Objects.isNull(exceptionModel.getSubFeature())) {
            errorCode = ConfigurableParameter.TABLE_MISSING_ALERT.getValue();
            value = null;
            alertMsg = exceptionModel.getError();

        } else {
            errorCode = ConfigurableParameter.GLOBAL_EXCEPTION_ALERT.getValue();
            value = exceptionModel.getTransactionId();
            alertMsg = exceptionModel.getTransactionId();
        }
        alertService.raiseAnAlert(value, errorCode, alertMsg, alertMsg, 0);
    }


    public String extractTableNameFromMessage(String message) {
        String tableName = null;
        Pattern pattern = Pattern.compile("Table '\\S+\\.(\\S+)' doesn't exist");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            tableName = matcher.group(1);
        }
        return tableName;
    }

}
