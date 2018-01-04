--
-- Data for Name: local_authority; Type: TABLE DATA; Schema: public; Owner: stela
--

INSERT INTO local_authority (uuid, name, client_id, client_secret, creator_id, creator_name, dc_id,
                             destruction_secret, instance_id, instance_registration_uri,
                             notified_to_kernel, service_id, status_changed_secret, siren)
  VALUES ('f706fe20-e28a-4bfc-8b0e-6ee47758cf37', 'SICTIAM', 'cf379cef-3ebd-4324-86e2-2c3923e89105',
          'sictiam-instantiation-secret', '7188c3b1-a284-4840-935d-036ba2b7cc6d', 'Administrator',
          'http://data.sictiam.fr/dc/type/orgfr:Organisation_0/FR/25060187900027', 'sictiam-destruction-secret',
          'cf379cef-3ebd-4324-86e2-2c3923e89105', 'https://kernel.sictiam.fr/apps/pending-instance/cf379cef-3ebd-4324-86e2-2c3923e89105',
          true, 'f4c56d61-e096-4f3d-897e-b259d0c223e7', 'sictiam-status-change-secret', '25060187900027');
