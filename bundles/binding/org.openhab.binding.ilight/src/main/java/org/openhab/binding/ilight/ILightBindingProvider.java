package org.openhab.binding.ilight;

import org.openhab.core.binding.BindingProvider;

public interface ILightBindingProvider extends BindingProvider {

	String getUID(String itemName);

	Integer getOut(String itemName);
}
