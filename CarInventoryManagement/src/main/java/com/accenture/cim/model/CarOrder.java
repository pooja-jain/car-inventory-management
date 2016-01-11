package com.accenture.cim.model;

import java.util.List;

public class CarOrder {

	private String customerName;

	private String region;

	private String vendor;

	private String model;

	private String variant;

	private String color;

	private String accessories;

	private String motorInsurance;

	private String personalProtectPlan;

	// used for internal processing
	private List<String> accesoryList;

	// used for internal processing
	private boolean carAvailable;

	public List<String> getAccesoryList() {
		return accesoryList;
	}

	public void setAccesoryList(List<String> accesoryList) {
		this.accesoryList = accesoryList;
	}

	public String getCustomerName() {
		return customerName;
	}

	public boolean isCarAvailable() {
		return carAvailable;
	}

	public void setCarAvailable(boolean carAvailable) {
		this.carAvailable = carAvailable;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getAccessories() {
		return accessories;
	}

	public void setAccessories(String accessories) {
		this.accessories = accessories;
	}

	public String getMotorInsurance() {
		return motorInsurance;
	}

	public void setMotorInsurance(String motorInsurance) {
		this.motorInsurance = motorInsurance;
	}

	public String getPersonalProtectPlan() {
		return personalProtectPlan;
	}

	public void setPersonalProtectPlan(String personalProtectPlan) {
		this.personalProtectPlan = personalProtectPlan;
	}

	@Override
	public String toString() {
		return "CarOrder [customerName=" + customerName + ", region=" + region + ", vendor=" + vendor + ", model=" + model + ", variant="
				+ variant + ", color=" + color + ", accessories=" + accessories + ", motorInsurance=" + motorInsurance
				+ ", personalProtectPlan=" + personalProtectPlan + "]";
	}

}
