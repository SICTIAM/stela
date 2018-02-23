package fr.sictiam.signature.pes.verifier;

public class SimplePesInformation$EntetePesInfo
{
  private String dteStr;
  private String idPost;
  private String idColl;
  private String codCol;
  private String codBud;
  private String libelleColBud;
  private GeneralSignaturePesData generalSignaturePesData;
  
  public SimplePesInformation$EntetePesInfo(String dteStr, String idPost, String idColl, String codCol, String codBud, String libelleColBud)
  {
    this.dteStr = dteStr;
    this.idPost = idPost;
    this.idColl = idColl;
    this.codCol = codCol;
    this.codBud = codBud;
    this.libelleColBud = libelleColBud;
  }
  
  public String getCodBud()
  {
    return codBud;
  }
  
  public String getCodCol()
  {
    return codCol;
  }
  
  public String getDteStr()
  {
    return dteStr;
  }
  
  public String getIdColl()
  {
    return idColl;
  }
  
  public String getIdPost()
  {
    return idPost;
  }
  
  public String getLibelleColBud()
  {
    return libelleColBud;
  }
  
  public GeneralSignaturePesData getGeneralSignaturePesData()
  {
    return generalSignaturePesData;
  }
  
  public void setGeneralSignaturePesData(GeneralSignaturePesData generalSignaturePesData)
  {
    this.generalSignaturePesData = generalSignaturePesData;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.pes.verifier.SimplePesInformation.EntetePesInfo
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */