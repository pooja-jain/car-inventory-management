package com.accenture.cim.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.accenture.cim.model.SuperInventoryModel;

public class CSVFileOperations {

	public <Y> Y readCSV(String fileName, Function<List<String[]>, Y> mapper) throws Exception {
		List<String[]> fileContent = read(fileName);
		if (fileContent != null) {
			return mapper.apply(fileContent);
		}
		return null;
	}

	public void writeCSV(String fileName, String header, List<? extends SuperInventoryModel> dataSet) throws Exception {
		FileWriter writer = new FileWriter(fileName);
		writer.append(header);
		writer.append("\n");
		dataSet.stream().forEach((SuperInventoryModel inventoryElement) -> {
			try {
				writer.append(inventoryElement.getCommaSeparatedValue());
				writer.append("\n");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		writer.flush();
		writer.close();
	}

	private List<String[]> read(String fileName) throws Exception {
		List<String[]> fileContent = new ArrayList<String[]>();
		String splitBy = ",";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] b = line.split(splitBy);
			fileContent.add(b);
		}
		fileContent.remove(0);
		br.close();
		return fileContent;
	}

}
