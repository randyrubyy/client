package sample.agent;

import java.io.File;
import java.net.URL;

import rescuecore2.config.Config;

/**
 * @author Knight
 */
public class Configuration {

	public static final String DEFAULT_CONFIG_FILE = "seu.cfg";

	public Config getSEUConfig() {
		URL configUrl;
		File file;
		Config config = null;

		try {
			configUrl = getClass().getResource(DEFAULT_CONFIG_FILE);
			file = new File(configUrl.toURI());
			config = new Config(file);
			return config;
		} catch (Exception ex) {
			// LOG.error("Couldn't read SEU config file.");
			// LOG.debug(null, ex);
		}
		return config;
	}

	/**
	 * Returns the merged configuration from simulator and seu In case of
	 * duplicated entries simulator configuration is preferred
	 * 
	 * @param simulatorConfig
	 *            configuration that comes from the simulator
	 * @return
	 */
	public Config getMergedConfig(Config simulatorConfig) {

		Config config;

		config = getSEUConfig();

		if (config == null) {
			config = simulatorConfig;
		} else {
			config.merge(simulatorConfig);
		}
		return config;
	}
}
