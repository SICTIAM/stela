<?xml version="1.0" encoding="ISO-8859-1"?>
<actes:Acte 
    xmlns:actes="http://www.interieur.gouv.fr/ACTES#v1.1-20040216" 
    xmlns:insee="http://xml.insee.fr/schema" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" actes:CodeNatureActe="$natureCode" actes:Date="$decisionDate" actes:NumeroInterne="$number">
    <actes:CodeMatiere1 actes:CodeMatiere="$codeMatiere1"/>
    <actes:CodeMatiere2 actes:CodeMatiere="$codeMatiere2"/>
    <actes:CodeMatiere3 actes:CodeMatiere="$codeMatiere3"/>
    <actes:CodeMatiere4 actes:CodeMatiere="$codeMatiere4"/>
    <actes:CodeMatiere5 actes:CodeMatiere="$codeMatiere5"/>
    <actes:Objet>$acteTitle</actes:Objet>
    <actes:ClassificationDateVersion>2009-06-26</actes:ClassificationDateVersion>
    <actes:Document>
        <actes:NomFichier>$acteFilename</actes:NomFichier>
    </actes:Document>
    <actes:Annexes actes:Nombre="$annexesFilenames.size">
        <%
            for(annexeFilename in annexesFilenames){
                println "<actes:Annexe>"
                println "<actes:NomFichier>${annexeFilename}</actes:NomFichier>"
                println "</actes:Annexe>"
            } 
        %>
    </actes:Annexes>
</actes:Acte>