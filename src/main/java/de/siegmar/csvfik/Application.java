package de.siegmar.csvfik;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final transient Docker docker;

    public Application(final Docker docker) {
        this.docker = docker;
    }

    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(final String... args) throws Exception {
        final List<Project> projects = listProjects();

        for (final Project project : projects) {
            LOG.info("Run for project {}", project);
            project.setImageId(docker.build(project.getName()));
        }

        final List<Test> tests = listTests();

        for (final Test test : tests) {
            for (final Project project : projects) {
                LOG.info("Run test {} for project {}", test, project);
                docker.run(project, test);
            }
        }
    }

    private static List<Project> listProjects() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get("impl"))) {
            return stream
                .map(p -> new Project(p.getFileName().toString()))
                .collect(Collectors.toList());
        }
    }

    private static List<Test> listTests() throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get("tests"))) {
            return stream
                .filter(Files::isRegularFile)
                .map(Test::new)
                .collect(Collectors.toList());
        }
    }

}
