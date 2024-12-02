package com.glocks.web_parser.service.parser.moi.loststolen;

import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.service.parser.moi.utility.ExceptionModel;
import com.glocks.web_parser.service.parser.moi.utility.IMEISeriesModel;
import com.glocks.web_parser.service.parser.moi.utility.MOIService;
import com.glocks.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MOILostStolenSingleRequest implements RequestTypeHandler<StolenDeviceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final WebActionDbRepository webActionDbRepository;
    private final MOIService moiService;
    private final MOILostStolenService moiLostStolenService;
    static int greyListDuration;

    @Override
    public void executeInitProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        executeValidateProcess(webActionDb, stolenDeviceMgmt);
    }


    @Override
    public void executeValidateProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        try {
            greyListDuration = Integer.parseInt(moiService.greyListDuration());
            logger.info("GREY_LIST_DURATION : {} ", greyListDuration);
            executeProcess(webActionDb, stolenDeviceMgmt);
        } catch (NumberFormatException e) {
            logger.info("Invalid GREY_LIST_DURATION value");
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(stolenDeviceMgmt.getRequestId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }

    @Override
    public void executeProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        try {
            String deviceLostDateTime = stolenDeviceMgmt.getDeviceLostDateTime();
            logger.info("deviceLostDateTime {} ", deviceLostDateTime);
            if (Objects.nonNull(deviceLostDateTime)) {
                IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();
                BeanUtils.copyProperties(stolenDeviceMgmt, imeiSeriesModel);
                List<String> imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
                if (!imeiList.isEmpty()) imeiList.forEach(imei -> {
                    if (!moiService.isNumericAndValid.test(imei)) {
                        logger.info("Invalid IMEI {} found", imei);
                    } else {
                        moiLostStolenService.recordProcess(imei, stolenDeviceMgmt, deviceLostDateTime, "Single", greyListDuration, webActionDb);
                    }
                });
                moiService.updateStatusInLostDeviceMgmt("Done", stolenDeviceMgmt.getRequestId());
                moiService.webActionDbOperation(4, webActionDb.getId());
            } else {
                logger.info("Invalid deviceLostDateTime value {}", deviceLostDateTime);
            }
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
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(stolenDeviceMgmt.getRequestId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }
}