package org.balab.minireal.data.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService
{

    public ZipService()
    {

    }

    // ** a service to zip a simulation directory
    public String createSimZip(String dirname)
    {
        try
        {
            String zip_name = dirname + ".zip";
            FileOutputStream fos = new FileOutputStream(zip_name);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(dirname);

            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
            return zip_name;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    // a helper method to zip file
    private void zipFile (File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException
    {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    // **  a service to allow user to download simulation zip file
    // public
}
