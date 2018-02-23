package fr.sictiam.signature.pes.verifier;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertPathBuilderException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.RSAKey;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.security.auth.x500.X500Principal;

public class CertificateProcessor$CertificatInformation
{
  private List<X509Certificate> signingCertificates;
  private List<X509Certificate> validatedCertPath;
  private CertPathBuilderException certPathBuilderException;
  private boolean authorizedCertPath;
  private boolean signCertAuthorized;
  
  public boolean isSignCertAuthorized()
  {
    return signCertAuthorized;
  }
  
  public void setSignCertAuthorized(boolean signCertAuthorized)
  {
    this.signCertAuthorized = signCertAuthorized;
  }
  
  public boolean isAuthorizedCertPath()
  {
    return authorizedCertPath;
  }
  
  public void setAuthorizedCertPath(boolean authorizedCertPath)
  {
    this.authorizedCertPath = authorizedCertPath;
  }
  
  public CertificateProcessor$CertificatInformation(List<X509Certificate> signingCertificates)
  {
    this.signingCertificates = signingCertificates;
  }
  
  public X509Certificate getSigningCertificate()
  {
    return (X509Certificate)signingCertificates.iterator().next();
  }
  
  public String getSignataireCN()
  {
    X500Principal principal = getSigningCertificate().getSubjectX500Principal();
    String rfc2253PrincipalName = principal.getName("RFC2253");
    StringTokenizer st = new StringTokenizer(rfc2253PrincipalName, ",");
    while (st.hasMoreTokens())
    {
      String token = st.nextToken();
      int idx = token.indexOf("CN=");
      if (idx >= 0) {
        return token.substring(idx + 3);
      }
    }
    return null;
  }
  
  public String getSigningPublicKeyDescription()
  {
    return getSigningCertificate().getPublicKey().getAlgorithm() + " (" + getSigningPublicKeyStrength() + " bits)";
  }
  
  public Integer getSigningPublicKeyStrength()
  {
    PublicKey publicKey = getSigningCertificate().getPublicKey();
    if ((publicKey instanceof RSAKey)) {
      return Integer.valueOf(((RSAKey)publicKey).getModulus().bitLength());
    }
    if ((publicKey instanceof DSAKey)) {
      return Integer.valueOf(((DSAKey)publicKey).getParams().getP().bitLength());
    }
    throw new IllegalArgumentException("PublicKey type not supported : " + publicKey.getClass().getName());
  }
  
  public CertPathBuilderException getCertPathBuilderException()
  {
    return certPathBuilderException;
  }
  
  public void setCertPathBuilderException(CertPathBuilderException certPathBuilderException)
  {
    this.certPathBuilderException = certPathBuilderException;
  }
  
  public List<X509Certificate> getValidatedCertPath()
  {
    return validatedCertPath;
  }
  
  public void setValidatedCertPath(List<X509Certificate> validatedCertPath)
  {
    this.validatedCertPath = validatedCertPath;
  }
  
  public boolean isBasicConstraintCritical()
  {
    boolean isCritical = true;
    if (getSigningCertificate().getBasicConstraints() == -1) {
      isCritical = false;
    }
    String BasicConstraintsExtensionOID = "2.5.29.19";
    Set nonCritSet = getSigningCertificate().getNonCriticalExtensionOIDs();
    Iterator i;
    if (nonCritSet != null) {
      for (i = nonCritSet.iterator(); i.hasNext();)
      {
        String oid = (String)i.next();
        if (BasicConstraintsExtensionOID.equals(oid)) {
          isCritical = false;
        }
      }
    }
    return isCritical;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.verifier.CertificateProcessor.CertificatInformation
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */