package io.minecast.uuid.fetchers;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class DefaultFetcher extends UUIDFetcher {

    private HashMap<String, String> uuidMap;
    private HashMap<String, Long> expireMap;

    private final String api = "http://uuid.turt2live.com/api/v2/uuid/";

    public DefaultFetcher() {
        uuidMap = new HashMap<String, String>();
        expireMap = new HashMap<String, Long>();
    }

    @Override
    public String fetchUUID(Player player) {
        if (player == null)
            return null;
        if (uuidMap.containsKey(player.getName())) {
            if(expireMap.get(uuidMap.get(player.getName())) > System.currentTimeMillis())
                return uuidMap.get(player.getName());
            else {
                expireMap.remove(uuidMap.get(player.getName()));
                uuidMap.remove(player.getName());
            }

        }
        try {
            URL url = new URL(api + player.getName());
            String rawJson = readJson(url);
            JsonObject object = new JsonParser().parse(rawJson).getAsJsonObject();
            String uuid = object.get("uuid").toString().replaceAll("\"", "");
            uuidMap.put(player.getName(), uuid);
            expireMap.put(uuid, System.currentTimeMillis() + 120000);
            return uuid;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String readJson(URL url) throws IOException {
        InputStream is = url.openStream();
        StringBuilder sb = new StringBuilder();
        Reader rd = new BufferedReader(new InputStreamReader(is));
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
