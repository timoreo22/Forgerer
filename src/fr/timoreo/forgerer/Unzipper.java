package fr.timoreo.forgerer;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A utility class to unzip and replace files in a zip
 *
 * @author timoreo
 */
public class Unzipper {

    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param zipFilePath the path to the zip to unzip
     * @param destDirectory the directory where put all the files
     * @throws IOException in case the program can't access to the zip or the directory
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn the ZipInputStream where the entry is
     * @param filePath the path where to put this file
     * @throws IOException in case the program can't access to the output
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /**
     * Replaces a file inside of a zipped file with a file
     * @param zipPath the path to the zip
     * @param content the file to remplace with
     * @param fileName the path of the file inside of the zip to replace eg : net/minecraftforge/installer/SimpleInstaller.class
     * @param zipOut the out location of the zip
     * @throws IOException if the program don't have access to the zip, file to modify and the zipout
     * @see Unzipper#replace(String, byte[], String, String)
     */
    public static void replace(String zipPath,File content,String fileName,String zipOut) throws IOException{
        replace(zipPath,Files.readAllBytes(content.toPath()),fileName,zipOut);
    }


    /**
     * Gets the size of a file inside of a zip
     *
     * @param zipPath the path to the zip
     * @param fileName the path to the file inside of the zip to replace eg : net/minecraftforge/installer/SimpleInstaller.class
     * @return the size of the file
     * @throws IOException in case the program don't have read access to the zip
     */
    public static long getFileSize(String zipPath,String fileName) throws  IOException{
        ZipFile zipFile = new ZipFile(zipPath);
        for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry entryIn = (ZipEntry) e.nextElement();
            System.out.println("File got :" + entryIn.getName());
            if(entryIn.getName().equalsIgnoreCase(fileName)){
                System.out.println("This is the good one!");
                return entryIn.getSize();
            }
        }
        return 0;
    }
    /**
     * Replaces a file inside of a zipped file with binary file
     * @param zipPath the path to the zip
     * @param content the byte array witch contains the file
     * @param fileName the path of the file inside of the zip to replace eg : net/minecraftforge/installer/SimpleInstaller.class
     * @param zipOut the out location of the zip
     * @throws IOException if the program don't have access to the zip and the zipout
     */
    public static void replace(String zipPath,byte[] content,String fileName,String zipOut) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipOut));
        for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry entryIn = (ZipEntry) e.nextElement();
            System.out.println("On fait : " + entryIn.getName());
            if (!entryIn.getName().equalsIgnoreCase(fileName)) {
                zos.putNextEntry(entryIn);
                InputStream is = zipFile.getInputStream(entryIn);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
            } else {
                System.out.println("Oh! faut remplacer!");
                zos.putNextEntry(new ZipEntry(fileName));
                //InputStream is = zipFile.getInputStream(entryIn);

                //while ((len = (is.read(buf))) > 0) {

                    zos.write(content, 0, content.length);
                    //}
                //}
                zos.closeEntry();
            }

        }
        zos.close();
    }
}
