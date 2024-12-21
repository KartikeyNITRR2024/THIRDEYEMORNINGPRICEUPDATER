package com.thirdeye.morningpriceupdater.controllers;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirdeye.morningpriceupdater.utils.AllMicroservicesData;
import com.thirdeye.morningpriceupdater.utils.TimeManagementUtil;
import com.thirdeye.morningpriceupdater.pojos.LiveStockPayload;
import com.thirdeye.morningpriceupdater.services.MorningStockPriceServices; 

@RestController
@RequestMapping("/api/morningstockprice")
public class MorningStockPriceController {

	@Autowired
	AllMicroservicesData allMicroservicesData;
	
	@Value("${uniqueMachineCode}")
    private String uniqueMachineCode;
	
    @Autowired
    TimeManagementUtil timeManagementUtil;
    
    @Autowired
    MorningStockPriceServices morningStockPriceServices;
	
	
    private static final Logger logger = LoggerFactory.getLogger(MorningStockPriceController.class);

    @PostMapping("morninglivemarketdata/{uniqueId}/{uniqueMachineCode}/{machineNo}")
    public ResponseEntity<Timestamp> morningLiveMarketData(@PathVariable("uniqueId") Integer pathUniqueId, @PathVariable("uniqueMachineCode") String pathUniqueMachineCode, @PathVariable("machineNo") Integer pathMachineNo, @RequestBody List<LiveStockPayload> liveStockData) {
    	if (pathUniqueId.equals(allMicroservicesData.current.getMicroserviceUniqueId())) {
        	logger.info("Status check for uniqueId {}: Found and uniqueMachineCode {}: Found", allMicroservicesData.current.getMicroserviceUniqueId(), uniqueMachineCode);
        	Timestamp currenttime = timeManagementUtil.getCurrentTime();
        	logger.info("Current Iteration time : {}", currenttime);
        	Boolean checkTime = timeManagementUtil.isMorningTime(currenttime);
        	if(checkTime == Boolean.TRUE)
        	{
        		logger.info("Morning time going to call function  morningStockPriceData");
        	    morningStockPriceServices.morningStockPriceData(liveStockData, pathMachineNo);
        	}
        	else if(checkTime == Boolean.FALSE)
        	{
        		logger.info("Evening time going to call function eveningStockPriceData");
        		morningStockPriceServices.eveningStockPriceData(liveStockData, pathMachineNo);
        	}
        	else
        	{
        		logger.info("Permission deniend for current time");
        	}
        	Timestamp nextIterationTime = timeManagementUtil.getNextIterationTime(currenttime);
            logger.info("Next Iteration time : {}", nextIterationTime);
            return ResponseEntity.ok(nextIterationTime);
        } else {
            logger.warn("Status check for uniqueId {}: Not Found", allMicroservicesData.current.getMicroserviceUniqueId());
            return ResponseEntity.notFound().build();
        }
    }
}
