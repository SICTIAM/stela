Vérification du certificat utilisé pour signer un flux PES
----------------------------------------------------------

 OBJECTIF :
    Il s'agit de vérifier que le certificat utilisé par un ordonnateur pour signer un flux PES a bien été émis par une authorité de certification (AC) habilitée
    à délivrer des certificats de signature pour des flux PES.
    
    D'autre part le certificat de signature utilisé doit aussi être valide à la date de signature (date de validité et type de certificat). 
  

 PRINCIPE :
    Le principe général répose sur 2 magasins de certificats : certs.zip et certsInter.zip.
 
    Le magasin certs.zip contient tous les certificats nécessaires à la constitution de la chaine de certification, c.à.d. l'ensemble des certificats depuis l'AC racine
    jusqu'au certificat de signature en passant par les éventuels certificats des AC intermédiaires.

    Le magasin certsInter.zip contient les certificats des AC habilitées à délivrer des certificats de signature pour des flux PES. En règle générale il s'agit d'AC intermédiaire,
    mais rien n'empêche que dans certains cas ce soit une AC racine.

 TRAITEMENT
    Etape 1 : Constitution de la chaine de certification à partir du certificat de signature provenant du fichier de signature et des certificats des AC contenus dans l'archive certs.zip.
              La chaine de certification est constituée par l'ensemble des certificats depuis l'AC racine jusqu'au certificat de signature en passant par les éventuels certificats des AC 
              intermédiaires,

   Etape 2 : Vérification qu'au moins un des certificats de la chaine contituée à l'étate 1 est bien dans le magasin certsInter.zip. 


 EXEMPLE
        
	A, B, C, D, E,F et G sont des certificats. A est une AC racine, B et E sont des AC intermédiaires et C, D, F et G sont des certificats de signatures utilisés par des Ordonateurs.
        Si il sont KO il ne peuvent pas être utilisé comme certificats de signature d'un flux PES.
 
	A				-->	dans certs.zip
		B			-->	dans certs.zip et certsInter.zip
			C	: OK
			D	: OK
		E			-->	dans certs.zip
			F	: KO
			G	: KO