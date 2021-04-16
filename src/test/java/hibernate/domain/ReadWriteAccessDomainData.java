package hibernate.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(
        {@NamedQuery(name = "domainDataNamedQuery", query = "from ReadWriteAccessDomainData where name = :name"),
                @NamedQuery(name = "domainDataNamedQueryById", query = "from ReadWriteAccessDomainData where id = :id")}
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = ReadWriteAccessDomainData.CACHE_REGION_NAME)
@NaturalIdCache
@Data
@NoArgsConstructor
@DynamicUpdate
public class ReadWriteAccessDomainData {
    public static final String CACHE_REGION_NAME = "DomainData";
    public static final String NATURAL_ID_CACHE_REGION_NAME = CACHE_REGION_NAME + "__NaturalId";

    @Id
    private Long id;

    private String name;

    public int value = 0;

    @NaturalId
    private long naturalId;

    @NaturalId
    private String naturalId2;

    public ReadWriteAccessDomainData(Long idAndNaturalId, String name) {
        this.name = name;
        id = idAndNaturalId;
        naturalId = idAndNaturalId;
        naturalId2 = name + idAndNaturalId;
    }
}
