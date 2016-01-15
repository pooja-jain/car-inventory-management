package com.accenture.cim.model;

public class CarInventory extends SuperWriterModel {

	public final static String FILE_HEADER = "vendor,model,variant,color,basePrice,quantityAvailable";

	private String vendor;

	private String model;

	private String variant;

	private String color;

	private float basePrice;

	private int quantityAvailable;

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

	public float getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(float basePrice) {
		this.basePrice = basePrice;
	}

	public int getQuantityAvailable() {
		return quantityAvailable;
	}

	public void setQuantityAvailable(int quantityAvailable) {
		this.quantityAvailable = quantityAvailable;
	}

	public void decrementQuantityAvailable() {
		if (quantityAvailable > 0) {
			setQuantityAvailable(quantityAvailable - 1);
		}
	}

	@Override
	public String toString() {
		return "CarInventory [vendor=" + vendor + ", model=" + model + ", variant=" + variant + ", color=" + color + ", basePrice="
				+ basePrice + ", quantityAvailable=" + quantityAvailable + "]";
	}

	@Override
	public String getCommaSeparatedValue() {
		return vendor + "," + model + "," + variant + "," + color + "," + String.valueOf(basePrice) + ","
				+ String.valueOf(quantityAvailable);
	}

	/**
	 * sample key 'Tata-Nano-Petrol-Gray'
	 * @return
	 */
	public String getKey() {
		return vendor + "-" + model + "-" + variant + "-" + color;
	}

}
