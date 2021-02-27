package de.siegmar.sfm.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.siegmar.fastcsv.cli.Test;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalStateException("Wrong usage");
        }

        final String mode = args[0];
        final Path srcFile = Paths.get(args[1]);
        final Path destFile = Paths.get(args[2]);

        if ("write".equals(mode)) {
            final ObjectMapper objectMapper = new ObjectMapper();
            final Test test = objectMapper.readValue(srcFile.toFile(), Test.class);

            new Impl().write(test, destFile);
        } else if ("read".equals(mode)) {
            new Impl().read(srcFile, destFile);
        } else {
            throw new IllegalStateException("Wrong mode: " + mode);
        }
    }

}
