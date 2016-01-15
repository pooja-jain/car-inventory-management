package com.accenture.cim.model;

public class AccessoryInventory extends SuperWriterModel {

	public final static String FILE_HEADER = "vendor,model,accessories,price,quantityAvailable";

	private String vendor;

	private String model;

	private String accessories;

	private float price;

	private int quantityAvailable;

	public String getVendor() {
		return vendor;
	}

	/**
	 * @param vendor
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getAccessories() {
		return accessories;
	}

	public void setAccessories(String accessories) {
		this.accessories = accessories;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
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
		return "AccessoryInventory [vendor=" + vendor + ", model=" + model + ", accessories=" + accessories + ", price=" + price
				+ ", quantityAvailable=" + quantityAvailable + "]";
	}

	@Override
	public String getCommaSeparatedValue() {
		return vendor + "," + model + "," + accessories + "," + price + "," + quantityAvailable;
	}

	/**
	 * sample key 'Tata-Nano-Seat Cover'
	 * @return
	 */
	public String getKey() {
		return vendor + "-" + model + "-" + accessories;
	}

}
