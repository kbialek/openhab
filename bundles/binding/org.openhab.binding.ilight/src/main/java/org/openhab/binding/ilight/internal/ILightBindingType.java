package org.openhab.binding.ilight.internal;

public enum ILightBindingType {

	Triac("triac"), Led("led");

	private final String configName;

	private ILightBindingType(String name) {
		this.configName = name;
	}

	public static ILightBindingType from(String s) {
		for (ILightBindingType v : values()) {
			if (v.configName.equals(s)) {
				return v;
			}
		}
		return Triac;
	}
}
