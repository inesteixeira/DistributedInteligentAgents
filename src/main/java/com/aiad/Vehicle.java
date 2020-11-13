package com.aiad;

/**
 * This class was automatically generated by the data modeler tool.
 */

public class Vehicle implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    private java.lang.Boolean isCar;
    private java.lang.Boolean isTruck;
    private java.lang.Boolean isMoto;
    private java.lang.Boolean ageBigger25;
    private java.lang.Boolean ageBetween25and60;
    private java.lang.Boolean ageBigger60;
    private java.lang.Boolean yearLess2000;
    private java.lang.Boolean yearGreater2000;
    private int price;

    public Vehicle() {
    }

    public java.lang.Boolean getIsCar() {
        return this.isCar;
    }

    public void setIsCar(java.lang.Boolean isCar) {
        this.isCar = isCar;
    }

    public java.lang.Boolean getIsTruck() {
        return this.isTruck;
    }

    public void setIsTruck(java.lang.Boolean isTruck) {
        this.isTruck = isTruck;
    }

    public java.lang.Boolean getIsMoto() {
        return this.isMoto;
    }

    public void setIsMoto(java.lang.Boolean isMoto) {
        this.isMoto = isMoto;
    }

    public java.lang.Boolean getAgeBigger25() {
        return this.ageBigger25;
    }

    public void setAgeBigger25(java.lang.Boolean ageBigger25) {
        this.ageBigger25 = ageBigger25;
    }

    public java.lang.Boolean getAgeBetween25and60() {
        return this.ageBetween25and60;
    }

    public void setAgeBetween25and60(java.lang.Boolean ageBetween25and60) {
        this.ageBetween25and60 = ageBetween25and60;
    }

    public java.lang.Boolean getAgeBigger60() {
        return this.ageBigger60;
    }

    public void setAgeBigger60(java.lang.Boolean ageBigger60) {
        this.ageBigger60 = ageBigger60;
    }

    public java.lang.Boolean getYearLess2000() {
        return this.yearLess2000;
    }

    public void setYearLess2000(java.lang.Boolean yearLess2000) {
        this.yearLess2000 = yearLess2000;
    }

    public java.lang.Boolean getYearGreater2000() {
        return this.yearGreater2000;
    }

    public void setYearGreater2000(java.lang.Boolean yearGreater2000) {
        this.yearGreater2000 = yearGreater2000;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Vehicle(java.lang.Boolean isCar, java.lang.Boolean isTruck,
                   java.lang.Boolean isMoto, java.lang.Boolean ageBigger25,
                   java.lang.Boolean ageBetween25and60, java.lang.Boolean ageBigger60,
                   java.lang.Boolean yearLess2000, java.lang.Boolean yearGreater2000,
                   int price) {
        this.isCar = isCar;
        this.isTruck = isTruck;
        this.isMoto = isMoto;
        this.ageBigger25 = ageBigger25;
        this.ageBetween25and60 = ageBetween25and60;
        this.ageBigger60 = ageBigger60;
        this.yearLess2000 = yearLess2000;
        this.yearGreater2000 = yearGreater2000;
        this.price = price;
    }

}