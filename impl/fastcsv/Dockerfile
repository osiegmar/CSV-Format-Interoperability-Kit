FROM openjdk:8
WORKDIR /app
COPY gradlew settings.gradle ./
COPY gradle gradle
RUN ./gradlew --version
COPY build.gradle ./
COPY src src
RUN ./gradlew installDist
COPY entrypoint.sh ./
ENTRYPOINT ["/app/entrypoint.sh"]
