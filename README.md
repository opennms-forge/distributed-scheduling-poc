# To Build

    $ mvn clean install

# To Run with Docker

    $ docker run --rm -p 8001:8181 opennms/distributed-scheduling-poc:local 

    # Run with a console and attach to it
    $ docker run -it -a STDIN -a STDOUT -a STDERR --rm -p 8001:8181 opennms/distributed-scheduling-poc:local --

    # Run with debugging enabled
    $ docker run -it -a STDIN -a STDOUT -a STDERR --rm -p 5005:5005 -p 8001:8181 opennms/distributed-scheduling-poc:local debug

# To Run in Karaf Native
Karaf Native = running Karaf directly from the O/S, instead of containerized or within a VM.

First you must patch your karaf bash script to account some odd reflection that Ignite is doing. Look for this section:

      if [ "${VERSION}" -gt "8" ]; then

And then add this in there

      --add-opens java.base/java.nio=ALL-UNNAMED \
     
Note that without this setting, Ignite fails fast with an error such as:

      Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make field long java.nio.Buffer.address accessible: module java.base does not "opens java.nio" to unnamed module @31ce8fe7
    
Also note this problem does not appear to occur on JDK 11, but it does on 17 and 18.

And then run these commands

      ./bin/karaf
      feature:repo-add mvn:org.opennms.poc/features/1.0.0-SNAPSHOT/xml/features
      feature:install ignite-poc

# Using the example docker-compose file

    $ cd tools
    $ docker-compose up

# To Exercise

    $ curl http://localhost:8001/poc/hi-all
    $ curl http://localhost:8002/poc/hi-all
    $ curl http://localhost:8001/poc/hi-youngest
    $ curl http://localhost:8002/poc/hi-youngest
    $ curl http://localhost:8001/poc/hi-oldest
    $ curl http://localhost:8002/poc/hi-oldest

## Service deployment

**NOTE** this service doesn't yet repeat, in spite of its name:

    $ curl http://localhost:8001/poc/hi-all-repeated-service
    $ curl -X DELETE http://localhost:8001/poc/hi-all-repeated-service

    **Service Startup Scale Testing**
    $ curl 'http://localhost:8001/poc/noop-service?count=10000'
    $ curl -X DELETE 'http://localhost:8001/poc/noop-service?count=10000'

## Play with workflows

    $ curl 'http://localhost:8001/poc/load-em-up?size=SMALL'

# Skaffold

* NOTE: this is currently non-functional as it needs updates to use Karaf instead of spring-boot

 
    $ skaffold dev

# UNSOLVED

* Adding linkerd containers to the ignite application pods interferes with the TcpDiscoveryKubernetesIpFinder.
  * Nodes fail to cluster together
  * See log errors such as:

    Caused by: class org.apache.ignite.spi.IgniteSpiOperationTimeoutException: Failed to perform handshake due to timeout (consider increasing 'connectionTimeout' configuration property).
