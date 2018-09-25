ALTER TABLE pes_history ADD COLUMN errors json DEFAULT NULL;

UPDATE pes_history SET errors = json_build_array(json_object(array[array['title', ''], array['message', message], array['source', '']]));