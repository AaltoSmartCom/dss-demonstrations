FROM maven:3.6.3-jdk-11-slim AS build
MAINTAINER Jaan Taponen(jaan.taponen@aalto.fi)
#
# This section is purely for caching purposes.
# Copy the module pom files for quicker rebuildment of the image.
#
COPY ./pom.xml ./pom.xml
COPY ./dss-mock-tsa/pom.xml ./dss-mock-tsa/
COPY ./dss-standalone-app/pom.xml ./dss-standalone-app/
COPY ./dss-standalone-app-package/pom.xml ./dss-standalone-app-package/
COPY ./dss-demo-webapp/pom.xml ./dss-demo-webapp/
COPY ./dss-demo-bundle/pom.xml ./dss-demo-bundle/
COPY ./dss-rest-doc-generation/pom.xml ./dss-rest-doc-generation/
RUN mvn install || true
RUN mvn dependency:go-offline -B || true
# 
# Actual build phase.
#
COPY . /home/
WORKDIR /home
RUN apt-get update && apt-get install -y git perl openssl
RUN mvn install || true && mvn package
# Compile the sources, move the binary to /home and set permissions.
#
RUN mv dss-demo-bundle/target/dss-demo-bundle*.tar.gz . && \
     tar -xzf dss-demo-bundle*.tar.gz && \
     cd dss-demo-bundle*/apache-tomcat*/bin && \
     chmod +x ./startup.sh && \
     chmod +x ./catalina.sh &&\
     cd .. && cd ..  && \
     mv apache-tomcat* tomcat
# Copying CORS configuration
COPY ./web.xml ./web.xml
RUN  mv ./web.xml /home/dss-demo-bundle*/tomcat/conf/web.xml
#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/dss-demo-bundle-* /home/
USER root
WORKDIR /home/tomcat/bin
EXPOSE 8080
CMD ["./catalina.sh", "run"]