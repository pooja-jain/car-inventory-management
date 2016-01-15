package com.accenture.cim.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.accenture.com.exception.CimRuntimeException;

public class CSVFileReader implements Runnable {

	private final String fileName;

	private volatile List<String[]> fileContent;

	public CSVFileReader(String fileName) {
		super();
		this.fileName = fileName;
	}

	public <Y> Y readCSV(Function<List<String[]>, Y> mapper) throws Exception {

		if (fileContent != null) {
			return mapper.apply(fileContent);
		}
		return null;
	}

	@Override
	public void run() {
		fileContent = new ArrayList<String[]>();
		String splitBy = ",";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] b = line.split(splitBy);
				fileContent.add(b);
			}
			fileContent.remove(0);
			br.close();
		} catch (IOException e) {
			throw new CimRuntimeException("Error while reading file : " + fileName);
		}

	}

}
