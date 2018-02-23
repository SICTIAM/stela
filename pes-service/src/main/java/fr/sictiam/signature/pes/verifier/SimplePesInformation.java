package fr.sictiam.signature.pes.verifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplePesInformation {
    private ByteArrayInputStream pesSourceFile;
    private ByteArrayOutputStream pesDestinationFile;
    private Document pesDocument;
    private EntetePesInfo1 entetePesInfo;
    private List<BordereauInfo1> bordereaux;
    private List<Element> signatureElements;
    private boolean imported = false;

    public SimplePesInformation() {
        bordereaux = new ArrayList();
        signatureElements = new ArrayList();
    }

    public ByteArrayInputStream getPesSourceFile() {
        return pesSourceFile;
    }

    void setPesSourceFile(ByteArrayInputStream pesFile) {
        pesSourceFile = pesFile;
    }

    public ByteArrayOutputStream getPesDestinationFile() {
        return pesDestinationFile;
    }

    public void setPesDestinationFile(ByteArrayOutputStream pesDestinationFile) {
        this.pesDestinationFile = pesDestinationFile;
    }

    public EntetePesInfo1 getEntetePesInfo() {
        return entetePesInfo;
    }

    void setEntetePesInfo(EntetePesInfo1 entetePesInfo) {
        this.entetePesInfo = entetePesInfo;
    }

    void addBordereau(BordereauInfo1 bordereau) {
        bordereaux.add(bordereau);
    }

    public List<BordereauInfo1> getBordereaux() {
        return Collections.unmodifiableList(bordereaux);
    }

    public List<Element> getSignatureElements() {
        return Collections.unmodifiableList(signatureElements);
    }

    public void addSignatureElement(Element signatureElement) {
        signatureElements.add(signatureElement);
    }

    public boolean isSigned() {
        return !signatureElements.isEmpty();
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fichier PES\n");
        sb.append(" Signé : ").append(isSigned()).append("\n");
        sb.append(" Entête\n");
        sb.append("  IdColl : ").append(entetePesInfo.getIdColl()).append("\n");
        sb.append("  IdPost : ").append(entetePesInfo.getIdPost()).append("\n");
        sb.append("  CodCol : ").append(entetePesInfo.getCodCol()).append("\n");
        sb.append("  CodBud : ").append(entetePesInfo.getCodBud()).append("\n");
        sb.append("  DteStr : ").append(entetePesInfo.getDteStr()).append("\n");
        for (BordereauInfo1 bordereau : bordereaux) {
            sb.append(" Bordereau\n");
            sb.append("  id : ").append(bordereau.getId()).append("\n");
            sb.append("  ex : ").append(bordereau.getExercice()).append("\n");
            sb.append("  ht : ").append(bordereau.getMontantHorsTaxe()).append("\n");
        }
        return sb.toString();
    }

    public Document getPesDocument() {
        return pesDocument;
    }

    public void setPesDocument(Document pesDocument) {
        this.pesDocument = pesDocument;
    }

    public static class BordereauInfo1 {
        private String id;
        private String exercice;
        private String montantHorsTaxe;
        private String XmlId;
        private String bordereauXmlId;

        public BordereauInfo1(String id, String exercice, String montantHorsTaxe, String XmlId, String bordereauXmlId) {
            this.id = id;
            this.exercice = exercice;
            this.montantHorsTaxe = montantHorsTaxe;
            this.XmlId = XmlId;
            this.bordereauXmlId = bordereauXmlId;
        }

        public String getExercice() {
            return exercice;
        }

        public String getId() {
            return id;
        }

        public String getMontantHorsTaxe() {
            return montantHorsTaxe;
        }

        public String getXmlId() {
            return XmlId;
        }

        public void setXmlId(String XmlId) {
            this.XmlId = XmlId;
        }

        public String getBordereauXmlId() {
            return bordereauXmlId;
        }

        public void setBordereauXmlId(String bordereauXmlId) {
            this.bordereauXmlId = bordereauXmlId;
        }
    }

    public static class EntetePesInfo1 {
        private String dteStr;
        private String idPost;
        private String idColl;
        private String codCol;
        private String codBud;
        private String libelleColBud;
        private GeneralSignaturePesData generalSignaturePesData;

        public EntetePesInfo1(String dteStr, String idPost, String idColl, String codCol, String codBud,
                String libelleColBud) {
            this.dteStr = dteStr;
            this.idPost = idPost;
            this.idColl = idColl;
            this.codCol = codCol;
            this.codBud = codBud;
            this.libelleColBud = libelleColBud;
        }

        public String getCodBud() {
            return codBud;
        }

        public String getCodCol() {
            return codCol;
        }

        public String getDteStr() {
            return dteStr;
        }

        public String getIdColl() {
            return idColl;
        }

        public String getIdPost() {
            return idPost;
        }

        public String getLibelleColBud() {
            return libelleColBud;
        }

        public GeneralSignaturePesData getGeneralSignaturePesData() {
            return generalSignaturePesData;
        }

        public void setGeneralSignaturePesData(GeneralSignaturePesData generalSignaturePesData) {
            this.generalSignaturePesData = generalSignaturePesData;
        }
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.SimplePesInformation Java Class Version: 6
 * (50.0) JD-Core Version: 0.7.1
 */