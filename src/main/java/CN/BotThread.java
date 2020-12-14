package CN;

import CN.dCommands.discordCommands;
import CN.dCommands.discordServerCommands;
import CN.dCommands.discordia;
import mindustry.net.Administration;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.util.Optional;

public class BotThread extends Thread{
    public DiscordApi api;
    private Thread mt;
    private JSONObject data;

    public BotThread(DiscordApi _api, Thread _mt, JSONObject _data) {
        api = _api; //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        mt = _mt;
        data = _data;

        //communication commands
        api.addMessageCreateListener(new discordCommands());
        api.addMessageCreateListener(new discordServerCommands());
        api.addMessageCreateListener(new discordia(api));
    }

    public void run(){
        try {
            Thread.sleep(60 * 1000);
        } catch (Exception e) {
        }
        int sixT = 0;
        while (this.mt.isAlive()) {
            try {
                Thread.sleep(1000);
                sixT++;
            } catch (Exception e) {
            }
            //run stuff
            if (data.has("lc_id"+Administration.Config.port.num())) {
                if (!Main.liveChatQueue.equals("")) {
                    TextChannel lc = this.getTextChannel(data.getString("lc_id"+Administration.Config.port.num()));
                    lc.sendMessage(Main.liveChatQueue);
                    Main.liveChatQueue = "";
                }
            }
            //60s timer
            if (sixT == 60) {
                sixT = 0;
                data = byteCode.get("settings");
            }
        }
        api.disconnect();
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

    public Role getRole(String id) {
        Optional<Role> r1 = this.api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: adminrole not found!");
            return null;
        }
        return r1.get();
    }

    public void setStatus(int players) {
        if (players == 0) {
            this.api.updateActivity("with nobody ;-;");
        } else if (players == 1) {
            this.api.updateActivity("with 1 player.");
        } else {
            this.api.updateActivity("with " + players + " players.");
        }
    }

}
