package de.siegmar.csvfik;

import java.util.StringJoiner;

public class Project {

    private final String name;
    private String imageId;

    public Project(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(final String imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Project.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("imageId='" + imageId + "'")
            .toString();
    }

}
