package com.glocks.web_parser.service.parser.moi.recover;

import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.service.parser.moi.utility.ExceptionModel;
import com.glocks.web_parser.service.parser.moi.utility.IMEISeriesModel;
import com.glocks.web_parser.service.parser.moi.utility.MOIService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MOIRecoverService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final StolenDeviceDetailRepository stolenDeviceDetailRepository;
    private final StolenDeviceDetailHisRepository stolenDeviceDetailHisRepository;
    private final AppConfig appConfig;
    private final BlackListRepository blackListRepository;
    private final BlackListHisRepository blackListHisRepository;
    private final GreyListRepository greyListRepository;
    private final GreyListHisRepository greyListHisRepository;
    private final MOIService moiService;

    public boolean fileProcessing(String filePath, StolenDeviceMgmt stolenDeviceMgmt, WebActionDb webActionDb) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String record;
            IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();
            String[] split;
            boolean headerSkipped = false;
            while ((record = reader.readLine()) != null) {
                if (!record.trim().isEmpty()) {
                    if (!headerSkipped) {
                        headerSkipped = true;
                    } else {
                        split = record.split(appConfig.getMoiFileSeparator(), -1);
                        imeiSeriesModel.setImeiSeries(split, "DEFAULT");
                        logger.info("IMEISeriesModel : {}", imeiSeriesModel);
                        List<String> imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
                        actionAtRecord(webActionDb, stolenDeviceMgmt, imeiList, "Bulk");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception in processing the file {}", e.getMessage());
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(webActionDb.getTxnId()).subFeature(webActionDb.getSubFeature()).build();
            moiService.exception(exceptionModel);
            return false;
        }
        return true;
    }


    public void blackListFlow(String imei, String requestID, String mode, String requestType, WebActionDb webActionDb) {
        logger.info("blackListFlow executed...");
        String imeiValue = moiService.getIMEI(imei);
        try {
            moiService.findBlackListByImei(imeiValue).ifPresent(response -> {
                String source = response.getSource();
                int val = (int) moiService.sourceCount.apply(source).longValue();
                switch (val) {
                    case 1 -> {
                        BlackListHis blackListHis = new BlackListHis();
                        BeanUtils.copyProperties(response, blackListHis);
                        blackListHis.setRequestType(requestType);
                        blackListHis.setTxnId(requestID);
                        blackListHis.setOperation(0);
                        logger.info("BlackListHis : {}", blackListHis);
                        BlackListHis save = moiService.save(blackListHis, blackListHisRepository::save);
                        logger.info("black_list id {}", response.getId());
                        if (save != null) {
                            blackListRepository.deleteById(Math.toIntExact(response.getId()));
                            logger.info("Record deleted from black_list for requestID {}", requestID);
                        }
                    }
                    case 2 -> {
                        String updatedSourceValue = moiService.remove(source);
                        moiService.updateSource(updatedSourceValue, imeiValue, "BLACK_LIST");
                    }
                    default -> logger.info("No valid source value {}  found", source);
                }
            });
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder()
                            .error(moiService.extractTableNameFromMessage(sqlException.getMessage()))
                            .transactionId(null)
                            .subFeature(null)
                            .build();
                    logger.info("{} table not found", moiService.extractTableNameFromMessage(sqlException.getMessage()));
                    moiService.exception(exceptionModel);
                }
            }
        } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(requestID).subFeature(webActionDb.getSubFeature()).build();
            moiService.exception(exceptionModel);

        }
    }

    public void greyListFlow(String imei, String mode, String requestID, String requestType, WebActionDb webActionDb) {
        logger.info("greyListFlow executed...");
        String imeiValue = moiService.getIMEI(imei);
        try {
            moiService.findGreyListByImei(imeiValue).ifPresent(response -> {
                String source = response.getSource();
                int val = (int) moiService.sourceCount.apply(source).longValue();
                switch (val) {
                    case 1 -> {
                        if (source.equals("MOI")) {
                            GreyListHis greyListHis = new GreyListHis();
                            BeanUtils.copyProperties(response, greyListHis);
                            greyListHis.setRequestType(requestType);
                            greyListHis.setTxnId(requestID);
                            greyListHis.setOperation(0);
                            logger.info("GreyListHis {}", greyListHis);
                            GreyListHis save = moiService.save(greyListHis, greyListHisRepository::save);
                            logger.info("grey_list id {}", response.getId());
                            if (save != null) {
                                greyListRepository.deleteById(response.getId());
                                logger.info("Record deleted from grey_list for requestID {}", requestID);
                            }
                        }
                    }
                    case 2 -> {
                        String updatedSourceValue = moiService.remove(source);
                        moiService.updateSource(updatedSourceValue, imeiValue, "GREY_LIST");
                    }
                    default -> logger.info("No valid source value {}  found in grey_list", source);
                }
            });
        } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(requestID).subFeature(webActionDb.getSubFeature()).build();
            moiService.exception(exceptionModel);

        }
    }

    public void lostDeviceDetailFlow(String imei, StolenDeviceMgmt stolenDeviceMgmt, WebActionDb webActionDb) {
        StolenDeviceDetailHis stolenDeviceDetailHis = StolenDeviceDetailHis.builder().imei(imei).requestId(stolenDeviceMgmt.getLostId()).operation(0).requestType("Recover").build();
        try {
            StolenDeviceDetailHis save = moiService.save(stolenDeviceDetailHis, stolenDeviceDetailHisRepository::save);
            if (save != null) {
                int i = stolenDeviceDetailRepository.deleteByImeiAndRequestTypeIgnoreCaseIn(imei, List.of("STOLEN", "LOST"));
                if (i > 0) logger.info("record delete for IMEI {} from stolen_device_detail", imei);
                else logger.info("No record found for delete operation against IMEI {}", imei);
            }
        } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(stolenDeviceMgmt.getRequestId()).subFeature(webActionDb.getSubFeature()).build();
            moiService.exception(exceptionModel);
        }
    }

    public void actionAtRecord(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt, List<String> imeiList, String mode) {
        if (imeiList.size() > 0) {
            for (String imei : imeiList) {
                if (moiService.isNumericAndValid.test(imei)) {
                    this.blackListFlow(imei, stolenDeviceMgmt.getRequestId(), mode, stolenDeviceMgmt.getRequestType(), webActionDb);
                    this.greyListFlow(imei, stolenDeviceMgmt.getRequestId(), mode, stolenDeviceMgmt.getRequestType(), webActionDb);
                    this.lostDeviceDetailFlow(imei, stolenDeviceMgmt, webActionDb);
                }
            }
        }
    }
}
