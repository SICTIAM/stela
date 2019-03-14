-- Remove old mails
delete from mail_template;


insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_CREATED',
    'Nouvelle convocation reçue sur STELA',
    '<p>Bonjour {{destinataire}}</p>
<p>Vous &ecirc;tes destinataire d&rsquo;une nouvelle convocation d&eacute;pos&eacute;e sur la plate-forme Stela par {{emetteur}} pour la collectivit&eacute; suivante : {{collectivite}}</p>
<p><a href="{{convocation}}">Cliquez sur ce lien</a> pour prendre connaissance de la convocation et y r&eacute;pondre.</p>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_UPDATED',
    'Modification intervenue sur une convocation',
    '<p>Bonjour {{destinataire}}</p>
<p>Une modification a &eacute;t&eacute; apport&eacute;e &agrave; la convocation suivante : &laquo;{{sujet}}&raquo;</p>
<p>Convocation &eacute;mise pour la collectivit&eacute; : {{collectivite}}.</p>
<p>Nature des modifications :</p>
<ul>{{modifications}}</ul>
<p><a href="{{convocation}}">Cliquez sur ce lien</a> pour consulter la convocation modifi&eacute;e.</p>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_CANCELLED',
    'Annulation d’une convocation',
    '<p>Bonjour {{destinataire}}</p>
<p>Veuillez prendre en compte l&rsquo;annulation de la convocation pour la cause suivante :</p>
<ul>
<li>Suppression de la s&eacute;ance du {{date}}</li>
<li>Convocation &eacute;mise pour la collectivit&eacute; : {{collectivite}}</li>
</ul>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'PROCURATION_RECEIVED',
    'Procuration reçue pour une convocation',
    '<p>Bonjour {{mandataire}}</p>
<p>{{destinataire}} vous a donn&eacute; procuration pour la s&eacute;ance suivante : &laquo;{{sujet}}&raquo; du {{date}}</p>
<p>Vous pouvez consulter la convocation <a href="{{convocation}}">en cliquant sur ce lien</a>.</p>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'PROCURATION_CANCELLED',
    'Annulation d''une procuration donnée',
    '<p>Bonjour {{mandataire}}</p>
<p>{{destinataire}} a retir&eacute; la procuration qu&rsquo;il/elle vous avait donn&eacute;e pour la s&eacute;ance suivante : &laquo;{{sujet}}&raquo; du {{date}}</p>
<p>Convocation &eacute;mise pour la collectivit&eacute; : {{collectivite}}</p>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_READ',
    'Convocation lue par un destinataire',
    '<p>Bonjour {{emetteur}},</p>
<p>{{destinataire}} a lu la convocation ayant pour objet : &laquo;{{sujet}}&raquo;</p>
<p>Vous pouvez acc&eacute;der au d&eacute;tail de la convocation <a href="{{convocation}}">en cliquant sur ce lien</a>.</p>
<br/>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_RESPONSE',
    'Réponse d''un destinataire à une convocation',
    '<p>Bonjour {{emetteur}},</p>
<p>Une r&eacute;ponse a &eacute;t&eacute; d&eacute;pos&eacute;e pour la convocation suivante : &laquo;{{sujet}}&raquo;</p>
<p>
R&eacute;ponse apport&eacute;e par : {{destinataire}}
<br/>
Nature de la r&eacute;ponse : {{reponse}}
</p>
<p>Vous pouvez consulter la r&eacute;ponse &agrave; la convocation <a href="{{convocation}}">en cliquant sur ce lien</a>.</p>
<br/>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'CONVOCATION_REMINDER',
    'Relance liée à une absence de réponse à une convocation reçue',
    '<p>Bonjour {{destinataire}}</p>
<p>Aucune r&eacute;ponse de votre part n&rsquo;a &eacute;t&eacute; transmise concernant la convocation suivante : &laquo;{{sujet}}&raquo;</p>
<p>Pour rappel : </p>
<ul>
<li>Date de s&eacute;ance : {{date}}</li>
<li>Convocation &eacute;mise pour la collectivit&eacute; : {{collectivite}}</li>
</ul>
<p>Vous pouvez r&eacute;pondre à la convocation <a href="{{convocation}}">en cliquant sur ce lien</a>.</p>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'NO_RESPONSE_INFO',
    'Liste des destinataires n''ayant pas répondu à une convocation',
    '<p>Bonjour {{emetteur}},</p>
<p>Objet de la convocation : &laquo;{{sujet}}&raquo;</p>
<p>Les destinataires suivants n’ont pas encore apport&eacute; de r&eacute;ponse :</p>
<ul><{{destinataires}}</ul>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

insert into mail_template values (
    uuid_generate_v4(),
    'MINUTES_ADDED',
    'Nouveau PV reçu suite à la tenue d’une séance',
    '<p>Bonjour {{destinataire}}</p>
<p>Un nouveau proc&egrave;s-verbal a &eacute;t&eacute; d&eacute;pos&eacute; sur la plate-forme Stela par {{emetteur}} pour la collectivit&eacute; suivante : {{collectivite}}</p>
<p><a href="{{convocation}}">Cliquez sur ce lien</a> pour prendre connaissance du PV.</p>
<p>Merci de ne pas r&eacute;pondre &agrave; ce message.</p>
<br>
<p>Cordialement,<br><i><a href="{{stela_url}}">L&rsquo;&eacute;quipe STELA</a></i></p>
<hr>
<p style="font-size:0.85em;color:grey;">Vous recevez cet email automatis&eacute; parce que vous avez choisi d&rsquo;utiliser la plate-forme de services d&eacute;mat&eacute;rialis&eacute;s du SICTIAM.</p>',
    null
);

