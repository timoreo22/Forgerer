package fr.timoreo.forgerer;

import java.io.IOException;
import java.util.ArrayList;

public class Tester {
    private static final Object notifier = new Object();
    private static String path;

    /**
     * Sets the path where to install forge
     *
     * @param path the full path to the folder
     */
    public static void setPath(String path) {
        synchronized (notifier) {
            Tester.path = path;
            notifier.notifyAll();
        }
    }

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
     *
     * @param args is useless
     */
    public static void main(String[] args) {

        Tester.path = "";
        System.out.println("On va installer Le forge MDK!");
        long time = System.currentTimeMillis();
        try {
            Frameator frm = new Frameator();
            ArrayList<String> mcVer = Downloader.getVersions();
            synchronized (notifier) {
                notifier.wait();

                System.out.println("Enfin!");
                String[] lel = new String[]{"", ""};
                String chooseVersion = Frameator.openListDialog(mcVer.toArray(lel));
                lel = new String[]{"", ""};
                String choose = Frameator.openListDialog(new String[]{"Forge MDK", "Forge Client", "Forge Server"});
                switch (choose) {
                    case "Forge MDK":
                        Downloader.downloadVersion(chooseVersion, Tester.path);
                        break;
                    case "Forge Client":
                        Downloader.downloadInstallerVersion(chooseVersion, Tester.path);
                        break;
                    case "Forge Server":
                        Downloader.downloadServerInstallerVersion(chooseVersion, Tester.path);
                        break;
                    default:
                        break;
                }
            }
            System.out.println("Fini !");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Tadam!, ca a pris " + (System.currentTimeMillis() - time) + " ms");

    }

}
