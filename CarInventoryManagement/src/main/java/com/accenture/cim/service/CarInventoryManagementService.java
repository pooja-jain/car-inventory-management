package com.accenture.cim.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.accenture.cim.model.AccessoryInventory;
import com.accenture.cim.model.CarInventory;
import com.accenture.cim.model.CarOrder;
import com.accenture.cim.model.MotorInsuranceProviderInventory;
import com.accenture.cim.model.RegionalTaxRateConfiguration;
import com.accenture.cim.utility.CSVFileOperations;
import com.accenture.cim.utility.PropertyLoader;

public class CarInventoryManagementService {

	public final static String ERROR_MSG_CAR_INVENTORY_UNAVAILABLE = "Car Inventory unavailable";

	public final static String ERROR_MSG_ACCESSORY_INVENTORY = "Mismatch with Accesories Inventory Available";

	public final static String ERROR_MSG_CAR_INVENTORY_INVALID = "Mismatch with Car Inventory Available";

	public final static String ERROR_MSG_REGION_INVALID = "Region not present";

	public final static String ERROR_MSG_INSURANCE_INVALID = "Invalid Insurance Provider";

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
			List<MotorInsuranceProviderInventory> motorInsuranceProviderInventoryList = carInventoryManagementService
					.getMotorInsuranceProviderInventory(properties.getProperty("file.location.motorInsuranceProviderInventory"),
							cSVFileOperations);
			List<RegionalTaxRateConfiguration> regionalTaxRateConfigurationList = carInventoryManagementService
					.getRegionalTaxRateConfiguration(properties.getProperty("file.location.regionalTaxRateConfiguration"),
							cSVFileOperations);

			System.out.println(carOrderList);
			System.out.println(carInventoryList);
			System.out.println(accessoryInventoryList);

			Map<String, CarInventory> carInventoryMap = carInventoryList.stream().collect(
					Collectors.toConcurrentMap(CarInventory::getKey, (CarInventory c) -> {
						return c;
					}));

			Map<String, AccessoryInventory> accessoryInventoryMap = accessoryInventoryList.stream().collect(
					Collectors.toConcurrentMap(AccessoryInventory::getKey, (AccessoryInventory a) -> {
						return a;
					}));

			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap = motorInsuranceProviderInventoryList.stream()
					.collect(
							Collectors.toConcurrentMap(MotorInsuranceProviderInventory::getMotorInsuranceProvider, (
									MotorInsuranceProviderInventory a) -> {
								return a;
							}));

			Map<String, Integer> regionalTaxRateConfigurationMap = regionalTaxRateConfigurationList.stream().collect(
					Collectors.toConcurrentMap(RegionalTaxRateConfiguration::getState, RegionalTaxRateConfiguration::getTaxRate));
			// System.out.println(carInventoryMap);
			// System.out.println(accessoryInventoryMap);
			List<CarOrder> validCarOrderList = carOrderList
					.stream()
					.filter(c -> carInventoryManagementService.validateOrderAgainstAccessoryInventory(c, accessoryInventoryMap)
							&& carInventoryManagementService.validateOrderAgainstCarInventory(c, carInventoryMap)
							&& carInventoryManagementService.validateMotorInsuranceProviderInventory(c, motorInsuranceProviderInventoryMap)
							&& carInventoryManagementService.validateRegionalTaxRateConfiguration(c, regionalTaxRateConfigurationMap))
					.map(c -> {
						c.setCarAvailable(true);
						return c;
					}).collect(Collectors.toList());
			// TODO : decrement car and accessory inventory quantity
			List<CarOrder> invalidValidCarOrderList = carOrderList.stream().filter((CarOrder c) -> {
				return !validCarOrderList.contains(c);
			}).collect(Collectors.toList());
			System.out.println("total orders: " + carOrderList.size());
			System.out.println("valid orders: " + validCarOrderList.size());
			System.out.println(validCarOrderList);
			System.out.println("invalid orders: " + invalidValidCarOrderList.size());
			// System.out.println(invalidValidCarOrderList);
			invalidValidCarOrderList.stream().forEach(c -> System.out.println(c));
			// cSVFileOperations.writeCSV(properties.getProperty("file.location.carInventory"),
			// CarInventory.FILE_HEADER, carInventoryList);
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
				if (!StringUtils.isBlank(car.getAccessories())) {
					// List<String> accessories = Arrays.asList(car.getAccessories().split(":"));
					// car.setAccesoryList(accessories.stream().map(s -> s.replaceAll("^\\s+",
					// "")).collect(Collectors.toList()));
					car.setAccesoryList(Arrays.asList(car.getAccessories().split(":")));
				}

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

