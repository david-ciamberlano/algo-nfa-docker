package it.davidlab.algonfa.domain;

import java.util.Objects;

public class Attribute {

    private String traitType;
    private String displayType;
    private String value;

    public Attribute() {
    }

    public Attribute(String traitType, String displayType, String value) {
        this.traitType = traitType;
        this.displayType = displayType;
        this.value = value;
    }

    public String getTraitType() {
        return traitType;
    }

    public String getDisplayType() {
        return displayType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        Attribute attribute = (Attribute) o;
        return traitType.equals(attribute.traitType) && Objects.equals(displayType, attribute.displayType) && value.equals(attribute.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traitType, displayType, value);
    }
}
