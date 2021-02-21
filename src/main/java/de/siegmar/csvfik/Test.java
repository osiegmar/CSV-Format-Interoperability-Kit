package de.siegmar.csvfik;

import java.nio.file.Path;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;

public class Test {

    private final String name;
    private final Path path;

    public Test(final Path path) {
        final Path fileName = path.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("File has no filename: " + path);
        }
        this.name = StringUtils.substringBefore(fileName.toString(), ".");
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Test.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("path=" + path)
            .toString();
    }

}
