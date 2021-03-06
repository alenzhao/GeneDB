package org.gmod.schema.mapped;

// Generated Aug 31, 2006 4:02:18 PM by Hibernate Tools 3.2.0.beta7

import org.gmod.schema.utils.propinterface.PropertyI;

import static javax.persistence.GenerationType.SEQUENCE; //Added explicit sequence generation behaviour 2.12.2015

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;

/**
 * PhylonodeProp generated by hbm2java
 */
@Entity
@Table(name = "phylonodeprop", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "phylonode_id", "type_id", "value", "rank" }) })
public class PhylonodeProp implements java.io.Serializable, PropertyI {

    // Fields
	@SequenceGenerator(name = "generator", sequenceName = "phylonodeprop_phylonodeprop_id_seq",  allocationSize=1)
    @Id @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "phylonodeprop_id", unique = true, nullable = false, insertable = true, updatable = true)
    private int phylonodePropId;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", unique = false, nullable = false, insertable = true, updatable = true)
    private CvTerm cvTerm;

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "phylonode_id", unique = false, nullable = false, insertable = true, updatable = true)
    private Phylonode phylonode;

    @Column(name="value", unique=false, nullable=false, insertable=true, updatable=true)
    private String value;

    @Column(name = "rank", unique = false, nullable = false, insertable = true, updatable = true)
    private int rank;

    // Constructors

    PhylonodeProp() {
        // Deliberately empty default constructor
    }

    /** full constructor */
    public PhylonodeProp(Phylonode phylonode, CvTerm type, String value, int rank) {
        this.cvTerm = type;
        this.phylonode = phylonode;
        this.value = value;
        this.rank = rank;
    }

    // Property accessors
    public int getPhylonodePropId() {
        return this.phylonodePropId;
    }

    public CvTerm getType() {
        return this.cvTerm;
    }

    void setType(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public Phylonode getPhylonode() {
        return this.phylonode;
    }

    void setPhylonode(Phylonode phylonode) {
        this.phylonode = phylonode;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getRank() {
        return this.rank;
    }
}
