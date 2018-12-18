package com.example.david.alphaversion;

public class Product {
    public String id;
    public String name;
    public double weight;

    public Product(String id, String name, double weight) {
        this.id = id;
        this.name = name;
        this.weight = weight;
    }

    public Product() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
