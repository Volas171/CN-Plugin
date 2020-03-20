package CN;

import CN.dCommands.discordCommands;
import CN.dCommands.discordServerCommands;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import org.javacord.api.DiscordApi;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.Thread;

import static mindustry.Vars.playerGroup;

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
        api.addMessageCreateListener(new discordServerCommands(data));
    }

    public void run(){
        int x = 0;

        while (this.mt.isAlive()){
            try{
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            //update players
            
            //update core resources

            //update teaminfo

            //save and add a minute
            if (x == 4) {
                x = 0;
                //output PI save file
                try {
                    FileOutputStream fileOut = new FileOutputStream("PDF.cn");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(Main.database);
                    out.flush();
                    out.close();
                    fileOut.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }

                //add 1 minute of play time for each player
                for (Player p : playerGroup.all()) {
                    if (Main.database.containsKey(p.uuid)) {
                        Main.database.get(p.uuid).addTp(1);
                        Call.onInfoToast(p.con,"+1 minutes played.", 3);
                        //auto congratulations
                        int y = Main.database.get(p.uuid).getTP() / 60;
                        float z = (float) Main.database.get(p.uuid).getTP()/60;
                        if ((float) y == z) {
                            Call.sendMessage("Congratulations to " + p.name + " [white]for staying active for " + y + " Hours!");
                        }
                        byteCode.aRank(p.uuid);
                    }
                }

                //Update player list

            } else {
                x++;
            }
        }
        if (data.has("serverdown_role_id")){
            Role r = new utilmethods().getRole(api, data.getString("serverdown_role_id"));
            TextChannel tc = new utilmethods().getTextChannel(api, data.getString("serverdown_channel_id"));
            if (r == null || tc ==  null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception _) {}
            } else {
                if (data.has("serverdown_name")){
                    String serverNaam = data.getString("serverdown_name");
                    new MessageBuilder()
                            .append(String.format("%s\nServer %s is down",r.getMentionTag(),((serverNaam != "") ? ("**"+serverNaam+"**") : "")))
                            .send(tc);
                } else {
                    new MessageBuilder()
                            .append(String.format("%s\nServer is down.", r.getMentionTag()))
                            .send(tc);
                }
            }
        }
        api.disconnect();
    }
}
