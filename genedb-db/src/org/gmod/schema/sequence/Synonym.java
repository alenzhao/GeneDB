package org.gmod.schema.sequence;


import static javax.persistence.GenerationType.SEQUENCE;

import org.gmod.schema.cv.CvTerm;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="synonym")
public class Synonym implements Serializable {

    // Fields    
    @SequenceGenerator(name="generator", sequenceName="synonym_synonym_id_seq")
    @Id @GeneratedValue(strategy=SEQUENCE, generator="generator")
    
    @Column(name="synonym_id", unique=false, nullable=false, insertable=true, updatable=true)
     private int synonymId;
     
    @ManyToOne(cascade={},
            fetch=FetchType.LAZY)
        
        @JoinColumn(name="type_id", unique=false, nullable=false, insertable=true, updatable=true)
     private CvTerm cvTerm;
     
    @Column(name="name", unique=false, nullable=false, insertable=true, updatable=true)
     private String name;
     
    @Column(name="synonym_sgml", unique=false, nullable=false, insertable=true, updatable=true)
     private String synonymSgml;
     
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="synonym")
     private Set<FeatureSynonym> featureSynonyms = new HashSet<FeatureSynonym>(0);

     // Constructors

    /** default constructor */
    private Synonym() {
    }

	/** minimal constructor */
    public Synonym(CvTerm cvTerm, String name, String synonymSgml) {
        this.cvTerm = cvTerm;
        this.name = name;
        this.synonymSgml = synonymSgml;
    }
    /** full constructor */
    private Synonym(CvTerm cvTerm, String name, String synonymSgml, Set<FeatureSynonym> featureSynonyms) {
       this.cvTerm = cvTerm;
       this.name = name;
       this.synonymSgml = synonymSgml;
       this.featureSynonyms = featureSynonyms;
    }
    
   
    // Property accessors

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#getSynonymId()
     */
    private int getSynonymId() {
        return this.synonymId;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#setSynonymId(int)
     */
    private void setSynonymId(int synonymId) {
        this.synonymId = synonymId;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#getCvterm()
     */
    public CvTerm getCvTerm() {
        return this.cvTerm;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#setCvterm(org.gmod.schema.cv.CvTermI)
     */
    public void setCvTerm(CvTerm cvterm) {
        this.cvTerm = cvterm;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#getName()
     */
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#getSynonymSgml()
     */
    private String getSynonymSgml() {
        return this.synonymSgml;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#setSynonymSgml(java.lang.String)
     */
    public void setSynonymSgml(String synonymSgml) {
        this.synonymSgml = synonymSgml;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#getFeatureSynonyms()
     */
    private Set<FeatureSynonym> getFeatureSynonyms() {
        return this.featureSynonyms;
    }
    
    /* (non-Javadoc)
     * @see org.genedb.db.jpa.SynonymI#setFeatureSynonyms(java.util.Set)
     */
    private void setFeatureSynonyms(Set<FeatureSynonym> featureSynonyms) {
        this.featureSynonyms = featureSynonyms;
    }




}


