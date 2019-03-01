alter table recipient rename additional_contact to guest;
alter table recipient_response add guest boolean default false;