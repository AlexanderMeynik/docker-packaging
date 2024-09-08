FROM maven:3.9.1-amazoncorretto-19 AS build
COPY ../ /home/app
WORKDIR /home/app
RUN mvn install -DskipTests
RUN mvn clean package spring-boot:repackage -pl backend -DskipTests


FROM openjdk:19 AS server
COPY --from=build /home/app/backend/target/backend-1.0-SNAPSHOT.jar /usr/local/lib/server.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/server.jar"]
CMD ["bash"]

FROM ubuntu:18.04 AS enviroment
RUN apt update && apt install --no-install-recommends -y xorg libgl1-mesa-glx && rm -rf /var/lib/apt/lists/* 
RUN apt update && apt -y install wget
RUN cd /home
RUN wget "https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb"
RUN dpkg -i jdk-21_linux-x64_bin.deb
RUN rm jdk-21_linux-x64_bin.deb
CMD ["bash"]



FROM enviroment AS client
COPY --from=build /home/app/client /home/app/client
COPY --from=build /home/app/clientComponents /home/app/clientComponents
WORKDIR /home/app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/app/client/target/client-1.0-SNAPSHOT.jar"]
CMD ["bash"]