package com.thirdeye.morningpriceupdater.pojos;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LiveStockPayload {
	private Integer batchId;
	private Long stockId;
	private Timestamp time;
	private Double price;
}
