package com.amandamcnair.testingassign2;

public class Food {
    public int id;
    public double carbs, fiber;
    public String description;
    public String dataType;
    public String foodCode;
    public String brandOwner = "";

    public Food() {}

    public Food(int id, String description, String dataType, String brandOwner) {
        this.id = id;
        this.description = description;
        this.dataType = dataType;
        this.brandOwner = brandOwner;
    }

    /*
    public Food(int id, String description, String dataType, String foodCode) {
        this.id = id;
        this.description = description;
        this.dataType = dataType;
        this.foodCode = foodCode;
    }

     */

    //constructor for tracker class
    public Food(int id, String description, double carbs, double fiber) {
        this.id = id;
        this.description = description;
        this.carbs = carbs;
        this.fiber = fiber;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDataType() {
        return dataType;
    }

    public String getFoodCode() {
        return foodCode;
    }

    public double getCarbs() { return carbs; }

    public double getFiber() { return fiber;}

    public String getBrandOwner() { return brandOwner; }


    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setFoodCode(String foodCode) {
        this.foodCode = foodCode;
    }

    public void setBrandOwner(String brandOwner) {this.brandOwner = brandOwner; }
}
