package fr.sictiam.signature.pes.producer;

import java.security.Key;
import java.security.cert.Certificate;

public class AbstractKeyStoreLoader$KeyAliases
{
  private Key key;
  private String alias;
  private Certificate[] certChain;
  
  public AbstractKeyStoreLoader$KeyAliases(AbstractKeyStoreLoader this$0, Key key, String alias, Certificate[] certChain)
  {
    this.key = key;
    this.alias = alias;
    this.certChain = certChain;
  }
  
  public Certificate getUserCert()
  {
    if ((certChain != null) && (certChain.length != 0)) {
      return certChain[0];
    }
    return null;
  }
  
  public String getAlias()
  {
    return alias;
  }
  
  public void setAlias(String alias)
  {
    this.alias = alias;
  }
  
  public Key getKey()
  {
    return key;
  }
  
  public void setKey(Key key)
  {
    this.key = key;
  }
  
  public Certificate[] getCertChain()
  {
    return certChain;
  }
  
  public void setCertChain(Certificate[] certChain)
  {
    this.certChain = certChain;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.producer.AbstractKeyStoreLoader.KeyAliases
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */