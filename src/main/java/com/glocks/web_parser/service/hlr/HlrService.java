package com.glocks.web_parser.service.hlr;


import com.glocks.web_parser.repository.app.HlrDumpRepository;
import com.glocks.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HlrService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    HlrDumpRepository hlrDumpRepository;
    @Autowired
    Validation validation;


    public String popluateImsi(String msisdn) {

        String imsi = hlrDumpRepository.findImsi(msisdn);
        if(validation.isEmptyAndNull(imsi)) {
            logger.error("The IMSI is not found in HLR for MSISDN {}.", msisdn);
        } else
        logger.info("The IMSI is {}, for the MSISDN {} in HLR", imsi, msisdn);
        return imsi;

    }

}
