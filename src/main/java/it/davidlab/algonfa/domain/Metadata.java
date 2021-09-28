package it.davidlab.algonfa.domain;

import java.util.List;
import java.util.Objects;

public class Metadata {

    private String description;
    private String externalUrl;
    private String image;
    private String name;
    private List<Attribute> attributes;

    public Metadata() {
    }

    public Metadata(String description, String externalUrl, String image, String name, List<Attribute> attributes) {
        this.description = description;
        this.externalUrl = externalUrl;
        this.image = image;
        this.name = name;
        this.attributes = attributes;
    }

    public String getDescription() {
        return description;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metadata)) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(description, metadata.description) && Objects.equals(externalUrl, metadata.externalUrl) && Objects.equals(image, metadata.image) && Objects.equals(name, metadata.name) && Objects.equals(attributes, metadata.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, externalUrl, image, name, attributes);
    }
}
