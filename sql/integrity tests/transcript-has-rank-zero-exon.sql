
#Transcript has rank zero exon

select organism.common_name as organism
     , transcript.uniquename as transcript_uniquename
     , chromosome.uniquename as chromosome
     , featureloc.fmin
     , featureloc.fmax
from feature transcript
join organism using (organism_id)
join featureloc using (feature_id)
join feature chromosome on featureloc.srcfeature_id = chromosome.feature_id
where transcript.type_id in (
      321 
    , 339 
    , 340 
    , 361 
)
and not exists (
    select *
    from feature_relationship exon_transcript
    join feature exon on exon_transcript.subject_id = exon.feature_id
    where exon_transcript.object_id = transcript.feature_id
    and   exon.type_id = 234 
    and   exon_transcript.rank = 0
)
;

