![Confluent Cloud](images/confluent_cloud.png#center)

# Confluent Cloud Workshop
This workshop has been created to get developers up-to-speed in how to implement Apache Kafka based applications using [Confluent Cloud](https://www.confluent.io/confluent-cloud). This workshop doesn't intend to teach the basis of Apache Kafka, therefore developers participating of this workshop should have previous familiarity with Kafka.

## Requirements

- Software VirtualBox installed: https://www.virtualbox.org/wiki/Downloads
- VM containing the environment: https://riferrei-sharing.s3-accelerate.amazonaws.com/confluent-workshop.zip
- Access to the internet (Download, Read)
- Access to endpoints over the port 9092

## Overview

The interceptors from the [OpenTracing Apache Kafka Client project](https://github.com/opentracing-contrib/java-kafka-client) are great implementations to use with Java-based applications where you own the code and are able to instantiate your own tracers and make them available throughout the JVM. However, there are situations in which records will be produced and consumed from JVMs that are automatically created for you, and you don't have any way to instantiate your own tracers because its source-code is not available. Or maybe it is, but you are not allowed to change it.

Typical examples include the use of [Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html), [REST Proxy](https://docs.confluent.io/current/kafka-rest/docs/index.html), [Kafka Connect](https://docs.confluent.io/current/connect/index.html), and [KSQL Servers](https://docs.confluent.io/current/ksql/docs/index.html). In these technologies, the JVMs are automatically created by pre-defined scripts and you would need a special type of interceptor -- one that can instantiate their own tracers based on configuration specified in an external file. For this particular situation, you can use the Jaeger Tracing support provided by this project.

![Sample](images/sample.png)

The example above shows the tracing across different Kafka topics. The first topic called `test` received a record sent by REST Proxy, which in turn had the interceptors installed. Then, the record is consumed by a stream called `FIRST_STREAM` created on KSQL, which also had the interceptors installed. Still in KSQL; multiple other streams (`SECOND_STREAM`, `THIRD_STREAM`, etc) were created to copy data from/to another stream, to test if the tracing can keep up with the flow.

## Usage

For example, if you want to use these interceptors with a KSQL Server, you need to edit the properties configuration file used to start the server and include the following lines:

```java
bootstrap.servers=localhost:9092
listeners=http://localhost:8088
auto.offset.reset=earliest

############################## Jaeger Tracing Configuration ################################

producer.interceptor.classes=io.confluent.devx.util.KafkaTracingProducerInterceptor
consumer.interceptor.classes=io.confluent.devx.util.KafkaTracingConsumerInterceptor

############################################################################################
```

By default, the interceptors will leverage any tracers registered using an [TracerResolver](https://github.com/opentracing-contrib/java-tracerresolver). Using this technique allows the interceptors to be tracer agonostic, and you are free to use any tracer that you are comfortable with.

### Customizing the Interceptors Behavior

If you plan to have multiple services deployed in a single JVM, then it is a best practice to clearly separate the services according to the topics that they use. Each service will have a set of topics they will operate, so it doesn't make any sense of having a single tracer for all of them. The good news is that you can solve this problem by specifying a JSON configuration file that details the relationship between services and topics, as well as the details of each tracer. This JSON configuration file can be specified using the environment variable `INTERCEPTORS_CONFIG_FILE`, as shown below.

```bash
export INTERCEPTORS_CONFIG_FILE=/etc/jaeger/ext/interceptorsConfig.json
```

Here is an example of the JSON configuration file:

```json
{
   "services":[
      {
         "service":"CustomerService",
         "config":{
            "sampler":{
               "type":"const",
               "param":1
            },
            "reporter":{
               "logSpans":true,
               "flushIntervalMs":1000,
               "maxQueueSize":10
            }
         },
         "topics":[
            "Topic-1",
            "Topic-2",
            "Topic-3"
         ]
      },
      {
         "service":"ProductService",
         "config":{
            "reporter":{
               "logSpans":false,
               "maxQueueSize":100
            }
         },
         "topics":[
            "Topic-4",
            "Topic-5"
         ]
      }
   ]
}
```
In the example above, two services were defined, `CustomerService` and `ProductService` respectively. In runtime, it will be created one tracer for each one of these services. However, every time a record is either produced or consumed for topic `Topic-1`, the interceptor knows that it should use the tracer associated for `CustomerService`, as well as every time a record is either produced or consumed for topic `Topic-4`, the interceptor knows that it should use the tracer associated for `ProductService`.

You can also use the JSON configuration file to customize the behavior of each tracer. This is particularly important if you want to fine-tune how the tracer behaves in terms of emitting traces, how it handles logs and also the details of the samplers and the reporters. Finally, you may also use the JSON configuration file to change the way traces are sent to the collectors, switching from Thrift UDP packages to plain HTTP requests.

Please keep in mind that this feature of customizing the interceptor behavior using a configuration file requires the usage of Jaeger as distributed tracing technology. Support for other tracers may be added in the future though.

## Dependencies

These are the dependencies that you will need to install in your classpath along with the interceptors:

```xml
<dependency>
    <groupId>io.opentracing</groupId>
    <artifactId>opentracing-api</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
    <groupId>io.opentracing</groupId>
    <artifactId>opentracing-util</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-kafka-client</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
   <groupId>io.opentracing.contrib</groupId>
   <artifactId>opentracing-tracerresolver</artifactId>
   <version>VERSION</version>
</dependency>

<dependency>
    <groupId>io.jaegertracing</groupId>
    <artifactId>jaeger-client</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
    <groupId>io.jaegertracing</groupId>
    <artifactId>jaeger-core</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
    <groupId>io.opentracing</groupId>
    <artifactId>opentracing-noop</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
    <groupId>io.jaegertracing</groupId>
    <artifactId>jaeger-thrift</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
    <groupId>org.apache.thrift</groupId>
    <artifactId>libthrift</artifactId>
    <version>VERSION</version>
</dependency>

<dependency>
   <groupId>com.google.code.gson</groupId>
   <artifactId>gson</artifactId>
   <version>VERSION</version>
</dependency>
```

## License

[Apache 2.0 License](./LICENSE).