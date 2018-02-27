package fr.sictiam.signature.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils
{
  public static final String TEMP_FILE_PREFIX = "temp";
  
  public static File createTemporaryFileFromStream(InputStream inputStream, String name)
    throws IOException
  {
    File file = File.createTempFile(name.substring(0, name.lastIndexOf(".")), name.substring(name.lastIndexOf(".")));
    file.deleteOnExit();
    org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, file);
    return file;
  }
}

/* Location:
 * Qualified Name:     com.axyus.signature.utils.FileUtils
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.1
 */