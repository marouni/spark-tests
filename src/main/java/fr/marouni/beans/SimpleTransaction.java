package fr.marouni.beans;

import java.io.Serializable;

public class SimpleTransaction implements Serializable {

    public String company;
  public String client;
    public int item;
  public double qty;
  public double price;

    public SimpleTransaction(String company, String client, int item, double qty, double price) {
        this.company = company;
        this.client = client;
        this.item = item;
        this.qty = qty;
        this.price = price;
    }


    public String getCompany() {
        return company;
    }

    public String getClient() {
        return client;
    }

    public int getItem() {
        return item;
    }

    public double getQty() {
        return qty;
    }

    public double getPrice() {
        return price;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}
