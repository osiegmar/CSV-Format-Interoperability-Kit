package de.siegmar.csvfik;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;

@Service
public class Docker {

    private static final Logger LOG = LoggerFactory.getLogger(Docker.class);

    private final transient DockerClient dockerClient;

    public Docker(final DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public String build(final ProjectName projectName) {
        return buildImage(projectName);
    }

    public void run(final Project project, final Test test) throws IOException, InterruptedException {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("test",
            project.getName() + "-" + test.getName())) {
            final String localPath = Paths.get("tests").toAbsolutePath().toString();
            final Path resultPath = Files.createDirectories(Paths.get("/tmp/csvfik", project.getName()));

            final String containerId = createContainer(project, localPath, resultPath, test);

            try {
                startAndLog(project, containerId);
            } finally {
                removeContainer(project, containerId);
            }

            compareResult(resultPath, test);
        }
    }

    private String buildImage(final ProjectName projectName) {
        LOG.info("Create docker image for {}", projectName.getName());

        final String imageId = dockerClient.buildImageCmd(new File("impl/" + projectName.getName()))
            .exec(new BuildImageResultCallback())
            .awaitImageId();

        LOG.info("Image for {} created: {}", projectName.getName(), imageId);
        return imageId;
    }

    private String createContainer(final Project project,
                                   final String localPath, final Path resultPath, final Test test) {
        final String containerId = dockerClient.createContainerCmd(project.getImageId())
            .withHostConfig(new HostConfig()
                .withBinds(
                    new Bind(localPath, new Volume("/tests"), AccessMode.ro),
                    new Bind(resultPath.toAbsolutePath().toString(), new Volume("/out"))
                )
            )
            .withCmd("write", "/" + test.getPath().toString(), "/out/" + test.getName() + ".csv")
            .exec().getId();

        LOG.info("Container for {} created: {} (Image-ID: {}); Starting container",
            project.getName(), containerId, project.getImageId());
        return containerId;
    }

    private void startAndLog(final Project project, final String containerId)
        throws InterruptedException, IOException {

        dockerClient.startContainerCmd(containerId).exec();

        LOG.info("Waiting for container for {} to finish (Container-ID: {})", project, containerId);
        dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitCompletion();

        LOG.info("Gathering logs of Container for {} (Container-ID: {})", project, containerId);

        try (ResultCallback.Adapter<Frame> resultCallback = newCallback(MDC.get("test"))) {
            dockerClient.logContainerCmd(containerId).withStdOut(true).withStdErr(true).exec(resultCallback)
                .awaitCompletion();
        }
    }

    private ResultCallback.Adapter<Frame> newCallback(final String testContext) {
        return new ResultCallback.Adapter<>() {
            @Override
            public void onNext(final Frame object) {
                try (MDC.MDCCloseable ignored = MDC.putCloseable("test", testContext)) {
                    if (object.getStreamType() == StreamType.STDOUT) {
                        LOG.info("Container output: {}", object);
                    } else if (object.getStreamType() == StreamType.STDERR) {
                        LOG.error("Container error: {}", object);
                    }
                }
            }
        };
    }

    private void removeContainer(final Project project, final String containerId) {
        LOG.info("Removing container for {} (Container-ID: {})", project, containerId);
        dockerClient.removeContainerCmd(containerId);
    }

    private void compareResult(final Path resultPath, final Test test) throws IOException {

        final byte[] dataResultFile = Files.readAllBytes(
            resultPath.resolve(Paths.get(test.getName() + ".csv")));
        final byte[] dataExpectedFile = Files.readAllBytes(
            Paths.get("expects", test.getName() + ".csv"));

        final boolean matches = Arrays.equals(dataResultFile, dataExpectedFile);
        if (matches) {
            LOG.info(">> Comparing impl output with expected: ok - matches");
        } else {
            LOG.error(">> Comparing impl output with expected: MISMATCH");
        }
    }

}
