package org.gmod.schema.phylogeny;
// Generated Aug 31, 2006 4:02:18 PM by Hibernate Tools 3.2.0.beta7


import org.gmod.schema.pub.Pub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * PhylotreePub generated by hbm2java
 */
@Entity
@Table(name="phylotree_pub", uniqueConstraints = { @UniqueConstraint( columnNames = { "phylotree_id", "pub_id" } ) })
public class PhylotreePub  implements java.io.Serializable {

    // Fields    

     private int phylotreePubId;
     private Phylotree phylotree;
     private Pub pub;

     // Constructors

    /** default constructor */
    public PhylotreePub() {
    	// Deliberately empty default constructor
    }

    /** full constructor */
    public PhylotreePub(int phylotreePubId, Phylotree phylotree, Pub pub) {
       this.phylotreePubId = phylotreePubId;
       this.phylotree = phylotree;
       this.pub = pub;
    }
   
    // Property accessors
     @Id
    
    @Column(name="phylotree_pub_id", unique=true, nullable=false, insertable=true, updatable=true)
    public int getPhylotreePubId() {
        return this.phylotreePubId;
    }
    
    public void setPhylotreePubId(int phylotreePubId) {
        this.phylotreePubId = phylotreePubId;
    }
@ManyToOne(cascade={}, fetch=FetchType.LAZY)
    
    @JoinColumn(name="phylotree_id", unique=false, nullable=false, insertable=true, updatable=true)
    public Phylotree getPhylotree() {
        return this.phylotree;
    }
    
    public void setPhylotree(Phylotree phylotree) {
        this.phylotree = phylotree;
    }
@ManyToOne(cascade={}, fetch=FetchType.LAZY)
    
    @JoinColumn(name="pub_id", unique=false, nullable=false, insertable=true, updatable=true)
    public Pub getPub() {
        return this.pub;
    }
    
    public void setPub(Pub pub) {
        this.pub = pub;
    }




}


