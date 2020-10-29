package atomicstryker.stalkercreepers;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("stalkercreepers")
public final class StalkerCreepers {

    public static final Logger LOGGER = LogManager.getLogger("Stalker Creepers");

    public StalkerCreepers() {
        LOGGER.info("dummy instanced! Needed because FML otherwise complains about a missing mod");
    }
}