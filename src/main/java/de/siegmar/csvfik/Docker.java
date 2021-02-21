package de.siegmar.csvfik;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public class Docker implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Docker.class);

    private final transient JerseyDockerHttpClient httpClient;
    private final transient DockerClient dockerClient;

    public Docker() throws URISyntaxException {
        httpClient = new JerseyDockerHttpClient.Builder()
            .dockerHost(new URI("unix:///var/run/docker.sock"))
            .build();

        dockerClient = DockerClientBuilder.getInstance()
            .withDockerHttpClient(httpClient)
            .build();
    }

    public void run(final String project, final String testId) throws IOException, InterruptedException {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("test", project + "-" + testId)) {
            final String imageId = buildImage(project);

            final String localPath = Paths.get("tests").toAbsolutePath().toString();
            final Path resultPath = Files.createDirectories(Paths.get("/tmp/csvfik", project));

            final String containerId = createContainer(project, imageId, localPath, resultPath, testId);

            try {
                startAndLog(project, containerId);
            } finally {
                removeContainer(project, containerId);
            }

            compareResult(resultPath, testId);
        }
    }

    private String buildImage(final String project) {
        LOG.info("Create docker image for {}", project);

        final String imageId = dockerClient.buildImageCmd(new File("impl/" + project))
            .exec(new BuildImageResultCallback())
            .awaitImageId();

        LOG.info("Image for {} created: {}; Creating container", project, imageId);
        return imageId;
    }

    private String createContainer(final String project, final String imageId,
                                   final String localPath, final Path resultPath, final String testId) {
        final String containerId = dockerClient.createContainerCmd(imageId)
            .withHostConfig(new HostConfig()
                .withBinds(
                    new Bind(localPath, new Volume("/tests"), AccessMode.ro),
                    new Bind(resultPath.toAbsolutePath().toString(), new Volume("/out"))
                )
            )
            .withCmd("write", "/tests/writer/" + testId + ".json", "/out/" + testId + ".csv")
            .exec().getId();

        LOG.info("Container for {} created: {} (Image-ID: {}); Starting container", project, containerId, imageId);
        return containerId;
    }

    private void startAndLog(final String project, final String containerId)
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

    private void removeContainer(final String project, final String containerId) {
        LOG.info("Removing container for {} (Container-ID: {})", project, containerId);
        dockerClient.removeContainerCmd(containerId);
    }

    private void compareResult(final Path resultPath, final String testId) throws IOException {
        final Path expectedFile = Paths.get("expects/" + testId + ".csv");

        final byte[] dataResultFile = Files.readAllBytes(resultPath.resolve(Paths.get(testId + ".csv")));
        final byte[] dataExpectedFile = Files.readAllBytes(expectedFile);
        if (Arrays.equals(dataResultFile, dataExpectedFile)) {
            LOG.info("Files are the same");
        } else {
            LOG.error("Files differ");
        }
    }

    @Override
    public void close() throws IOException {
        dockerClient.close();
        httpClient.close();
    }

}