	public List<MotorInsuranceProviderInventory> getMotorInsuranceProviderInventory(String fileName, CSVFileOperations cSVFileOperations)
			throws Exception {

		return cSVFileOperations.readCSV(fileName, fileContent -> {
			return fileContent.stream().map(line -> {
				MotorInsuranceProviderInventory motorInsuranceProviderInventory = new MotorInsuranceProviderInventory();
				motorInsuranceProviderInventory.setFirstYearPremium(line[2]);
				motorInsuranceProviderInventory.setMotorInsuranceProvider(line[0]);
				motorInsuranceProviderInventory.setPersonalProtectPlanOffered(line[1]);
				return motorInsuranceProviderInventory;
			}).collect(Collectors.toList());
		});
	}

	public List<RegionalTaxRateConfiguration> getRegionalTaxRateConfiguration(String fileName, CSVFileOperations cSVFileOperations)
			throws Exception {

		return cSVFileOperations.readCSV(fileName, fileContent -> {
			return fileContent.stream().map(line -> {
				RegionalTaxRateConfiguration regionalTaxRateConfiguration = new RegionalTaxRateConfiguration();
				regionalTaxRateConfiguration.setState(line[0]);
				regionalTaxRateConfiguration.setTaxRate(Integer.valueOf(line[1]));
				return regionalTaxRateConfiguration;
			}).collect(Collectors.toList());
		});
	}

	private boolean validateOrderAgainstCarInventory(CarOrder carOrder, Map<String, CarInventory> carInventoryMap) {

		if (carInventoryMap.containsKey(createCarInventoryKey(carOrder))) {
			if (carInventoryMap.get(createCarInventoryKey(carOrder)).getQuantityAvailable() > 0) {
				return true;
			}
			carOrder.setErrorMsg(ERROR_MSG_CAR_INVENTORY_UNAVAILABLE);
		} else {
			carOrder.setErrorMsg(ERROR_MSG_CAR_INVENTORY_INVALID);
		}

		return false;
	}

	private boolean validateOrderAgainstAccessoryInventory(CarOrder carOrder, Map<String, AccessoryInventory> accessoryInventoryMap) {

		List<String> keyList = createAccessoryInventoryKey(carOrder);
		if (keyList == null
				|| keyList.stream().allMatch(
						k -> accessoryInventoryMap.containsKey(k) && accessoryInventoryMap.get(k).getQuantityAvailable() > 0)) {
			return true;
		}
		carOrder.setErrorMsg(ERROR_MSG_ACCESSORY_INVENTORY);
		return false;
	}

	private boolean validateMotorInsuranceProviderInventory(CarOrder carOrder,
			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap) {

		if (motorInsuranceProviderInventoryMap.containsKey(carOrder.getMotorInsurance())
				|| carOrder.getMotorInsurance().equalsIgnoreCase("NA")) {
			return true;
		}
		carOrder.setErrorMsg(ERROR_MSG_INSURANCE_INVALID);
		return false;
	}

	private boolean validateRegionalTaxRateConfiguration(CarOrder carOrder, Map<String, Integer> regionalTaxRateConfigurationMap) {
		if (regionalTaxRateConfigurationMap.containsKey(carOrder.getRegion())) {
			return true;
		}
		carOrder.setErrorMsg(ERROR_MSG_REGION_INVALID);
		return false;
	}

	private String createCarInventoryKey(CarOrder carOrder) {

		return carOrder.getVendor() + "-" + carOrder.getModel() + "-" + carOrder.getVariant() + "-" + carOrder.getColor();

	}

	private List<String> createAccessoryInventoryKey(CarOrder carOrder) {

		if (!StringUtils.isBlank(carOrder.getAccessories())) {
			List<String> keyList = new ArrayList<String>();
			for (String accessory : carOrder.getAccesoryList()) {
				keyList.add(carOrder.getVendor() + "-" + carOrder.getModel() + "-" + accessory.replaceAll("^\\s+", ""));
			}
			return keyList;
		}

		return null;
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
