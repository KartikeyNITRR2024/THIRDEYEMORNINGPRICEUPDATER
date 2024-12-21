package com.thirdeye.morningpriceupdater.externalcontrollers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.thirdeye.morningpriceupdater.pojos.Changes;
import com.thirdeye.morningpriceupdater.utils.AllMicroservicesData;

@Service
public class Thirdeye_Messenger_Connection {
    
	@Value("${messengingMicroservice}")
    private String messengingMicroservice;
    
    @Autowired
	AllMicroservicesData allMicroservicesData;

    @Autowired
    private RestTemplate restTemplate;
    
    private static final Logger logger = LoggerFactory.getLogger(Thirdeye_Messenger_Connection.class);
    
    public void sendMorningPricePayload(List<Changes> changeInPriceList) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Content-Type", "application/json");
//            HttpEntity<Object> request = new HttpEntity<>(changeInPriceList, headers);
//
//            ResponseEntity<Boolean> response = restTemplate.exchange(
//                allMicroservicesData.allMicroservices.get(messengingMicroservice).getMicroserviceUrl() + "api/morningpriceupdatermessenger/" + allMicroservicesData.allMicroservices.get(messengingMicroservice).getMicroserviceUniqueId(),
//                HttpMethod.POST,
//                request,
//                Boolean.class
//            );
//
//            return response.getBody() != null && response.getBody();
//        } catch (Exception e) {
//            logger.error("Error while sending live Stock Payload To messenger: ", e);
//            return false;
//        }
    	List<Changes> immutableChangeInPriceList = new ArrayList<>(changeInPriceList);

    	CompletableFuture.runAsync(() -> {
	    	try {
	            HttpHeaders headers = new HttpHeaders();
	            headers.set("Content-Type", "application/json");
	            HttpEntity<Object> request = new HttpEntity<>(immutableChangeInPriceList, headers);
	
	            ResponseEntity<Boolean> response = restTemplate.exchange(
	                allMicroservicesData.allMicroservices.get(messengingMicroservice).getMicroserviceUrl() + "api/morningpriceupdatermessenger/" + allMicroservicesData.allMicroservices.get(messengingMicroservice).getMicroserviceUniqueId(),
	                HttpMethod.POST,
	                request,
	                Boolean.class
	            );
	
	        } catch (Exception e) {
	            logger.error("Error while sending live Stock Payload To messenger: ", e);
	        }
    	});
    }

}
