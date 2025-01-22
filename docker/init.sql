create database "auction-stream";
create user admin_user with password 'guess-what';
grant all privileges on database "auction-stream" to admin_user;
alter database "auction-stream" owner to admin_user;