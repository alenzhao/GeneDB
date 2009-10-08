#Pubmed ids have reasonable unique name

select *
from pub
where uniquename like 'PMID:%'
and uniquename !~ E'^PMID:[0-9]+$'
;
