package fr.timoreo.forgerer;

import java.io.IOException;
import java.util.ArrayList;

public class Tester {
    /**
     * Sets the path where to install forge
     * @param path the full path to the folder
     */
	public static void setPath(String path) {
		Tester.path = path;
	} 
	 private static String path;

    /**
     * Gets the path where to install forge
     * @return the full path to the folder
     */
    public static String getPath() {
        return path;
    }

    /**
     * The main method
     * This will make a window appear and do stuff
     * @param args is useless
     * @throws IOException in case something append during
     */
	public static void main(String[] args) throws IOException {
		
		Tester.path = "";
		System.out.println("On va installer Le forge MDK!");
		long time = System.currentTimeMillis();
		try {
			Frameator frm = new Frameator();
			ArrayList<String> mcVer = Downloader.getMCVersions();
			while(Tester.path.equals("")) {
				System.out.println("On t'attends!");
			}
			System.out.println("Enfin!");
			String[] lel = new String[] {"",""};
					String chooseVersion = Frameator.openListDialog( mcVer.toArray(lel));
					lel = new String[] {"",""};
			String forge = Frameator.openListDialog(Downloader.getForgeVersion(System.getProperty("java.io.tmpdir"), chooseVersion).toArray(lel));
			String choose = Frameator.openListDialog(new String[]{"Forge MDK","Forge Client","Forge Server"});
			switch (choose){
                case "Forge MDK":
                    Downloader.downloadVersion(Downloader.getMCForgeVersion(chooseVersion,forge), Tester.path);
                    break;
                case "Forge Client":
                    Downloader.downloadInstallerVersion(Downloader.getMCForgeVersion(chooseVersion,forge),Tester.path);
                    break;
                case "Forge Server":
                    Downloader.downloadServerInstallerVersion(Downloader.getMCForgeVersion(chooseVersion,forge),Tester.path);
                    break;
                    default:
                        break;
            }
			System.out.println("Fini !");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Tadam!, ca a pris " + (System.currentTimeMillis() - time) + " ms");
			
	}

}
