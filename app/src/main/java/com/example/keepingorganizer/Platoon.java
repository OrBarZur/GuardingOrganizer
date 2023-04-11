package com.example.keepingorganizer;

import java.util.ArrayList;

public class Platoon {
    private ArrayList<Person> persons;
    private ArrayList<Integer> hourRatings; // numbers from 1 to 5

    public Platoon() {
        this.persons = new ArrayList<>();
        this.hourRatings = new ArrayList<>();
    }

    public ArrayList<Integer> getHourRatings() {
        return this.hourRatings;
    }

    public void setHourRatings(ArrayList<Integer> hourRatings) {
        this.hourRatings = hourRatings;
    }

    public ArrayList<Person> getPersons() {
        return this.persons;
    }

    public void addPerson(Person person) {
        this.persons.add(person);
    }

    public void removePerson(Person person) {
        this.persons.remove(person);
    }

    public ArrayList<Person> getPersonsFromNames(ArrayList<String> names) {
        ArrayList<Person> arrayList = new ArrayList<>();
        for (int i = 0; i < this.persons.size(); i++) {
            if (names.contains(this.persons.get(i).getName()))
                arrayList.add(this.persons.get(i));
        }
        return arrayList;
    }

    public Person getPersonByName(String name) {
        for (int i = 0; i < this.persons.size(); i++)
            if (this.persons.get(i).getName().equals(name))
                return this.persons.get(i);

        return null;
    }

    public boolean hasName(String name) {
        for (int i = 0; i < this.persons.size(); i++)
            if (this.persons.get(i).getName().equals(name))
                return true;

        return false;
    }

    public void resetValues() {
        for (int i = 0; i < persons.size(); i++)
            persons.get(i).resetValues();
    }

    public void jsonToPlatoon(Platoon platoon) {
        this.hourRatings = platoon.hourRatings;

        this.persons.clear();
        this.persons.addAll(platoon.persons);
    }

    @Override
    public String toString() {
        return "Platoon{" +
                "persons=" + persons +
                ", hourRatings=" + hourRatings +
                '}';
    }
}
