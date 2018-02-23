package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.utils.DomUtils;
import org.apache.xml.security.signature.Reference;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLDsigSignatureAndReferencesProcessor$XMLDsigReference
{
  private static final String XADES_1_1_1_SIGNED_PROPS_TYPE_URI = "http://uri.etsi.org/01903/v1.1.1#SignedProperties";
  private static final String XADES_1_2_2_SIGNED_PROPS_TYPE_URI = "http://uri.etsi.org/01903/v1.2.2#SignedProperties";
  private static final String XADES_OTHER_VERSION_SIGNED_PROPS_TYPE_URI = "http://uri.etsi.org/01903#SignedProperties";
  private Reference reference;
  private boolean verified = false;
  private boolean c14algoAccepted = false;
  
  public XMLDsigSignatureAndReferencesProcessor$XMLDsigReference(Reference reference)
  {
    this.reference = reference;
  }
  
  public Reference getReference()
  {
    return reference;
  }
  
  public void setVerified(boolean verified)
  {
    this.verified = verified;
  }
  
  public boolean isVerified()
  {
    return verified;
  }
  
  public boolean isSignedPropertiesReference()
  {
    return ("http://uri.etsi.org/01903/v1.1.1#SignedProperties".equals(reference.getType())) || 
      ("http://uri.etsi.org/01903#SignedProperties".equals(reference.getType()));
  }
  
  public boolean isSignedPropertiesReferenceLookup(Document doc)
  {
    String id = reference.getURI();
    if ((id != null) && (!id.isEmpty())) {
      try
      {
        Node n = XPathAPI.selectSingleNode(doc, "//*[@Id='" + ((id != null) && 
          (id.startsWith("#")) ? id.substring(1) : id) + "']", 
          DomUtils.createXMLDSigAndPesNamespaceNode(doc));
        if (n != null)
        {
          String reftype = n.lookupNamespaceURI(n.getPrefix()) + n.getLocalName();
          if (("http://uri.etsi.org/01903/v1.1.1#SignedProperties".equals(reftype)) || 
            ("http://uri.etsi.org/01903/v1.2.2#SignedProperties".equals(reftype)) || 
            ("http://uri.etsi.org/01903#SignedProperties".equals(reftype))) {
            return true;
          }
        }
      }
      catch (Exception e)
      {
        return false;
      }
    }
    return false;
  }
  
  public boolean isC14algoAccepted()
  {
    return c14algoAccepted;
  }
  
  public void setC14algoAccepted(boolean c14algoAccepted)
  {
    this.c14algoAccepted = c14algoAccepted;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */