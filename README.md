# hibernate-arcus

### Hibernate 2nd Cache implemetation using [Arcus cache cloud](https://github.com/naver/arcus) <br>
_Arcus is memcached based cache cloud_ <br>
_Hibernate second cache interface puts the result entities into a provided cache storage and reuse them without accessing a database. 
<br>hibernate-arcus offers an implementation, and employs arcus cache cloud as a cache storage_

---
<br>

### Quick Tutorial

#### 1. Add the hibernate-arcus
Available in [MavenCentral](https://search.maven.org/artifact/com.github.kims-developergroup/hibernate-arcus/1.2.0-RELEASE/jar)
```
<dependency>
  <groupId>com.github.kims-developergroup</groupId>
  <artifactId>hibernate-arcus</artifactId>
  <version>1.2.0-RELEASE</version>
</dependency>
```
Snapshot maven repository: https://oss.sonatype.org/content/repositories/snapshots/
<br>
<br>

#### 2. Set the properties below

if you use spring jpa
```
//application.properties

spring.jpa.properties.hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true

spring.jpa.properties.hibernate.cache.arcus.serviceCode=${arcus_service_code}  # required
spring.jpa.properties.hibernate.cache.arcus.poolSize=${arcus_client_pool_size} # default poolSize=1
spring.jpa.properties.hibernate.cache.arcus.host=${hostName}:{$port}           # required
```

or if you use only hibernate
```
//hibernate.properties

hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true

hibernate.cache.arcus.serviceCode=${arcus_service_code}  # required
hibernate.cache.arcus.poolSize=${arcus_client_pool_size} # default poolSize=1
hibernate.cache.arcus.host=${hostName}:{$port}           # required
```
In case that query cache is not required, and set `hibernate.cache.use_query_cache=false` <br>
However, `hibernate.cache.use_query_cache=true` is necessary set with `hibernate.cache.use_second_level_cache=true`
<br><br>

#### 3. How to use cache
1. Attach @Cache annotation to an entity class <br>
Entities are cached with an id. 
```
@Cache(usage = ${CacheConcurrencyStrategy}, region = "${regionName}")
public static class ParentDomainData implements Serializable {
  @Id
  long id;
...
}
```
_Note: **when changing CacheConcurrencyStrategy, regionName also should be modified not to reuse exsting cache items**.<br>
For more detail: [When @Cache.usage changes, casting exception is thrown](https://github.com/Kims-DeveloperGroup/hibernate-arcus/issues/1)_
<br><br>

2. @NaturalId <br>
You may want to cache entities with a natural id, then attach @NaturalId annotation
```
@Cache(usage = ${CacheConcurrencyStrategy}, region = "${regionName}")
public static class ParentDomainData implements Serializable {
...
@NaturalId
long someNaturalId;
...
}
```

3. @QueryHint <br>
You may want to cache queries, then attach @QueryHint
```
@QueryHints(value = {@QueryHint(value="true", name = HINT_CACHEABLE)})
    Optional<Entity> findByName(String name);
```
----
<br><br>

### How to run tests
1. Run arcus cache locally _(DockerImage is ready [here](https://hub.docker.com/repository/docker/devookim/arcus-memcached))_ <br>

Just run the docker-compose in the project root
```
docker-compose up
```
 2.
```
gradlew clean test --info
```
---

### Additional Configuration Properties
```
hibernate.cache.arcus.fallbackEnabled  (default: true) // whether to use fallback mode or not.
hibernate.cache.arcus.initFallbackMode (default: false) // Initial value of fallback mode
hibernate.cache.arcus.reconnectIntervalInSec (default: 10000) unit mills // ArcusClient retry connection interval in seconds
hibernate.cache.arcus.opTimeout (default 10000) unit mills // arcus client cache operation timeout.
hibernate.cache.arcus.healthCheckIntervalInSec(default 10) unit sec // interval time of health check
hibernate.cache.arcus.domainData.evictionRegionGroupOnCacheUpdate(default "") // domain data region group to be evicted on cache update
```

### Contribution
_Always welcome all shapes of your contribution and use-cases_
