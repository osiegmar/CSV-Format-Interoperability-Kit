package de.siegmar.csvfik;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

// TODO die Klasse representiert einen Test Fixture, oder?
public class Test {

    private final String name;
    private final Path path;

    public Test(final Path path) {
        // e.g. tests/writer/writer-1.json
        Preconditions.checkArgument(Files.isReadable(path),
            "Test fixture file must exist at <%s>", path);

        // TODO soll hier immer der suffix .json vorausgesetzt werden?
        this.name = StringUtils.substringBefore(path.getFileName().toString(), ".");
        this.path = path;
    }

    /**
     * Technical identifier of test.
     *
     * @return e.g. "writer-1"
     */
    public String getName() {
        return name;
    }

    /**
     * Path to fixture file.
     *
     * @return test configuration file
     */
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
