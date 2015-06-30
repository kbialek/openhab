package org.openhab.binding.ilight.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.openhab.binding.ilight.ILightBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ILightBinding extends AbstractActiveBinding<ILightBindingProvider> implements ManagedService {

	private static final Logger logger = LoggerFactory.getLogger(ILightBinding.class);

	private final Map<String, InetAddress> hostMap = new HashMap<String, InetAddress>();

	private Executor executor = Executors.newSingleThreadExecutor();

	@Override
	protected void execute() {

	}

	private String findHostByAddress(InetAddress addr) {
		for (Map.Entry<String, InetAddress> kv : hostMap.entrySet()) {
			if (kv.getValue().equals(addr)) {
				return kv.getKey();
			}
		}
		return null;
	}

	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		for (ILightBindingProvider provider : providers) {
			String uid = provider.getUID(itemName);
			InetAddress host = hostMap.get(uid);
			logger.info("Host: " + host);
			try {
				byte[] request = ILightRequestFactory.createSetState(itemName, provider, command);
				if (request != null) {
					send(host, request);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private byte[] send(InetAddress host, byte[] requestData) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, host, 9999);
		byte[] responseData = new byte[128];
		DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
		socket.send(requestPacket);
		socket.receive(responsePacket);
		byte[] result = responsePacket.getData();
		socket.close();
		return result;
	}

	@Override
	protected long getRefreshInterval() {
		return 1000;
	}

	@Override
	protected String getName() {
		return "ILight Binding";
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		try {
			if (properties != null) {
				Enumeration<String> keys = properties.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					String[] parts = key.split("\\.");
					if (parts.length == 2 && "host".equals(parts[1])) {
						InetAddress host = Inet4Address.getByName((String) properties.get(key));
						hostMap.put(parts[0], host);
					} else if ("notificationPort".equals(key)) {
						Integer port = Integer.valueOf((String) properties.get(key));
						executor.execute(new NotificationServer(port));
						logger.info("ILight binding notification port configured. Listening on " + port);
					}
				}

				setProperlyConfigured(true);
			}
		} catch (Exception e) {
			throw new ConfigurationException("", "", e);
		}
	}

	// @Override
	// public void addBindingProvider(ILightBindingProvider provider) {
	// super.addBindingProvider(provider);
	// }
	//
	private class NotificationServer implements Runnable {

		private DatagramSocket notificationSocket;

		public NotificationServer(int port) throws ConfigurationException {
			try {
				notificationSocket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
			} catch (SocketException | UnknownHostException e) {
				throw new ConfigurationException("notificationPort", "", e);
			}
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				byte[] buf = new byte[128];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					notificationSocket.receive(packet);
					processNotifications(packet.getAddress(), buf);
				} catch (IOException e) {
				}
			}
		}

		private void processNotifications(InetAddress addr, byte[] receiveData) {
			if (addr != null) {
				String uid = ILightBinding.this.findHostByAddress(addr);

				ByteArrayInputStream is = new ByteArrayInputStream(receiveData);

				for (;;) {
					int code = is.read();
					// Triac Output changed
					if (code == 1) {
						int out = is.read();
						boolean state = is.read() != 0;
						for (ILightBindingProvider provider : providers) {
							for (String itemName : provider.getItemNames()) {
								if (provider.getType(itemName) == ILightBindingType.Triac
										&& provider.getUID(itemName).equals(uid) && out == provider.getOut(itemName)) {
									eventPublisher.postUpdate(itemName, state ? OnOffType.ON : OnOffType.OFF);
								}
							}
						}
					} else if (code == 2) {
						// LED Output changed
						boolean state = is.read() != 0;
						for (ILightBindingProvider provider : providers) {
							for (String itemName : provider.getItemNames()) {
								if (provider.getType(itemName) == ILightBindingType.Led
										&& provider.getUID(itemName).equals(uid)) {
									eventPublisher.postUpdate(itemName, state ? OnOffType.ON : OnOffType.OFF);
								}
							}
						}
					} else {
						break;
					}
				}
			}
		}
	}

}
