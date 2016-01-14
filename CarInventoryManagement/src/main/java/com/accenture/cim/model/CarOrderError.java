package com.accenture.cim.model;

public class CarOrderError extends SuperWriterModel {
	public final static String FILE_HEADER = "customer Name ,Region,Vendor,Model,Variant,Color,accessories,motorInsurance,personalProtectPlan,Error Message";

	private String customerName;

	private String region;

	private String vendor;

	private String model;

	private String variant;

	private String color;

	private String accessories;

	private String motorInsurance;

	private boolean personalProtectPlan;

	private String errorMsg;

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
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

	public boolean getPersonalProtectPlan() {
		return personalProtectPlan;
	}

	public void setPersonalProtectPlan(boolean personalProtectPlan) {
		this.personalProtectPlan = personalProtectPlan;
	}

	public String getCustomerName() {
		return customerName;
	}

	@Override
	public String toString() {
		return "CarOrderError [customerName=" + customerName + ", region=" + region + ", vendor=" + vendor + ", model=" + model
				+ ", variant=" + variant + ", color=" + color + ", accessories=" + accessories + ", motorInsurance=" + motorInsurance
				+ ", personalProtectPlan=" + personalProtectPlan + ", errorMsg=" + errorMsg + "]";
	}

	@Override
	public String getCommaSeparatedValue() {
		return customerName + "," + region + "," + vendor + "," + model + "," + variant + "," + color + "," + accessories + ","
				+ motorInsurance + "," + personalProtectPlan + "," + errorMsg;
	}

}
