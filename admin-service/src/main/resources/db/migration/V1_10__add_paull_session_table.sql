CREATE TABLE paull_connection (
    session_id character varying(32) NOT NULL,
 	token character varying(1000) NOT NULL
);

ALTER TABLE ONLY paull_connection
    ADD CONSTRAINT paull_connection_pkey PRIMARY KEY (session_id);