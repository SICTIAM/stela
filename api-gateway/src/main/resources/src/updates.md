### 07/03/2019

#### Contrôle de légalité

- Amélioration de la fonction d'apposition du tampon (cas d'actes ayant subi des rotations, ...)
- Améliorations graphique et ergonomique des formulaires de dépôt
- Ajout de traductions manquantes sur certains changements d'état
- Amélioration des cas d'erreurs sur les transmissions de PES PJ depuis Ciril
- Intégration de l'infrastructure de publication automatique en Open Data des délibérations

### 22/02/2019

#### Général

- Optimisation du chargement de l'interface utilisateur
- Correction de la gestion du temps de session utilisateur
- Mise en place de la supervision des flux PES
- Expérimentation de la détection d'anomalies sur les flux PES

#### Contrôle de légalité

- Amélioration de la gestion d'unicité des actes
- Amélioration du suivi de l'archivage des actes

#### Flux comptable

- Corrections sur les connecteurs Pastell & Sesile

### 06/02/2019

#### Général

- Correction de l'affichage du bandeau d'alerte

#### Contrôle de légalité

- Amélioration de la mise en forme du justificatif obtenu suite à la réception de l'AR
- Amélioration de la précision du positionnement du tampon

#### Flux comptable

- Corrections sur les web services de récupération de l'avancement des PES PJ
- Amélioration de la restitution des erreurs retournées par la Trésorerie

### 21/01/2019

#### Général

- Améliorations des performances d'affichage
- Informations sur le statut du certificat utilisé
- Mise à jour du favicon

#### Contrôle de légalité

- Majuscules forcées pour le numéro d'acte

#### Flux comptable

- Ajout du lien vers le classeur Sesile lorsqu'un PES est envoyé pour signature

#### Connecteurs

- Amélioration de la robustesse des échanges avec Sesile

### 11/01/2019

#### Général

- Correctifs sur l'interface
  - changement de collectivité via le bouton
  - affichage du contenu d'un PES retour
  - meilleure gestion de la pagination lors d'une recherche dans une liste

### 07/01/2019

#### Contrôle de légalité

- Ajout de l'option multi-canal lors du dépôt
- Dépôt des fichiers par glisser-déposer
- Affichage d'un message d'avertissement si la nomenclature n'a pas été demandée
- Affichage de la taille cumulée des fichiers joints lors du dépôt

#### Flux comptable

- Ajout d'une distinction spéciale pour les anomalies 1984 et 1968 sur la page d'un flux
- Correction sur le nommage des fichiers PES lorsqu'ils sont envoyés au serveur Helios
- Externalisation du stockage des fichiers pour améliorer les performances et l'exploitation

#### Connecteurs

- Amélioration de la robustesse des échanges avec les éditeurs métiers
- Possibilité de vérifier le token de connexion à SESILE
- Amélioration de la réactivité des échanges entre STELA et SESILE

#### Général

- Ajout de marqueurs pour les champs obligatoires dans les formulaires
- Possibilité de faire un clic droit sur une ligne du tableau de résultats de recherche
- Mise en avant du nombre de jours restants avant expiration du certificat

### 05/11/2018
- Ajout du détail des anomalies PES dans les mails
- Ajout d'un groupe de migration aux actes migrés de Stela 2
- Correction des mots "status" en "statut" sur différentes pages
- Mise en place de la vérification de la taille des PES, maximum 100 Mo
- Réorganisation des liens de téléchargement d'un acte pour plus de praticité
- Correction d'un bug qui empêchait le téléchargement du justificatif d'AR d'un acte
- Correction d'un bug qui permettait de recevoir par mail la liste des PES en anomalie d'une autre collectivité
- Correction d'un bug lors du dépôt d'un PES dans SESILE pour un agent multi-collectivité
- Correction d'un bug qui pouvait stopper la migration des actes/pes lorsqu'une données de Stela2 était non compatible avec le modèle de données de Stela3

### 18/10/2018
- Ajout des liens vers les actes/pes dans tous les mails de Stela 3
- Optimisation des performances et de la réactivité de la liste des actes
- Correction d'un bug qui affichait les actes migrés avec aucun statut dans la liste des actes

### 03/10/2018
- Réécriture de tous les mails envoyés par Stela et corrections des erreurs d'encodage
- La notification par mail en cas d'AR trésorerie est maintenant obligatoire et non désactivable
- Ajout d'une notification (désactivable) pour la réception d'une anomalie d'un PES
- Ajout d'un mail quotidien (désactivable) qui reprend les anomalies de PES de la veille
- Ajout d'une page "Notes de mise à jour" et un espace "Dernière mise à jour" sur la page d'accueil
- Correction du status des PES dans la liste qui indiquait "Notification envoyée" au lieu de "AR trésorerie" (non rétroactif)
- Correction du status dans le bandeau de la page d'un PES qui indiquait "Notification envoyée" au lieu de "AR trésorerie" (non rétroactif)

### 02/10/2018
- Correction des actes budgétaires qui étaient refusés par la préfecture
- Correction de la date de décision au format JJ/DD/AAAA sur la page de dépôt d'un acte
- Correction de la page de dépôt de lot d'actes
- Ajout d'une fenêtre contextuelle sur le bouton "Déposer" de la page de dépôt d'un acte qui liste les éléments manquant du formulaire
- Les fichiers annexes d'un acte sont maintenant estampillés
- La liste qui permet de changer de collectivité est maintenant triée alphabétiquement
- Toutes les anomalies d'un flux PES sont maintenant affichées sur la page d'un PES
- \[Admin\] Il est maintenant possible de lister, modifier et supprimer les comptes génériques