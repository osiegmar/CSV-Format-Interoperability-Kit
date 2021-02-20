package de.siegmar.csvfik;

import java.io.IOException;
import java.net.URISyntaxException;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class Runner {

    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(final String[] args) throws IOException, InterruptedException, URISyntaxException {
        try (Docker docker = new Docker()) {
            docker.run("fastcsv", "writer-1");
        }
    }

}
