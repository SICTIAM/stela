### 04/12/2018
- Renommage des fichiers PES lorsqu'ils sont envoyés au serveur Helios
- Stockage des fichiers PES sur Amazon S3
- Possibilité de vérifer le token de connextion à Sesile
- Option multi-canal lors du dépôt de fichiers Actes
- Dépôt des fichiers par glisser-déposer dans le module Actes
- Correctifs sur la communication avec Stela
- Correctifs sur l'interface

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