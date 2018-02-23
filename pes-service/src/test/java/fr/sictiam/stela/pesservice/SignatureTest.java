package fr.sictiam.stela.pesservice;

import fr.sictiam.signature.pes.producer.SigningPolicies.SigningPolicy1;
import fr.sictiam.signature.pes.verifier.CertificateProcessor.CertificatInformation1;
import fr.sictiam.signature.pes.verifier.PesAllerAnalyser;
import fr.sictiam.signature.pes.verifier.PesAllerAnalyser.InvalidPesAllerFileException;
import fr.sictiam.signature.pes.verifier.SignatureValidation;
import fr.sictiam.signature.pes.verifier.SignatureValidationError;
import fr.sictiam.signature.pes.verifier.SignatureVerifierResult;
import fr.sictiam.signature.pes.verifier.SimplePesInformation;
import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1;
import fr.sictiam.signature.pes.verifier.XadesInfoProcessor.XadesInfoProcessResult1;
import fr.sictiam.signature.utils.DateUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.Init;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SignatureTest {

    @Test
    public void testsigned() throws IOException {
        Init.init();
        InputStream file = new ClassPathResource("data/30002-2015-P-DN-16-1429552171140-sign.xml").getInputStream();
        assertThat(testXemeliosPesV2(file), is(true));

        InputStream fileNotSigned = new ClassPathResource("data/28000-2017-P-RN-22-1516807373820.xml").getInputStream();
        assertThat(testXemeliosPesV2(fileNotSigned), is(false));

    }

    public SimplePesInformation computeSimplePesInformation(byte[] file) throws InvalidPesAllerFileException {
        ByteArrayInputStream bais = new ByteArrayInputStream(file);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PesAllerAnalyser pesAllerAnalyser = new PesAllerAnalyser(bais, stream);

        pesAllerAnalyser.setDoSchemaValidation(true);
        pesAllerAnalyser.computeSimpleInformation();
        return pesAllerAnalyser.getSimplePesInformation();
    }

    public boolean isSigned(SimplePesInformation simplePesInformation) {
        return simplePesInformation.isSigned();
    }

    public SignatureValidation isValidSignature(SimplePesInformation simplePesInformation)
            throws InvalidPesAllerFileException {
        PesAllerAnalyser pesAllerAnalyser = new PesAllerAnalyser(simplePesInformation.getPesSourceFile());
        pesAllerAnalyser.computeSignaturesVerificationResults();
        pesAllerAnalyser.computeSignaturesTypeVerification();

        SignatureValidation signatureValidation = new SignatureValidation();
        List<SignatureValidationError> signatureValidationErrors = new ArrayList<>();
        signatureValidation.setSignatureValidationErrors(signatureValidationErrors);
        signatureValidation.setValid(true);
        if ((pesAllerAnalyser.isDoSchemaValidation()) && (!pesAllerAnalyser.isSchemaOK())) {
            signatureValidation.setValid(false);
            signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.INVALID_SCHEMA);
        }
        if (!signatureValidation.isValid()) {
            return signatureValidation;
        }

        for (Element element : simplePesInformation.getSignatureElements()) {
            SignatureVerifierResult verificationResult = pesAllerAnalyser.getSignaturesVerificationResults()
                    .get(element);
            if ((!verificationResult.isSignatureGlobalePresente())
                    && (verificationResult.getListeBordereauxNonSignes() != null)) {
                System.out.println("NOK : Des bordereaux n'ont pas été signés.");
                signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.NOT_SIGNED_CONTENT);
            }
            if (verificationResult.getUnverifiableSignatureException() != null) {
                System.out.println(
                        "NOK : Signature non vérifiable. Cette signature n'a pu être analysée (problème grave de corruption du fichier)");
                signatureValidationErrors.add(SignatureValidationError.UNVERIFIABLE_SIGNATURE);
            } else {
                int verificationResultHash = verificationResult.hashCode();

                XadesInfoProcessResult1 xadesInfoProcessResult = verificationResult.getXadesInfoProcessResult();
                List<XMLDsigReference1> listRef = verificationResult.getSignatureAndRefsVerificationResult()
                        .getReferencesInfo();

                boolean isSomeSignedPropertyReference = false;
                for (XMLDsigReference1 ref : listRef) {
                    if (ref.isSignedPropertiesReferenceLookup(simplePesInformation.getPesDocument())) {
                        isSomeSignedPropertyReference = true;
                    }
                }

                boolean signatureVerifiedOk = verificationResult.getSignatureAndRefsVerificationResult()
                        .isSignatureVerified();

                boolean certificatProcessOk = verificationResult.getCertificateProcessException() == null;
                boolean certificatHashOk = (xadesInfoProcessResult.getSigCertExpectedHash() == null)
                        || (xadesInfoProcessResult.getSigCertExpectedHash()
                                .equals(xadesInfoProcessResult.getSigCertcalculatedHash()));

                boolean mainC14Ok = verificationResult.getSignatureAndRefsVerificationResult().isMainC14Accepted();
                boolean allrefsC14Ok = verificationResult.getSignatureAndRefsVerificationResult()
                        .isAllrefsC14Accepted();

                boolean certificateConfianceOk = false;
                CertificatInformation1 certificatInformation = null;
                if (certificatProcessOk) {
                    certificatInformation = verificationResult.getCertificatInformation();
                    certificateConfianceOk = (certificatInformation.getValidatedCertPath() != null)
                            && (!certificatInformation.getValidatedCertPath().isEmpty());
                }

                boolean certificatSerialNumberOk = (xadesInfoProcessResult.getSigCertExpectedSerialNumber() == null)
                        || (xadesInfoProcessResult.getSigCertExpectedSerialNumber()
                                .equals(xadesInfoProcessResult.getSigCertSerialNumber()));
                boolean certificateIssuerOk = (xadesInfoProcessResult.getSigCertExpectedIssuerName() == null)
                        || (xadesInfoProcessResult.getSigCertExpectedIssuerName().replaceAll(" ", "")
                                .equals(xadesInfoProcessResult.getSigCertIssuerName().replaceAll(" ", "")));
                boolean certificatdigitalSignatureOk = certificatInformation.getSigningCertificate()
                        .getKeyUsage() != null ? certificatInformation.getSigningCertificate().getKeyUsage()[1] : false;
                boolean certificatChainOk = certificatInformation.isSignCertAuthorized();
                boolean certificatChainAutoriseOk = certificatInformation.isAuthorizedCertPath();

                if (!certificateIssuerOk) {
                    certificateIssuerOk = (xadesInfoProcessResult.getSigCertExpectedIssuerName() == null)
                            || (xadesInfoProcessResult.getSigCertExpectedIssuerName().replaceAll(" ", "")
                                    .equals(xadesInfoProcessResult.getSigCertIssuerNameRFC2253().replaceAll(" ", "")));
                }

                boolean certificatBasicConstraintsCritical = certificatInformation.isBasicConstraintCritical();

                boolean xadesProcessOk = verificationResult.getXadesExtractionException() == null;
                boolean xadesSigPolicyHashOk = (xadesInfoProcessResult.getSigExpectedSecurityPolicyIdHash() == null)
                        || (xadesInfoProcessResult.getSigSecurityPolicyIdHash() == null)
                        || (xadesInfoProcessResult.getSigExpectedSecurityPolicyIdHash()
                                .equals(xadesInfoProcessResult.getSigSecurityPolicyIdHash()));
                boolean problemRef = false;
                boolean problemSignedPropertyRef = false;
                for (XMLDsigReference1 ref : listRef) {
                    if (!ref.isVerified()) {
                        problemRef = true;
                    }

                    if (ref.isSignedPropertiesReferenceLookup(simplePesInformation.getPesDocument())) {
                        isSomeSignedPropertyReference = true;
                        if (!ref.isVerified()) {
                            problemSignedPropertyRef = true;
                        }
                    }
                }

                if (!((signatureVerifiedOk) && (isSomeSignedPropertyReference) && (certificatProcessOk)
                        && (certificatSerialNumberOk) && (certificateIssuerOk) && (certificateConfianceOk)
                        && ((certificatdigitalSignatureOk) || (!certificatBasicConstraintsCritical))
                        && (certificatHashOk) && (certificatChainOk) && (certificatChainAutoriseOk) && (mainC14Ok)
                        && (allrefsC14Ok))) {
                    System.out.println("NOK : Des anomalies de controle ont été détectées");
                    signatureValidation.setValid(false);
                    signatureValidation.getSignatureValidationErrors()
                            .add(SignatureValidationError.SIGNATURE_CONTROL_ERRORS);
                }

                if (certificatProcessOk) {
                    if ((certificateConfianceOk)
                            && ((certificatdigitalSignatureOk) || (!certificatBasicConstraintsCritical))
                            && (certificatChainOk) && (certificatChainAutoriseOk)) {
                        if ((!certificatdigitalSignatureOk) && (!certificatBasicConstraintsCritical)) {
                            System.out.println(
                                    "NOK : Le certificat utilisé n'a pas vocation à faire de la signature de document (l'usage de \"non-répudiation\" est absent)");
                        }
                    } else {
                        System.out.println(
                                "NOK : Cela signifie qu'il est possible que cette signature soit émise par une personne malintentionnée. Merci de transmettre ce rapport à votre administrateur"
                                        + verificationResultHash);
                    }

                } else {
                    System.out.println(
                            "NOK : Le traitement de reconnaissance du certificat du signataire à rencontré une erreur : "
                                    + verificationResult.getCertificateProcessException().getLocalizedMessage());
                }

                if ((!mainC14Ok) || (!allrefsC14Ok)) {
                    System.out.println(
                            "NOK : Cela signifie que le logiciel de signature n'a pas respecté les recommandations. Merci de transmettre ce rapport à votre administrateur"
                                    + verificationResultHash
                                    + ". Le(s) algorithme(s) de canonisation ne sont pas corrects");
                }

                if (xadesProcessOk) {
                    if (!isSomeSignedPropertyReference) {
                        System.out.println("NOK : Les \"SignedProperties\" Xades n'ont pas été signées.");
                    }

                    if (problemSignedPropertyRef) {
                        System.out.println("NOK : Les informations Xades ont été modifiées.");
                    }

                    if ((xadesSigPolicyHashOk) && (certificatSerialNumberOk) && (certificateIssuerOk)
                            && (certificatHashOk)) {
                    } else {
                        System.out.println(
                                "NOK : Cela signifie qu'il est possible que certaines informations XADES aient été modifiées. Merci de transmettre ce rapport à votre administrateur. Les informations Xades ne peuvent pas être validées");
                    }

                    Date date = verificationResult.getXadesInfo().getSigningTime().getTime();
                    String tmp;
                    if (date != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        tmp = sdf.format(date);
                    } else {
                        tmp = null;
                    }

                    if (tmp != null) {
                        if (!DateUtils.isStrictUtcFormat(verificationResult.getXadesInfo().getSigningTimeAsString())) {
                            System.out.println("NOK : La date de signature n'est pas au format UTC : "
                                    + verificationResult.getXadesInfo().getSigningTimeAsString());
                        }
                    } else {
                        System.out.println("NOK : Date de la signature : NON_RENSEIGNE");
                    }

                    if (verificationResult.getXadesInfo().getSigClaimedRole() != null) {
                    } else {
                        System.out.println("NOK : Rôle du signataire : NON_RENSEIGNE");
                    }

                    String tmp1 = verificationResult.getXadesInfo().getSigPolicyId();

                    SigningPolicy1 signingPolicy = verificationResult.getXadesInfoProcessResult().getSigningPolicy();
                    if (signingPolicy != null) {

                        if ((tmp1 == null) || (tmp1.isEmpty())) {
                            System.out.println(
                                    "OK : Pas d'identifiant politique de signature. Il est préconisé de mettre  l'OID de la PS De la DGFiP : \"urn:oid:1.2.250.1.131.1.5.18.21.1.4\"");
                        }

                        String hv = verificationResult.getXadesInfo().getSigPolicyHashDigestValue();
                        if (hv == null) {
                            System.out.println(
                                    "OK : Pas de Hash de la politique de signature: il est préconisé de mettre le hash de la PS de la DGFiP : \"Jkdb+aba0Hz6+ZPKmKNhPByzQ+Q=\"");
                        }

                        String spq = verificationResult.getXadesInfo().getSigPolicyQualifier();
                        if (spq == null) {
                            System.out.println(
                                    "OK : Pas de SigPolicyQualifier : il est préconisé de mettre celui de la PS de la DGFiP  : \"https://portail.dgfip.finances.gouv.fr/documents/PS_Helios_DGFiP.pdf\"");
                        }

                    } else {
                        if (tmp1 != null) {
                            System.out.println("OK : Identifiant de politique de signature inconnu: " + tmp1);
                        } else {
                            System.out.println("OK : Pas d'identifiant de politique de signature : ");
                        }
                        System.out.println(
                                "OK : Il est préconisé de mettre la politique de signature de la DGFiP : \"Politique de signature Helios de la DGFiP\"");
                    }
                } else {
                    if (verificationResult.getXadesExtractionException() != null) {
                        System.out.println(
                                "NOK : Le traitement d'extraction des informations XADES a rencontré une erreur : "
                                        + verificationResult.getXadesExtractionException().getLocalizedMessage() != null
                                                ? verificationResult.getXadesExtractionException().getLocalizedMessage()
                                                : "");
                    }
                    if (!isSomeSignedPropertyReference) {
                        System.out.println("NOK : Les \"SignedProperties\" Xades n'ont pas été signées");
                    }
                }

                if ((!problemRef) && (isSomeSignedPropertyReference)) {
                } else {
                    System.out.println("NOK : Les données signées ne sont pas conformes.");
                    if (!isSomeSignedPropertyReference) {
                        System.out.println("NOK : Les informations Xades ne sont pas signées.");
                    }
                    if (problemRef) {
                        System.out.println(
                                "NOK : Cela signifie que les données peuvent avoir été falsifiée ou modifiée. Merci de transmettre ce rapport à votre administrateur. Les données signées ont été modifiées : "
                                        + verificationResultHash);
                    }
                }
            }
        }
        return signatureValidation;
    }

    public static Boolean testXemeliosPesV2(InputStream Helios) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(Helios));
        PesAllerAnalyser pesAllerAnalyser = new PesAllerAnalyser(bais);

        try {
            pesAllerAnalyser.setDoSchemaValidation(true);
            pesAllerAnalyser.computeSimpleInformation();
            SimplePesInformation simplePesInformation = pesAllerAnalyser.getSimplePesInformation();

            if (!simplePesInformation.isSigned()) {
                System.out.println("OK : Le fichier n'est pas signé");
                return false;
            } else {
                pesAllerAnalyser.computeSignaturesVerificationResults();
                pesAllerAnalyser.computeSignaturesTypeVerification();

                if ((pesAllerAnalyser.isDoSchemaValidation()) && (!pesAllerAnalyser.isSchemaOK())) {
                    System.out.println("NOK : Le fichier n'est pas conforme au schema PES.");
                }

            }
        } catch (PesAllerAnalyser.InvalidPesAllerFileException ex) {

        }

        return false;
    }
}
