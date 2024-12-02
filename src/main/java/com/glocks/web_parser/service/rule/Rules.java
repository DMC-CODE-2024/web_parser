package com.glocks.web_parser.service.rule;


import com.gl.rule_engine.RuleInfo;
import com.glocks.web_parser.config.AppDbConfig;
import com.glocks.web_parser.dto.RuleDto;
import com.glocks.web_parser.repository.app.SysParamRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Service
public class Rules {

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    AppDbConfig appDbConfig;

    @Autowired
    SysParamRepository sysParamRepository;

    public String applyRule(List<RuleDto> ruleList, String imei, boolean gracePeriod, Connection conn, String source) {
        BufferedWriter bw = null;
        String ans = "";
        try {
            for (RuleDto ruleDto : ruleList) {
                Statement stmt = conn.createStatement();
                String sub_imei = imei.length() > 14 ? imei.substring(0, 14) : imei;
                RuleInfo ruleInfo = new RuleInfo(ruleDto.getName(), "app", "executeRule",
                        sub_imei, conn, source, stmt, imei);
                String ruleName = ruleDto.getName();
                String executeRuleOutput = com.gl.rule_engine.RuleEngineApplication.startRuleEngine(ruleInfo);
                String expectedRuleOutput = ruleDto.getOutput();
                if (executeRuleOutput.equalsIgnoreCase(expectedRuleOutput)) {
                    logger.info("Rule {} passed for imei {}", ruleName, imei);
                } else if (executeRuleOutput.equalsIgnoreCase("error")) {
                    logger.error("Error in GDCE rule. Moving to next record.");
                    ans = "error_" + ruleDto.getName();
                    break;
                } else {
                    ans = ruleDto.getName();
                    ruleInfo = new RuleInfo(ruleDto.getName(), "app", "executeAction",
                            sub_imei, conn, "source", (gracePeriod ? ruleDto.getGraceAction() : ruleDto.getPostGraceAction()), stmt);
                    logger.info("Rule {} failed for imei {}", ruleName, imei);
                    String actionOutput = com.gl.rule_engine.RuleEngineApplication.startRuleEngine(ruleInfo);
                    String currentAction = gracePeriod ? ruleDto.getFailedRuleActionGrace() :
                            ruleDto.getFailedRuleActionPostGrace();
                    if (currentAction.equalsIgnoreCase("Record")) {
                        logger.info("Moving to the next record");
                        break;
                    } else if (currentAction.equalsIgnoreCase("Rule")) {
                        logger.info("Moving to next rule");
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error with {} ", e.getMessage());
        }
        return ans;
    }

//    public String applyRule(List<RuleDto> ruleList, TrcLocalManufacturedDevice trcLocalManufacturedDevice, boolean gracePeriod, Connection conn) {
//        String[] args = {
//                "", "", "", "", "","","","","","","","","",""
//        };
////        boolean gracePeriod = checkGracePeriod();
//        BufferedWriter bw = null;
//        String ans = "";
//        try {
//
//            for(RuleDto ruleDto: ruleList) {
//
//                String ruleName = ruleDto.getName();
//                args[0] = ruleName;
//                args[1] = "1";
//                args[3] = imei.length() > 14 ?
//                        imei.substring(0,14) : imei;
//
//
//                String executeRuleOutput = RuleEngineApplication.startRuleEngine(args, conn, bw);
//                String expectedRuleOutput = ruleDto.getOutput();
//
//                if(executeRuleOutput.equalsIgnoreCase(expectedRuleOutput)) {
//                    logger.info("Rule {} passed for imei {}", ruleName, imei);
//                }
//                else {
//                    ans = ruleDto.getRuleMessage();
//                    logger.info("Rule {} failed for imei {}", ruleName, imei);
//                    args[1] = "2";
//                    args[13] = gracePeriod ? ruleDto.getGraceAction() : ruleDto.getPostGraceAction();
//                    String actionOutput = RuleEngineApplication.startRuleEngine(args, conn, bw);
//                    String currentAction = gracePeriod ? ruleDto.getFailedRuleActionGrace() :
//                            ruleDto.getFailedRuleActionPostGrace();
//                    if(currentAction.equalsIgnoreCase("Record")) {
//                        logger.info("Moving to the next record");
//                        break;
//                    }
//                    else if(currentAction.equalsIgnoreCase("Rule")) {
//                        logger.info("Moving to next rule");
//                        continue;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error with {} ", e.getMessage());
//            ans = e.getMessage();
//        }
//        return ans;
//    }

    public boolean checkGracePeriod() {
        try {
            String value = sysParamRepository.getValueFromTag("GRACE_PERIOD_END_DATE");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = new Date();
            Date graceDate = sdf.parse(value);
            if (currentDate.compareTo(graceDate) > 0) {
                logger.info("Grace Period.");
                return true;
            } else {
                logger.info("Not Grace Period.");
                return false;
            }
        } catch (Exception ex) {
            logger.error("Exception while calculating grace period. {}", ex.getMessage());
            return false;
        }
    }


}
