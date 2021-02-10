# hibernate-arcus

### Hibernate 2nd Cache implemetation using [Arcus cache cloud](https://github.com/naver/arcus) <br>
_Arcus is memcached based cache cloud_ <br>
_Hibernate second cache interface puts the result entities into a provided cache storage and reuse them without accessing a database. 
<br>hibernate-arcus offers an implementation, and employs arcus cache cloud as a cache storage_

---
<br>

### Quick Tutorial

#### 1. Add the hibernate-arcus
_MavenCentral Deployment in progress_ <br>
Request jira ticket: https://issues.sonatype.org/browse/OSSRH-64363
<br>
<br>
#### 2. Set the properties below

if you use spring jpa
```
//application.properties

spring.jpa.properties.hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true

spring.jpa.properties.hibernate.cache.arcus.serviceCode=${arcus_service_code}
spring.jpa.properties.hibernate.cache.arcus.poolSize=${arcus_client_pool_size}
spring.jpa.properties.hibernate.cache.arcus.host=${hostName}:{$port}
```

or if you use only hibernate
```
//hibernate.properties

hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true

hibernate.cache.arcus.serviceCode=${arcus_service_code}
hibernate.cache.arcus.poolSize=${arcus_client_pool_size}
hibernate.cache.arcus.host=${hostName}:{$port}
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
    Optional<PayAccount> findByName(String name);
```
----
<br><br>

### How to run tests
1. Run arcus cache locally (_docker image is ready. https://hub.docker.com/repository/docker/devookim/arcus-memcached_) <br>

Just run the docker-compose in the project root
```
docker-compose up
```
 2.
```
gradlew clean test --info
```
---


### Contribution
_Always welcome all shapes of your contribution and use-cases_
