package fr.sictiam.signature.pes.producer;

import java.security.cert.Certificate;

public class AbstractKeyStoreLoader$CertAliases
{
  private Certificate certificate;
  private String alias;
  
  public AbstractKeyStoreLoader$CertAliases(AbstractKeyStoreLoader this$0, Certificate certificate, String alias)
  {
    this.certificate = certificate;
    this.alias = alias;
  }
  
  public Certificate getCertificate()
  {
    return certificate;
  }
  
  public void setCertificate(Certificate certificate)
  {
    this.certificate = certificate;
  }
  
  public String getAlias()
  {
    return alias;
  }
  
  public void setAlias(String alias)
  {
    this.alias = alias;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.producer.AbstractKeyStoreLoader.CertAliases
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */