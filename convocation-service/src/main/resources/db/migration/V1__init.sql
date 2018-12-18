--
-- Name: admin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE admin (
    uuid character varying(255) NOT NULL,
    alert_message character varying(255),
    alert_message_displayed boolean NOT NULL
);


--
-- Name: assembly_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE assembly_type (
    uuid character varying(255) NOT NULL,
    name character varying(255),
    local_authority_uuid character varying(255),
    delay int NOT NULL DEFAULT 0,
    reminder_delay int NOT NULL DEFAULT 0,
    location character varying(512),
    active boolean,
    use_procuration boolean
);


--
-- Name: attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE attachment (
    uuid character varying(255) NOT NULL,
    date timestamp without time zone,
    storage_key character varying(255),
    filename character varying(255),
    size bigint NOT NULL
);


--
-- Name: convocation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation (
    uuid character varying(255) NOT NULL,
    comment character varying(255),
    creation_date timestamp without time zone,
    meeting_date timestamp without time zone,
    place character varying(255),
    subject character varying(255),
    profile_uuid character varying(255),
    group_uuid character varying(255),
    assembly_type_uuid character varying(255),
    attachment_uuid character varying(255)
);


--
-- Name: convocation_annexes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation_annexes (
    convocation_uuid character varying(255) NOT NULL,
    annexes_uuid character varying(255) NOT NULL
);


--
-- Name: convocation_external_observer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation_external_observer (
    convocation_uuid character varying(255) NOT NULL,
    external_observer_uuid character varying(255) NOT NULL
);


--
-- Name: convocation_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation_history (
    uuid character varying(255) NOT NULL,
    convocation_uuid character varying(255),
    date timestamp without time zone,
    file bytea,
    file_name character varying(255),
    message character varying(1024),
    status character varying(255)
);


--
-- Name: convocation_questions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation_questions (
    convocation_uuid character varying(255) NOT NULL,
    questions_uuid character varying(255) NOT NULL
);


--
-- Name: convocation_response; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation_response (
    uuid character varying(255) NOT NULL,
    profile_uuid character varying(255),
    response_type character varying(255),
    substitute_profile_uuid character varying(255),
    convocation_uuid character varying(255),
    recipient_uuid character varying(255),
    substitute_recipient_uuid character varying(255)
);


--
-- Name: convocation_response_question_responses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE convocation_response_question_responses (
    convocation_response_uuid character varying(255) NOT NULL,
    question_responses_uuid character varying(255) NOT NULL
);


--
-- Name: recipient; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE recipient (
    uuid character varying(255) NOT NULL,
    email character varying(255),
    firstname character varying(255),
    lastname character varying(255),
    phone_number character varying(64),
    local_authority_uuid character varying(255),
    token character varying(255),
    active boolean default true
);


--
-- Name: local_authority; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE local_authority (
    uuid character varying(255) NOT NULL,
    active boolean,
    name character varying(255),
    resident_number bigint,
    siren character varying(255),
    slug_name character varying(255)
);


--
-- Name: observer_profile_uuids; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE observer_profile_uuids (
    convocation_uuid character varying(255) NOT NULL,
    profile_uuid character varying(255)
);


--
-- Name: profile_uuids; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE profile_uuids (
    assembly_type_uuid character varying(255) NOT NULL,
    profile_uuid character varying(255)
);


--
-- Name: question; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE question (
    uuid character varying(255) NOT NULL,
    question character varying(255)
);


--
-- Name: question_response; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE question_response (
    uuid character varying(255) NOT NULL,
    response boolean,
    convocation_response_uuid character varying(255),
    question_uuid character varying(255)
);


CREATE TABLE assembly_type_recipient (
    assembly_type_uuid character varying(255) NOT NULL,
    recipient_uuid character varying(255) NOT NULL
);

--
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (uuid);


--
-- Name: assembly_type assembly_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY assembly_type
    ADD CONSTRAINT assembly_type_pkey PRIMARY KEY (uuid);


--
-- Name: attachment attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachment
    ADD CONSTRAINT attachment_pkey PRIMARY KEY (uuid);


--
-- Name: convocation_annexes convocation_annexes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_annexes
    ADD CONSTRAINT convocation_annexes_pkey PRIMARY KEY (convocation_uuid, annexes_uuid);


