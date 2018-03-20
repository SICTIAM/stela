package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.verifier.SimplePesInformation.BordereauInfo1;
import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SignatureTypeCalculator {
    private SimplePesInformation simplePesInformation;
    private Map<Element, SignatureVerifierResult> signatureVerifierResultMap;
    private boolean bordereauNonSigne = false;
    private boolean bordereauSigne = false;
    private List<String> listeBordereauxNonSignes;
    private boolean signatureGlobalePresente = false;

    public SignatureTypeCalculator(SimplePesInformation simplePesInformation,
            Map<Element, SignatureVerifierResult> signatureVerifierResultMap) {
        this.simplePesInformation = simplePesInformation;
        this.signatureVerifierResultMap = signatureVerifierResultMap;
    }

    public String process(Element signature) {
        GeneralSignaturePesData generalSignaturePesData = getSimplePesInformation().getEntetePesInfo()
                .getGeneralSignaturePesData();
        List<BordereauInfo1> bordereauxList = getSimplePesInformation().getBordereaux();

        SignatureVerifierResult signatureVerifierResult = getSignatureVerifierResultMap().get(signature);
        if (signatureVerifierResult.getUnverifiableSignatureException() != null) {
            return "Signature invalide";
        }
        for (XMLDsigReference1 reference : signatureVerifierResult.getSignatureAndRefsVerificationResult()
                .getReferencesInfo()) {
            String uri = reference.getReference().getElement().getAttribute("URI");
            uri = (uri != null) && (!uri.equals("")) && (uri.startsWith("#")) ? uri.substring(1) : uri;
            if (((generalSignaturePesData.getIdPesAller() != null)
                    && (generalSignaturePesData.getIdPesAller().equals(uri))) || ("".equals(uri))) {
                if (getSignatureVerifierResultMap().size() == 1) {
                    if ((generalSignaturePesData.isEnteteRecetteValue())
                            || (generalSignaturePesData.isEnteteDepenseValue())) {
                        setSignatureGlobalePresente(true);
                        return "Signature Métier Globale ";
                    }
                    if ((generalSignaturePesData.isNotEnteteRecetteValue())
                            || (generalSignaturePesData.isNotEnteteDepenseValue())) {
                        setSignatureGlobalePresente(true);
                        return "Signature Technique Globale ";
                    }
                } else {
                    setSignatureGlobalePresente(true);
                    return "Signature Technique Globale ";
                }
            }
            for (BordereauInfo1 bordereaux : bordereauxList) {
                if (((bordereaux.getXmlId() != null) && (!bordereaux.getXmlId().equals(""))
                        && (bordereaux.getXmlId().equals(uri)))
                        || ((bordereaux.getBordereauXmlId() != null) && (!bordereaux.getBordereauXmlId().equals(""))
                                && (bordereaux.getBordereauXmlId().equals(uri)))) {
                    if ((generalSignaturePesData.isEnteteRecetteValue())
                            || (generalSignaturePesData.isEnteteDepenseValue())) {
                        return "Signature de Bordereaux " + ((bordereauSigne) && (bordereauNonSigne)
                                ? "(attention certains bordereaux ne sont pas signés) "
                                : "");
                    }
                    if ((!generalSignaturePesData.isNotEnteteRecetteValue())
                            && (!generalSignaturePesData.isNotEnteteDepenseValue())) {
                        break;
                    }
                    return "Signature de Bordereaux (attention flag InfoDematerialisee = 0"
                            + ((bordereauSigne) && (bordereauNonSigne)
                                    ? "attention certains bordereaux ne sont pas signés) "
                                    : ")");
                }
            }
        }
        String uri;
        return "Signature de type indéfini";
    }

    public void verifAllBordereauxSigned() {
        GeneralSignaturePesData generalSignaturePesData = getSimplePesInformation().getEntetePesInfo()
                .getGeneralSignaturePesData();
        List<BordereauInfo1> bordereauxList = getSimplePesInformation().getBordereaux();
        Map.Entry[] tab = new Map.Entry[bordereauxList.size()];
        BordereauInfo1 bordereaux;
        Iterator<?> localIterator1;
        Map.Entry<Element, SignatureVerifierResult> entry;
        for (int i = 0; i < bordereauxList.size(); i++) {
            bordereaux = bordereauxList.get(i);
            for (localIterator1 = getSignatureVerifierResultMap().entrySet().iterator(); localIterator1.hasNext();) {
                entry = (Map.Entry<Element, SignatureVerifierResult>) localIterator1.next();
                if (entry.getValue().getUnverifiableSignatureException() == null) {
                    for (XMLDsigReference1 reference : entry.getValue().getSignatureAndRefsVerificationResult()
                            .getReferencesInfo()) {
                        String uri = reference.getReference().getElement().getAttribute("URI");
                        uri = (uri != null) && (!uri.equals("")) && (uri.startsWith("#")) ? uri.substring(1) : uri;
                        if (((bordereaux.getXmlId() != null) && (!bordereaux.getXmlId().equals(""))
                                && (bordereaux.getXmlId().equals(uri)))
                                || ((bordereaux.getBordereauXmlId() != null)
                                        && (!bordereaux.getBordereauXmlId().equals(""))
                                        && (bordereaux.getBordereauXmlId().equals(uri)))) {
                            tab[i] = entry;
                        }
                    }
                }
            }
        }
        for (int j = 0; j < tab.length; j++) {
            if (tab[j] == null) {
                if (getListeBordereauxNonSignes() == null) {
                    listeBordereauxNonSignes = new ArrayList<>();
                }
                String foundId = bordereauxList.get(j).getId();
                if ((foundId == null) || (foundId.isEmpty())) {
                    foundId = bordereauxList.get(j).getBordereauXmlId();
                }
                if ((foundId == null) || (foundId.isEmpty())) {
                    foundId = " en position " + (j + 1) + " (pas d'identifiant)";
                }
                getListeBordereauxNonSignes().add(foundId);
                bordereauNonSigne = true;
            } else {
                bordereauSigne = true;
            }
        }
    }

    public List<String> getListeBordereauxNonSignes() {
        return listeBordereauxNonSignes;
    }

    public boolean isSignatureGlobalePresente() {
        return signatureGlobalePresente;
    }

    public void setSignatureGlobalePresente(boolean signatureGlobalePresente) {
        this.signatureGlobalePresente = signatureGlobalePresente;
    }

    public static class SignatureAnexInfo1 {
        private String typeSignature;

        public SignatureAnexInfo1(String typeSignature) {
            this.typeSignature = typeSignature;
        }

        public String getTypeSignature() {
            return typeSignature;
        }

        public void setTypeSignature(String typeSignature) {
            this.typeSignature = typeSignature;
        }
    }

    public SimplePesInformation getSimplePesInformation() {
        return simplePesInformation;
    }

    public void setSimplePesInformation(SimplePesInformation simplePesInformation) {
        this.simplePesInformation = simplePesInformation;
    }

    public Map<Element, SignatureVerifierResult> getSignatureVerifierResultMap() {
        return signatureVerifierResultMap;
    }

    public void setSignatureVerifierResultMap(Map<Element, SignatureVerifierResult> signatureVerifierResultMap) {
        this.signatureVerifierResultMap = signatureVerifierResultMap;
    }

    public boolean isBordereauSigne() {
        return bordereauSigne;
    }

    public void setBordereauSigne(boolean bordereauSigne) {
        this.bordereauSigne = bordereauSigne;
    }

    public boolean isBordereauNonSigne() {
        return bordereauNonSigne;
    }

    public void setBordereauNonSigne(boolean bordereauNonSigne) {
        this.bordereauNonSigne = bordereauNonSigne;
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.SignatureTypeCalculator Java Class Version:
 * 6 (50.0) JD-Core Version: 0.7.1
 */