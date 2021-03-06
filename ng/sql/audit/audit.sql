-- Autogenerated on Tue May 26 17:40:01 2009 by mkaudit.pl

drop schema audit cascade;
create schema audit;
grant usage on schema audit to chado_rw_role, chado_ro_role;
create sequence audit.audit_seq;
grant usage on sequence audit.audit_seq to chado_rw_role, chado_ro_role;

create table audit.audit (
  audit_id   integer default nextval('audit.audit_seq' :: regclass) not null primary key
, type       character varying not null
, username   character varying not null default user
, time       timestamp without time zone not null default now()
);
grant select on audit.audit to chado_ro_role;
grant select on audit.audit to chado_rw_role;

create table audit.checkpoint (
  key      character varying not null primary key
, audit_id integer not null
);
grant select, insert, update on audit.checkpoint to chado_ro_role;
grant all on audit.checkpoint to chado_rw_role;

\i audit_cvterm.sql
\i audit_cvterm_dbxref.sql
\i audit_dbxref.sql
\i audit_feature.sql
\i audit_feature_cvterm.sql
\i audit_feature_cvterm_dbxref.sql
\i audit_feature_cvterm_pub.sql
\i audit_feature_cvtermprop.sql
\i audit_feature_dbxref.sql
\i audit_feature_pub.sql
\i audit_feature_relationship.sql
\i audit_feature_synonym.sql
\i audit_featureloc.sql
\i audit_featureprop.sql
\i audit_featureprop_pub.sql
\i audit_organism.sql
\i audit_organismprop.sql
\i audit_pub.sql
\i audit_pub_dbxref.sql
\i audit_synonym.sql
