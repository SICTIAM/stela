
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
-- Name: group_to_profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE group_to_profile (
    groups_uuid character varying(255) NOT NULL,
    profiles_uuid character varying(255) NOT NULL
);



--
-- Name: profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE profile (
    uuid character varying(255) NOT NULL,
    admin boolean,
    agent_uuid character varying(255),
    local_authority_uuid character varying(255)
);


--
-- Name: work_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE work_group (
    uuid character varying(255) NOT NULL,
    name character varying(255),
    local_authority_uuid character varying(255)
);


--
-- Name: agent agent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT agent_pkey PRIMARY KEY (uuid);


--
-- Name: group_to_profile group_to_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_to_profile
    ADD CONSTRAINT group_to_profile_pkey PRIMARY KEY (groups_uuid, profiles_uuid);

--
-- Name: profile profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT profile_pkey PRIMARY KEY (uuid);



--
-- Name: agent uk_1esd56rr9t2fut3w5pjic0f9o; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY agent
    ADD CONSTRAINT uk_1esd56rr9t2fut3w5pjic0f9o UNIQUE (sub);


--
-- Name: work_group work_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY work_group
    ADD CONSTRAINT work_group_pkey PRIMARY KEY (uuid);



--
-- Name: profile fk6km3qhkb0oxknl605eq9rgb53; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT fk6km3qhkb0oxknl605eq9rgb53 FOREIGN KEY (agent_uuid) REFERENCES agent(uuid);


--
-- Name: group_to_profile fk8y5do2npagw5h8kpiv6d5baw2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_to_profile
    ADD CONSTRAINT fk8y5do2npagw5h8kpiv6d5baw2 FOREIGN KEY (groups_uuid) REFERENCES work_group(uuid);


--
-- Name: work_group fk9nr4guyqnkipcj54jvw3kdkqe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY work_group
    ADD CONSTRAINT fk9nr4guyqnkipcj54jvw3kdkqe FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: profile fkllnpsqqh0sl7w2w9yk5brk9e8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile
    ADD CONSTRAINT fkllnpsqqh0sl7w2w9yk5brk9e8 FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: group_to_profile fkr70638b0kk7drls70a9snyq6v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_to_profile
    ADD CONSTRAINT fkr70638b0kk7drls70a9snyq6v FOREIGN KEY (profiles_uuid) REFERENCES profile(uuid);


--
-- PostgreSQL database dump complete
--



alter table local_authority add column active boolean;

alter table acte add column profile_uuid character varying(255);

ALTER TABLE ONLY acte
    ADD CONSTRAINT fk_acte_profile FOREIGN KEY (profile_uuid) REFERENCES profile(uuid);



