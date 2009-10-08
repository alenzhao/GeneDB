#Checks that genes have atleast one transcript
#Type ids: 792=gene, 321=mRNA, 339=rRNA, 340=tRNA, 361=snRNA

select feature_id, uniquename
from feature gene
where gene.type_id = 792 
and not exists (
    select 8
    from feature_relationship transcript_gene
    join feature transcript on transcript_gene.subject_id = transcript.feature_id
    where transcript_gene.object_id = gene.feature_id
    and   transcript.type_id in (
          321
        , 339
        , 340
        , 361 
    )
)
;
