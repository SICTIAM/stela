package fr.sictiam.signature.pes.verifier;

public class XMLDsigSignatureAndReferencesProcessor$XMLDsigC14Algorithm {
    private static final String[] acceptedAlgorithms = { "http://www.w3.org/2001/10/xml-exc-c14n#",
            "http://www.w3.org/2001/10/xml-exc-c14n#WithComments" };
    private static final String[] notAcceptedAlgorithms = { "http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" };

    public static boolean isC14Algorithm(String algo) {
        for (String s : getAcceptedAlgorithms()) {
            if (s.equals(algo)) {
                return true;
            }
        }
        for (String s : getNotAcceptedAlgorithms()) {
            if (s.equals(algo)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAcceptedC14Algorithm(String algo) {
        for (String s : getAcceptedAlgorithms()) {
            if (s.equals(algo)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getAcceptedAlgorithms() {
        return acceptedAlgorithms;
    }

    public static String[] getNotAcceptedAlgorithms() {
        return notAcceptedAlgorithms;
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.
 * XMLDsigC14Algorithm Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */