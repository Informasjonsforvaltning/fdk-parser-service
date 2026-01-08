# fdk-parser-service

This service is responsible for publishing RDF parse events. The service consumes reasoned events (Kafka) and parses RDF from these events to JSON.

## Requirements

- maven
- java 21
- docker
- docker-compose

## Generate sources

Kafka messages are serialized using Avro. Avro schema's are located in the kafka/schemas directory.
To generate sources from Avro schema, run the following command:

```
mvn generate-sources
```

## Run tests

```
mvn test
```

## Run locally

### Start Kafka cluster and setup topics/schemas

Topics and schemas are setup automatically when starting the Kafka cluster.
Docker compose uses the scripts create-topics.sh and create-schemas.sh to setup topics and schemas.

```
docker-compose up -d
```

If you have problems starting kafka, check if all health checks are ok.
Make sure number at the end (after 'grep') matches desired topics.

### Start parser service

Start the service locally using maven. Use Spring profile **develop**.

```
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```

### Produce messages

Check if schema id is correct in the produce-messages.sh script. This should be 1 if there
is only one schema in your registry.

```
sh ./kafka/produce-messages.sh
```
