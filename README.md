# hibernate-arcus

Hibernate 2nd Cache implemetation using [Arcus cache cloud](https://github.com/naver/arcus) <br>
_Arcus is memcached based cache cloud_


### Quick Tutorial

1. Add the hibernate-arcus <br>
_Maven Gradle not supported yet_

2. Set the properties below

if you use spring jpa
`
spring.jpa.properties.hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true
`

or if you use only hibernate
`
spring.jpa.properties.hibernate.cache.region.factory_class=com.devookim.hibernatearcus.factory.HibernateArcusRegionFactory
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true
`

3. Attach @Cache annotation to a entity class <br>
`
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "ParentDomainData")
`
