package com.thirdeye.morningpriceupdater.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.thirdeye.morningpriceupdater.entity.Stocks;
import com.thirdeye.morningpriceupdater.pojos.LiveStockPayload;
import com.thirdeye.morningpriceupdater.repositories.StocksRepo;
import com.thirdeye.morningpriceupdater.services.StocksListService;

@Service
public class StocksListServiceImpl implements StocksListService {

    private Map<Long, Stocks> idToStock = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(StocksListServiceImpl.class);
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    private StocksRepo stocksRepo;
    
    @Value("${batchSizeToGetSetFromDatabase}")
    private Long batchSizeToGetSetFromDatabase;

    public void getStockListInBatches() throws Exception {
    	Map<Long, Stocks> idToStock1 = new HashMap<>();
        logger.info("Starting to fetch stock list in batches.");
        try {
            long totalStocks = stocksRepo.count();
            int pageSize = batchSizeToGetSetFromDatabase.intValue();
            int totalPages = (int) Math.ceil((double) totalStocks / pageSize);
            for (int page = 0; page < totalPages; page++) {
                Pageable pageable = PageRequest.of(page, pageSize);
                Page<Stocks> stockListBatch = stocksRepo.findAll(pageable);
                for (Stocks stock : stockListBatch.getContent()) { 
                	idToStock1.put(stock.getId(), stock);
                }
            }
            lock.writeLock().lock();
            idToStock.clear();
            idToStock = new HashMap<>(idToStock1);
            logger.info("Successfully fetched all {} stocks in batches.", totalStocks);
        } catch (Exception e) {
            logger.error("Error occurred while fetching stock list in batches: {}", e.getMessage(), e);
            throw new Exception("Failed to retrieve stock list in batches", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Stocks getIdToStock(Long stockId) {
    	Stocks stock = null;
    	lock.readLock().lock();
        try {
	    	if(idToStock.containsKey(stockId))
	    	{
	    		stock = idToStock.get(stockId);
	    	}
	        return stock;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Integer getStocksSize() {
        lock.readLock().lock();
        try {
            return idToStock.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Map<Long, Stocks> getIdToStockInBatch(Long start, Long end) {
    	lock.readLock().lock();
        Map<Long, Stocks> getIdToStockBatch = new HashMap<>();
        try {
            for (Long i = start; i <= end; i++) {
                if (idToStock.containsKey(i)) {
                    getIdToStockBatch.put(i, idToStock.get(i));
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return getIdToStockBatch;
    }
    
    @Override
    public void updateStockInBatches(List<LiveStockPayload> data) throws Exception
    {
    	lock.writeLock().lock();
    	try {
	    	List<Stocks> batches = new ArrayList<>();
	    	for(LiveStockPayload liveStockPayload: data)
	    	{
	    		Stocks stock = idToStock.get(liveStockPayload.getStockId());
	    		stock.setLastDayMarketPrice(liveStockPayload.getPrice());
	    		idToStock.put(liveStockPayload.getStockId(), stock);
	    		batches.add(stock);
	    	} 
	    	stocksRepo.saveAll(batches);
	    	logger.info("Successfully updated stock list in batches.");
    	} catch (Exception e) {
            logger.error("Error occurred while updating stock list in batches: {}", e.getMessage(), e);
            throw new Exception("Failed to update stock list in batches", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
