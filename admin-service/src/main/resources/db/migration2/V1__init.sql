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
-- Name: agent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE agent (
    uuid character varying(255) NOT NULL,
    admin boolean NOT NULL,
    email character varying(255) NOT NULL,
    family_name character varying(255),
    given_name character varying(255),
    sub character varying(255) NOT NULL
);


--
-- Name: group_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE group_profiles (
    groups_uuid character varying(255) NOT NULL,
    profiles_uuid character varying(255) NOT NULL,
    group_uuid character varying(255) NOT NULL,
    group_group_uuid character varying(255) NOT NULL,
    profiles_profile_uuid character varying(255) NOT NULL
);


--
-- Name: group_to_profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE group_to_profile (
    groups_uuid character varying(255) NOT NULL,
    profiles_profile_uuid character varying(255) NOT NULL
);


--
-- Name: local_authority; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE local_authority (
    uuid character varying(255) NOT NULL,
    name character varying(255),
    client_id character varying(255),
    client_secret character varying(255),
    creator_id character varying(255),
    creator_name character varying(255),
    dc_id character varying(255),
    destruction_secret character varying(255),
    instance_id character varying(255),
    instance_registration_uri character varying(255),
    kernel_id character varying(255),
    notified_to_kernel boolean NOT NULL,
    service_id character varying(255),
    status_changed_secret character varying(255),
    siren character varying(255)
);


--
-- Name: local_authority_activated_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE local_authority_activated_modules (
    local_authority_uuid character varying(255) NOT NULL,
    activated_modules character varying(255)
);


--
-- Name: profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE profile (
    uuid character varying(255) NOT NULL,
    admin boolean,
    agent_uuid character varying(255),
    local_authority_uuid character varying(255),
    profile_uuid character varying(255) NOT NULL
);


--
-- Name: profile_groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE profile_groups (
    profile_uuid character varying(255) NOT NULL,
    groups_uuid character varying(255) NOT NULL,
    profile_profile_uuid character varying(255) NOT NULL,
    groups_group_uuid character varying(255) NOT NULL
);


--
-- Name: schema_version; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE schema_version (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: working_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE working_group (
    uuid character varying(255) NOT NULL,
    local_authority_uuid character varying(255)
);


--
-- Name: agent agent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT agent_pkey PRIMARY KEY (uuid);


--
-- Name: group_profiles group_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_profiles
    ADD CONSTRAINT group_profiles_pkey PRIMARY KEY (groups_uuid, profiles_uuid);


--
-- Name: group_to_profile group_to_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_to_profile
    ADD CONSTRAINT group_to_profile_pkey PRIMARY KEY (groups_uuid, profiles_profile_uuid);


--
-- Name: local_authority local_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY local_authority
    ADD CONSTRAINT local_authority_pkey PRIMARY KEY (uuid);


--
-- Name: profile_groups profile_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile_groups
    ADD CONSTRAINT profile_groups_pkey PRIMARY KEY (profile_uuid, groups_uuid);


--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (uuid);


--
-- Name: schema_version schema_version_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY schema_version
    ADD CONSTRAINT schema_version_pk PRIMARY KEY (installed_rank);


--
-- Name: agent uk_1esd56rr9t2fut3w5pjic0f9o; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT uk_1esd56rr9t2fut3w5pjic0f9o UNIQUE (sub);


--
-- Name: local_authority uk_quuxjcbv1uoyf11682s27h3nk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY local_authority
    ADD CONSTRAINT uk_quuxjcbv1uoyf11682s27h3nk UNIQUE (siren);


--
-- Name: working_group working_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY working_group
    ADD CONSTRAINT working_group_pkey PRIMARY KEY (uuid);


--
-- Name: schema_version_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX schema_version_s_idx ON schema_version USING btree (success);


--
-- Name: group_profiles fk4dqc8khtkmq32o3juyqpu2fcw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_profiles
    ADD CONSTRAINT fk4dqc8khtkmq32o3juyqpu2fcw FOREIGN KEY (profiles_profile_uuid) REFERENCES profile(uuid);


--
-- Name: working_group fk4xlhskog99gd9hj5120hh9po; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY working_group
    ADD CONSTRAINT fk4xlhskog99gd9hj5120hh9po FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: profile fk6km3qhkb0oxknl605eq9rgb53; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT fk6km3qhkb0oxknl605eq9rgb53 FOREIGN KEY (agent_uuid) REFERENCES agent(uuid);


--
-- Name: group_to_profile fk88ym99gel0s3gkhn9e8ythm47; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_to_profile
    ADD CONSTRAINT fk88ym99gel0s3gkhn9e8ythm47 FOREIGN KEY (groups_uuid) REFERENCES working_group(uuid);


--
-- Name: group_profiles fka8mhmm5vo7jacga4su6dno8kv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_profiles
    ADD CONSTRAINT fka8mhmm5vo7jacga4su6dno8kv FOREIGN KEY (profiles_uuid) REFERENCES profile(uuid);


--
-- Name: profile_groups fkbiqyftuhv1qy7oxno13ftq2bb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile_groups
    ADD CONSTRAINT fkbiqyftuhv1qy7oxno13ftq2bb FOREIGN KEY (profile_uuid) REFERENCES profile(uuid);


--
-- Name: profile_groups fkkck28y9xbva3mgnvr2xkjwurs; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile_groups
    ADD CONSTRAINT fkkck28y9xbva3mgnvr2xkjwurs FOREIGN KEY (profile_profile_uuid) REFERENCES profile(uuid);


--
-- Name: profile fkllnpsqqh0sl7w2w9yk5brk9e8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT fkllnpsqqh0sl7w2w9yk5brk9e8 FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: group_to_profile fkm7l7md3hpen5cv406xf5jwhab; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_to_profile
    ADD CONSTRAINT fkm7l7md3hpen5cv406xf5jwhab FOREIGN KEY (profiles_profile_uuid) REFERENCES profile(uuid);


--
-- Name: local_authority_activated_modules fkq3io6ryxenco5lv6xi6lqakx0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY local_authority_activated_modules
    ADD CONSTRAINT fkq3io6ryxenco5lv6xi6lqakx0 FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- PostgreSQL database dump complete
--


