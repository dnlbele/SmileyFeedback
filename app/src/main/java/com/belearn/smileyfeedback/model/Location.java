package com.belearn.smileyfeedback.model;

/**
 * Created by dnlbe on 12/20/2017.
 */

public class Location {
    private int idLocation;
    private String text;
    private int active;

    public int getIdLocation() {
        return idLocation;
    }
    public Location(int idLocation, String text, int active) {
        this.idLocation = idLocation;
        this.text = text;
        this.active = active;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location)) {
            return false;
        }
        return this.idLocation == ((Location)obj).idLocation;
    }
}

