package com.thirdeye.morningpriceupdater.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thirdeye.morningpriceupdater.entity.Stocks;
import com.thirdeye.morningpriceupdater.pojos.Changes;
import com.thirdeye.morningpriceupdater.pojos.LiveStockPayload;
import com.thirdeye.morningpriceupdater.services.MorningStockPriceServices;
import com.thirdeye.morningpriceupdater.utils.PropertyLoader;
import com.thirdeye.morningpriceupdater.externalcontrollers.Thirdeye_Messenger_Connection;

@Service
public class MorningStockPriceServicesImpl implements MorningStockPriceServices {

	@Autowired
	StocksListServiceImpl stocksListServiceImpl;
	
	@Autowired
    PropertyLoader propertyLoader;
	
	@Autowired
	Thirdeye_Messenger_Connection thirdeye_Messenger_Connection;
    
    @Value("${startingIdOfStocks}")
    private Integer startingIdOfStocks;
    
    private Set<Integer> machineSet = new HashSet<>();
    
    private static final Logger logger = LoggerFactory.getLogger(MorningStockPriceServicesImpl.class);
    
    List<Changes> changeInPriceList = new ArrayList<>();
	
	@Override
	public void morningStockPriceData(List<LiveStockPayload> data, Integer machineNo) {
		Integer stocksSize = stocksListServiceImpl.getStocksSize();
	    Integer totalMachines = propertyLoader.noOfMachine;

	    if (totalMachines <= 0) {
	         logger.error("Total machines must be greater than zero.");
	         throw new IllegalArgumentException("Total machines must be greater than zero.");
	    }

	    Integer noOfStocksInBatch = (stocksSize / totalMachines) + ((stocksSize % totalMachines == 0) ? 0 : 1);
	    Integer startingPos = startingIdOfStocks+(noOfStocksInBatch * (machineNo - 1)) + 1;
	    Integer endPos = startingIdOfStocks+Math.min(stocksSize, noOfStocksInBatch * machineNo);

	    logger.info("Fetching stocks for batch: Machine No: {}, Starting Pos: {}, End Pos: {}", machineNo, startingPos, endPos);
		Map<Long,Stocks> idToStocks = stocksListServiceImpl.getIdToStockInBatch((long) startingPos, (long)endPos);
		
		for(LiveStockPayload liveStockPayload : data)
		{
			Double oldPrice = idToStocks.get(liveStockPayload.getStockId()).getLastDayMarketPrice();
			if(oldPrice == null)
			{
				oldPrice = liveStockPayload.getPrice();
			}
			Double newPrice = liveStockPayload.getPrice();
			Changes changes = new Changes(liveStockPayload.getStockId(),oldPrice,newPrice,newPrice-oldPrice,((newPrice-oldPrice)/oldPrice)*100);
			logger.info("Changes in stock {} is : {}", idToStocks.get(liveStockPayload.getStockId()).getMarketName()+" "+idToStocks.get(liveStockPayload.getStockId()).getStockSymbol(), changes);
			changeInPriceList.add(changes);
		}
		
		if (machineSet.size() >= propertyLoader.noOfMachine) {
//		if (machineSet.size() >= 0) {
			filterStocksToUpdate();
            machineSet.clear();
            changeInPriceList.clear();
        } else {
            machineSet.add(machineNo);
        }	
	}
	
	@Override
	public void filterStocksToUpdate() {
		changeInPriceList.sort((c1, c2) -> c2.getPercentChanges().compareTo(c1.getPercentChanges()));
		if(propertyLoader.noOfStockForMorningUpdate < changeInPriceList.size())
		{
			changeInPriceList.subList(propertyLoader.noOfStockForMorningUpdate, changeInPriceList.size()).clear();
		}
		logger.info("Price of stocks is going to update : {}", changeInPriceList);
		thirdeye_Messenger_Connection.sendMorningPricePayload(changeInPriceList);
	}
	
	@Override
	public void eveningStockPriceData(List<LiveStockPayload> data, Integer machineNo) {
		logger.info("Going to update stocks prices of machine number {} in evening.", machineNo);
		try {
			stocksListServiceImpl.updateStockInBatches(data);
			logger.info("Successfully updated stocks prices of machine number {} in evening.", machineNo);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Failed to update stocks prices of machine number {} in evening.", machineNo);
		}
		if (machineSet.size() >= 0) {
            machineSet.clear();
            logger.info("Successfully updated all stocks prices in evening.");
        } else {
            machineSet.add(machineNo);
        }
	}
	
	
}
