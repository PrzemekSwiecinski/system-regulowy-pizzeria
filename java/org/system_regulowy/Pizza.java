package org.system_regulowy;

public class Pizza {
    private String name;
    private double price;
    private boolean usedInPromotion = false;

    public Pizza(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isUsedInPromotion() {
        return usedInPromotion;
    }

    public void setUsedInPromotion(boolean usedInPromotion) {
        this.usedInPromotion = usedInPromotion;
    }

    @Override
    public String toString() {
        return "Pizza{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", usedInPromotion=" + usedInPromotion +
                '}';
    }
}