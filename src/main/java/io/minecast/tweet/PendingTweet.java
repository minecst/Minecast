package io.minecast.tweet;

import io.minecast.Minecast;
import io.minecast.exceptions.MinecastException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;
import org.bukkit.entity.Player;

public class PendingTweet {

    private String tweet;
    private Player player;

    /**
     * @param player Tweeting player
     * @param tweet  Contents of the tweet
     */
    public PendingTweet(final Player player, String tweet) {
        this.player = player;
        this.tweet = tweet;
        show();
        Minecast.getInstance().getServer().getScheduler().runTaskLater(Minecast.getInstance(), new Runnable() {
            @Override
            public void run() {
                Minecast.queueTweet(player, null);
            }
        }, Minecast.getInstance().getConfig().getLong("tweet-expire", 100L));
    }

    /**
     * Displays the tweet to the user.
     */
    private void show() {
        for (String s : Minecast.getInstance().getConfig().getStringList("lang.pending-tweet")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', s.replace("=tweet=", tweet)));
        }
    }

    /**
     * Send the tweet off to Minecast!
     * WARNING: Only send tweets with the User's Permission, or your account will be banned.
     */
    public void tweet() {
        Bukkit.getScheduler().runTaskAsynchronously(Minecast.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    JsonObject ob = new JsonObject();
                    ob.addProperty("message", tweet);
                    String uuid = Minecast.getInstance().getUUIDManager().getUUID(player);
                    ob.addProperty("uuid", uuid);
                    String response = Request.Post("https://www.minecast.io/api/v1/" + Minecast.getInstance().getConfig().getString("api-key") + "/tweet")
                            .bodyForm(Form.form().add("request", ob.toString()).build())
                            .execute()
                            .returnContent().asString();
                    ob = new JsonParser().parse(response).getAsJsonObject();
                    if (!ob.get("status").toString().equals("\"okay\"")) {
                        throw new MinecastException(ob.get("code").getAsInt(), "Minecast Error " + ob.get("code").toString() + ": " + ob.get("message").toString());
                    }
                    player.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.tweet_success"));
                } catch (MinecastException e) {
                    e.printStackTrace();
                    switch (e.getCode()) {
                        case 403:
                            player.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.unlinked_account"));
                            break;
                        case 404:
                            player.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.not_trusted"));
                            player.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.not_trusted_url").replaceAll("=trust_url=", Minecast.getTrustURL()));
                            break;
                        default:
                            player.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.tweet_error"));
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Minecast.lang("lang.title") + Minecast.lang("lang.tweet_error"));
                }
            }
        });
    }


}
