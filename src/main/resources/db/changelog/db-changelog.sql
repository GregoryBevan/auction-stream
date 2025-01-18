--liquibase formatted sql
-- Auction event log

-- changeset me.elgregos:create-auction-event-table
create table if not exists auction_event (
  id uuid primary key,
  created_at timestamp not null default (now() at time zone 'utc'),
  created_by uuid not null,
  version int not null,
  event_type varchar(100) not null,
  aggregate_id uuid not null,
  event jsonb not null);
--rollback drop table if exists auction_event;

-- changeset me.elgregos:create-auction-table
create table if not exists auction (
 id uuid primary key,
 version int not null,
 created_at timestamp not null,
 created_by uuid not null,
 updated_at timestamp not null,
 updated_by uuid not null,
 details jsonb not null);
--rollback drop table if exists auction;
