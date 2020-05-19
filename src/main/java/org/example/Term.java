package org.example;

public class Term {
    private final String name;
    private Double frequency;

    public Term(String name, Double frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return name;
    }
}
