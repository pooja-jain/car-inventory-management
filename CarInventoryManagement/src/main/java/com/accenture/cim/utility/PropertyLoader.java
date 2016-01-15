package com.accenture.cim.utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.accenture.com.exception.CimRuntimeException;

public class PropertyLoader {

	public Properties loadProperties() {
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

		} catch (IOException e) {
			throw new CimRuntimeException("Error while loading property file : ");
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				throw new CimRuntimeException("Error while loading property file");
			}
		}
		return prop;
	}

}
