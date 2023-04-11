package com.example.keepingorganizer;

import org.joda.time.LocalDateTime;

public class KeepingHour {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int hourRating; // numbers from 1 to 5
    private Person person;

    public KeepingHour(LocalDateTime startTime, LocalDateTime endTime, int hourRating) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.hourRating = hourRating;
        this.person = null;
    }

    public KeepingHour(KeepingHour keepingHour) {
        this.startTime = keepingHour.startTime;
        this.endTime = keepingHour.endTime;
        this.hourRating = keepingHour.hourRating;
        this.person = keepingHour.person;
    }

    public int getHourRating() {
        return this.hourRating;
    }

    public void setHourRating(int hourRating) {
        this.hourRating = hourRating;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return (this.person != null ? this.person.getName() : "") + ": " +
                String.format("%02d", this.startTime.getHourOfDay()) + ":" + String.format("%02d", this.startTime.getMinuteOfHour()) +
                " - " + String.format("%02d", this.endTime.getHourOfDay()) + ":" + String.format("%02d", this.endTime.getMinuteOfHour());
    }
}
