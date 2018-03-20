V�rification du certificat utilis� pour signer un flux PES
----------------------------------------------------------

 OBJECTIF :
    Il s'agit de v�rifier que le certificat utilis� par un ordonnateur pour signer un flux PES a bien �t� �mis par une authorit� de certification (AC) habilit�e
    � d�livrer des certificats de signature pour des flux PES.
    
    D'autre part le certificat de signature utilis� doit aussi �tre valide � la date de signature (date de validit� et type de certificat). 
  

 PRINCIPE :
    Le principe g�n�ral r�pose sur 2 magasins de certificats : certs.zip et certsInter.zip.
 
    Le magasin certs.zip contient tous les certificats n�cessaires � la constitution de la chaine de certification, c.�.d. l'ensemble des certificats depuis l'AC racine
    jusqu'au certificat de signature en passant par les �ventuels certificats des AC interm�diaires.

    Le magasin certsInter.zip contient les certificats des AC habilit�es � d�livrer des certificats de signature pour des flux PES. En r�gle g�n�rale il s'agit d'AC interm�diaire,
    mais rien n'emp�che que dans certains cas ce soit une AC racine.

 TRAITEMENT
    Etape 1 : Constitution de la chaine de certification � partir du certificat de signature provenant du fichier de signature et des certificats des AC contenus dans l'archive certs.zip.
              La chaine de certification est constitu�e par l'ensemble des certificats depuis l'AC racine jusqu'au certificat de signature en passant par les �ventuels certificats des AC 
              interm�diaires,

   Etape 2 : V�rification qu'au moins un des certificats de la chaine contitu�e � l'�tate 1 est bien dans le magasin certsInter.zip. 


 EXEMPLE
        
	A, B, C, D, E,F et G sont des certificats. A est une AC racine, B et E sont des AC interm�diaires et C, D, F et G sont des certificats de signatures utilis�s par des Ordonateurs.
        Si il sont KO il ne peuvent pas �tre utilis� comme certificats de signature d'un flux PES.
 
	A				-->	dans certs.zip
		B			-->	dans certs.zip et certsInter.zip
			C	: OK
			D	: OK
		E			-->	dans certs.zip
			F	: KO
			G	: KO