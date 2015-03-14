package org.openhab.binding.xm8dr.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xm8dr.XM8DRBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XM8DRBinding extends AbstractActiveBinding<XM8DRBindingProvider> implements ManagedService {

	private static final Logger logger = LoggerFactory.getLogger(XM8DRBinding.class);   
	
	private final Map<String, InetAddress> hostMap = new HashMap<String, InetAddress>();
	
	@Override
	protected void execute() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		for (XM8DRBindingProvider provider : providers) {
			String uid = provider.getUID(itemName);
			InetAddress host = hostMap.get(uid);
			Integer out = provider.getOut(itemName);
			logger.info("Host: " + host);
			try {
				byte state = 0;
				if (command == OnOffType.ON) {
					state = 1;
				} else if (command == OnOffType.OFF) {
					state = 0;
				}

				send(host, new byte[] {1, out.byteValue(), state});
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
		return "XM8DR Binding";
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		try {
			if (properties != null) {
				Enumeration<String> keys = properties.keys();
				while(keys.hasMoreElements()) {
					String key = keys.nextElement();
					String[] parts = key.split("\\.");
					if ("host".equals(parts[1])) {
						InetAddress host = Inet4Address.getByName((String) properties.get(key));
						hostMap.put(parts[0], host);		
					}
				}
				
				setProperlyConfigured(true);
			}
		} catch (Exception e) {
			throw new ConfigurationException("", "");
		}
	}
	
}
