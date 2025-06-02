FROM gradle:8.14.1-jdk21-alpine AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle bootJar

FROM eclipse-temurin:21-jre-alpine
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]