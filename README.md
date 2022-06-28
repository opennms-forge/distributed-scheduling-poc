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

    **Service Startup Scale Testing**
    $ curl 'http://localhost:8001/ignite-worker/noop-service?count=10000'
    $ curl -X DELETE 'http://localhost:8001/ignite-worker/noop-service?count=10000'

## Play with workflows

   $ curl 'http://localhost:8001/ignite-worker/load-em-up?size=SMALL'

# Network Metrics Reporting

**NOTE** network statistics are read directly from /proc/net/dev for Linux systems; all other systems will fail to report

    # Report on all network devices using default interval (30 seconds)
    $ java -Dpoc.network.stat.enable=true -Dserver.port=8000 -jar ignite-worker/target/ignite-worker-${PROJECT_VERSION}.jar 

    # Report on devices "enp42s0", "lo" and "docker0" only every 2 seconds
    $ java -Dpoc.network.stat.interval=2000 -Dpoc.network.stat.enable=true -Dpoc.network.stat.devs=enp42s0,lo,docker0 -Dserver.port=8000 -jar ignite-worker/target/ignite-worker-${PROJECT_VERSION}.jar 

# Skaffold

    $ skaffold dev


# Performance Notes

* Starting 20,000 no-op services with `curl 'http://localhost:8000/ignite-worker/noop-service?count=20000'`
  * Startup with single node taking about 30s on 1 node

* Starting 50,000 no-op services with `curl 'http://localhost:8000/ignite-worker/noop-service?count=50000'`
  * Startup with single node taking about 250s on 1 node

* /test-thread-startup run with 20,000 threads
  * STARTUP 20000 THREADS: 1s 289ms; check=20000

