create table mail_template (uuid varchar(255) not null, notification_type varchar(255), subject varchar(255),body varchar(2048), local_authority_uuid varchar(255),  primary key (uuid));

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_CREATED',
    'Nouvelle convocation',
    'Bonjour ${recipient},<BR>La convocation ${name} a été créée : ${url}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_UPDATED',
    'Convocation mise à jour',
    'Bonjour ${recipient},<BR>La convocation ${name} a été mise à jour : ${url}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_CANCELLED',
    'Convocation annulée',
    'Bonjour ${recipient},<BR>La convocation ${name} a été annulée : ${url}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'PROCURATION_RECEIVED',
    'Réception d''une procuration',
    'Bonjour ${recipient},<BR>Vous avez reçu une procuration de la part de ${substitute} pour la convocation ${name} : ${url}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_READ',
    'Une convocation a été lue',
    'Bonjour ${recipient},<BR>La convocation ${name} a été lue par ${recipient} : ${url}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_RESPONSE',
    'Réponse à une convocation',
    'Bonjour ${recipient},<BR>Le destinataire ${recipient} a donné sa reponse à la convocation ${name} : ${response}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_REMINDER',
    'Relance à une convocation',
    'Bonjour ${recipient},<BR>Vous n''avez toujours pas répondu à la convocation ${name} : ${url}<BR><BR>L''équipe Stela',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'NO_RESPONSE_INFO',
    'Participants qui n''ont pas répondu',
    'Bonjour ${recipient},<BR>Certains participants n''ont toujours pas répondu à la convocation ${name} : ${url}<BR><BR>L''équipe Stela',
    null
);
