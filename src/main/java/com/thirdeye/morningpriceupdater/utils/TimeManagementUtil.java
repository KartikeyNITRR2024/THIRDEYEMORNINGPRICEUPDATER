package com.thirdeye.morningpriceupdater.utils;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeManagementUtil {
	
	@Autowired
	PropertyLoader propertyLoader;
	
	public Timestamp getCurrentTime() {
        ZonedDateTime indianTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime localDateTime = indianTime.toLocalDateTime();
        return Timestamp.valueOf(localDateTime);
    }
	
	public Timestamp convertToIndianTime(Timestamp timestamp) {
		Instant instant = timestamp.toInstant();
	    ZonedDateTime indianTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
	    return Timestamp.from(indianTime.toInstant());
	}
	
	public Timestamp getNextIterationTime(Timestamp currentTime) {
	    ZonedDateTime currentZonedTime = currentTime.toInstant().atZone(ZoneId.of("Asia/Kolkata"));
	    currentZonedTime = currentZonedTime.minusHours(5).minusMinutes(30);
	    LocalTime currentLocalTime = currentZonedTime.toLocalTime();
	    LocalTime startLocalTime = propertyLoader.morningTime.toLocalTime();
	    LocalTime endLocalTime = propertyLoader.eveningTime.toLocalTime();
	    
	    if(currentZonedTime.getDayOfWeek() == DayOfWeek.SUNDAY || currentZonedTime.getDayOfWeek() == DayOfWeek.SATURDAY)
	    {
	    	ZonedDateTime nextDay = currentZonedTime.plusDays(1);
	    	if(currentZonedTime.getDayOfWeek() == DayOfWeek.SATURDAY)
	    	{
	    		nextDay = currentZonedTime.plusDays(2);
	    	}
	    	return Timestamp.valueOf(startLocalTime.atDate(nextDay.toLocalDate()));
	    }
	    
	    if (currentLocalTime.isBefore(startLocalTime)) {
	        return Timestamp.valueOf(startLocalTime.atDate(currentZonedTime.toLocalDate()));
	    }
	    if (currentLocalTime.isAfter(startLocalTime) && currentLocalTime.isBefore(endLocalTime)) {
	        return Timestamp.valueOf(endLocalTime.atDate(currentZonedTime.toLocalDate()));
	    }
	    if (currentLocalTime.isAfter(endLocalTime)) {
	        ZonedDateTime nextDay = currentZonedTime.plusDays(1);
	        if (currentZonedTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
	            nextDay = currentZonedTime.plusDays(3);
	        }
	        return Timestamp.valueOf(startLocalTime.atDate(nextDay.toLocalDate()));
	    }
	    return Timestamp.valueOf(startLocalTime.atDate(currentZonedTime.toLocalDate()));
	}

	
	public Boolean isMorningTime(Timestamp currentTime)
	{
		ZonedDateTime currentZonedTime = currentTime.toInstant().atZone(ZoneId.of("Asia/Kolkata"));
		currentZonedTime = currentZonedTime.minusHours(5).minusMinutes(30);
        LocalTime currentLocalTime = currentZonedTime.toLocalTime();
        LocalTime startLocalTime = propertyLoader.morningTime.toLocalTime();
        LocalTime endLocalTime = propertyLoader.eveningTime.toLocalTime();
        if(currentZonedTime.getDayOfWeek() == DayOfWeek.SATURDAY || currentZonedTime.getDayOfWeek() == DayOfWeek.SUNDAY)
	    {
	    	return null;
	    }
        if (currentLocalTime.isAfter(startLocalTime) && currentLocalTime.isBefore(endLocalTime)) {
            return true;
        } else if (currentLocalTime.isAfter(endLocalTime)) {
        	return false;
        }
		return null;
	}


}
