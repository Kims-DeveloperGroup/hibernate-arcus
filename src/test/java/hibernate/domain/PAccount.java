package hibernate.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries(@NamedQuery(name = "pAccountById", query = "from PAccount where id = :id"))
@Data
@NoArgsConstructor
@DynamicUpdate
@Cache(region = PAccount.CACHE_REGION_NAME, usage = CacheConcurrencyStrategy.READ_WRITE)
public class PAccount {
    public static final String CACHE_REGION_NAME = "PAccount";

    @Id
    private Long id;

    private String status;

    private int lastAccessAt = 0;

    public PAccount(Long id, int lastAccessAt, String status) {
        this.id = id;
        this.lastAccessAt = lastAccessAt;
        this.status = status;
    }
}
