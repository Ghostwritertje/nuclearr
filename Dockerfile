FROM cimg/openjdk:17.0
MAINTAINER ghostwritertje
COPY target/nuclearr-0.0.1-SNAPSHOT.jar nuclearr-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","nuclearr-0.0.1-SNAPSHOT.jar"]