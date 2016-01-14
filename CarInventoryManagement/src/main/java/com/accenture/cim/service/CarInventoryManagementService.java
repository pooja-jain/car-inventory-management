package com.accenture.cim.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.accenture.cim.model.AccessoryInventory;
import com.accenture.cim.model.CarInventory;
import com.accenture.cim.model.CarOrder;
import com.accenture.cim.model.CarOrderError;
import com.accenture.cim.model.MotorInsuranceProviderInventory;
import com.accenture.cim.model.RegionalEstimatedSales;
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
			Map<String, CarInventory> carInventoryMap = carInventoryManagementService.getCarInventory(
					properties.getProperty("file.location.carInventory"), cSVFileOperations);
			Map<String, AccessoryInventory> accessoryInventoryMap = carInventoryManagementService.getAccessoryInventory(
					properties.getProperty("file.location.accessoryInventory"), cSVFileOperations);
			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap = carInventoryManagementService
					.getMotorInsuranceProviderInventory(properties.getProperty("file.location.motorInsuranceProviderInventory"),
							cSVFileOperations);
			Map<String, Float> regionalTaxRateConfigurationMap = carInventoryManagementService.getRegionalTaxRateConfiguration(
					properties.getProperty("file.location.regionalTaxRateConfiguration"), cSVFileOperations);

			// validate Car orders against various inventories
			List<CarOrder> validCarOrderList = carInventoryManagementService.getValidCarOrders(carOrderList, carInventoryMap,
					accessoryInventoryMap, motorInsuranceProviderInventoryMap, regionalTaxRateConfigurationMap);

			List<CarOrder> invalidCarOrderList = carOrderList.stream().filter((CarOrder c) -> {
				return !validCarOrderList.contains(c);
			}).collect(Collectors.toList());

			// calculate Tax Expense, premium for each car order
			carInventoryManagementService.calculateCostAndTaxExpense(validCarOrderList, carInventoryMap, accessoryInventoryMap,
					regionalTaxRateConfigurationMap, motorInsuranceProviderInventoryMap);

			// RegionalEstimatedSalesRepor population
			List<RegionalEstimatedSales> regionalEstimatedSalesList = carInventoryManagementService.generateRegionalEstimatedSalesReport(
					validCarOrderList, carInventoryMap, regionalTaxRateConfigurationMap);

			List<CarInventory> carInventoryList = new ArrayList<CarInventory>();
			carInventoryList.addAll(carInventoryMap.values());
			List<AccessoryInventory> accessoryInventoryList = new ArrayList<AccessoryInventory>();
			accessoryInventoryList.addAll(accessoryInventoryMap.values());
			// generate CSV reports
			carInventoryManagementService.createCSVReport(carInventoryList, accessoryInventoryList, invalidCarOrderList,
					regionalEstimatedSalesList, properties);

			System.out.println("total orders: " + carOrderList.size());
			System.out.println("valid orders: " + validCarOrderList.size());
			validCarOrderList.stream().forEach(
					c -> {
						System.out.println("customerName=" + c.getCustomerName() + ", region=" + c.getRegion() + ", vendor="
								+ c.getVendor() + ", model=" + c.getModel() + ", variant=" + c.getVariant() + ", totalPrice="
								+ c.getTotalPrice());
					});
			System.out.println("invalid orders: " + invalidCarOrderList.size());
			invalidCarOrderList.stream().forEach(c -> System.out.println(c));
			System.out.println("**************************************");
			regionalEstimatedSalesList.stream().forEach(c -> System.out.println(c));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<CarOrder> getValidCarOrders(List<CarOrder> carOrderList, Map<String, CarInventory> carInventoryMap,
			Map<String, AccessoryInventory> accessoryInventoryMap,
			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap,
			Map<String, Float> regionalTaxRateConfigurationMap) {

		return carOrderList
				.stream()
				.filter(c -> validateCarOrder(c, carInventoryMap, accessoryInventoryMap, motorInsuranceProviderInventoryMap,
						regionalTaxRateConfigurationMap) && decrementQuantityAvailable(c, accessoryInventoryMap, carInventoryMap))
				.map(c -> {
					c.setCarAvailable(true);
					return c;
				}).collect(Collectors.toList());

	}

	private void calculateCostAndTaxExpense(List<CarOrder> validCarOrderList, Map<String, CarInventory> carInventoryMap,
			Map<String, AccessoryInventory> accessoryInventoryMap, Map<String, Float> regionalTaxRateConfigurationMap,
			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap) {

		validCarOrderList.stream().forEach(c -> {
			float basePrice = carInventoryMap.get(createCarInventoryKey(c)).getBasePrice();
			double accessoriesPrice = 0;
			List<String> keys = createAccessoryInventoryKey(c);
			if (keys != null) {
				accessoriesPrice = keys.stream().mapToDouble(key -> {
					return accessoryInventoryMap.get(key).getPrice();
				}).sum();
			}
			c.setTotalAccessoriesPrice((float) accessoriesPrice);
			float taxRate = regionalTaxRateConfigurationMap.get(c.getRegion()) / 100;
			c.setTaxExpense((basePrice + accessoriesPrice) * taxRate);
			c.setTotalPrice(basePrice + (float) accessoriesPrice + (float) c.getTaxExpense());
		});

		calculatePremiumAmt(validCarOrderList, motorInsuranceProviderInventoryMap);

	}

	private List<RegionalEstimatedSales> generateRegionalEstimatedSalesReport(List<CarOrder> validCarOrderList,
			Map<String, CarInventory> carInventoryMap, Map<String, Float> regionalTaxRateConfigurationMap) {

		Map<String, List<CarOrder>> carOrderByRegionMap = new HashMap<String, List<CarOrder>>();
		regionalTaxRateConfigurationMap.keySet().stream().forEach(region -> {
			List<CarOrder> carOdrList = validCarOrderList.stream().filter(c -> {
				return c.getRegion().equalsIgnoreCase(region);
			}).collect(Collectors.toList());
			carOrderByRegionMap.put(region, carOdrList);
		});

		return carOrderByRegionMap
				.entrySet()
				.stream()
				.map(entry -> {
					float totalEstimatedSales = (float) entry
							.getValue()
							.stream()
							.mapToDouble(
									c -> {
										return carInventoryMap.get(createCarInventoryKey(c)).getBasePrice() + c.getTotalAccessoriesPrice()
												+ c.getPremiumAmt() + (float) c.getTaxExpense();
									}).sum();

					float totalTaxExpense = (float) entry.getValue().stream().mapToDouble(c -> {
						return c.getTaxExpense();

					}).sum();
					RegionalEstimatedSales regionalEstimatedSales = new RegionalEstimatedSales();
					regionalEstimatedSales.setState(entry.getKey());
					regionalEstimatedSales.setTotalEstimatedSales(BigDecimal.valueOf(totalEstimatedSales));
					regionalEstimatedSales.setEstimatedSalesUnits(entry.getValue().size());
					regionalEstimatedSales.setEstimatedNetIncome(totalEstimatedSales - totalTaxExpense);
					return regionalEstimatedSales;
				}).collect(Collectors.toList());

	}

	private void createCSVReport(List<CarInventory> carInventoryList, List<AccessoryInventory> accessoryInventoryList,
			List<CarOrder> invalidCarOrderList, List<RegionalEstimatedSales> regionalEstimatedSalesList, Properties properties) {
		CSVFileOperations cSVFileOperations = new CSVFileOperations();
		List<CarOrderError> CarOrderErrorList = invalidCarOrderList.stream().map(i -> {
			CarOrderError c = new CarOrderError();
			c.setAccessories(i.getAccessories());
			c.setColor(i.getColor());
			c.setCustomerName(i.getCustomerName());
			c.setErrorMsg(i.getErrorMsg());
			c.setModel(i.getModel());
			c.setMotorInsurance(i.getMotorInsurance());
			c.setPersonalProtectPlan(i.getPersonalProtectPlan());
			c.setRegion(i.getRegion());
			c.setVariant(i.getVariant());
			c.setVendor(i.getVendor());
			return c;
		}).collect(Collectors.toList());
		try {
			cSVFileOperations.writeCSV(properties.getProperty("file.location.carInventory"), CarInventory.FILE_HEADER, carInventoryList);
			cSVFileOperations.writeCSV(properties.getProperty("file.location.accessoryInventory"), AccessoryInventory.FILE_HEADER,
					accessoryInventoryList);
			cSVFileOperations.writeCSV(properties.getProperty("file.location.carStandingOrdersErrors"), CarOrderError.FILE_HEADER,
					CarOrderErrorList);
			cSVFileOperations.writeCSV(properties.getProperty("file.location.regionalEstimatedSalesReport"),
					RegionalEstimatedSales.FILE_HEADER, regionalEstimatedSalesList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<CarOrder> getCarOrders(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

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
				if (line[8] != null && line[8].equalsIgnoreCase("yes")) {
					car.setPersonalProtectPlan(true);
				}
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

	private Map<String, CarInventory> getCarInventory(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

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
		}).stream().collect(Collectors.toConcurrentMap(CarInventory::getKey, (CarInventory c) -> {
			return c;
		}));
	}

	private Map<String, AccessoryInventory> getAccessoryInventory(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

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
		}).stream().collect(Collectors.toConcurrentMap(AccessoryInventory::getKey, (AccessoryInventory a) -> {
			return a;
		}));
	}

	private Map<String, MotorInsuranceProviderInventory> getMotorInsuranceProviderInventory(String fileName,
			CSVFileOperations cSVFileOperations) throws Exception {

		return cSVFileOperations
				.readCSV(fileName, fileContent -> {
					return fileContent.stream().map(line -> {
						MotorInsuranceProviderInventory motorInsuranceProviderInventory = new MotorInsuranceProviderInventory();
						motorInsuranceProviderInventory.setFirstYearPremium(line[2]);
						motorInsuranceProviderInventory.setMotorInsuranceProvider(line[0]);
						if (line[1] != null && line[1].equalsIgnoreCase("yes")) {
							motorInsuranceProviderInventory.setPersonalProtectPlanOffered(true);
						}
						return motorInsuranceProviderInventory;
					}).collect(Collectors.toList());
				})
				.stream()
				.collect(
						Collectors.toConcurrentMap(MotorInsuranceProviderInventory::getMotorInsuranceProvider, (
								MotorInsuranceProviderInventory a) -> {
							return a;
						}));
	}

	private Map<String, Float> getRegionalTaxRateConfiguration(String fileName, CSVFileOperations cSVFileOperations) throws Exception {

		return cSVFileOperations.readCSV(fileName, fileContent -> {
			return fileContent.stream().map(line -> {
				RegionalTaxRateConfiguration regionalTaxRateConfiguration = new RegionalTaxRateConfiguration();
				regionalTaxRateConfiguration.setState(line[0]);
				regionalTaxRateConfiguration.setTaxRate(Integer.valueOf(line[1]));
				return regionalTaxRateConfiguration;
			}).collect(Collectors.toList());
		}).stream().collect(Collectors.toConcurrentMap(RegionalTaxRateConfiguration::getState, RegionalTaxRateConfiguration::getTaxRate));
	}

	private boolean validateCarOrder(CarOrder c, Map<String, CarInventory> carInventoryMap,
			Map<String, AccessoryInventory> accessoryInventoryMap,
			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap,
			Map<String, Float> regionalTaxRateConfigurationMap) {
		return validateAgainstInventory(c, carInventoryMap, accessoryInventoryMap)
				&& validateMotorInsuranceProviderInventory(c, motorInsuranceProviderInventoryMap)
				&& validateRegionalTaxRateConfiguration(c, regionalTaxRateConfigurationMap);
	}

	private boolean validateAgainstInventory(CarOrder c, Map<String, CarInventory> carInventoryMap,
			Map<String, AccessoryInventory> accessoryInventoryMap) {
		return validateOrderAgainstAccessoryInventory(c, accessoryInventoryMap) && validateOrderAgainstCarInventory(c, carInventoryMap);
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

	private boolean validateRegionalTaxRateConfiguration(CarOrder carOrder, Map<String, Float> regionalTaxRateConfigurationMap) {
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

	private boolean decrementQuantityAvailable(CarOrder carOrder, Map<String, AccessoryInventory> accessoryInventoryMap,
			Map<String, CarInventory> carInventoryMap) {
		carInventoryMap.get(createCarInventoryKey(carOrder)).decrementQuantityAvailable();
		List<String> keys = createAccessoryInventoryKey(carOrder);
		if (keys != null) {
			keys.stream().forEach((key -> {
				accessoryInventoryMap.get(key).decrementQuantityAvailable();
			}));
		}
		return true;
	}

	private void calculatePremiumAmt(List<CarOrder> carOrderList,
			Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap) {
		carOrderList.stream()
				.forEach(
						c -> {
							if (motorInsuranceProviderInventoryMap.get(c.getMotorInsurance()).getPersonalProtectPlanOffered()
									&& c.getPersonalProtectPlan()) {
								c.setPremiumAmt(Float.valueOf(motorInsuranceProviderInventoryMap.get(c.getMotorInsurance())
										.getFirstYearPremium()));
							}
						});

	}
}
