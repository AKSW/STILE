package org.aksw.mlbenchmark.config;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Convert property files to hierarchical config layout (like HierarchicalConfigurationConverter)
 */
public class FlatConfigHierarchicalConverter {
	static void process(Configuration in, HierarchicalConfiguration<ImmutableNode> out) {
		ExpressionEngine ee = out.getExpressionEngine();
		out.setExpressionEngine(DefaultExpressionEngine.INSTANCE);
		Iterator<String> keys = in.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			out.addProperty(key, in.getProperty(key));
		}
		out.setExpressionEngine(ee);
	}

	public static PropertyListConfiguration convert(Configuration config) {
		PropertiesConfigurationFromDotkeys temp = new PropertiesConfigurationFromDotkeys();
		temp.copy(config);
		PropertyListConfiguration ret = new PropertyListConfiguration();
		process(temp, ret);
		return ret;
	}

	public static void main(String[] args) throws ConfigLoaderException, IOException, ConfigurationException {
		HierarchicalConfiguration<ImmutableNode> config = new ConfigLoader("test.ini").load().config();
		PropertyListConfiguration c1 = new PropertyListConfiguration(config);
		PropertyListConfiguration c2 = new PropertyListConfiguration();
		process(config, c2);
		PropertyListConfiguration c3 = convert(config);
		c1.write(new FileWriter("c1.plist"));
		c2.write(new FileWriter("c2.plist"));
		c3.write(new FileWriter("c3.plist"));
		//new INIConfigurationWriteDotkeys(c3).write(new FileWriter("c3.ini"));
		new INIConfiguration(c3).write(new FileWriter("c3b.ini"));
	}
}
