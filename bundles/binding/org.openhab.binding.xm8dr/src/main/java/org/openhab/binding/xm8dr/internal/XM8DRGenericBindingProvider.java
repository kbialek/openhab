package org.openhab.binding.xm8dr.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.openhab.binding.xm8dr.XM8DRBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XM8DRGenericBindingProvider extends
		AbstractGenericBindingProvider implements XM8DRBindingProvider {

	static final Logger logger = 
			LoggerFactory.getLogger(XM8DRGenericBindingProvider.class);

	
	@Override
	public String getBindingType() {
		return "xm8dr";
	}

	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		// accept all
	}
	
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);

		String multiLineProps = bindingConfig.replaceAll(",", "\n");
		Properties props = new Properties();
		try {
			props.load(new StringReader(multiLineProps));
		} catch (IOException e) {
			throw new BindingConfigParseException(e.getMessage());
		}
		
		XM8DRBindingConfig config = new XM8DRBindingConfig();
		config.uid = props.getProperty("uid");
		config.out = Integer.valueOf(props.getProperty("out"));
				
		addBindingConfig(item, config);
	}

	static private class XM8DRBindingConfig implements BindingConfig {
		public String uid;
		public int out;
	}

	@Override
	public String getUID(String itemName) {
		XM8DRBindingConfig config = (XM8DRBindingConfig) this.bindingConfigs.get(itemName);
		return config.uid;
	}
	
	@Override
	public Integer getOut(String itemName) {
		XM8DRBindingConfig config = (XM8DRBindingConfig) this.bindingConfigs.get(itemName);
		return config.out;
	}
	
}
