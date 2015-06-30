package org.openhab.binding.ilight.internal;

import org.openhab.binding.ilight.ILightBindingProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

public class ILightRequestFactory {

	public static byte[] createSetState(String itemName, ILightBindingProvider provider, Command command) {
		ILightBindingType type = provider.getType(itemName);
		if (type == ILightBindingType.Triac) {
			Integer out = provider.getOut(itemName);
			return new byte[] { (byte) 1, out.byteValue(), commandToByte(command) };
		} else if (type == ILightBindingType.Led) {
			return new byte[] { (byte) 7, commandToByte(command) };
		} else {
			return null;
		}
	}
	
	private static byte commandToByte(Command command) {
		byte state = 0;
		if (command == OnOffType.ON) {
			state = 1;
		} else if (command == OnOffType.OFF) {
			state = 0;
		}
		return state;
	}
}
