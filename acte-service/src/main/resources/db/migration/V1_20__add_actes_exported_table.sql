CREATE TABLE acte_export (
    uuid character varying(255) NOT NULL,
    acte_uuid character varying(255) NOT NULL,
    transmission_date_time timestamp with time zone,
    file_name character varying(255),
    files_name_list character varying(2000),
    siren character varying(255),
    department character varying(255),
    district character varying(255),
    agent_name character varying(255),
    agent_first_name character varying(255),
    agent_email character varying(255)

);