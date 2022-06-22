# To Run

    $ PROJECT_VERSION="$(mvn -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive -q org.codehaus.mojo:exec-maven-plugin:1.6.0:exec)"
    $ mvn clean install
    $ java -Dserver.port=8000 -jar ignite-worker/target/ignite-worker-${PROJECT_VERSION}.jar
    $ java -Dserver.port=8001 -jar ignite-worker/target/ignite-worker-${PROJECT_VERSION}.jar

# To Exercise

    $ curl http://localhost:8000/ignite-worker/hi-all
    $ curl http://localhost:8001/ignite-worker/hi-all
    $ curl http://localhost:8000/ignite-worker/hi-youngest
    $ curl http://localhost:8001/ignite-worker/hi-youngest
    $ curl http://localhost:8000/ignite-worker/hi-oldest
    $ curl http://localhost:8001/ignite-worker/hi-oldest

## Service deployment

**NOTE** this service doesn't yet repeat, in spite of its name:

    $ curl http://localhost:8001/ignite-worker/hi-all-repeated-service
    $ curl -X DELETE http://localhost:8001/ignite-worker/hi-all-repeated-service
