package com.accenture.cim.utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

	public Properties loadProperties() throws IOException {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			String filename = "config.properties";
			input = getClass().getClassLoader().getResourceAsStream(filename);
			if (input != null) {
				prop.load(input);
			} else {
				throw new FileNotFoundException("property file '" + filename + "' not found in the classpath");
			}

		} finally {
			input.close();
		}
		return prop;
	}

}
