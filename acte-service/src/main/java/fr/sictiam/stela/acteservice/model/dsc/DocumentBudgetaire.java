/*
 * Copyright
 *   2008 axYus - www.axyus.com
 *   2008 C.Marchand - christophe.marchand@axyus.com
 *   2011 J.Léger - johann.leger@axyus.com
 *
 * This file is part of DSC.
 *
 * DSC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DSC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DSC; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package fr.sictiam.stela.acteservice.model.dsc;

import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.GregorianCalendar;

/**
 * Représente un document budgétaire.<br>
 * (<i>Refactoring</i>: Auparavant, la logique de cette classe était éparpillée dans la class fr.gouv.finances.cp.dsc.Starter)
 *
 * @author johann.leger
 */
public class DocumentBudgetaire {

    private static Logger logger = LoggerFactory.getLogger(DocumentBudgetaire.class);

    private static final String DOC_BUDG_NS = "http://www.minefi.gouv.fr/cp/demat/docbudgetaire";

    private Builder xomBuilder;
    private XPathContext docBudgCtx;
    private Document budget;
    private Element enteteBudget;

    public static DocumentBudgetaire buildFromFile(File f) throws IOException, ParsingException {
        return new DocumentBudgetaire(f);
    }

    public static DocumentBudgetaire buildFromBytes(byte[] bytes) throws IOException, ParsingException {
        return new DocumentBudgetaire(bytes);
    }

    private DocumentBudgetaire(File f) throws IOException, ParsingException {
        xomBuilder = new Builder();
        this.docBudgCtx = new XPathContext("n", DOC_BUDG_NS);
        initFromFile(f);
    }

    private DocumentBudgetaire(byte[] bytes) throws IOException, ParsingException {
        xomBuilder = new Builder();
        this.docBudgCtx = new XPathContext("n", DOC_BUDG_NS);
        initFromBytes(bytes);
    }

    public DocumentBudgetaire(Document budget) {
        xomBuilder = new Builder();
        this.docBudgCtx = new XPathContext("n", DOC_BUDG_NS);
        this.budget = budget;
        logger.debug("Recherche de l'entete du docbudg");
        enteteBudget = (Element) budget.query("//n:EnTeteDocBudgetaire", docBudgCtx).get(0);
    }

    private void initFromBytes(byte[] bytes) throws IOException, ParsingException {
        if (bytes == null) {
            logger.error("file bytes are null");
            throw new FileNotFoundException();
        }
        InputStream isInput = new ByteArrayInputStream(bytes);
        try {
            initFromStream(isInput);
        } catch (ParsingException ex) {
            logger.error("Le fichier (from bytes) n'est pas un document XML valide.");
            throw ex;
        } finally {
            isInput.close();
        }
    }

