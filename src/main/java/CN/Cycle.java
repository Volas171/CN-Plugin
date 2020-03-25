package CN;

//imports
import arc.util.Log;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.json.JSONObject;

import java.util.Optional;

public class Cycle extends Thread {
    private DiscordApi api;
    private Thread mt;
    private JSONObject data;

    public Cycle(DiscordApi _api, Thread _mt, JSONObject _data) {
        api = _api; //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        mt = _mt;
        data = _data;
    }

    public void run() {
        //let things load
        try{
            Thread.sleep(30 * 1000);
        } catch (Exception e) {
        }
        Log.info("Started Cycle.");
        //start
        String liveChat = "";
        //run

        //repeat until server die
        while (mt.isAlive()) {
            //import livechat
            liveChat = Main.liveChat;
            //do stuff
            if (data.has("live_chat_channel_id")) {
                TextChannel tc = this.getTextChannel(data.getString("live_chat_channel_id"));
                if (!liveChat.equals("")) tc.sendMessage(liveChat);
                Main.liveChat = "";
            }
            //wait 1/4 s
            try{
                Thread.sleep(250);
            } catch (Exception e) {
            }
        }
        return;
    }

    public TextChannel getTextChannel(String id){
        Optional<Channel> dc =  ((Optional<Channel>)this.api.getChannelById(id));
        if (!dc.isPresent()) {
            System.out.println("[ERR!] discordplugin: channel not found!");
            return null;
        }
        Optional<TextChannel> dtc = dc.get().asTextChannel();
        if (!dtc.isPresent()){
            System.out.println("[ERR!] discordplugin: textchannel not found!");
            return null;
        }
        return dtc.get();
    }
}
