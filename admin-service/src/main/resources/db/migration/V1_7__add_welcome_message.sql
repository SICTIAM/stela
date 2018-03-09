CREATE TABLE instance (
    uuid character varying(255) NOT NULL,
    welcome_message character varying(5000)
);

INSERT INTO instance (uuid, welcome_message) VALUES (uuid_in(md5(random()::text || now()::text)::cstring), '');
