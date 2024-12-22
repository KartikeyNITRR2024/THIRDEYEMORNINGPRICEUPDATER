package com.thirdeye.morningpriceupdater.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thirdeye.morningpriceupdater.entity.Stocks;
import com.thirdeye.morningpriceupdater.services.StocksListService;
import com.thirdeye.morningpriceupdater.services.UpdateInitiaterService;
import com.thirdeye.morningpriceupdater.utils.AllMicroservicesData;


@RestController
@RequestMapping("/api/stocklist")
public class StockListController {

	@Autowired
	AllMicroservicesData allMicroservicesData;
	
	@Autowired
	private StocksListService stocksListService;
	
    private static final Logger logger = LoggerFactory.getLogger(StockListController.class);

    @GetMapping("/{uniqueId}")
    public ResponseEntity<List<Stocks>> getAllStocks(@PathVariable("uniqueId") Integer pathUniqueId) {
        Integer currentUniqueId = allMicroservicesData.current.getMicroserviceUniqueId();
        if (pathUniqueId.equals(currentUniqueId)) {
            logger.info("Status check for uniqueId {}: Found", pathUniqueId);
            try {
                List<Stocks> stocksList = stocksListService.getAllStocks();
                if (stocksList == null || stocksList.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(stocksList);
            } catch (Exception e) {
                logger.error("Error occurred while fetching stocks: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            logger.warn("Status check for uniqueId {}: Not Found", pathUniqueId);
            return ResponseEntity.notFound().build();
        }
    }
    
}


