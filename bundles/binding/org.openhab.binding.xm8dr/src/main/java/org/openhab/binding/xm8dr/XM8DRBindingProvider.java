package org.openhab.binding.xm8dr;

import org.openhab.core.binding.BindingProvider;

public interface XM8DRBindingProvider extends BindingProvider {

	String getUID(String itemName);

	Integer getOut(String itemName);
}
