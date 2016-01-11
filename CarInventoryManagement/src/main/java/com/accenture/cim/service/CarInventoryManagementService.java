package com.accenture.cim.service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.accenture.cim.model.AccessoryInventory;
import com.accenture.cim.model.CarInventory;
import com.accenture.cim.model.CarOrder;
import com.accenture.cim.utility.CSVFileOperations;
import com.accenture.cim.utility.PropertyLoader;

public class CarInventoryManagementService {

	public static void main(String args[]) {
		PropertyLoader propertyLoader = new PropertyLoader();
		Properties properties;
		try {
			properties = propertyLoader.loadProperties();

			CarInventoryManagementService carInventoryManagementService = new CarInventoryManagementService();

			CSVFileOperations cSVFileOperations = new CSVFileOperations();

			List<CarOrder> carOrderList = carInventoryManagementService.getCarOrders(properties.getProperty("file.location.carOrders"),
					cSVFileOperations);
			List<CarInventory> carInventoryList = carInventoryManagementService.getCarInventory(
					properties.getProperty("file.location.carInventory"), cSVFileOperations);
			List<AccessoryInventory> accessoryInventoryList = carInventoryManagementService.getAccessoryInventory(
					properties.getProperty("file.location.accessoryInventory"), cSVFileOperations);

			System.out.println(carOrderList);
			System.out.println(carInventoryList);
			System.out.println(accessoryInventoryList);
			cSVFileOperations.writeCSV(properties.getProperty("file.location.carInventory"), CarInventory.FILE_HEADER, carInventoryList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<CarOrder> getCarOrders(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

		return cSVFileOperations.readCSV(fileName, fileContent -> {
			return fileContent.stream().map(line -> {
				CarOrder car = new CarOrder();
				car.setCustomerName(line[0]);
				car.setRegion(line[1]);
				car.setVendor(line[2]);
				car.setModel(line[3]);
				car.setVariant(line[4]);
				car.setColor(line[5]);
				car.setAccessories(line[6]);
				car.setMotorInsurance(line[7]);
				car.setPersonalProtectPlan(line[8]);
				String[] accessories = car.getAccessories().split(":");
				car.setAccesoryList(Arrays.asList(accessories));
				return car;
			}).collect(Collectors.toList());
		});
	}

	public List<CarInventory> getCarInventory(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

		return cSVFileOperations.readCSV(fileName, fileContent -> {
			return fileContent.stream().map(line -> {
				CarInventory carInventory = new CarInventory();
				carInventory.setVendor(line[0]);
				carInventory.setModel(line[1]);
				carInventory.setVariant(line[2]);
				carInventory.setColor(line[3]);
				carInventory.setBasePrice(Float.valueOf(line[4]));
				carInventory.setQuantityAvailable(Integer.valueOf(line[5]));
				return carInventory;
			}).collect(Collectors.toList());
		});
	}

	public List<AccessoryInventory> getAccessoryInventory(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

		return cSVFileOperations.readCSV(fileName, fileContent -> {
			return fileContent.stream().map(line -> {
				AccessoryInventory accessoryInventory = new AccessoryInventory();
				accessoryInventory.setVendor(line[0]);
				accessoryInventory.setModel(line[1]);
				accessoryInventory.setAccessories(line[2]);
				accessoryInventory.setPrice(Float.valueOf(line[3]));
				accessoryInventory.setQuantityAvailable(Integer.valueOf(line[4]));
				return accessoryInventory;
			}).collect(Collectors.toList());
		});
	}

	public boolean checkOrderAndCarInventory(List<CarOrder> orderl, List<CarInventory> carInventoryl,
			List<AccessoryInventory> accessoryInventoryList) {
		for (CarOrder order : orderl) {
			CarInventory car = new CarInventory();
			car.setColor(order.getColor());
			car.setModel(order.getModel());
			car.setVariant(order.getVariant());
			car.setVendor(order.getVendor());
			for (CarInventory carInventory : carInventoryl) {
				int carQuantity = carInventory.getQuantityAvailable();
				if (car.equals(carInventory) && carQuantity > 0) {
					AccessoryInventory accessor = new AccessoryInventory();
					accessor.setModel(order.getModel());
					accessor.setVendor(order.getVendor());
					for (String accer : order.getAccesoryList()) {
						accessor.setAccessories(accer.replaceAll("\\s", ""));
						for (AccessoryInventory accessoryInventory : accessoryInventoryList) {
							int accessoryQuantity = accessoryInventory.getQuantityAvailable();
							if (accessor.equals(accessoryInventory) && accessoryQuantity > 0) {
								order.setCarAvailable(true);
								carInventory.setQuantityAvailable(carQuantity - 1);
								accessoryInventory.setQuantityAvailable(accessoryQuantity);
								break;
							}
						}
					}

				}
			}
		}

		return false;
	}

}
