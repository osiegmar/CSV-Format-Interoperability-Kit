package de.siegmar.csvfik;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

@Configuration
public class Config {

    @Bean
    public JerseyDockerHttpClient httpClient() throws URISyntaxException {
        return new JerseyDockerHttpClient.Builder()
            .dockerHost(new URI("unix:///var/run/docker.sock"))
            .build();
    }

    @Bean
    public DockerClient dockerClient(final DockerHttpClient httpClient) {
        return DockerClientBuilder.getInstance()
            .withDockerHttpClient(httpClient)
            .build();
    }

}
