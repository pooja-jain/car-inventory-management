package com.accenture.cim.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.accenture.cim.model.AccessoryInventory;
import com.accenture.cim.model.CarInventory;
import com.accenture.cim.model.CarOrder;
import com.accenture.cim.model.MotorInsuranceProviderInventory;

/**
 * @author pooja.v.jain
 */
public class CarOrderValidation {

	public final static String ERROR_MSG_CAR_INVENTORY_UNAVAILABLE = "Car Inventory unavailable";

	public final static String ERROR_MSG_ACCESSORY_INVENTORY = "Mismatch with Accesories Inventory Available";

	public final static String ERROR_MSG_CAR_INVENTORY_INVALID = "Mismatch with Car Inventory Available";

	public final static String ERROR_MSG_REGION_INVALID = "Region not present";

	public final static String ERROR_MSG_INSURANCE_INVALID = "Invalid Insurance Provider";

	public List<CarOrder> getValidCarOrders(List<CarOrder> carOrderList, Map<String, CarInventory> carInventoryMap,
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

	/**
	 * Check for car availability
	 * @param carOrder
	 * @param carInventoryMap
	 * @return
	 */
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

	/**
	 * Check for accessory availability
	 * @param carOrder
	 * @param accessoryInventoryMap
	 * @return
	 */
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

}
