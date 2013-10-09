package atomicstryker.minions.common;

public enum PacketType
{
    /**
     * HasMinions override call from server to client
     */
    HASMINIONS,
    /**
     * Evil Deed Done Packet from client to server
     */
    EVILDEEDDONE,
    /**
     * Client requesting xp setting
     */
    REQUESTXPSETTING,
    
    /**
     * pickup entity from client to server: String username, int playerID, int entID
     */
    CMDPICKUPENT,
    /**
     * Minion drop all command from client to server: String username, int playerID, int minionID
     */
    CMDDROPALL,
    /**
     * Minion spawn command from client to server: String username, int x, int y, int z
     */
    CMDMINIONSPAWN,
    /**
     * Minion chop trees command from client to server: String username, int x, int y, int z
     */
    CMDCHOPTREES,
    /**
     * Minion dig stairwell command from client to server: String username, int x, int y, int z
     */
    CMDSTAIRWELL,
    /**
     * Minion dig stripmine command from client to server: String username, int x, int y, int z
     */
    CMDSTRIPMINE,
    /**
     * Minion chest assign command from client to server: String username, bool isSneaking, int x, int y, int z
     */
    CMDASSIGNCHEST,
    /**
     * Minions moveto command from client to server: String username, int x, int y, int z
     */
    CMDMOVETO,
    /**
     * Minion mineorevein command from client to server: String username, int x, int y, int z
     */
    CMDMINEOREVEIN,
    /**
     * Minion follow command from client to server: String username
     */
    CMDFOLLOW,
    /**
     * Minion unsummon command from client to server: String username
     */
    CMDUNSUMMON,
    /**
     * Minion custom space dig command from client to server: String username, int x, int y, int z, int width, int height
     */
    CMDCUSTOMDIG,
    
    /**
     * lightning bolt request client to server, server sends to all: double xstart, double ystart, double zstart, double xend, double yend, double zend, double rand
     */
    LIGHTNINGBOLT,
    /**
     * client requests sound distribution packet: int entID, String soundString
     */
    SOUNDTOALL,
    /**
     * entity grab propagation server to client: int minionEntID, int targetEntID
     */
    ENTITYMOUNTED,
    /**
     * Testing purposes and stuff.
     */
    HAX;
    
    public static PacketType byID(int ID)
    {
        return values()[ID];
    }
}
