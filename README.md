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
```

or if you use only hibernate
```
//hibernate.properties

hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true
```

#### 3. Attach @Cache annotation to a entity class <br>
```
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "ParentDomainData")
public static class ParentDomainData implements Serializable {
...
}           
```

### How to run tests
1. Run arcus cache locally <br>
Run the docker-compose in the project root
```
docker-compose up
```
 2.
```
gradlew clean test --info
```
