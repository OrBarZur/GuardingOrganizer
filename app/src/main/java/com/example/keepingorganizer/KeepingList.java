package com.example.keepingorganizer;

import org.joda.time.LocalDateTime;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class KeepingList {
    private int MAX_RATE = 5;
    private ArrayList<KeepingHour> keepingHours;
    private Platoon platoon;
    private ArrayList<Person> absents;
    private ArrayList<Person> potentialKeepers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private boolean isRandom;
    private boolean isNextDay;
    private boolean isAutoDuration;



    private boolean isEditable;

    public KeepingList(Platoon platoon) {
        this.emptyKeepingHours();
        this.platoon = platoon;
        this.absents = new ArrayList<>();
        this.potentialKeepers = new ArrayList<>(this.platoon.getPersons());
        this.startTime = null;
        this.endTime = null;
        this.duration = 0;
        this.isRandom = false;
        this.isNextDay = false;
        this.isAutoDuration = true;
        this.isEditable = false;
    }

    public String create() {

        this.fillKeepingList(this.startTime, this.endTime, this.duration);

        this.sortPotentialKeepers();

        return "Created";
    }

    public void fillPersonsToKeepingHours() {
        if (!this.isRandom)
            this.sortKeepingHoursByRating();

        for (int i = 0; i < this.keepingHours.size(); i++)
            this.keepingHours.get(i).setPerson(potentialKeepers.get(i));
    }

    public void sortPotentialKeepers() {
        Collections.shuffle(this.potentialKeepers);

        if (this.isRandom)
            return;

        Collections.sort(this.potentialKeepers, new Comparator<Person>() {
            public int compare(Person p1, Person p2) {
                return ((Integer)p1.getSumValues()).compareTo(p2.getSumValues());
            }
        });
    }

    public void sortKeepingHoursByRating() {
        Collections.sort(this.keepingHours, new Comparator<KeepingHour>() {
            public int compare(KeepingHour h1, KeepingHour h2) {
                return ((Integer)h1.getHourRating()).compareTo(h2.getHourRating());
            }
        });
    }

    public void sortKeepingHoursByTime() {
        Collections.sort(this.keepingHours, new Comparator<KeepingHour>() {
            public int compare(KeepingHour h1, KeepingHour h2) {
                if (h1.getStartTime().isBefore(h2.getStartTime()))
                    return -1;
                return 1;
            }
        });
    }

    public void fillKeepingList(LocalDateTime startTime, LocalDateTime endTime, int duration) {
        ArrayList<LocalDateTime> hoursArrayList = new ArrayList<>();

        while (startTime.isBefore(endTime)) {
            hoursArrayList.add(startTime);
            startTime = startTime.plusMinutes(duration);
        }

        if (this.platoon.getHourRatings().size() != hoursArrayList.size()) {
            while (this.platoon.getHourRatings().size() > hoursArrayList.size())
                this.platoon.getHourRatings().remove(this.platoon.getHourRatings().size() / 2);

            while (this.platoon.getHourRatings().size() < hoursArrayList.size())
                this.platoon.getHourRatings().add(this.platoon.getHourRatings().size() / 2, (this.MAX_RATE + 1) / 2);
        }

        if (!this.keepingHours.isEmpty())
            this.emptyKeepingHours();

        for (int i = 0; i < hoursArrayList.size(); i++) {
            this.keepingHours.add(new KeepingHour(hoursArrayList.get(i), hoursArrayList.get(i).plusMinutes(duration),
                    this.platoon.getHourRatings().get(i)));
        }
    }

    public String accept() {
        if (this.isRandom)
            return "Accepted";

        for (int i = 0; i < this.keepingHours.size(); i++)
            this.keepingHours.get(i).getPerson().addValue(MAX_RATE + 1 - this.keepingHours.get(i).getHourRating());

        return "Accepted";
    }

    public String removePoints() {
        if (this.isRandom)
            return "Accepted";

        for (int i = 0; i < this.keepingHours.size(); i++)
            this.keepingHours.get(i).getPerson().removeValue();

        return "Accepted";
    }

    public int getMinDuration() {
        long minutes = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            minutes = ChronoUnit.MINUTES.between(java.time.LocalDateTime.of(this.startTime.getYear(), this.startTime.getMonthOfYear(), this.startTime.getDayOfMonth(), this.startTime.getHourOfDay(), this.startTime.getMinuteOfHour()),
                    java.time.LocalDateTime.of(this.endTime.getYear(), this.endTime.getMonthOfYear(), this.endTime.getDayOfMonth(), this.endTime.getHourOfDay(), this.endTime.getMinuteOfHour()));
        }
        return (int)Math.ceil((double)minutes / this.potentialKeepers.size());
    }

    public boolean isKeeping(Person person) {
        for (KeepingHour kh : this.keepingHours) {
            if (kh.getPerson() != null && kh.getPerson().getName().equals(person.getName()))
                return true;
        }
        return false;
    }

    public boolean isAbsent(Person person) {
        for (Person p : this.absents)
            if (p.getName().equals(person.getName()))
                return true;

        return false;
    }

    public ArrayList<KeepingHour> getKeepingHours() {
        return this.keepingHours;
    }

    public void emptyKeepingHours() {
        this.keepingHours = new ArrayList<>();
    }

    public Platoon getPlatoon() {
        return this.platoon;
    }

    public void setPlatoon(Platoon platoon) {
        this.platoon = platoon;
    }

    public void addPerson(Person person) {
        this.platoon.addPerson(person);
        this.potentialKeepers.add(person);
    }

    public void removePerson(Person person) {
        this.platoon.removePerson(person);

        if (this.absents.contains(person))
            this.absents.remove(person);
        else
            this.potentialKeepers.remove(person);
    }

    public ArrayList<Person> getAbsents() {
        return this.absents;
    }

    public void setAbsents(ArrayList<Person> absents) {
        this.absents = absents;
        this.potentialKeepers = new ArrayList<>(this.platoon.getPersons());
        this.potentialKeepers.removeAll(this.absents);
    }

    public void addAbsent(Person person) {
        this.absents.add(person);
        this.potentialKeepers.remove(person);
    }

    public void removeAbsent(Person person) {
        this.absents.remove(person);
        this.potentialKeepers.add(person);
    }

    public ArrayList<Person> getPotentialKeepers() {
        return this.potentialKeepers;
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

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isRandom() {
        return this.isRandom;
    }

    public void setRandom(boolean random) {
        this.isRandom = random;
    }

    public boolean isNextDay() {
        return this.isNextDay;
    }

    public void setNextDay(boolean nextDay) {
        this.isNextDay = nextDay;
    }

    public boolean isAutoDuration() {
        return this.isAutoDuration;
    }

    public void setAutoDuration(boolean autoDuration) {
        this.isAutoDuration = autoDuration;
    }

    public int MAX_RATE() {
        return MAX_RATE;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean editable) {
        this.isEditable = editable;
    }

    public String toStringKeepingHours() {
        String s = "";
        for (int i = 0; i < keepingHours.size(); i++)
            s = s.concat((i == 0 ? "" : "\n") + keepingHours.get(i));

        return s;
    }

    public void jsonToKeepingList(KeepingList keepingList) {
        if (keepingList == null)
            return;

        this.MAX_RATE = keepingList.MAX_RATE;
        this.platoon.jsonToPlatoon(keepingList.platoon);

        this.absents.clear();
        for (Person person : keepingList.absents)
            this.absents.add(this.platoon.getPersonByName(person.getName()));

        this.duration = keepingList.duration;
        this.isAutoDuration = keepingList.isAutoDuration;
        this.isRandom = keepingList.isRandom;
        this.isNextDay = keepingList.isNextDay;

        this.keepingHours.clear();
        for (KeepingHour keepingHour : keepingList.keepingHours) {
            if (keepingHour.getPerson() != null)
                keepingHour.setPerson(this.platoon.getPersonByName(keepingHour.getPerson().getName()));
            this.keepingHours.add(new KeepingHour(keepingHour));
        }

        this.potentialKeepers.clear();
        for (Person person : keepingList.potentialKeepers)
            this.potentialKeepers.add(this.platoon.getPersonByName(person.getName()));

        this.startTime = keepingList.startTime;
        this.endTime = keepingList.endTime;
    }

    @Override
    public String toString() {
        return "KeepingList{" +
                "keepingHours=" + keepingHours +
                ", platoon=" + platoon +
                ", absents=" + absents +
                ", potentialKeepers=" + potentialKeepers +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", isRandom=" + isRandom +
                ", isNextDay=" + isNextDay +
                '}';
    }
}
