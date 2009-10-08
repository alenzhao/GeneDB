
#A polypeptide must always belong to a transcript

select polypeptide.uniquename
from feature polypeptide
where polypeptide.type_id = 191
and not exists (
    select 8
    from feature_relationship polypeptide_transcript
    join feature transcript on polypeptide_transcript.object_id = transcript.feature_id
    where polypeptide_transcript.subject_id = polypeptide.feature_id
    and transcript.type_id in (
      321
    , 604
    )
)
;
