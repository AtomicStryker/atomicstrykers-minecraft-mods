package atomicstryker.kenshiro.common;

public enum PacketType
{
    /**
     * Sent from client to server and back to announce both sides have the mod. No args.
     */
    HANDSHAKE,
    
    /**
     * Sent from client to server to tell about punching a Block dead. Args: x,y,z coord ints
     */
    BLOCKPUNCHED,
    
    /**
     * Sent from client to server and back to all, to tell about punching an Entity. Args: entityID int
     */
    ENTITYPUNCHED,
    
    /**
     * Sent from client to server to tell about starting a Kenshiro Volley, no args.
     */
    KENSHIROSTARTED,
    
    /**
     * Send from client to server to tell it has finished it's Kenshiro Volley, no args
     */
    KENSHIROENDED,
    
    /**
     * Sent from client to server to tell about kicking an Entity. Args: kickerID int, entityID int
     */
    ENTITYKICKED,
    
    /**
     * Sent from client to server and back to tell about causing some kind of noise. Args: sound String, x,y,z coord ints
     */
    SOUNDEFFECT,
    
    /**
     * Sent from client to server to cause Animation by using Packet18Animation from server towards players.
     * See NetClientHandler.handleAnimation for relevant Packet data. Args: entityID int, animationType int
     */
    ANIMATION
}
