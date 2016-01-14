package com.accenture.cim.model;

public class RegionalTaxRateConfiguration {
	private String state;

	private float taxRate;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public float getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(int taxRate) {
		this.taxRate = taxRate;
	}

}
