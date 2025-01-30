package app.web_parser.controller;

import app.web_parser.config.AppConfig;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.parser.FeatureInterface;
import app.web_parser.service.parser.FeatureList;
import app.web_parser.service.parser.ListMgmt.ListMgmtFeature;
import app.web_parser.service.parser.TRC.TRCFeature;
import app.web_parser.service.parser.moi.utility.ExceptionModel;
import app.web_parser.service.parser.moi.utility.MOIService;
import app.web_parser.utils.VirtualIpAddressUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class MainController {

    @Autowired
    AppConfig appConfig;

    @Autowired
    WebActionDbRepository webActionDbRepository;

    @Autowired
    TRCFeature trcFeature;
    @Autowired
    ListMgmtFeature listMgmtFeature;

    @Autowired
    FeatureList featureList;

    @Autowired
    VirtualIpAddressUtil virtualIpAddressUtil;
    @Autowired
    MOIService moiService;
    private final Logger logger = LogManager.getLogger(MainController.class);
    AtomicInteger isRunning = new AtomicInteger(0);


   // @Scheduled(cron = "${web.parser.sleep.time}")
    public void listPendingProcessTask() throws InterruptedException {
        // check if vip here or not, if yes then process
        while (true) {
            if (virtualIpAddressUtil.getFullList()) {  //remove !
                break;
                //     break; // Server is active, no need to keep checking
            } else {
                logger.info("VIP not found. Sleeping for " + appConfig.getWebParserSleepTime() + " seconds.");
                return;
            }
        }
        if (isRunning.get() == 1) {
            logger.info("Process already running...");
        } else {
            try {
                logger.info("Starting the web parser process.");
                List<WebActionDb> listOfPendingTasks = webActionDbRepository.getListOfPendingTasks(
                        appConfig.getFeatureList(), appConfig.getWebParserQueryGap());
                logger.info(listOfPendingTasks);
                if (listOfPendingTasks.isEmpty()) {
                    logger.info("No tasks to perform");
                } else {
                    for (WebActionDb webActionDb : listOfPendingTasks) {
                        startProcess(webActionDb);
                    }
                }
                isRunning.set(0);
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
                        moiService.exception(exceptionModel);
                    }
                }
            }
        }

    }

    public void startProcess(WebActionDb wb) {

        logger.info("Starting process for the entry in web_action_db {}", wb);
        String state = wb.getState() == 1 ? "init" : wb.getState() == 2 ? "validateProcess" :
                wb.getState() == 3 ? "executeProcess" : "";
        if (state.isEmpty()) {
            logger.error("The web_action_db entry does not have the state column value populated.");
            return;
        }

        featureList.getFeatures()
                .entrySet()
                .stream()
                .filter(a -> a.toString().contains(wb.getFeature()))
                .map(Map.Entry::getValue)
                .map(FeatureInterface.class::cast)
                .reduce(new String(), (result, ruleNode) -> {
                    if (state.contains("init")) {
                        ruleNode.executeInit(wb);
                    } else if (state.contains("validateProcess")) {
                        ruleNode.validateProcess(wb);
                    } else {
                        ruleNode.executeProcess(wb);
                    }
                    return result;
                }, (cumulative, intermediate) -> {
                    return intermediate;
                });


    }

    private static void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}


// return RulesList.getItems()
//                .stream()
//                .filter(a -> a.getClass().getName().toString().contains(ruleEngine.ruleName))
//        .map(ExecutionInterface.class::cast)
//                .reduce(new String(), (result, ruleNode) -> {
//String key = ruleEngine.executeRuleAction;
//                    if (key.contains("executeRule")) {
//result = ruleNode.executeRule(ruleEngine);
//                    } else {
//result = ruleNode.executeAction(ruleEngine);
//                    }
//                            return result;
//                }, (cumulative, intermediate) -> {
//        return intermediate;
//                });