package org.openhab.binding.ilight.internal;

import org.openhab.binding.ilight.ILightBindingProvider;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

public class ILightRequestFactory {

	public static byte[] createSetState(String itemName, ILightBindingProvider provider, Command command) {
		ILightBindingType type = provider.getType(itemName);
		if (type == ILightBindingType.Triac) {
			Integer out = provider.getOut(itemName);
			return new byte[] { (byte) 1, out.byteValue(), commandToByte(command) };
		} else if (type == ILightBindingType.Led && command instanceof OnOffType) {
			return new byte[] { (byte) 7, commandToByte(command) };
		} else if (type == ILightBindingType.Led && command instanceof HSBType) {
			HSBType hsb = (HSBType) command;
			byte r = (byte) (hsb.getRed().intValue() / 10);
			byte g = (byte) (hsb.getGreen().intValue() / 10);
			byte b = (byte) (hsb.getBlue().intValue() / 10);
			return new byte[] { (byte) 9, r, g, b };
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
