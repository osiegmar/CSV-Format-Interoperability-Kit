FROM gradle:6.8.2-jdk11
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY src src
RUN gradle installDist
ENTRYPOINT ["/app/build/install/fastcsv-cli/bin/fastcsv-cli"]