    private void initFromFile(File f) throws IOException, ParsingException {
        if (!f.exists()) {
            logger.error(f.getAbsolutePath() + " does not exists");
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        InputStream isInput = new FileInputStream(f);
        try {
            initFromStream(isInput);
        } catch (ParsingException ex) {
            logger.error("Le fichier " + f.getName() + " n'est pas un document XML valide.");
            throw ex;
        } finally {
            isInput.close();
        }
    }

    private void initFromStream(InputStream isInput) throws IOException, ParsingException {
        logger.debug("Chargement du docbudg");
        budget = xomBuilder.build(isInput);
        logger.debug("Recherche de l'entete du docbudg");
        enteteBudget = (Element) budget.query("//n:EnTeteDocBudgetaire", docBudgCtx).get(0);
    }

    public Document getBudget() {
        return budget;
    }

    public Element getEntete() {
        return enteteBudget;
    }

    /**
     * Calcule l'empreinte d'un document budgétaire.
     *
     * @return Empreinte calculée.
     */
    public Empreinte calculateEmpreinte() throws IOException, ParsingException, ParserConfigurationException {
        // On s'assure de l'indentation du fichier avant de calculer son empreinte pour le scellement.
        File tmpFileForIndentation = File.createTempFile("dsc_beforehash", null);
        try {
            saveBudget(tmpFileForIndentation);
            Document budgetCopy = xomBuilder.build(tmpFileForIndentation);

            nu.xom.Nodes nodes = budgetCopy.query("/n:DocumentBudgetaire/n:Scellement", docBudgCtx);
            if (nodes != null && nodes.size() > 0) {
                nu.xom.Node scellement = nodes.get(0);
                ParentNode parent = scellement.getParent();
                int idx = parent.indexOf(scellement);
                // suppression du scellement
                parent.removeChild(idx);
                // suppresion du noeud texte qui suit ("\n" avec notre identation)
                Node n = parent.getChild(idx);
                if (n instanceof Text) {
                    parent.removeChild(idx);
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            nu.xom.canonical.Canonicalizer canonicalizer = new nu.xom.canonical.Canonicalizer(baos,
                    nu.xom.canonical.Canonicalizer.CANONICAL_XML);
            canonicalizer.write(budgetCopy);
            baos.flush();
            baos.close();

            byte[] docBudgDataToHash = baos.toByteArray();
//            AbstractChecksum md5 = JacksumAPI.getChecksumInstance("md5");
//            AbstractChecksum sha1 = JacksumAPI.getChecksumInstance("sha1");
//            md5.update(docBudgDataToHash);
//            sha1.update(docBudgDataToHash);
//            return new Empreinte(md5.getFormattedValue(), sha1.getFormattedValue());
            return new Empreinte(DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(docBudgDataToHash)),
                    DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(docBudgDataToHash)));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } finally {
            tmpFileForIndentation.delete();
        }
    }

    /**
     * Lit la version du schéma présente dans le document budgétaire.
     *
     * @return
     */
    public String readSchemaVersion() {
        String versionSchema = null;
        Nodes nodes = budget.query("/n:DocumentBudgetaire/n:VersionSchema/@V", docBudgCtx);
        if (nodes.size() > 0) {
            versionSchema = nodes.get(0).getValue();
        }
        return versionSchema;
    }

    /**
     * Retire la version du schéma présente dans le document budgétaire.
     */
    public void removeSchemaVersion() {
        try {
            Element versionSchemaElt = (Element) budget.query("//n:VersionSchema", docBudgCtx).get(0);
            if (versionSchemaElt != null) {
                versionSchemaElt.getParent().removeChild(versionSchemaElt);
            }
        } catch (IndexOutOfBoundsException e) {
            // ca peut arriver
        }
    }

    /**
     * Ajoute la version du schéma au budget.
     */
    public void addSchemaVersion(String versionSchema) {
        Element elVersion = new Element("VersionSchema", DOC_BUDG_NS);
        elVersion.addAttribute(new Attribute("V", versionSchema));
        budget.getRootElement().insertChild(elVersion, 0);
    }

    /**
     * Lit la version de dsc-common présente dans le document budgétaire.
     *
     * @return
     */
    public String readDscCommonVersion() {
        String versionDscCommon = null;
        Nodes nodes = budget.query("//n:VersionOutil[@outil='DSC_COMMON']/@version", docBudgCtx);
        if (nodes.size() > 0) {
            versionDscCommon = nodes.get(0).getValue();
        }
        return versionDscCommon;
    }

    /**
     * Retire la version de dsc-common présente dans le document budgétaire.
     */
    public void removeDscCommonVersion() {
        try {
            Element versionDscCommonElt = (Element) budget.query("//n:VersionOutil[@outil='DSC_COMMON']", docBudgCtx).get(0);
            if (versionDscCommonElt != null) {
                versionDscCommonElt.getParent().removeChild(versionDscCommonElt);
            }
        } catch (IndexOutOfBoundsException e) {
            // ca peut arriver
        }
    }

    /**
     * Ajoute la version de dsc-common au budget.
     */
    public void addDscCommonVersion(String versionDscCommon) {
        Element elVersion = new Element("VersionOutil", DOC_BUDG_NS);
        elVersion.addAttribute(new Attribute("outil", "DSC_COMMON"));
        elVersion.addAttribute(new Attribute("version", versionDscCommon));
        int index = enteteBudget.getParent().indexOf(enteteBudget);
        enteteBudget.getParent().insertChild(elVersion, index);
    }

    /**
     * Lit la version des outils de remat. présente dans le document budgétaire.
     *
     * @return
     */
    public String readRematVersion() {
        String version = null;
        try {
            version = budget.query("//n:VersionOutil[@outil='REMAT']/@version", docBudgCtx).get(0).getValue();
        } catch (IndexOutOfBoundsException e) {
            // ca peut arriver
        }
        return version;
    }

    /**
     * Retire la version des outils de remat. présente dans le document budgétaire.
     */
    public void removeRematVersion() {
        try {
            Element versionRemat = (Element) budget.query("//n:VersionOutil[@outil='REMAT']", docBudgCtx).get(0);
            if (versionRemat != null) {
                versionRemat.getParent().removeChild(versionRemat);
            }
        } catch (IndexOutOfBoundsException e) {
            // ca peut arriver
        }
    }

    /**
     * Ajoute la version de dsc-common au budget.
     */
    public void addRematVersion(String versionRemat) {
        Element elVersion = new Element("VersionOutil", DOC_BUDG_NS);
        elVersion.addAttribute(new Attribute("outil", "REMAT"));
        elVersion.addAttribute(new Attribute("version", versionRemat));
        int index = enteteBudget.getParent().indexOf(enteteBudget);
        enteteBudget.getParent().insertChild(elVersion, index);
    }

    public String readNatFonc() {
        return budget.query("//n:BlocBudget/n:NatFonc/@V", docBudgCtx).get(0).getValue();
    }

    public String readNatDec() {
        return budget.query("//n:BlocBudget/n:NatDec/@V", docBudgCtx).get(0).getValue();
    }

    /**
     * Lit l'exercice présent dans le document budgétaire.
     *
     * @return
     */
    public String readExercice() {
        return budget.query("//n:BlocBudget/n:Exer/@V", docBudgCtx).get(0).getValue();
    }

    /**
     * Définit l'exercice du document budgétaire.
     *
     * @param exercice
     */
    public void setExercice(String exercice) {
        Element exerciceNode = (Element) budget.query("//n:BlocBudget/n:Exer", docBudgCtx).get(0);
        exerciceNode.addAttribute(new Attribute("V", exercice));
    }

    /**
     * Lit la nomenclature présente dans le document budgétaire.
     *
     * @return
     */
    public String readNomenclature() {
        return budget.query("//n:EnTeteBudget/n:Nomenclature/@V", docBudgCtx).get(0).getValue();
    }

    /**
     * Scelle le budget.
     */
    public void seal(String sealXsDateTime) throws IOException, DatatypeConfigurationException, ParsingException, ParserConfigurationException {
        String dscCommonVersion = readDscCommonVersion();
        String rematVersion = readRematVersion();
        if (dscCommonVersion == null || dscCommonVersion.trim().equals("")
                || rematVersion == null || rematVersion.trim().equals("")) {
            logger.error("Impossible de sceller un flux jamais rematérialisé.");
            throw new IllegalStateException("Impossible de sceller un flux jamais rematérialisé.");
        }

        Empreinte empreinte = this.calculateEmpreinte();

        Element scellement = new Element("Scellement", DOC_BUDG_NS);
        scellement.addAttribute(new Attribute("md5", empreinte.getMd5()));
        scellement.addAttribute(new Attribute("sha1", empreinte.getSha1()));
        XMLGregorianCalendar calendar = null;
        if (sealXsDateTime == null) {
            GregorianCalendar gcal = new GregorianCalendar();
            calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } else {
            // On controle que la date passée est bien au format xs:datetime.
            // IllegalArgumentException est lancé si la date n'est pas OK.
            calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(sealXsDateTime);
        }
        scellement.addAttribute(new Attribute("date", calendar.toXMLFormat()));
        Nodes scellNodes = budget.query("/n:DocumentBudgetaire/n:Scellement", docBudgCtx);
        Element el = null;
        if (scellNodes.size() > 0) {
            el = (Element) scellNodes.get(0);
        }
        if (el != null) {
            logger.error("Impossible de sceller un flux déjà scellé");
            throw new IllegalStateException("Impossible de sceller un flux déjà scellé");
        }
        enteteBudget.getParent().insertChild(scellement, enteteBudget.getParent().indexOf(enteteBudget));
    }

    /**
     * Indique si le flux est scellé.
     *
     * @return true si scellé, false sinon.
     */
    public boolean isSealed() {
        Nodes scellNodes = budget.query("/n:DocumentBudgetaire/n:Scellement", docBudgCtx);
        return scellNodes.size() > 0;
    }

    /**
     * Vérifie que le budget est conforme au scellement s'il est scellé.
     *
     * @return true si OK, false sinon.
     */
    public boolean checkSealIfExist() throws IOException, ParsingException, ParserConfigurationException {
        Empreinte empreinte = this.calculateEmpreinte();

        // on vérifie si le flux est scellé que l'empreinte n'a pas été modifiée
        Nodes scellNodes = budget.query("/n:DocumentBudgetaire/n:Scellement", docBudgCtx);
        if (scellNodes.size() > 0) {
            Element el = (Element) scellNodes.get(0);
            if (el != null) {
                String md5 = el.getAttributeValue("md5");
                String sha1 = el.getAttributeValue("sha1");
                if (!md5.toLowerCase().equals(empreinte.getMd5().toLowerCase())
                        || !sha1.toLowerCase().equals(empreinte.getSha1().toLowerCase())) {
                    logger.error("Le flux a été modifié depuis son scellement !");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sauvegarde le document budgétaire dans un fichier.
     *
     * @param target Fichier dans lequel sauvegarder le document.
     */
    public void saveBudget(File target) throws IOException, ParserConfigurationException {
        FileOutputStream fos = new FileOutputStream(target);
        Serializer serializer = new Serializer(fos, "ISO-8859-1");
        serializer.setIndent(3);
        serializer.setMaxLength(0);
        try {
            serializer.write(budget);
            serializer.flush();
        } finally {
            fos.flush();
            fos.close();
        }
    }

    public static String getNormeFromNomenclature(String nomenclature) {
        int ix = nomenclature.indexOf("-");
        if (ix > 0) {
            return nomenclature.substring(0, ix);
        } else {
            return nomenclature;
        }
    }

    public static String getDeclinaisonFromNomenclature(String nomenclature) {
        int ix = nomenclature.indexOf("-");
        if (ix > 0) {
            return nomenclature.substring(ix + 1);
        } else {
            return nomenclature;
        }
    }

}
