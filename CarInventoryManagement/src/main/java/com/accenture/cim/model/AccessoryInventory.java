package com.accenture.cim.model;

public class AccessoryInventory extends SuperInventoryModel {

	public final static String FILE_HEADER = "vendor,model,accessories,price,quantityAvailable";

	private String vendor;

	private String model;

	private String accessories;

	private float price;

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

	public int decrementQuantityAvailable() {
		if (quantityAvailable > 0) {
			return quantityAvailable - 1;
		}
		return quantityAvailable;
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

	public String getKey() {
		return vendor + "-" + model + "-" + accessories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessories == null) ? 0 : accessories.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((vendor == null) ? 0 : vendor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccessoryInventory other = (AccessoryInventory) obj;
		if (accessories == null) {
			if (other.accessories != null)
				return false;
		} else if (!accessories.equals(other.accessories))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (vendor == null) {
			if (other.vendor != null)
				return false;
		} else if (!vendor.equals(other.vendor))
			return false;
		return true;
	}

}
