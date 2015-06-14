/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.RXTXCommDriver;
import gnu.io.SerialPort;

import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents Satel INT-RS module. Implements method required to connect and
 * communicate with that module over serial protocol.
 * 
 * @author Krzysztof Goworek
 * @author Krzysztof Białek
 * @since 1.7.0
 */
public class IntRSModule extends SatelModule {
	private static final Logger logger = LoggerFactory
			.getLogger(Ethm1Module.class);

	private final SerialCommunicationChannel communicationChannel = new SerialCommunicationChannel();

	private final String portName;
	private SerialPort port;

	/**
	 * Creates new instance with port and timeout set to specified values.
	 * 
	 * @param port
	 *            serial port the module is connected to
	 * @param timeout
	 *            timeout value in milliseconds for connect/read/write
	 *            operations
	 * @throws ConfigurationException
	 *             unconditionally throws this exception as it is not
	 *             implemented yet
	 */
	public IntRSModule(String port, int timeout) throws ConfigurationException {
		super(timeout);
		this.portName = port;
	}

	@Override
	protected CommunicationChannel connect() {
		try {
			RXTXCommDriver rxtx = new RXTXCommDriver();
			rxtx.initialize();

			this.port = (SerialPort) rxtx.getCommPort(portName,	CommPortIdentifier.PORT_SERIAL);
			this.port.setSerialPortParams(19200, SerialPort.DATABITS_8,	SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			return communicationChannel;
		} catch (Exception e) {
			logger.error("Unable to open serial port", e);
			return null;
		}
	}

	private class SerialCommunicationChannel implements CommunicationChannel {

		@Override
		public InputStream getInputStream() throws IOException {
			return port.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return port.getOutputStream();
		}

		@Override
		public void disconnect() {
			port.close();
		}

	}
}
