ALTER TABLE admin ADD COLUMN alert_message_displayed boolean default false;
ALTER TABLE admin ADD COLUMN alert_message varchar(255);