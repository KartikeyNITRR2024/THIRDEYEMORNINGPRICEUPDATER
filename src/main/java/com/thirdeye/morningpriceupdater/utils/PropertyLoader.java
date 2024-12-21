package com.thirdeye.morningpriceupdater.utils;

import java.sql.Time;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thirdeye.morningpriceupdater.repositories.ConfigUsedRepo;
import com.thirdeye.morningpriceupdater.entity.ConfigUsed;
import com.thirdeye.morningpriceupdater.entity.ConfigTable;
import com.thirdeye.morningpriceupdater.repositories.ConfigTableRepo;

@Component 
public class PropertyLoader {
    private Long configId;
    public Integer noOfMachine;
    public Integer noOfStockForMorningUpdate;
    public Time morningTime;
    public Time eveningTime;
    
    @Autowired
    private ConfigUsedRepo configUsedRepo;

    private static final Logger logger = LoggerFactory.getLogger(PropertyLoader.class);

    @Autowired
    private ConfigTableRepo configTableRepo;

    public void updatePropertyLoader() {
        try {
        	logger.info("Fetching currently config used.");
            ConfigUsed configUsed = configUsedRepo.findById(1L).get();
            configId = configUsed.getId();
            logger.debug("Fetching configuration for configId: {}", configId);
            Optional<ConfigTable> configTable = configTableRepo.findById(configId);
            if (configTable.isPresent()) {
            	noOfMachine = configTable.get().getNoOfMachineForLiveMarket();
            	noOfStockForMorningUpdate = configTable.get().getNoOfStockForMorningUpdate();
            	morningTime = configTable.get().getMorningPriceUpdateTime();
            	eveningTime = configTable.get().getEveningPriceUpdateTime();
            	logger.info("MorningTime is : {}", morningTime);
            	logger.info("EveningTime is : {}", eveningTime);
            	logger.info("Number of Machines are : {}", noOfMachine);
            } else {
                logger.warn("No configuration found for configId: {}", configId);
            }
        } catch (Exception e) {
            logger.error("An error occurred while fetching configuration: {}", e.getMessage(), e);
        }
    }
}
