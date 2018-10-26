CREATE TABLE pes_export (
    uuid character varying(255) NOT NULL,
    pes_uuid character varying(255) NOT NULL,
    transmission_date_time timestamp with time zone,
    file_name character varying(255),
    size bigint NOT NULL,
    sha1 character varying(255),
    siren character varying(255),
    agent_name character varying(255),
    agent_first_name character varying(255),
    agent_email character varying(255)
);