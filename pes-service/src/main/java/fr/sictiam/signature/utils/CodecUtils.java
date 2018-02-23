package fr.sictiam.signature.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class CodecUtils
{
  public static String hexToBase64(String hexstring)
  {
    if (hexstring == null) {
      return null;
    }
    try
    {
      byte[] binval = Hex.decodeHex(hexstring.toCharArray());
      return Base64.encodeBase64String(binval);
    }
    catch (Exception e) {}
    return null;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.utils.CodecUtils
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */