package com.amandamcnair.testingassign2;

public class Nutrition {

    public void setId(int id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getId() {
        return id;
    }
    public String getNumber() {
        return number;
    }
    public String getName() {
        return name;
    }

    public int id;
    public String number;
    public String name;

    public Nutrition() { }

    public Nutrition(int id, String number, String name) {
        this.id = id;
        this.number = number;
        this.name = name;
    }
}
