package org.gmod.schema.phylogeny;
// Generated Aug 31, 2006 4:02:18 PM by Hibernate Tools 3.2.0.beta7


import org.gmod.schema.general.DbXRef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * PhylonodeDbXRef generated by hbm2java
 */
@Entity
@Table(name="phylonode_dbxref", uniqueConstraints = { @UniqueConstraint( columnNames = { "phylonode_id", "dbxref_id" } ) })
public class PhylonodeDbXRef  implements java.io.Serializable {

    // Fields    

     private int phylonodeDbXRefId;
     private DbXRef dbXRef;
     private Phylonode phylonode;

     // Constructors

    /** default constructor */
    public PhylonodeDbXRef() {
    	// Deliberately empty default constructor
    }

    /** full constructor */
    public PhylonodeDbXRef(int phylonodeDbXRefId, DbXRef dbXRef, Phylonode phylonode) {
       this.phylonodeDbXRefId = phylonodeDbXRefId;
       this.dbXRef = dbXRef;
       this.phylonode = phylonode;
    }
   
    // Property accessors
     @Id
    
    @Column(name="phylonode_dbxref_id", unique=true, nullable=false, insertable=true, updatable=true)
    public int getPhylonodeDbXRefId() {
        return this.phylonodeDbXRefId;
    }
    
    public void setPhylonodeDbXRefId(int phylonodeDbXRefId) {
        this.phylonodeDbXRefId = phylonodeDbXRefId;
    }
    
@ManyToOne(cascade={},fetch=FetchType.LAZY)
    
    @JoinColumn(name="dbxref_id", unique=false, nullable=false, insertable=true, updatable=true)
    public DbXRef getDbXRef() {
        return this.dbXRef;
    }
    
    public void setDbXRef(DbXRef dbXRef) {
        this.dbXRef = dbXRef;
    }
@ManyToOne(cascade={}, fetch=FetchType.LAZY)
    @JoinColumn(name="phylonode_id", unique=false, nullable=false, insertable=true, updatable=true)
    public Phylonode getPhylonode() {
        return this.phylonode;
    }
    
    public void setPhylonode(Phylonode phylonode) {
        this.phylonode = phylonode;
    }




}


