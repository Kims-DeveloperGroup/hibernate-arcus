package hibernate.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.*;

@Entity
@NamedQueries(@NamedQuery(name = "domainDataNamedQuery", query = "from ReadWriteAccessDomainData where name = :name"))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = ReadWriteAccessDomainData.CACHE_REGION_NAME)
@NaturalIdCache
@Data
@NoArgsConstructor
public class ReadWriteAccessDomainData {
    public static final String CACHE_REGION_NAME = "DomainData";
    public static final String NATURAL_ID_CACHE_REGION_NAME = CACHE_REGION_NAME + "__NaturalId";

    @Id
    @GeneratedValue(generator = "increment")
    private Long id;

    private String name;
    
    @NaturalId
    private long naturalId;

    public ReadWriteAccessDomainData(String name) {
        this.name = name;
        naturalId = System.currentTimeMillis();
    }
}