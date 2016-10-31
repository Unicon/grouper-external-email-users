CREATE TABLE custom_external_users (mail VARCHAR(100) NOT NULL, givenName VARCHAR(40), surname VARCHAR(40), created_on BIGINT NOT NULL, created_by VARCHAR(40) NOT NULL, updated_on BIGINT, updated_by VARCHAR(40), PRIMARY KEY (mail));
insert into custom_external_users(mail, created_on, created_by) values("tanderson1@example.org", 1, "Grouper");
insert into custom_external_users(mail, givenName, surname, created_on, created_by) values("tanderson2@example.org", "Tom", "Anderson", 1, "Grouper");
