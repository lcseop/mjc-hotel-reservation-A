FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir -p /app/uploads /app/uploads/review /app/uploads/room /app/uploads/hotel

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 33000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]