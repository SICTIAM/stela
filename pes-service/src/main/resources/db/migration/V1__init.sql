--
-- PostgreSQL database dump
--

-- Dumped from database version 10.1
-- Dumped by pg_dump version 10.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: admin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE admin (
    uuid character varying(255) NOT NULL,
    helios_available boolean NOT NULL,
    unavailability_helios_end_date timestamp without time zone NOT NULL,
    unavailability_helios_start_date timestamp without time zone NOT NULL
);


--
-- Name: attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE attachment (
    uuid character varying(255) NOT NULL,
    date timestamp without time zone,
    file bytea,
    filename character varying(255),
    size bigint NOT NULL
);


--
-- Name: local_authority; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE local_authority (
    uuid character varying(255) NOT NULL,
    active boolean,
    name character varying(255),
    server_code character varying(255),
    siren character varying(255),
    siret character varying(255)
);


--
-- Name: pending_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pending_message (
    uuid character varying(255) NOT NULL,
    date timestamp without time zone,
    file bytea,
    file_name character varying(255),
    message character varying(1024),
    pes_uuid character varying(255)
);


--
-- Name: pes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pes (
    uuid character varying(255) NOT NULL,
    attachment_only boolean NOT NULL,
    comment character varying(255),
    creation timestamp without time zone,
    group_uuid character varying(255),
    objet character varying(500),
    profile_uuid character varying(255),
    signed boolean NOT NULL,
    local_authority_uuid character varying(255),
    pes_receive_uuid character varying(255),
    pes_sent_uuid character varying(255)
);


--
-- Name: pes_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pes_history (
    uuid character varying(255) NOT NULL,
    date timestamp without time zone,
    file bytea,
    file_name character varying(255),
    message character varying(1024),
    pes_uuid character varying(255),
    status character varying(255)
);


--
-- Name: pes_pes_histories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pes_pes_histories (
    pes_uuid character varying(255) NOT NULL,
    pes_histories_uuid character varying(255) NOT NULL
);


--
-- Name: sirens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE sirens (
    local_authority_uuid character varying(255) NOT NULL,
    siren character varying(255)
);


--
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (uuid);


--
-- Name: attachment attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachment
    ADD CONSTRAINT attachment_pkey PRIMARY KEY (uuid);


--
-- Name: local_authority local_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY local_authority
    ADD CONSTRAINT local_authority_pkey PRIMARY KEY (uuid);


--
-- Name: pending_message pending_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pending_message
    ADD CONSTRAINT pending_message_pkey PRIMARY KEY (uuid);


--
-- Name: pes_history pes_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes_history
    ADD CONSTRAINT pes_history_pkey PRIMARY KEY (uuid);


--
-- Name: pes_pes_histories pes_pes_histories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes_pes_histories
    ADD CONSTRAINT pes_pes_histories_pkey PRIMARY KEY (pes_uuid, pes_histories_uuid);


--
-- Name: pes pes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes
    ADD CONSTRAINT pes_pkey PRIMARY KEY (uuid);


--
-- Name: pes_pes_histories uk_b34rbi19jfr0uqvewwtvqx4dw; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes_pes_histories
    ADD CONSTRAINT uk_b34rbi19jfr0uqvewwtvqx4dw UNIQUE (pes_histories_uuid);


--
-- Name: sirens fk241tjnx9ivf6xhugi3lv9x1yw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sirens
    ADD CONSTRAINT fk241tjnx9ivf6xhugi3lv9x1yw FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: pes fk2e61gk9nbsuc50r2t2u12oac4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes
    ADD CONSTRAINT fk2e61gk9nbsuc50r2t2u12oac4 FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: pes_pes_histories fk2mfj5yss2lm7re12l0qrtlyt1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes_pes_histories
    ADD CONSTRAINT fk2mfj5yss2lm7re12l0qrtlyt1 FOREIGN KEY (pes_histories_uuid) REFERENCES pes_history(uuid);


--
-- Name: pes fk5p8o9kxd7bmfyfs285078p5h6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes
    ADD CONSTRAINT fk5p8o9kxd7bmfyfs285078p5h6 FOREIGN KEY (pes_sent_uuid) REFERENCES attachment(uuid);


--
-- Name: pes_pes_histories fk9iw6bi5kf9nyljmnsvcashay0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes_pes_histories
    ADD CONSTRAINT fk9iw6bi5kf9nyljmnsvcashay0 FOREIGN KEY (pes_uuid) REFERENCES pes(uuid);


--
-- Name: pes fkjlrsavd1nh2nmkhyhlv81k9bx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pes
    ADD CONSTRAINT fkjlrsavd1nh2nmkhyhlv81k9bx FOREIGN KEY (pes_receive_uuid) REFERENCES attachment(uuid);


--
-- PostgreSQL database dump complete
--
