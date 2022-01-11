FROM maven:3-jdk-11 as build

COPY . .
RUN mvn -Pdist,standalone clean install


# Final running image
FROM openjdk:11-jre-slim

# Import the lsq-cli jar from the build step
COPY --from=build lsq-cli/target/lsq-cli-*-jar-with-dependencies.jar /app/lsq-cli.jar

RUN apt-get update && \
    apt-get install -y wget

# # Install Spark for standalone context in /opt
# ENV APACHE_SPARK_VERSION=3.2.0
# ENV HADOOP_VERSION=3.2
# ENV SPARK_HOME=/opt/spark
# ENV SPARK_OPTS="--driver-java-options=-Xms1024M --driver-java-options=-Xmx2048M --driver-java-options=-Dlog4j.logLevel=info"
# RUN wget -q -O spark.tgz https://archive.apache.org/dist/spark/spark-${APACHE_SPARK_VERSION}/spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
#     tar xzf spark.tgz -C /opt && \
#     rm "spark.tgz" && \
#     ln -s "/opt/spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}" $SPARK_HOME


# Using /data as working directory that will be shared with host for input/output files
WORKDIR /data
VOLUME [ "/data" ]

ENTRYPOINT ["java","-jar","/app/lsq-cli.jar"]
CMD ["-h"]

# Usage:
# docker run -it -v $(pwd):/data ghcr.io/aksw/lsq rx rdfize --endpoint=http://dbpedia.org/sparql virtuoso.dbpedia.log 