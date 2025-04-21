package com.taiwanlife.teamwalk.model;

/**
 * Author : Ryans
 * Date : 2023/8/22
 * Introduction :
 */
public class FitCalories {
    private String dayAt;
    private float calories;

    public FitCalories() {
    }

    public FitCalories(String dayAt) {
        this.dayAt = dayAt;
    }

    public String getDayAt() {
        return dayAt;
    }

    public void setDayAt(String dayAt) {
        this.dayAt = dayAt;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }
}
