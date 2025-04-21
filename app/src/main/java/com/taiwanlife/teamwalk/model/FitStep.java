package com.taiwanlife.teamwalk.model;

/**
 * Author : Ryans
 * Date : 2023/8/22
 * Introduction :
 */
public class FitStep {

    private String dayAt;
    private int steps;

    public FitStep() {
    }

    public FitStep(String dayAt) {
        this.dayAt = dayAt;
    }

    public String getDayAt() {
        return dayAt;
    }

    public void setDayAt(String dayAt) {
        this.dayAt = dayAt;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}
