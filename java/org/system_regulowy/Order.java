package org.system_regulowy;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<Pizza> pizzas = new ArrayList<>();
    private double total;
    private double initialTotal;
    private List<String> notes = new ArrayList<>();
    private boolean discountApplied = false;

    public Order() {}

    public List<Pizza> getPizzas() {
        return pizzas;
    }

    public void setPizzas(List<Pizza> pizzas) {
        this.pizzas = pizzas;
    }

    public void addPizza(Pizza pizza) {
        if (this.pizzas == null) {
            this.pizzas = new ArrayList<>();
        }
        this.pizzas.add(pizza);
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getInitialTotal() {
        return initialTotal;
    }

    public void setInitialTotal(double initialTotal) {
        this.initialTotal = initialTotal;
    }

    public void calculateInitialTotal() {
        if (this.pizzas == null) {
            this.initialTotal = 0.0;
        } else {
            this.initialTotal = this.pizzas.stream()
                    .mapToDouble(Pizza::getPrice)
                    .sum();
        }
        this.total = this.initialTotal;
        this.discountApplied = false;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void addNote(String note) {
        if (this.notes == null) {
            this.notes = new ArrayList<>();
        }
        this.notes.add(note);
    }

    public boolean isDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(boolean discountApplied) {
        this.discountApplied = discountApplied;
    }

    @Override
    public String toString() {
        return "Order{" +
                "pizzas=" + pizzas +
                ", total=" + total +
                ", initialTotal=" + initialTotal +
                ", notes=" + notes +
                ", discountApplied=" + discountApplied +
                '}';
    }
}