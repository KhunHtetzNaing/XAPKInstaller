package com.htetznaing.xapkinstaller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by HtetzNaing on 12/5/2017.
 */

public class Fucker {
    public boolean unZip(String zipFile, String targetPath)
    {
        if ((zipFile == null) || (zipFile.equals(""))) {
            System.out.println("Invalid source file");
            return false;
        }
        System.out.println("Zip file extracted!");
        return unzip(zipFile, targetPath);
    }

    private static boolean unzip(String zipFile, String targetPath){
        try
        {
            File fSourceZip = new File(zipFile);
            File temp = new File(targetPath);
            temp.mkdir();
            System.out.println(targetPath + " created");
            ZipFile zFile = new ZipFile(fSourceZip);
            Enumeration e = zFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)e.nextElement();
                File destinationFilePath = new File(targetPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (!entry.isDirectory())
                {
                    System.out.println("Extracting " + destinationFilePath);
                    BufferedInputStream bis = new BufferedInputStream(zFile.getInputStream(entry));

                    byte[] buffer = new byte['Ѐ'];
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                    int b;
                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                }
            }
        }
        catch (IOException ioe) {
            System.out.println("IOError :" + ioe);
            return false;
        }
        return true;
    }

    public boolean deleteDirectory(String path) {
        File directory = new File(path);

        // If the directory exists then delete
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files == null) {
                return true;
            }
            // Run on all sub files and folders and delete them
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                }
            }
        }
        return directory.delete();
    }

    public boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }
}
