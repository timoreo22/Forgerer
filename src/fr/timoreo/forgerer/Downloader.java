package fr.timoreo.forgerer;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;


public class Downloader {

    /**
     * Counts lines of a text-based file
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
	            for (int i=0; i<1024;) {
	                if (c[i++] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        // count remaining characters
	        while (readChars != -1) {
	            System.out.println(readChars);
	            for (int i=0; i<readChars; ++i) {
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
     * @param outputFolder the file where to output
     * @param inputURL the URL to the file
     * @throws IOException in case the program can't access the outputFolder
     */
	public static void download(String outputFolder,String inputURL) throws IOException {
		File file = new File(outputFolder);
		file.createNewFile();
		URL website = new URL(inputURL);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(outputFolder);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

    /**
     * Gets every forge version for this MC version
     * @param tempFolder the temporary folder where to stock a temporary file
     * @param MCVersion the mc version to get forge versions from
     * @return a array list of forge versions NOT inculding mc version, in case the MCversion isn't supported by forge, returns a empty array list
     * @throws IOException in case the program can't access the tempFolder
     * @see Downloader#getMCVersions()
     */
	public static ArrayList<String> getForgeVersion(String tempFolder,String MCVersion) throws IOException {
        System.out.println("On prends les versions de forge!");
		download(tempFolder+"\\tempver.html","https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_"+MCVersion+".html");
		File file = new File(tempFolder+"\\tempver.html");
        ArrayList<String> ar = new ArrayList<>();
		double i =  (double)countLinesNew(file);
		if(i == 0){
		    return ar;
        }
		BufferedReader reader;
		String line;
		int num = 0;
		reader = new BufferedReader(new FileReader(file));
		line = reader.readLine();
		boolean isRawVersion = false;

		while (line != null) 
        {
            
             if(line.contains("<td class=\"download-version\">")) {
            	 isRawVersion = true;
             }
             // System.out.println("Contenu : " + line);
             System.out.println("Ligne Lue! " + num + "/" + i + " Pourcentage : " + num*100/i +"%");
            line = reader.readLine();
            if(isRawVersion) {
            	isRawVersion = false;
            	ar.add(line.substring("                                ".length()));
            }
            num++;
          
            
        }
		
		reader.close();
		return ar;
	}
	/**
	* Gets every  Minecraft version supported by Forge
     * Returns it at format "1.12.2"
     *
     * @return a ArrayList of versions like "1.1","1.2"...
     */
	public static ArrayList<String> getMCVersions() {
		int ver = 1;
		int mod = 0;

		ArrayList<String> ar = new ArrayList<>();
		while(ver <= 13) {
			while(mod <=10) {
				mod++;
				System.out.println("Searching for : " +"1." + ver+"."+mod );
				if(exists("https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1."+ver+"."+mod+".html")) {
					ar.add("1."+ver+"."+ mod);
				}
			}
			mod=0;
			ver++;
		}

		return ar;
	}

    /**
     * gets if internet page or file exists
     *
     * @param URLName the full url to the page like http://test.com/index.html
     * @return <code>true</code> if the page returns a 200 code, <code>false</code> if the page returns a other code
     *
     */
	public static boolean exists(String URLName){
	    try {
	      HttpURLConnection.setFollowRedirects(true);
	      // note : you may also need
	      //        HttpURLConnection.setInstanceFollowRedirects(false)
	      HttpURLConnection con =
	         (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	    }
	    catch (Exception e) {
	       e.printStackTrace();
	       return false;
	    }
	  }

    /**
     * Downloads and install a forge MDK
     * @param version the forge and minecraft version
     * @param output the output folder
     * @throws IOException in case the program don't have access to the %temp% folder or to the output
     * @see Downloader#getMCForgeVersion(String, String)
     */
	public static void downloadVersion(String version,String output) throws IOException {
		download(output+"\\temp.zip","https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version +"/forge-"+version+"-mdk.zip");
		Unzipper.unzip(output+"\\temp.zip",output);
		installMDK(output);
	}

    /**
     * get the mc and forge version from them
     * this is equivalent to <code>mc+"-"+forge</code>
     *
     * @param mc the minecraft version
     * @param forge the forge version
     * @return the forge and minecraft version
     * @see Downloader#getMCVersions()
     * @see Downloader#getForgeVersion(String, String)
     */
	public static String getMCForgeVersion(String mc,String forge){
	    return mc+"-"+forge;
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
	    	String[] environment = {"PATH=" + javaProgram,"TEMP=" + System.getProperty("java.io.tmpdir"),"java.io.tempdir=" + System.getProperty("java.io.tmpdir"),"PATH="+System.getenv("PATH")};
	    	Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start", "gradlew.bat","setupDecompWorkspace","eclipse"}, environment, file);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

    /**
     * Downloads and installs a forge client to a .minecraft
     * @param version the forge and minecraft version
     * @param output the .minecraft folder where to install forge
     * @throws IOException in case the installer can't access the output folder
     * @see Downloader#getMCForgeVersion(String, String)
     */
	public static void downloadInstallerVersion(String version,String output) throws IOException {
		download(output+"\\temp.jar","https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version +"/forge-"+version+"-installer.jar");
        new File(output+"\\temp.jar").deleteOnExit();
		//https://https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12.2-14.23.4.2726/forge-1.12.2-14.23.4.2726-installer.jar
        modifyCode(output+"\\temp.jar");
        new File(output+"\\out.jar").deleteOnExit();
        useInstaller(output);
        new File(output+"\\out.jar.log").deleteOnExit();

	}

    /**
     * Downloads and install a forge server
     *
     * @param version the forge and mc version
     * @param output the folder to output
     * @throws IOException in case the installer can't access the output folder
     * @see Downloader#getMCForgeVersion(String, String)
     */
    public static void downloadServerInstallerVersion(String version,String output) throws IOException {
        download(output+"\\temp.jar","https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version +"/forge-"+version+"-installer.jar");
        new File(output+"\\temp.jar").deleteOnExit();
        //https://https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12.2-14.23.4.2726/forge-1.12.2-14.23.4.2726-installer.jar
        useServerInstaller(output);

        File f = new File(output+"\\forge-"+version+"-universal.jar");
        long fsize = Unzipper.getFileSize(output+"\\temp.jar",f.getName());
        while(!f.exists()||f.length() < fsize){
           System.out.println("Waiting for finishing...");
        }
        f.renameTo(new File(output+"\\forge.jar"));
        new File(output+"\\temp.jar.log").deleteOnExit();
        writeStartupFile(output+"\\start.bat");
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
     * Uses the forge installer to install a forge server to the folder
     * @param folder  the folder where the installer is
     */
    public static void useServerInstaller(String folder) {
        File file = new File(folder);
        try {

            String javaProgram = System.getProperty("java.home") + "\\bin";
            String[] environment = {"PATH=" + javaProgram,"TEMP=" + System.getProperty("java.io.tmpdir"),"java.io.tempdir=" + System.getProperty("java.io.tmpdir"),"PATH="+System.getenv("PATH")};
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start","/WAIT", "java","-jar",folder +"\\temp.jar","-installServer"}, environment, file);

        } catch (IOException e) {

            e.printStackTrace();
        }
	}

    /**
     * Makes the forge client installer compatible with the command line
     * @param path the full path to the installer
     * @throws IOException in case the program can't access to the file
     */
	public static void modifyCode(String path) throws IOException {
	    System.out.println("Fini de Télécharger, on passe a la modification");
	    byte[] b1 = ReInstallerDump.dump();
        //TimeUnit.SECONDS.sleep(10);
        //overrideFile(path,b1);
        Unzipper.replace(path,b1,"net/minecraftforge/installer/SimpleInstaller.class",path.substring(0,path.length()-8)+"out.jar");
        System.out.println("on a visité!");
	}

    /**
     * Uses the Modified forge installer to install a forge client to the folder
     * @param folder  the .minecraft folder
     * @see Downloader#modifyCode(String)
     */
	public static void useInstaller(String folder){
        File file = new File(folder);
        try {

            String javaProgram = System.getProperty("java.home") + "\\bin";
            String[] environment = {"PATH=" + javaProgram,"TEMP=" + System.getProperty("java.io.tmpdir"),"java.io.tempdir=" + System.getProperty("java.io.tmpdir"),"PATH="+System.getenv("PATH")};
            Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start","/WAIT", "java","-jar",folder +"\\out.jar","-installClient",folder}, environment, file);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