--
-- Name: convocation_external_observer convocation_external_observer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_external_observer
    ADD CONSTRAINT convocation_external_observer_pkey PRIMARY KEY (convocation_uuid, external_observer_uuid);


--
-- Name: convocation_history convocation_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_history
    ADD CONSTRAINT convocation_history_pkey PRIMARY KEY (uuid);


--
-- Name: convocation convocation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation
    ADD CONSTRAINT convocation_pkey PRIMARY KEY (uuid);


--
-- Name: convocation_questions convocation_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_questions
    ADD CONSTRAINT convocation_questions_pkey PRIMARY KEY (convocation_uuid, questions_uuid);


--
-- Name: convocation_response convocation_response_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response
    ADD CONSTRAINT convocation_response_pkey PRIMARY KEY (uuid);


--
-- Name: convocation_response_question_responses convocation_response_question_responses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response_question_responses
    ADD CONSTRAINT convocation_response_question_responses_pkey PRIMARY KEY (convocation_response_uuid, question_responses_uuid);


--
-- Name: recipient recipient_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY recipient
    ADD CONSTRAINT recipient_pkey PRIMARY KEY (uuid);


--
-- Name: local_authority local_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY local_authority
    ADD CONSTRAINT local_authority_pkey PRIMARY KEY (uuid);


--
-- Name: question question_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY question
    ADD CONSTRAINT question_pkey PRIMARY KEY (uuid);


--
-- Name: question_response question_response_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY question_response
    ADD CONSTRAINT question_response_pkey PRIMARY KEY (uuid);

--
-- Name: convocation_response_question_responses uk_7mk061u81ls2i2heraqun4vut; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response_question_responses
    ADD CONSTRAINT uk_7mk061u81ls2i2heraqun4vut UNIQUE (question_responses_uuid);


--
-- Name: convocation_external_observer uk_a5fkvmkl3flwe6ih2vn71cjw; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_external_observer
    ADD CONSTRAINT uk_a5fkvmkl3flwe6ih2vn71cjw UNIQUE (external_observer_uuid);


--
-- Name: convocation_annexes uk_atdqli5lbh7xeoja6dv162sx8; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_annexes
    ADD CONSTRAINT uk_atdqli5lbh7xeoja6dv162sx8 UNIQUE (annexes_uuid);


--
-- Name: convocation_questions uk_nb79i0asd223kbjo0n28lpyg2; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_questions
    ADD CONSTRAINT uk_nb79i0asd223kbjo0n28lpyg2 UNIQUE (questions_uuid);

--
-- Name: convocation_response_question_responses fk2yqh6at37f6dcdjil7daq2bmo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response_question_responses
    ADD CONSTRAINT fk2yqh6at37f6dcdjil7daq2bmo FOREIGN KEY (convocation_response_uuid) REFERENCES convocation_response(uuid);


--
-- Name: convocation_response fk45vi0u9tfjl9jm9e8din5xqux; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response
    ADD CONSTRAINT fk45vi0u9tfjl9jm9e8din5xqux FOREIGN KEY (recipient_uuid) REFERENCES recipient(uuid);


--
-- Name: convocation_response fk4qad89l9c877xe5wl5h8nc5oj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response
    ADD CONSTRAINT fk4qad89l9c877xe5wl5h8nc5oj FOREIGN KEY (convocation_uuid) REFERENCES convocation(uuid);


--
-- Name: convocation_response fk7wvsoau3nj5ornsmse87pi8j3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response
    ADD CONSTRAINT fk7wvsoau3nj5ornsmse87pi8j3 FOREIGN KEY (substitute_recipient_uuid) REFERENCES recipient(uuid);


--
-- Name: convocation_external_observer fk85rds2u7d4gdcyiq9t2y3vapb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_external_observer
    ADD CONSTRAINT fk85rds2u7d4gdcyiq9t2y3vapb FOREIGN KEY (convocation_uuid) REFERENCES convocation(uuid);


--
-- Name: convocation fk9fvthp33qkg1lfkpcvntj98s3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation
    ADD CONSTRAINT fk9fvthp33qkg1lfkpcvntj98s3 FOREIGN KEY (assembly_type_uuid) REFERENCES assembly_type(uuid);


--
-- Name: convocation_questions fkaa3vq524ra0t12ifyn762r0p4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_questions
    ADD CONSTRAINT fkaa3vq524ra0t12ifyn762r0p4 FOREIGN KEY (questions_uuid) REFERENCES question(uuid);


--
-- Name: question_response fkac3l998d62dge9kvyclu9929r; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY question_response
    ADD CONSTRAINT fkac3l998d62dge9kvyclu9929r FOREIGN KEY (convocation_response_uuid) REFERENCES convocation_response(uuid);


--
-- Name: observer_profile_uuids fkae30hit5povy76rman5mgxtso; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY observer_profile_uuids
    ADD CONSTRAINT fkae30hit5povy76rman5mgxtso FOREIGN KEY (convocation_uuid) REFERENCES convocation(uuid);


--
-- Name: convocation_questions fkavm7od5b0mxxswllvm594aw5f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_questions
    ADD CONSTRAINT fkavm7od5b0mxxswllvm594aw5f FOREIGN KEY (convocation_uuid) REFERENCES convocation(uuid);


--
-- Name: assembly_type fkb086693dw4c2kjh25cqxqb1ra; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY assembly_type
    ADD CONSTRAINT fkb086693dw4c2kjh25cqxqb1ra FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: convocation_annexes fkbe1dnyo0mlbm1ye99xh06lsga; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_annexes
    ADD CONSTRAINT fkbe1dnyo0mlbm1ye99xh06lsga FOREIGN KEY (annexes_uuid) REFERENCES attachment(uuid);


--
-- Name: convocation_external_observer fkbwp916gjyybax4pqb6hncsrjx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_external_observer
    ADD CONSTRAINT fkbwp916gjyybax4pqb6hncsrjx FOREIGN KEY (external_observer_uuid) REFERENCES recipient(uuid);


--
-- Name: convocation fkk45anwhkdihwuqvm4nrsawy11; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation
    ADD CONSTRAINT fkk45anwhkdihwuqvm4nrsawy11 FOREIGN KEY (attachment_uuid) REFERENCES attachment(uuid);


--
-- Name: profile_uuids fkkewx12yb3uxed95p5fqilg11c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile_uuids
    ADD CONSTRAINT fkkewx12yb3uxed95p5fqilg11c FOREIGN KEY (assembly_type_uuid) REFERENCES assembly_type(uuid);


--
-- Name: recipient fkkmtgr5dfkasyobr33lddr7shk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY recipient
    ADD CONSTRAINT fkkmtgr5dfkasyobr33lddr7shk FOREIGN KEY (local_authority_uuid) REFERENCES local_authority(uuid);


--
-- Name: convocation_response_question_responses fkl07e2vu55ih8pbofvd0od8bhh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_response_question_responses
    ADD CONSTRAINT fkl07e2vu55ih8pbofvd0od8bhh FOREIGN KEY (question_responses_uuid) REFERENCES question_response(uuid);


--
-- Name: question_response fkn771wco9pbgfql47cg65fu60q; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY question_response
    ADD CONSTRAINT fkn771wco9pbgfql47cg65fu60q FOREIGN KEY (question_uuid) REFERENCES question(uuid);


--
-- Name: convocation_annexes fktoj1siayae5sbu3knh6kqvve0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY convocation_annexes
    ADD CONSTRAINT fktoj1siayae5sbu3knh6kqvve0 FOREIGN KEY (convocation_uuid) REFERENCES convocation(uuid);


--
-- Name: assembly_type_external_users assembly_type_external_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY assembly_type_recipient
    ADD CONSTRAINT assembly_type_recipient_pkey PRIMARY KEY (assembly_type_uuid, recipient_uuid);


--
-- Name: assembly_type_recipient fko891mqq0am2kwm1xnsf9yfdjy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY assembly_type_recipient
    ADD CONSTRAINT fko891mqq0am2kwm1xnsf9yfdjy FOREIGN KEY (assembly_type_uuid) REFERENCES assembly_type(uuid);


--
-- Name: assembly_type_recipient fksq0bvj8n5rdacbm3gln0oq3hj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY assembly_type_recipient
    ADD CONSTRAINT fksq0bvj8n5rdacbm3gln0oq3hj FOREIGN KEY (recipient_uuid) REFERENCES recipient(uuid);



--
-- PostgreSQL database dump complete
--