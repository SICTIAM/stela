package fr.sictiam.signature.pes;

import java.security.cert.X509Certificate;
import java.util.Collection;

public class CertificateContainer$SimpleCertificateContainer
  extends CertificateContainer
{
  private Collection<X509Certificate> allCertificates;
  
  public CertificateContainer$SimpleCertificateContainer(Collection<X509Certificate> allCertificates)
  {
    this.allCertificates = allCertificates;
  }
  
  public Collection<X509Certificate> getAllCertificates()
  {
    return allCertificates;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.CertificateContainer.SimpleCertificateContainer
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */