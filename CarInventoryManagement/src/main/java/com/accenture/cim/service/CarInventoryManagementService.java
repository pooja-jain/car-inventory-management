package com.accenture.cim.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.accenture.cim.model.AccessoryInventory;
import com.accenture.cim.model.CarInventory;
import com.accenture.cim.model.CarOrder;
import com.accenture.cim.model.CarOrderError;
import com.accenture.cim.model.MotorInsuranceProviderInventory;
import com.accenture.cim.model.RegionalEstimatedSales;
import com.accenture.cim.model.RegionalTaxRateConfiguration;
import com.accenture.cim.utility.CSVFileReader;
import com.accenture.cim.utility.CSVFileWriter;
import com.accenture.cim.utility.PropertyLoader;
import com.accenture.cim.validation.CarOrderValidation;

/**
 * @author pooja.v.jain
 */
public class CarInventoryManagementService {

	public final static String ERROR_MSG_CAR_INVENTORY_UNAVAILABLE = "Car Inventory unavailable";

	public final static String ERROR_MSG_ACCESSORY_INVENTORY = "Mismatch with Accesories Inventory Available";

	public final static String ERROR_MSG_CAR_INVENTORY_INVALID = "Mismatch with Car Inventory Available";

	public final static String ERROR_MSG_REGION_INVALID = "Region not present";

	public final static String ERROR_MSG_INSURANCE_INVALID = "Invalid Insurance Provider";

	static List<CarOrder> carOrderList;

	static Map<String, CarInventory> carInventoryMap;

	static Map<String, AccessoryInventory> accessoryInventoryMap;

	static Map<String, MotorInsuranceProviderInventory> motorInsuranceProviderInventoryMap;

	static Map<String, Float> regionalTaxRateConfigurationMap;

	public static Properties properties;

	public static void main(String args[]) {

		long start = System.currentTimeMillis();

		PropertyLoader propertyLoader = new PropertyLoader();
		try {
			properties = propertyLoader.loadProperties();

			CarInventoryManagementService carInventoryManagementService = new CarInventoryManagementService();

			carInventoryManagementService.readInputFiles();

			// validate Car orders against various inventories
			CarOrderValidation carOrderValidation = new CarOrderValidation();
			List<CarOrder> validCarOrderList = carOrderValidation.getValidCarOrders(carOrderList, carInventoryMap, accessoryInventoryMap,
					motorInsuranceProviderInventoryMap, regionalTaxRateConfigurationMap);
			// Orders with inappropriate data
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
					regionalEstimatedSalesList);

			System.out.println("total orders: " + carOrderList.size());
			System.out.println("invalid orders: " + invalidCarOrderList.size());
			System.out.println("valid orders: " + validCarOrderList.size());
			validCarOrderList.stream().forEach(
					c -> {
						System.out.println("customerName=" + c.getCustomerName() + ", region=" + c.getRegion() + ", vendor="
								+ c.getVendor() + ", model=" + c.getModel() + ", variant=" + c.getVariant() + ", totalPrice="
								+ c.getTotalPrice());
					});

			System.out.print("Execution time : ");
			System.out.print(System.currentTimeMillis() - start);

		} catch (Exception e) {
			System.out.print(e.getMessage());
			System.out.print("terminating the application");
		}

	}

	/**
	 * Reads inventory files by spawning independent thread for each file
	 * @throws Exception
	 */
	private void readInputFiles() throws Exception {
		ExecutorService tp = Executors.newFixedThreadPool(5);
		CSVFileReader[] readers = new CSVFileReader[5];
		readers[0] = new CSVFileReader(properties.getProperty("file.location.carOrders"));
		readers[1] = new CSVFileReader(properties.getProperty("file.location.carInventory"));
		readers[2] = new CSVFileReader(properties.getProperty("file.location.accessoryInventory"));
		readers[3] = new CSVFileReader(properties.getProperty("file.location.motorInsuranceProviderInventory"));
		readers[4] = new CSVFileReader(properties.getProperty("file.location.regionalTaxRateConfiguration"));
		for (CSVFileReader reader : readers) {
			tp.execute(reader);
		}

		carOrderList = getCarOrders(readers[0]);
		carInventoryMap = getCarInventory(readers[1]);
		accessoryInventoryMap = getAccessoryInventory(readers[2]);
		motorInsuranceProviderInventoryMap = getMotorInsuranceProviderInventory(readers[3]);
		regionalTaxRateConfigurationMap = getRegionalTaxRateConfiguration(readers[4]);

		tp.shutdownNow();

	}

	private List<CarOrder> getCarOrders(CSVFileReader reader) throws Exception {

		return reader.readCSV(fileContent -> {
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
					car.setAccesoryList(Arrays.asList(car.getAccessories().split(":")));
				}

				return car;
			}).collect(Collectors.toList());
		});
	}

	private Map<String, CarInventory> getCarInventory(CSVFileReader reader) throws Exception {

		return reader.readCSV(fileContent -> {
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

	private Map<String, AccessoryInventory> getAccessoryInventory(CSVFileReader reader) throws Exception {

		return reader.readCSV(fileContent -> {
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

	private Map<String, MotorInsuranceProviderInventory> getMotorInsuranceProviderInventory(CSVFileReader reader) throws Exception {

		return reader
				.readCSV(fileContent -> {
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

	private Map<String, Float> getRegionalTaxRateConfiguration(CSVFileReader reader) throws Exception {

		return reader.readCSV(fileContent -> {
			return fileContent.stream().map(line -> {
				RegionalTaxRateConfiguration regionalTaxRateConfiguration = new RegionalTaxRateConfiguration();
				regionalTaxRateConfiguration.setState(line[0]);
				regionalTaxRateConfiguration.setTaxRate(Integer.valueOf(line[1]));
				return regionalTaxRateConfiguration;
			}).collect(Collectors.toList());
		}).stream().collect(Collectors.toConcurrentMap(RegionalTaxRateConfiguration::getState, RegionalTaxRateConfiguration::getTaxRate));
	}

	/**
	 * Calculate TotalAccessoriesPrice, TaxExpense, TotalPrice, Premium amount for each valid car
	 * order
	 * @param validCarOrderList
	 * @param carInventoryMap
	 * @param accessoryInventoryMap
	 * @param regionalTaxRateConfigurationMap
	 * @param motorInsuranceProviderInventoryMap
	 */
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

	/**
	 * Calculate TotalEstimatedSales, EstimatedSalesUnits, EstimatedNetIncome region wise
	 * @param validCarOrderList
	 * @param carInventoryMap
	 * @param regionalTaxRateConfigurationMap
	 * @return
	 */
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

	/**
	 * Write output files by spawning independent thread for each file
	 * @param carInventoryList
	 * @param accessoryInventoryList
	 * @param invalidCarOrderList
	 * @param regionalEstimatedSalesList
	 */
	private void createCSVReport(List<CarInventory> carInventoryList, List<AccessoryInventory> accessoryInventoryList,
			List<CarOrder> invalidCarOrderList, List<RegionalEstimatedSales> regionalEstimatedSalesList) {
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

		ExecutorService tp = Executors.newFixedThreadPool(5);
		CSVFileWriter[] writers = new CSVFileWriter[4];
		writers[0] = new CSVFileWriter(properties.getProperty("file.location.carInventory"), CarInventory.FILE_HEADER, carInventoryList);
		writers[1] = new CSVFileWriter(properties.getProperty("file.location.accessoryInventory"), AccessoryInventory.FILE_HEADER,
				accessoryInventoryList);
		writers[2] = new CSVFileWriter(properties.getProperty("file.location.carStandingOrdersErrors"), CarOrderError.FILE_HEADER,
				CarOrderErrorList);
		writers[3] = new CSVFileWriter(properties.getProperty("file.location.regionalEstimatedSalesReport"),
				RegionalEstimatedSales.FILE_HEADER, regionalEstimatedSalesList);

		for (CSVFileWriter writer : writers) {
			tp.execute(writer);
		}

		tp.shutdown();
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

	/**
	 * @param carOrderList
	 * @param motorInsuranceProviderInventoryMap
	 */
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
