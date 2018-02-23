package fr.sictiam.signature.pes.verifier;

public class SimplePesInformation$BordereauInfo
{
  private String id;
  private String exercice;
  private String montantHorsTaxe;
  private String XmlId;
  private String bordereauXmlId;
  
  public SimplePesInformation$BordereauInfo(String id, String exercice, String montantHorsTaxe, String XmlId, String bordereauXmlId)
  {
    this.id = id;
    this.exercice = exercice;
    this.montantHorsTaxe = montantHorsTaxe;
    this.XmlId = XmlId;
    this.bordereauXmlId = bordereauXmlId;
  }
  
  public String getExercice()
  {
    return exercice;
  }
  
  public String getId()
  {
    return id;
  }
  
  public String getMontantHorsTaxe()
  {
    return montantHorsTaxe;
  }
  
  public String getXmlId()
  {
    return XmlId;
  }
  
  public void setXmlId(String XmlId)
  {
    this.XmlId = XmlId;
  }
  
  public String getBordereauXmlId()
  {
    return bordereauXmlId;
  }
  
  public void setBordereauXmlId(String bordereauXmlId)
  {
    this.bordereauXmlId = bordereauXmlId;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.verifier.SimplePesInformation.BordereauInfo
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */