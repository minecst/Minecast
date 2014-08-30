package io.minecast.uuid;

import io.minecast.Minecast;
import io.minecast.uuid.fetchers.DefaultFetcher;
import io.minecast.uuid.fetchers.UUIDFetcher;
import org.bukkit.entity.Player;

public class UUIDManager {

    private UUIDFetcher fetcher;
    private Minecast plugin;

    public UUIDManager(Minecast plugin) {
        this.plugin = plugin;
        this.fetcher = new DefaultFetcher();
    }

    /**
     * Returns the UUID of the specified player.
     *
     * @param player Player to fetch the uuid of.
     * @return Player's UUID
     */
    public String getUUID(Player player) {
        if (plugin.getServer().getOnlineMode()) { //UUID is reliable if server is Online
            return player.getUniqueId().toString();
        } else { //UUID may not be reliable if the server is in offline mode, fetch it externally.
            if (fetcher == null)
                return null;
            return fetcher.fetchUUID(player);
        }
    }

    /**
     * Allows you to set a custom UUID fetcher in case you want to supply a custom UUID source.
     *
     * @param fetcher The custome UUIDFetcher
     */
    public void setUUIDFetcher(UUIDFetcher fetcher) {
        this.fetcher = fetcher;
    }
}
