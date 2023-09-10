FROM openjdk:11

RUN mkdir /app

COPY . /app

WORKDIR /app

RUN chmod +x /app/gradlew && cd app && /app/gradlew build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/id.bts.leave-app-0.0.1.jar"]