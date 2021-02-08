# hibernate-arcus

### Hibernate 2nd Cache implemetation using [Arcus cache cloud](https://github.com/naver/arcus) <br>
Arcus is memcached based cache cloud_


### Quick Tutorial

#### 1. Add the hibernate-arcus
_Maven Gradle not supported yet_

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

#### 3. Attach @Cache annotation to a entity class <br>
```
@Cache(usage = ${CacheConcurrencyStrategy}, region = "${regionName}")
public static class ParentDomainData implements Serializable {
...
}           
```

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
