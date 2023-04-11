package com.example.keepingorganizer;
import java.util.ArrayList;

public class Person {
    private String name;
    private ArrayList<Integer> pastValues;
    private int sumValues;

    public Person(String name) {
        this.name = name;
        this.pastValues = new ArrayList<>();
        this.sumValues = 0;
    }

    public Person(Person person) {
        this.name = person.name;
        this.pastValues = person.pastValues;
        this.sumValues = person.sumValues;
    }

    public ArrayList<Integer> getPastValues() {
        return this.pastValues;
    }

    public void addValue(int value) {
        this.pastValues.add(value);
        this.sumValues += value;
    }

    public void removeValue() {
        this.sumValues -= this.pastValues.get(this.pastValues.size() - 1);
        this.pastValues.remove(this.pastValues.size() - 1);
    }

    public int getSumValues() {
        return this.sumValues;
    }

    public void setSumValues(int sumValues) {
        this.sumValues = sumValues;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void resetValues() {
        this.pastValues.clear();
        this.sumValues = 0;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", pastValues=" + pastValues +
                ", sumValues=" + sumValues +
                '}';
    }
}


