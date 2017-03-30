package com.everdata.server;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;


public class ESUtils {
	private static ESLogger logger=Loggers.getLogger("ESUtils.class");
	private static TransportClient client = null;

	public synchronized static TransportClient getEsClient() {
		if (null == client) {
			String name =ConfigUtils.get(Constants.ES_CLUSTER_NAME);
			
			String address = ConfigUtils.get(Constants.ES_CLUSTER_HOST);
			client = getNewClient(name, address);
		}
		return client;
	}

	public static TransportClient getNewClient(String name, String address) {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", name).build();
		client = new TransportClient(settings);
		if (address != null) {
			String[] arr = address.split(",");
			for (String tmp : arr) {
				String[] tmpArr = tmp.split(":");
				if (tmpArr.length >= 2) {
					String host = tmpArr[0];
					String portStr = tmpArr[1];
					if (host != null && host.length() != 0 && portStr != null
							&& portStr.length() != 0) {
						try {
							int port = Integer.parseInt(portStr);
							client.addTransportAddress(new InetSocketTransportAddress(
									host, port));
						} catch (Exception e) {
							logger.info("es client add host error,host" + tmp, e);
						}
					}
				}
			}
		}
		if (address != null) {
				String[] tmpArr = address.split(":");
				if (tmpArr.length >= 2) {
					String host = tmpArr[0];
					String portStr = tmpArr[1];
					if (host != null && host.length() != 0 && portStr != null
							&& portStr.length() != 0) {
						try {
							int port = Integer.parseInt(portStr);
							client.addTransportAddress(new InetSocketTransportAddress(
									"192.168.1.45", 9300));
						} catch (Exception e) {
							
						}
					}
				}
			
		}
		return client;
	}

	public synchronized static void closeEsClient(TransportClient client) {
		if (null != client) {
			client.close();
		}
	}

	

}
