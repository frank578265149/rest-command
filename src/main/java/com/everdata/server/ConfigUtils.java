 package com.everdata.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {
	private static final Logger LOG = LoggerFactory
			.getLogger(ConfigUtils.class);
	private static Properties props = new Properties();

	static {
		init();
	}

	private static void init() {
		try {
			props.load(ConfigUtils.class.getClassLoader().getResourceAsStream(
					Constants.CONFIG_FILE_NAME));
		} catch (IOException e) {
			LOG.error("can not load config file alarm.properties", e);
		}
	}

	public static Properties getAll() {
		return props;
	}

	public static String get(String key) {
		return props.getProperty(key);
	}

	public static String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public static Integer getInt(String key, int defaultValue) {
		Object obj = props.getProperty(key);
		if (obj == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(obj.toString());
		}
	}

	public static Long getLong(String key, long defaultValue) {
		Object obj = props.getProperty(key);
		if (obj == null) {
			return defaultValue;
		} else {
			return Long.parseLong(obj.toString());
		}
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		Object obj = props.getProperty(key);
		if (obj == null) {
			return defaultValue;
		} else {
			return "true".equalsIgnoreCase(obj.toString());
		}
	}

	public static List<String> getList(String key) {
		Object obj = props.getProperty(key);
		if (obj == null) {
			return null;
		}
		List<String> list = new ArrayList<>();
		for (String tmp : ((String) obj).split(",")) {
			list.add(tmp);
		}
		return list;
	}
}
