package de.siegmar.csvfik;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;

public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        try (JerseyDockerHttpClient httpClient = newHttpClient();
             DockerClient dockerClient = newDockerClient(httpClient)) {

            String project = "fastcsv";

            LOG.info("Create docker image for {}", project);

            final String imageId = dockerClient.buildImageCmd(new File(System.getProperty("user.dir") + "/impl/" + project))
                .exec(new BuildImageResultCallback())
                .awaitImageId();

            LOG.info("Image for {} created: {}; Creating container", project, imageId);

            final String containerId = dockerClient.createContainerCmd(imageId)
                .withCmd("world")
                .exec().getId();

            LOG.info("Container for {} created: {} (Image-ID: {}); Starting container", project, containerId, imageId);

            dockerClient.startContainerCmd(containerId).exec();

            LOG.info("Waiting for container for {} to finish (Container-ID: {})", project, containerId);
            dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitCompletion();

            LOG.info("Gathering logs of Container for {} (Container-ID: {})", project, containerId);
            final ResultCallback.Adapter<Frame> resultCallback = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(final Frame object) {
                    if (object.getStreamType() == StreamType.STDOUT) {
                        LOG.info("Container output: {}", StringUtils.chomp(new String(object.getPayload())));
                    }
                }
            };
            dockerClient.logContainerCmd(containerId).withStdOut(true).exec(resultCallback).awaitCompletion();

            LOG.info("Removing container for {} (Container-ID: {})", project, containerId);
            dockerClient.removeContainerCmd(containerId);
        }
    }

    private static JerseyDockerHttpClient newHttpClient() throws URISyntaxException {
        return new JerseyDockerHttpClient.Builder().dockerHost(new URI("unix:///var/run/docker.sock")).build();
    }

    private static DockerClient newDockerClient(final JerseyDockerHttpClient httpClient) {
        return DockerClientBuilder.getInstance().withDockerHttpClient(httpClient).build();
    }

}
