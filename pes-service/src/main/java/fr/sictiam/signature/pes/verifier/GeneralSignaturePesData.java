package fr.sictiam.signature.pes.verifier;

public class GeneralSignaturePesData
{
  private String enTeteRecetteValue;
  private String enteteDepenseValue;
  private String idPesAller;
  
  public String getEnTeteRecetteValue()
  {
    return enTeteRecetteValue;
  }
  
  public void setEnTeteRecetteValue(String enTeteRecetteValue)
  {
    this.enTeteRecetteValue = enTeteRecetteValue;
  }
  
  public String getEnteteDepenseValue()
  {
    return enteteDepenseValue;
  }
  
  public void setEnteteDepenseValue(String enteteDepenseValue)
  {
    this.enteteDepenseValue = enteteDepenseValue;
  }
  
  public String getIdPesAller()
  {
    return idPesAller;
  }
  
  public void setIdPesAller(String idPesAller)
  {
    this.idPesAller = idPesAller;
  }
  
  public boolean isEnteteDepenseValue()
  {
    return ("1".equals(enteteDepenseValue)) || ("true".equals(enteteDepenseValue));
  }
  
  public boolean isEnteteRecetteValue()
  {
    return ("1".equals(enTeteRecetteValue)) || ("true".equals(enTeteRecetteValue));
  }
  
  public boolean isNotEnteteDepenseValue()
  {
    return ("0".equals(enteteDepenseValue)) || ("false".equals(enteteDepenseValue));
  }
  
  public boolean isNotEnteteRecetteValue()
  {
    return ("0".equals(enTeteRecetteValue)) || ("false".equals(enTeteRecetteValue));
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.verifier.GeneralSignaturePesData
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */