--
-- Data for Name: local_authority; Type: TABLE DATA; Schema: public; Owner: stela
--

INSERT INTO local_authority (uuid, name, client_id, client_secret, creator_id, creator_name, dc_id,
                             destruction_secret, instance_id, instance_registration_uri,
                             notified_to_kernel, service_id, status_changed_secret, siren)
  VALUES ('639fd48c-93b9-4569-a414-3b372c71e0a1', 'SICTIAM-Test', '7b638c0a-ec1a-4d37-ac5b-9e91f43ea68f',
          'Va4gjDY9XzMXOCtWy57Oxl0OmFUR0nuJ6cROzc8mjc0', '7188c3b1-a284-4840-935d-036ba2b7cc6d', 'Admin Dev SICTIAM',
          'http://data.sictiam.fr/dc/type/orgfr:Organisation_0/FR/25060187900027', 'Qs27IWFc69k8iqw89lfRgSWqY4Gs4SBnwDIhn7y65qZvOCYx6vMYdAylnPOqgmo',
          '7b638c0a-ec1a-4d37-ac5b-9e91f43ea68f', 'https://kernel.sictiam.fr', true, '28c16508-98a3-459d-b5a7-c520294f9a1c',
          null, '25060187900027');
