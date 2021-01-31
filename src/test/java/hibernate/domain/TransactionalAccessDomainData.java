package hibernate.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.*;


@Entity
@NamedQueries(@NamedQuery(name = TransactionalAccessDomainData.NAMED_QUERY_NAME, query = "from TransactionalAccessDomainData where name = :name"))
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = TransactionalAccessDomainData.CACHE_REGION_NAME)
@NaturalIdCache
@Data
@NoArgsConstructor
public class TransactionalAccessDomainData {
    public static final String NAMED_QUERY_NAME = "DomainDataTransactionalNamedQuery";
    public static final String CACHE_REGION_NAME = "DomainDataTransactional";
    public static final String NATURAL_ID_CACHE_REGION_NAME = CACHE_REGION_NAME + "__NaturalId";

    @Id
    private Long id;

    private String name;
    
    @NaturalId
    private long naturalId;

    public TransactionalAccessDomainData(Long idAndNaturalId, String name) {
        this.id = idAndNaturalId;
        this.naturalId = idAndNaturalId;
        this.name = name;
    }
}
