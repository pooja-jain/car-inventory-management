package com.accenture.cim.model;

import java.math.BigDecimal;
import java.util.Formatter;

public class RegionalEstimatedSales extends SuperWriterModel {

	public final static String FILE_HEADER = "State ,Estimated Sales in Units ,Total Estimated Sales ,Estimated Net Income";

	public final static Formatter f = new Formatter();

	private String state;

	private int estimatedSalesUnits;

	private BigDecimal totalEstimatedSales;

	public BigDecimal getTotalEstimatedSales() {
		return totalEstimatedSales;
	}

	public void setTotalEstimatedSales(BigDecimal totalEstimatedSales) {
		this.totalEstimatedSales = totalEstimatedSales;
	}

	private float EstimatedNetIncome;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getEstimatedSalesUnits() {
		return estimatedSalesUnits;
	}

	public void setEstimatedSalesUnits(int estimatedSalesUnits) {
		this.estimatedSalesUnits = estimatedSalesUnits;
	}

	public float getEstimatedNetIncome() {
		return EstimatedNetIncome;
	}

	public void setEstimatedNetIncome(float estimatedNetIncome) {
		EstimatedNetIncome = estimatedNetIncome;
	}

	@Override
	public String toString() {
		return "RegionalEstimatedSales [state=" + state + ", estimatedSalesUnits=" + estimatedSalesUnits + ", totalEstimatedSales="
				+ totalEstimatedSales + ", EstimatedNetIncome=" + EstimatedNetIncome + "]";
	}

	@Override
	public String getCommaSeparatedValue() {
		return state + "," + estimatedSalesUnits + "," + totalEstimatedSales + "," + EstimatedNetIncome;
	}

}
