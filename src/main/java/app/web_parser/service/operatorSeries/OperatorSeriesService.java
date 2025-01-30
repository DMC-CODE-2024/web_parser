package app.web_parser.service.operatorSeries;


import app.web_parser.config.AppConfig;
import app.web_parser.model.app.OperatorSeries;
import app.web_parser.repository.app.OperatorSeriesRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class OperatorSeriesService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    AppConfig appConfig;
    @Autowired
    OperatorSeriesRepository operatorSeriesRepository;

    HashMap<String, OperatorSeries> operatorSeriesHash;

    @PostConstruct
    public void fillOperatorSeriesHash() {

        // populate the hash map of operator list
        List<OperatorSeries> allOperatorSeries = operatorSeriesRepository.findAll();
        logger.debug("All the entries of operator Series {}", allOperatorSeries);
        operatorSeriesHash = new HashMap<String, OperatorSeries>();
        for(OperatorSeries operatorSeries: allOperatorSeries) {
            int j = operatorSeries.getSeriesStart();
            while(j <= operatorSeries.getSeriesEnd()) {
                operatorSeriesHash.put(String.valueOf(j), operatorSeries);
                j++;
            }
        }
        logger.debug("Operator series hash count {}", operatorSeriesHash.size());
    }

    public String getOperatorName(boolean imsiEmpty, boolean msisdnEmpty, String imsi, String msisdn) {

        boolean found = false;
        for (String key : operatorSeriesHash.keySet()) {
            if (!imsiEmpty && imsi.startsWith(key)) {
                logger.info("IMSI prefix matched with operator {}", operatorSeriesHash.get(key));
                return operatorSeriesHash.get(key).getOperatorName();
            }
            if(!msisdnEmpty && msisdn.startsWith(key)) {
                logger.info("MSISDN prefix matched with operator {}", operatorSeriesHash.get(key));
                return operatorSeriesHash.get(key).getOperatorName();
            }
        }
        return "";
    }

    public boolean validLengthMsisdn(String msisdn) {

        for (String key : operatorSeriesHash.keySet()) {
            if(msisdn.startsWith(key) &&
                    !String.valueOf(msisdn.length()).equals(operatorSeriesHash.get(key).getLength())) {
                logger.info("allowed length for prefix {} is {}", key, operatorSeriesHash.get(key).getLength());
                logger.info("MSISDN length validation failed {}", operatorSeriesHash.get(key));
                return false;
            }
        }
        return true;
    }


}
