package io.minecast;

import io.minecast.tweet.PendingTweet;
import io.minecast.util.Updater;
import io.minecast.uuid.UUIDManager;
import org.apache.http.client.fluent.Request;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

public class Minecast extends JavaPlugin implements Listener {

    private UUIDManager uuidManager;
    private static Minecast instance;
    private HashMap<String, PendingTweet> pendingTweets;
    private String url = null;

    private boolean update;
    private String name;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        checkUpdate();

        this.instance = this;

        uuidManager = new UUIDManager(this);
        pendingTweets = new HashMap<String, PendingTweet>();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tweet")) {
            StringBuilder tweet = new StringBuilder();
            for (String a : args)
                tweet.append(a + " ");
            Minecast.tweet((Player) sender, tweet.toString().trim());
            return true;
        } else if (cmd.getName().equalsIgnoreCase("yes")) {
            PendingTweet tweet = Minecast.getPendingTweet((Player) sender);
            if (tweet == null)
                sender.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.no_pending"));
            else
                tweet.tweet();
            return true;
        } else if (cmd.getName().equalsIgnoreCase("no")) {
            sender.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.pending_cancel"));
            Minecast.queueTweet((Player) sender, null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks for an Update
     */
    protected void checkUpdate() {
        if (getConfig().getBoolean("check-update", true)) {
            final Minecast plugin = this;
            final File file = this.getFile();
            final Updater.UpdateType updateType = getConfig().getBoolean("download-update", true) ? Updater.UpdateType.DEFAULT : Updater.UpdateType.NO_DOWNLOAD;
            final Updater updater = new Updater(plugin, 77361, file, updateType, false);
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                    name = updater.getLatestName();
                    if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
                        getLogger().log(Level.INFO, "Successfully updated Minecast to version {0} for next restart!", updater.getLatestName());
                    } else if (updater.getResult() == Updater.UpdateResult.NO_UPDATE) {
                        getLogger().log(Level.INFO, "We didn't find an update!");
                    }
                }
            });
        }
    }

    /**
     * @return The uuid manager used to fetch UUIDs
     */
    public UUIDManager getUUIDManager() {
        return this.uuidManager;
    }

    /**
     * @return the current instance of the Minecast plugin
     */
    public static Minecast getInstance() {
        return instance;
    }

    /**
     * Gets the current pending tweet for the player
     *
     * @param player Player to check
     * @return Pending Tweet of the player, or null if none exists.
     */
    public static PendingTweet getPendingTweet(Player player) {
        if (instance.pendingTweets.containsKey(player.getName()))
            return instance.pendingTweets.get(player.getName());
        return null;
    }

    /**
     * Fetch a value from the Lang file
     *
     * @param location yml location
     * @return Formatted Lang String
     */
    public static String lang(String location) {
        return ChatColor.translateAlternateColorCodes('&', getInstance().getConfig().getString(location));
    }

    /**
     * Sets the players current PendingTweet.
     * In spite of the name, doesnt actually queue tweets,
     * the previous pending tweet will be removed before
     * adding the new one.
     * @param player
     * @param tweet
     */
    public static void queueTweet(Player player, PendingTweet tweet) {
        instance.pendingTweets.remove(player.getName());
        instance.pendingTweets.put(player.getName(), tweet);
    }

    /**
     * Have a player send a tweet!
     *
     * @param player Player to tweet from
     * @param tweet  Contents of the tweet
     */
    public static void tweet(Player player, String tweet) {
        PendingTweet t = new PendingTweet(player, tweet);
        queueTweet(player, t);
    }

    /**
     * Should only be called off the Main thread.
     *
     * @param player Player to get trusted status
     * @return Returns true if the player is trusted.
     */
    public static boolean isPlayerTrusted(Player player) {
        try {
            String uuid = getInstance().getUUIDManager().getUUID(player);
            String resp = Request.Get("https://www.minecast.io/api/v1/" + getInstance().getConfig().getString("api-key") + "/trusted/" + uuid).execute().returnContent().asString();
            JsonObject ob = new JsonParser().parse(resp).getAsJsonObject();
            if (ob.get("status").toString().contains("okay")) {
                return Boolean.parseBoolean(ob.get(uuid).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Should only be called off the Main thread.
     * Players have to click the link and trust the
     * server before they are able to tweet.
     *
     * @return the server's designated trusted url.
     */
    public static String getTrustURL() {
        if (Minecast.getInstance().url != null)
            return Minecast.getInstance().url;
        try {
            JsonObject ob = new JsonParser().parse(Request.Get("https://www.minecast.io/api/v1/" + getInstance().getConfig().getString("api-key") + "/url").execute().returnContent().asString()).getAsJsonObject();
            if (ob.get("status").toString().contains("okay")) {
                String s = ob.get("url").toString();
                return s.substring(1, s.length() - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
