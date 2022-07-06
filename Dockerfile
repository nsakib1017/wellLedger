FROM gradle:7.2.0-jdk11 as build

ADD . .
RUN gradle build
RUN ls build/libs

FROM openjdk:11.0.6-jre-slim as deploy
COPY --from=build /home/gradle/build/libs .
ENTRYPOINT ["java", "-jar", "wallet.jar"]
CMD []