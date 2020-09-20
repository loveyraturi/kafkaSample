# ############################################################
# # Dockerfile to build custom Ubuntu image
# # Use this as base image
# # Tag microapp:v1
# ############################################################

FROM maven:3.6-jdk-11-slim as WAR
RUN apt-get update && apt-get -y install git wget bash -y openssh-client
ENV APP_NAME=fynd
ENV APP_VERSION=0.0.1-SNAPSHOT
RUN git clone https://github.com/loveyraturi/kafkaSample.git
WORKDIR /kafkaSample
RUN mvn clean install -DskipTests 
RUN mv target/${APP_NAME}-${APP_VERSION}.war /fynd.war

FROM ubuntu
RUN apt-get upgrade
RUN apt-get update --fix-missing 
RUN apt-get install -y wget bash
RUN apt-get install -y software-properties-common --fix-missing
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get install -y openjdk-8-jdk --fix-missing

ENV CATALINA_HOME=/opt/tomcat
ENV PATH="$PATH:$CATALINA_HOME/bin"
ENV JAVA_OPTS="-Xms1024M -Xmx1024M -Djava.security.egd=file:/dev/./urandom"
ENV artifactory_username=''
ENV  artifactory_password=''
RUN wget https://downloads.apache.org/tomcat/tomcat-8/v8.5.58/bin/apache-tomcat-8.5.58.tar.gz
RUN tar -xvf apache-tomcat-8.5.58.tar.gz -C /opt/ && \
mv /opt/apache-tomcat-8.5.58 /opt/tomcat && \
rm apache-tomcat-8.5.58.tar.gz

COPY --from=WAR /fynd.war /opt/tomcat/webapps/
ENV JAVA_OPTS="-Dport.http.nonssl=6001 -Xms6G -Xmx6G -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50 -XX:MaxMetaspaceFreeRatio=80 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:garbage-collection.log -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5  -XX:+UseStringDeduplication -XX:StringDeduplicationAgeThreshold=5 -Xss512K -XX:+UseCompressedOops -XX:MaxGCPauseMillis=100"
CMD ["bash","/opt/tomcat/bin/catalina.sh","run"]