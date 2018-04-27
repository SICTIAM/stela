
CREATE TABLE agent_connection (
    uuid character varying(255) NOT NULL,
    connection_date timestamp without time zone NOT NULL,
 	profile_uuid character varying(255) NOT NULL
);

ALTER TABLE ONLY agent_connection
    ADD CONSTRAINT agent_connection_pkey PRIMARY KEY (uuid);
    
ALTER TABLE ONLY agent_connection
    ADD CONSTRAINT agent_connection_profile_fkey FOREIGN KEY (profile_uuid) REFERENCES profile(uuid);