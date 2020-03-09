#
# Build stage UNDER CONSTRUCTION!
#
FROM maven:3.6.3-jdk-11-slim AS build
COPY . /home/demos/
WORKDIR /home
RUN apt-get update && \
    apt-get install -y git
RUN cd /home/demos && mvn install || true && mvn package

#
# Package stage
#
#FROM openjdk:11-jre-slim
#COPY --from=build /home/dss-demo-bundle-5.5 /home
#USER root
#WORKDIR /home/apache-tomcat-8.5.45/bin
#EXPOSE 8080
CMD ["./catalina.sh", "run"]
#CMD ["tail" ,"-f" ,"/dev/null"]