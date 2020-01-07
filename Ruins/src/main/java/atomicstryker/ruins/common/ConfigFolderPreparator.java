package atomicstryker.ruins.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * As curse finally outlawed zip files, the setup portion of Ruins will have to
 * be automated and extract from the jar itself.
 */
public class ConfigFolderPreparator {

    /**
     * check if target folder .minecraft/config/ruins_config exists, if not,
     * copy it from the jar
     *
     * @param ruinsMod
     */
    public static void copyFromJarIfNotPresent(RuinsMod ruinsMod, File targetDir) {
        if (targetDir.exists()) {
            RuinsMod.LOGGER.info("config/ruins_config exists, not extracting");
            return;
        }
        targetDir.mkdir();

        try {
            InputStream inputStream = RuinsMod.class.getClassLoader()
                    .getResourceAsStream("assets\\ruins_config.zip");

            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                RuinsMod.LOGGER.info("extracting {}", zipEntry);
                if (!zipEntry.isDirectory()) {
                    File newFile = new File(targetDir, zipEntry.getName());
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                } else {
                    File directory = new File(targetDir, zipEntry.toString());
                    directory.mkdirs();
                    RuinsMod.LOGGER.info("created subdirectory");
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            System.err.println("Ruins couldn't prepare template defaults for some reason:");
            e.printStackTrace();
        }
    }

}
