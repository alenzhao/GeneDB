-- Autogenerated on Tue May 26 17:40:05 2009 by mkaudit.pl

create table audit.synonym (
    synonym_sgml character varying(255) not null
  , type_id integer not null
  , name character varying(255) not null
  , synonym_id integer not null
) inherits (audit.audit);

create or replace function audit.audit_synonym_insert_proc()
returns trigger
as $$
BEGIN
  raise exception 'Cannot insert directly into audit.synonym. Use one of the child tables.';
END;
$$ language plpgsql;
create trigger synonym_insert_tr before insert on audit.synonym
    for each statement execute procedure audit.audit_synonym_insert_proc();
grant select on audit.synonym to chado_ro_role;
grant select, insert on audit.synonym to chado_rw_role;
grant execute on function audit.audit_synonym_insert_proc() to chado_rw_role;


create table audit.synonym_insert (
    constraint synonym_insert_ck check (type = 'INSERT')
) inherits (audit.synonym);
alter table audit.synonym_insert alter type set default 'INSERT';
grant select on audit.synonym_insert to chado_ro_role;
grant select, insert on audit.synonym_insert to chado_rw_role;

create or replace function audit.public_synonym_insert_proc()
returns trigger
as $$
BEGIN
  insert into audit.synonym_insert (
      synonym_id, name, type_id, synonym_sgml
  ) values (
      new.synonym_id, new.name, new.type_id, new.synonym_sgml
  );
  return new;
END;
$$ language plpgsql;
create trigger synonym_audit_insert_tr after insert on public.synonym
    for each row execute procedure audit.public_synonym_insert_proc();
grant execute on function audit.public_synonym_insert_proc() to chado_rw_role;


create table audit.synonym_update (
    constraint synonym_update_ck check (type = 'UPDATE')
  , old_type_id integer not null
  , old_synonym_sgml character varying(255) not null
  , old_name character varying(255) not null
) inherits (audit.synonym);
alter table audit.synonym_update alter type set default 'UPDATE';
grant select on audit.synonym_update to chado_ro_role;
grant select, insert on audit.synonym_update to chado_rw_role;

create or replace function audit.public_synonym_update_proc()
returns trigger
as $$
BEGIN
  if old.synonym_id <> new.synonym_id or old.synonym_id is null <> new.synonym_id is null then
    raise exception 'If you want to change synonym.synonym_id (do you really?) then disable the audit trigger synonym_audit_update_tr';
  end if;
  insert into audit.synonym_update (
      synonym_id, name, type_id, synonym_sgml,
      old_name, old_type_id, old_synonym_sgml
   ) values (
       new.synonym_id, new.name, new.type_id, new.synonym_sgml,
       old.name, old.type_id, old.synonym_sgml
   );
  return new;
END;
$$ language plpgsql;
create trigger synonym_audit_update_tr after update on public.synonym
    for each row execute procedure audit.public_synonym_update_proc();
grant execute on function audit.public_synonym_update_proc() to chado_rw_role;


create table audit.synonym_delete (
    constraint synonym_delete_ck check (type = 'DELETE')
) inherits (audit.synonym);
alter table audit.synonym_delete alter type set default 'DELETE';
grant select on audit.synonym_delete to chado_ro_role;
grant select, insert on audit.synonym_delete to chado_rw_role;

create or replace function audit.public_synonym_delete_proc()
returns trigger
as $$
BEGIN
  insert into audit.synonym_delete (
      synonym_id, name, type_id, synonym_sgml
  ) values (
      old.synonym_id, old.name, old.type_id, old.synonym_sgml
  );
  return old;
END;
$$ language plpgsql;
create trigger synonym_audit_delete_tr after delete on public.synonym
    for each row execute procedure audit.public_synonym_delete_proc();
grant execute on function audit.public_synonym_delete_proc() to chado_rw_role;
