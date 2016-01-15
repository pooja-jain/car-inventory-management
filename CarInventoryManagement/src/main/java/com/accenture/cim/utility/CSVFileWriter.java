package com.accenture.cim.utility;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.accenture.cim.model.SuperWriterModel;
import com.accenture.com.exception.CimRuntimeException;

public class CSVFileWriter implements Runnable {

	private final String fileName;

	private final String header;

	private final List<? extends SuperWriterModel> dataSet;

	public CSVFileWriter(String fileName, String header, List<? extends SuperWriterModel> dataSet) {
		super();
		this.fileName = fileName;
		this.header = header;
		this.dataSet = dataSet;
	}

	public void writeCSV(String fileName, String header, List<? extends SuperWriterModel> dataSet) throws Exception {
		FileWriter writer = new FileWriter(fileName);
		writer.append(header);
		writer.append("\n");
		dataSet.stream().forEach((SuperWriterModel inventoryElement) -> {
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

	@Override
	public void run() {
		FileWriter writer;
		try {
			writer = new FileWriter(fileName);

			writer.append(header);
			writer.append("\n");
			dataSet.stream().forEach((SuperWriterModel inventoryElement) -> {
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
		} catch (IOException e1) {
			throw new CimRuntimeException("Error while writing file : " + fileName);

		}

	}

}
