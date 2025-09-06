# Dockerfile (multi-stage)
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src ./src
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="siddhantpatni0407"

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

ENV TZ=Asia/Kolkata
ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Kolkata"

EXPOSE 8010

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
