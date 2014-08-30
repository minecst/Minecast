package io.minecast.uuid.fetchers;

import org.bukkit.entity.Player;

public abstract class UUIDFetcher {

    /**
     * Fetch the UUID of a given player. Should be called OFF THE MAIN THREAD
     *
     * @param player Player who's UUID should be fetched
     * @return the uuid of the specified player
     */
    public abstract String fetchUUID(Player player);

}
