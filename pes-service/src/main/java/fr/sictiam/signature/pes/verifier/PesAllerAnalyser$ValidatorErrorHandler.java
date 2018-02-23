package fr.sictiam.signature.pes.verifier;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class PesAllerAnalyser$ValidatorErrorHandler extends DefaultHandler {
    public boolean validationError = false;

    private PesAllerAnalyser$ValidatorErrorHandler(PesAllerAnalyser paramPesAllerAnalyser) {
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        System.err.println("error :" + exception + " line " + exception.getLineNumber());
        if ((exception.getLineNumber() == 1)) {
            return;
        }
        validationError = true;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        System.err.println("Fatal error :" + exception + " line " + exception.getLineNumber());

        validationError = true;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        System.err.println("Warning : " + exception + " line " + exception.getLineNumber());
    }
}

/*
 * Location: Qualified Name:
 * com.axyus.signature.pes.verifier.PesAllerAnalyser.ValidatorErrorHandler Java
 * Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */