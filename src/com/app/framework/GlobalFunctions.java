package com.app.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalFunctions
{
  static Logger log = LoggerFactory.getLogger(GlobalFunctions.class);
	
  public void fCopyXLS(String inputXLS, String destXLS)
  {
    try
    {
      File f1 = new File(inputXLS);
      File f2 = new File(destXLS);
      InputStream in = new FileInputStream(f1);
      OutputStream out = new FileOutputStream(f2);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0)
      {        
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    }
    catch (FileNotFoundException ex)
    {
      log.info(ex.getMessage() + " in the specified directory.");
      System.exit(0);
    }
    catch (IOException e)
    {
      log.info(e.getMessage());
      System.exit(0);
    }
  }
  
  public void fGlobalDeleteFolder(File FolderPath)
  {
    if (FolderPath.isDirectory())
    {
      String[] arrChildNodes = FolderPath.list();
      for (int i = 0; i < arrChildNodes.length; i++) {
        fGlobalDeleteFolder(new File(FolderPath, arrChildNodes[i]));
      }
    }
    FolderPath.delete();
  }
}
