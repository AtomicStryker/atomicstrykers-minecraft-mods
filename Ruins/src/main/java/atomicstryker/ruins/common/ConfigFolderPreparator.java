package atomicstryker.ruins.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * As curse finally outlawed zip files, the setup portion of Ruins will have to
 * be automated and extract from the jar itself.
 */
public class ConfigFolderPreparator
{

    /**
     * check if target folder .minecraft/config/ruins_config exists, if not,
     * copy it from the jar
     * 
     * @param ruinsMod
     */
    public static void copyFromJarIfNotPresent(RuinsMod ruinsMod, File targetDir)
    {
        if (targetDir.exists())
        {
            return;
        }

        URL url = ruinsMod.getClass().getClassLoader().getResource(RuinsMod.TEMPLATE_PATH_JAR);
        try
        {
            copyJarResourceToFolder((JarURLConnection) url.openConnection(), targetDir);
        }
        catch (Exception e)
        {
            System.err.println("Ruins couldn't prepare template defaults for some reason:");
            e.printStackTrace();
        }
    }

    /**
     * This method will copy resources from the jar file of the current thread
     * and extract it to the destination folder. source:
     * https://stackoverflow.com/questions/1386809/copy-directory-from-a-jar-file
     * and the tess4j project
     */
    private static void copyJarResourceToFolder(JarURLConnection jarConnection, File destDir)
    {

        try
        {
            JarFile jarFile = jarConnection.getJarFile();

            /**
             * Iterate all entries in the jar file.
             */
            for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();)
            {

                JarEntry jarEntry = e.nextElement();
                String jarEntryName = jarEntry.getName();
                String jarConnectionEntryName = jarConnection.getEntryName();

                /**
                 * Extract files only if they match the path.
                 */
                if (jarEntryName.startsWith(jarConnectionEntryName))
                {

                    String filename = jarEntryName.startsWith(jarConnectionEntryName) ? jarEntryName.substring(jarConnectionEntryName.length()) : jarEntryName;
                    File currentFile = new File(destDir, filename);

                    if (jarEntry.isDirectory())
                    {
                        currentFile.mkdirs();
                    }
                    else
                    {
                        InputStream is = jarFile.getInputStream(jarEntry);
                        OutputStream out = FileUtils.openOutputStream(currentFile);
                        IOUtils.copy(is, out);
                        is.close();
                        out.close();
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
