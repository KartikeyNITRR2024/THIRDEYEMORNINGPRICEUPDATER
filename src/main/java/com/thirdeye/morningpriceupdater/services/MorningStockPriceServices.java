package com.thirdeye.morningpriceupdater.services;

import java.util.List;

import com.thirdeye.morningpriceupdater.pojos.LiveStockPayload;

public interface MorningStockPriceServices {
	void morningStockPriceData(List<LiveStockPayload> data, Integer machineNo);
	void filterStocksToUpdate();
	void eveningStockPriceData(List<LiveStockPayload> data, Integer machineNo);
}
