#### add below
```xml
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-reactive-routes</artifactId>
        </dependency>

        <dependency>
            <groupId>io.vertx</groupId>
            <artifactId>vertx-http-proxy</artifactId>
        </dependency>
```

* rename to AMProxy.java
* makes sure istio sends /ctrl to fabrik.panorama.svc.cluster.local
* remove envoyFilter and put the filter in AMProxy
* 


#### goal
```
/ctrl to be owned by quarkus
true BFF
openam an auth engine only
```
