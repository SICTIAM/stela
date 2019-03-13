-- Remove old mails
delete from mail_template;


insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_CREATED',
    'Nouvelle convocation reçue sur STELA',
    '<p>Bonjour {{destinataire}},</p><p>Une nouvelle convocation a &eacute;t&eacute; d&eacute;pos&eacute;e sur la plate-forme Stela par {{emetteur}} pour la collectivit&eacute; suivante : {{collectivite}}.</p><p>Cliquez sur le lien suivant pour prendre connaissance de la convocation :</p><p><a href="{{convocation}}">{{convocation}}</a></p><p>-----------</p><p>Cordialement</p><p>{{emetteur}}</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_UPDATED',
    'Modification intervenue sur une convocation',
    '<p>Bonjour {{destinataire}},</p><p>Une modification a &eacute;t&eacute; apport&eacute;e &agrave; la convocation suivante&nbsp;: {{sujet}}</p><p>Convocation &eacute;mise pour la collectivit&eacute;&nbsp;: {{collectivite}}.</p><p>Objet de la modification&nbsp;:</p><ul>{{modifications}}</ul><p>Cliquez sur le lien suivant pour prendre connaissance de la convocation :</p><p><a href="{{convocation}}">{{convocation}}</a></p><p>-----------</p><p>Cordialement</p><p>{{emetteur}}</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_CANCELLED',
    'Annulation d’une convocation',
    '<p>Bonjour {{destinataire}},</p><p>Veuillez prendre en compte l''annulation de la convocation pour la cause suivante :</p><ul><li>Suppression de la s&eacute;ance du {{date}}</li><li>Convocation &eacute;mise pour la collectivit&eacute;&nbsp;: {{collectivite}}.</li></ul><p>-----------</p><p>Cordialement</p><p>{{emetteur}}</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'PROCURATION_RECEIVED',
    'Procuration reçue pour une convocation',
    '<p>Bonjour {{mandataire}},</p><p>{{destinataire}} vous a donn&eacute; procuration pour la s&eacute;ance suivante : {{sujet}}&nbsp; du {{date}}</p><p>Vous pouvez consulter la convocation &agrave; l&rsquo;adresse suivante :</p><p><u><a href="{{convocation}}">{{convocation}}</a></u></p><p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p><p>-----------</p><p>Cordialement</p><p>L''&eacute;quipe <a href="{{stela_url}}">STELA</a></p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'PROCURATION_CANCELLED',
    'Annulation d''une procuration donnée',
    '<p>Bonjour {{mandataire}},</p><p>{{destinataire}} vous a enlev&eacute; sa procuration pour la s&eacute;ance suivante : {{sujet}} du {{date}}</p><p>Convocation &eacute;mise pour la collectivit&eacute;&nbsp;: {{collectivite}}.</p><p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p><p>-----------</p><p>Cordialement</p><p>L''&eacute;quipe <a href="{{stela_url}}">STELA</a></p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_READ',
    'Convocation lue par un destinataire',
    '<p>Bonjour {{emetteur}},</p><p>{{destinataire}} a lu la convocation ayant pour objet&nbsp;: {{sujet}}</p><p>Vous pouvez acc&eacute;der au d&eacute;tail de la convocation &agrave; l&rsquo;adresse suivante :</p><p><a href="{{convocation}}">{{convocation}}</a></p><p>-----------</p><p>Cordialement</p><p>L''&eacute;quipe <a href="{{stela_url}}">STELA</a></p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_RESPONSE',
    'Réponse d''un destinataire à une convocation',
    '<p>Bonjour {{emetteur}},</p><p>Une r&eacute;ponse a &eacute;t&eacute; d&eacute;pos&eacute;e pour la convocation suivante : {{sujet}}</p><p>R&eacute;ponse apport&eacute;e par&nbsp;: {{destinataire}}</p><p>Nature de la r&eacute;ponse&nbsp;: {{reponse}}</p><p>Vous pouvez consulter la r&eacute;ponse &agrave; la convocation &agrave; l''adresse suivante :</p><p><a href="{{convocation}}">{{convocation}}</a></p><p>-----------</p><p>Cordialement&nbsp;</p><p>L''&eacute;quipe <a href="{{stela_url}}">STELA</a></p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_REMINDER',
    'Relance liée à une absence de réponse à une convocation reçue',
    '<p>Bonjour {{destinataire}},</p><p>Aucune r&eacute;ponse de votre part n&rsquo;a &eacute;t&eacute; transmise concernant la convocation suivante&nbsp;: {{sujet}}</p><p>Pour rappel :</p><ul><li>Date s&eacute;ance : {{date}}</li><li>Convocation &eacute;mise pour la collectivit&eacute;&nbsp;: {{collectivite}}.</li></ul><p>Vous pouvez r&eacute;pondre &agrave; la convocation &agrave; l&rsquo;adresse suivante :</p><p><a href="{{convocation}}">{{convocation}}</a></p><p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p><p>-----------</p><p>Cordialement</p><p>{{emetteur}}</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'NO_RESPONSE_INFO',
    'Liste des destinataires n''ayant pas répondu à une convocation',
    '<p>Bonjour {{emetteur}},</p><p>Objet de la convocation&nbsp;: {{sujet}}</p><p>Les destinataires suivants n''ont pas encore apport&eacute; de r&eacute;ponse&nbsp;:</p><ul>{{destinataires}}</ul><p>-----------</p><p>Cordialement</p><p>L''&eacute;quipe <a href="{{stela_url}}">STELA</a></p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'MINUTES_ADDED',
    'Nouveau PV reçu suite à la tenue d’une séance',
    '<p>Bonjour {{destinataire}},</p><p>Un nouveau proc&egrave;s-verbal a &eacute;t&eacute; d&eacute;pos&eacute; sur la plate-forme Stela par {{emetteur}} pour la collectivit&eacute; suivante : {{collectivite}}.</p><p>Cliquez sur le lien suivant pour prendre connaissance du PV :</p><p><u><a href="{{convocation}}">{{convocation}}</a></u></p><p>-----------</p><p>Cordialement</p><p>{{emetteur}}</p>',
    null
);

