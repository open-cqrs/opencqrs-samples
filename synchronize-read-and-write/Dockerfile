#TODO Make multistage build once framework is public

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]