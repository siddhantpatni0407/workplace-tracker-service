# Dockerfile (multi-stage)
# Stage 1: build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# copy only gradle wrapper and build files first to leverage caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
# copy source
COPY src ./src
# ensure gradlew is executable
RUN chmod +x ./gradlew

# Build the jar (skip tests to speed up in CI; remove -x test in dev if you want tests)
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="siddhantpatni0407"

WORKDIR /app
# copy jar from builder stage
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# timezone and basic jvm memory defaults (override with JAVA_OPTS env)
ENV TZ=Asia/Kolkata
ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Kolkata"

EXPOSE 8010

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
