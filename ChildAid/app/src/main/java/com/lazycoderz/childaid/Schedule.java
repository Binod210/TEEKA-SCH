package com.lazycoderz.childaid;

public class Schedule {


    private int time;
    private String name, desc, quantity;

    public Schedule() {
    }

    public Schedule(int time, String name, String desc, String quantity) {
        this.time = time;
        this.name = name;
        this.desc = desc;
        this.quantity = quantity;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}