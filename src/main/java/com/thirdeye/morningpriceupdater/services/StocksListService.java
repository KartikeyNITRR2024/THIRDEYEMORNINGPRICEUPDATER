package com.thirdeye.morningpriceupdater.services;

import java.util.List;
import java.util.Map;

import com.thirdeye.morningpriceupdater.entity.Stocks;
import com.thirdeye.morningpriceupdater.pojos.LiveStockPayload;

public interface StocksListService {
	public void getStockListInBatches() throws Exception;
	public Integer getStocksSize();
	public Stocks getIdToStock(Long stockId);
	Map<Long, Stocks> getIdToStockInBatch(Long start, Long end);
	void updateStockInBatches(List<LiveStockPayload> data) throws Exception;
}
