package com.everdata.server;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.Booleans;


public class RequestMap {
	
	private  Map<String, String> map=new HashMap<String, String>();
	public   String get(String key){
		 return map.get(key);
	}
	 public String get(String key, String defaultValue) {
	        String sValue = map.get(key);
	        if (sValue == null) {
	            return defaultValue;
	        }
	        try {
	            return sValue;
	        } catch (NumberFormatException e) {
	            throw new IllegalArgumentException("Failed to parse int parameter [" + key + "] with value [" + sValue + "]", e);
	        }
	    }
	public  void put(String key,String value){
		map.put(key, value);
	}
	 public int paramAsInt(String key, int defaultValue) {
	        String sValue = map.get(key);
	        if (sValue == null) {
	            return defaultValue;
	        }
	        try {
	            return Integer.parseInt(sValue);
	        } catch (NumberFormatException e) {
	            throw new IllegalArgumentException("Failed to parse int parameter [" + key + "] with value [" + sValue + "]", e);
	        }
	    }
	 public long paramAsLong(String key, long defaultValue) {
	        String sValue = map.get(key);
	        if (sValue == null) {
	            return defaultValue;
	        }
	        try {
	            return Long.parseLong(sValue);
	        } catch (NumberFormatException e) {
	            throw new IllegalArgumentException("Failed to parse long parameter [" + key + "] with value [" + sValue + "]", e);
	        }
	    }

	    public boolean paramAsBoolean(String key, boolean defaultValue) {
	        return Booleans.parseBoolean(map.get(key), defaultValue);
	    }

	    public Boolean paramAsBoolean(String key, Boolean defaultValue) {
	        return Booleans.parseBoolean(map.get(key), defaultValue);
	    }

}
