package atomicstryker.ruins.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

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
            return;
        }

        URL url = ruinsMod.getClass().getClassLoader().getResource(RuinsMod.TEMPLATE_PATH_JAR);
        try {
            FileUtils.copyDirectory(new File(url.getPath()), targetDir);
            System.out.println("Ruins successfully extracted default templates");
        } catch (Exception e) {
            System.err.println("Ruins couldn't prepare template defaults for some reason:");
            e.printStackTrace();
        }
    }

}
