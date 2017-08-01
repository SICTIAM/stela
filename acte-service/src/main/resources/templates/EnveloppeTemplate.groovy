<?xml version="1.0" encoding="ISO-8859-1"?>
<actes:EnveloppeCLMISILL 
    xmlns:actes="http://www.interieur.gouv.fr/ACTES#v1.1-20040216" 
    xmlns:insee="http://xml.insee.fr/schema" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <actes:Emetteur>
        <actes:IDCL insee:SIREN="$siren" actes:Departement="$departement" actes:Arrondissement="$arrondissement" actes:Nature="$nature" />
        <% 
            if (contactName||contactPhoneNumber||contactEmail) {
                print "<actes:Referent>"
                if (contactName) {
                   print "<actes:Nom>${contactName}</actes:Nom>"
                }

                if (contactPhoneNumber){
                    print "<actes:Telephone>${contactPhoneNumber}</actes:Telephone>"
                }

                if (contactEmail){
                    print "<actes:Email>${contactEmail}</actes:Email>"
                }
                print "</actes:Referent>"
            }
        %>
    </actes:Emetteur>
    <actes:AdressesRetour>
        <%
             for(mail in callbackEmails) {
                print "<actes:Email> ${mail} </actes:Email>"
            }
        %>
    </actes:AdressesRetour>
    <actes:FormulairesEnvoyes>
        <actes:Formulaire>
            <actes:NomFichier>$messageFilename</actes:NomFichier>
        </actes:Formulaire>
    </actes:FormulairesEnvoyes>
</actes:EnveloppeCLMISILL>