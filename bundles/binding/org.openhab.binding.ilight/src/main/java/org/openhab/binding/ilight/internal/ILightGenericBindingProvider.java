package org.openhab.binding.ilight.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.openhab.binding.ilight.ILightBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ILightGenericBindingProvider extends AbstractGenericBindingProvider implements ILightBindingProvider {

	static final Logger logger = LoggerFactory.getLogger(ILightGenericBindingProvider.class);

	@Override
	public String getBindingType() {
		return "ilight";
	}

	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		// accept all
	}

	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig)
			throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);

		String multiLineProps = bindingConfig.replaceAll(",", "\n");
		Properties props = new Properties();
		try {
			props.load(new StringReader(multiLineProps));
		} catch (IOException e) {
			throw new BindingConfigParseException(e.getMessage());
		}

		ILightBindingConfig config = new ILightBindingConfig();
		config.type = ILightBindingType.from(props.getProperty("type"));
		config.uid = props.getProperty("uid");
		String outString = props.getProperty("out");
		config.out = outString != null ? Integer.valueOf(outString) : -1;

		addBindingConfig(item, config);
	}

	static private class ILightBindingConfig implements BindingConfig {
		public ILightBindingType type;
		public String uid;
		public int out;
	}

	@Override
	public ILightBindingType getType(String itemName) {
		ILightBindingConfig config = (ILightBindingConfig) this.bindingConfigs.get(itemName);
		return config.type;
	}

	@Override
	public String getUID(String itemName) {
		ILightBindingConfig config = (ILightBindingConfig) this.bindingConfigs.get(itemName);
		return config.uid;
	}

	@Override
	public Integer getOut(String itemName) {
		ILightBindingConfig config = (ILightBindingConfig) this.bindingConfigs.get(itemName);
		return config.out;
	}

}
