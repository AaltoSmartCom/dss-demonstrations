FROM maven:3.6.3-jdk-11-slim AS build
MAINTAINER Jaan Taponen(jaan.taponen@aalto.fi)
COPY . /home/
WORKDIR /home
RUN apt-get update && apt-get install -y git perl openssl
RUN mvn install || true && mvn package
# Compile the sources, move the binary to /home and set permissions.
RUN mv dss-demo-bundle/target/dss-demo-bundle-5.6.tar.gz . && \
    tar -xzf dss-demo-bundle-5.6.tar.gz && \
    cd dss-demo-bundle-5.6/apache-tomcat-8.5.50/bin && \
    chmod +x ./startup.sh && \
    chmod +x ./catalina.sh
#
# Package stage
FROM openjdk:11-jre-slim
COPY --from=build /home/dss-demo-bundle-5.6 /home
USER root
WORKDIR /home/apache-tomcat-8.5.50/bin
EXPOSE 8080
CMD ["./catalina.sh", "run"]
#CMD ["tail" ,"-f" ,"/dev/null"]