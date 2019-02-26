package com.example.david.alphaversion;

public class Product {
    public String id;
    public String name;
    public String enterD;
    public String enterT;
    public String expired;
    public String barcode;
    public String weight;
    public boolean isScan=false;

    public Product(String id, String name, String enterD, String enterT, String expired, String barcode, String weight, boolean isScan) {
        this.id = id;
        this.name = name;
        this.enterD = enterD;
        this.enterT = enterT;
        this.expired = expired;
        this.barcode=barcode;
        this.weight = weight;
        this.isScan = isScan;
    }

    public Product() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getEnterD() {
        return enterD;
    }

    public void setEnterD(String enterD) {
        this.enterD = enterD;
    }

    public String getEnterT() {
        return enterT;
    }

    public void setEnterT(String enterT) {
        this.enterT = enterT;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expire) {
        this.expired = expire;
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

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public boolean getIsScan() {
        return isScan;
    }

    public void setIsScan(boolean isScan) {
        isScan = isScan;
    }
}
