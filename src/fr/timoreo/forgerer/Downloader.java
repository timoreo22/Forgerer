package fr.timoreo.forgerer;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;


public class Downloader {

    /**
     * Counts lines of a text-based file
     *
     * @param filename the full path to the file
     * @return the number of lines
     * @throws IOException in case the program don't have read access to the file
     */
    private static int countLinesNew(File filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i = 0; i < 1024; ) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                System.out.println(readChars);
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }

    /**
     * Downloads a file to a file
     *
     * @param outputFolder the file where to output
     * @param inputURL     the URL to the file
     * @throws IOException in case the program can't access the outputFolder
     */
    public static void download(String outputFolder, String inputURL) throws IOException {
        File file = new File(outputFolder);
        file.createNewFile();
        URL website = new URL(inputURL);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(outputFolder);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    /**
     * Gets every Minecraft+Forge version
     * Returns it at format "1.12.2-14.23.0.2486"
     *
     * @return a ArrayList of versions
     */
    public static ArrayList<String> getVersions() throws IOException {
        File temp = File.createTempFile("versions", ".xml");
        download(temp.getAbsolutePath(), "https://files.minecraftforge.net/maven/net/minecraftforge/forge/maven-metadata.xml");
        try {
            SAXParser sax = SAXParserFactory.newInstance().newSAXParser();
            XMLVersionParser parser = new XMLVersionParser();
            sax.parse(temp, parser);
            return parser.versions;
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Downloads and install a forge MDK
     *
     * @param version the forge and minecraft version
     * @param output  the output folder
     * @throws IOException in case the program don't have access to the %temp% folder or to the output
     */
    public static void downloadVersion(String version, String output) throws IOException {
        download(output + "\\temp.zip", "https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-mdk.zip");
        Unzipper.unzip(output + "\\temp.zip", output);
        installMDK(output);
    }

    /**
     * Install a forge MDK assuming that he is downloaded to the folder
     *
     * @param folder the folder where the mdk has been downloaded
     */
    public static void installMDK(String folder) {
        File file = new File(folder);
        try {

            String javaProgram = System.getProperty("java.home") + "\\bin";
            String[] environment = {"PATH=" + javaProgram, "TEMP=" + System.getProperty("java.io.tmpdir"), "java.io.tempdir=" + System.getProperty("java.io.tmpdir"), "PATH=" + System.getenv("PATH")};
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start", "gradlew.bat", "setupDecompWorkspace", "eclipse"}, environment, file);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Downloads and installs a forge client to a .minecraft
     *
     * @param version the forge and minecraft version
     * @param output  the .minecraft folder where to install forge
     * @throws IOException in case the installer can't access the output folder
     */
    public static void downloadInstallerVersion(String version, String output) throws IOException {
        download(output + "\\temp.jar", "https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-installer.jar");
        new File(output + "\\temp.jar").deleteOnExit();
        //https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12.2-14.23.4.2726/forge-1.12.2-14.23.4.2726-installer.jar
        modifyCode(output + "\\temp.jar");
        new File(output + "\\out.jar").deleteOnExit();
        useInstaller(output);
        new File(output + "\\out.jar.log").deleteOnExit();

    }

    /**
     * Downloads and install a forge server
     *
     * @param version the forge and mc version
     * @param output the folder to output
     * @throws IOException in case the installer can't access the output folder
     */
    public static void downloadServerInstallerVersion(String version, String output) throws IOException {
        download(output+"\\temp.jar","https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version +"/forge-"+version+"-installer.jar");
        new File(output+"\\temp.jar").deleteOnExit();
        //https://https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12.2-14.23.4.2726/forge-1.12.2-14.23.4.2726-installer.jar
        useServerInstaller(output);

        File f = new File(output + "\\forge-" + version + "-universal.jar");
        long fsize = Unzipper.getFileSize(output + "\\temp.jar", f.getName());
        while (!f.exists() || f.length() < fsize) {
            System.out.println("Waiting for finishing...");
        }
        f.renameTo(new File(output + "\\forge.jar"));
        new File(output + "\\temp.jar.log").deleteOnExit();
        writeStartupFile(output + "\\start.bat");
    }

    /**
     * Uses the forge installer to install a forge server to the folder
     *
     * @param folder the folder where the installer is
     */
    public static void useServerInstaller(String folder) {
        File file = new File(folder);
        try {

            String javaProgram = System.getProperty("java.home") + "\\bin";
            String[] environment = {"PATH=" + javaProgram, "TEMP=" + System.getProperty("java.io.tmpdir"), "java.io.tempdir=" + System.getProperty("java.io.tmpdir"), "PATH=" + System.getenv("PATH")};
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start", "/WAIT", "java", "-jar", folder + "\\temp.jar", "-installServer"}, environment, file);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Writes a startup file for a forge server named forge.jar
     *
     * @param folder the folder where the server is and where the startup file is going to be write
     * @throws IOException in case the program can't access the folder
     */
    public static void writeStartupFile(String folder) throws IOException {
        File file = new File(folder);
        file.createNewFile();
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("java -jar forge.jar" + System.lineSeparator() + "PAUSE");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Makes the forge client installer compatible with the command line
     *
     * @param path the full path to the installer
     * @throws IOException in case the program can't access to the file
     */
    public static void modifyCode(String path) throws IOException {
        System.out.println("Fini de Télécharger, on passe a la modification");
        byte[] b1 = ReInstallerDump.dump();
        //TimeUnit.SECONDS.sleep(10);
        //overrideFile(path,b1);
        Unzipper.replace(path, b1, "net/minecraftforge/installer/SimpleInstaller.class", path.substring(0, path.length() - 8) + "out.jar");
        System.out.println("on a visité!");
    }

    /**
     * Uses the Modified forge installer to install a forge client to the folder
     *
     * @param folder the .minecraft folder
     * @see Downloader#modifyCode(String)
     */
    public static void useInstaller(String folder) {
        File file = new File(folder);
        try {

            String javaProgram = System.getProperty("java.home") + "\\bin";
            String[] environment = {"PATH=" + javaProgram, "TEMP=" + System.getProperty("java.io.tmpdir"), "java.io.tempdir=" + System.getProperty("java.io.tmpdir"), "PATH=" + System.getenv("PATH")};
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start", "/WAIT", "java", "-jar", folder + "\\out.jar", "-installClient", folder}, environment, file);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * This class will parse any version info formatted like this
     * <pre>{@code     <versions>
     *        <version>1.12-1320561</version>
     *        <version>1.74-1.5.69</version>
     *     </versions>}<pre/>Will extract this {@code ["1.12","1.17"]} and {@code ["1320561","1.5.69"]}
     */
    static class XMLVersionParser extends DefaultHandler {
        public ArrayList<String> versions = new ArrayList<>();
        private boolean isParsingVersions = false;

        @Override
        public void startDocument() {
            versions.clear();
            isParsingVersions = false;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals("versions")) {
                isParsingVersions = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equals("versions")) {
                isParsingVersions = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (isParsingVersions) {
                String version = new String(ch, start, length);
                if (!version.startsWith("\n")) {
                    versions.add(version);
                }
            }
        }
    }
}
