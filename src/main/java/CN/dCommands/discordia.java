package CN.dCommands;

import CN.Main;

import CN.byteCode;
import arc.util.Strings;

import mindustry.entities.type.Player;

import mindustry.gen.Call;
import mindustry.net.Administration;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.util.Optional;


import static mindustry.Vars.*;

public class discordia implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;
    private DiscordApi api;


    public discordia(DiscordApi api){this.api = api;}

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        data = byteCode.get("settings");
        //command
        if (data.has("prefix"+Administration.Config.port.num()) && data.has("bot_channel_admin_id") && event.getChannel().getIdAsString().equals(data.getString("bot_channel_admin_id"))) {
            JSONObject login = byteCode.get("login_info");

            String[] arg = event.getMessageContent().split(" ", 3);

            if (arg[0].startsWith("//") || arg[0].startsWith(data.getString("prefix"+Administration.Config.port.num()))) {
                arg[0] = arg[0].replaceAll("//","").replaceAll(data.getString("prefix"+Administration.Config.port.num()),"");
                switch (arg[0]) {
                    case "gpi":
                        if (arg.length > 1) {
                            if (arg[1].startsWith("#") && Strings.canParseInt(arg[1].substring(1)) && arg[1].length() > 3) {
                                int id = Strings.parseInt(arg[1].substring(1));
                                Player p = playerGroup.getByID(id);
                                if (p == null) {
                                    if (Main.idTempDatabase.containsKey(id)) {
                                        p = Main.idTempDatabase.get(id);
                                    } else {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID `" + id + "`.");
                                        return;
                                    }
                                }
                                //get info
                                JSONObject data = byteCode.get(Main.currentLogin.get(p.uuid));
                                if (data == null) {
                                    event.getChannel().sendMessage("ERROR - Account missing dataID");
                                    return;
                                }
                                StringBuilder builder = new StringBuilder();
                                builder.append("name : ").append(p.name).append("\n");
                                builder.append("times joined : ").append(p.getInfo().timesJoined).append("\n");
                                builder.append("times kicked : ").append(p.getInfo().timesKicked).append("\n");
                                builder.append("uuid : ").append(p.uuid).append("\n");
                                for (String keyStr : data.keySet()) {
                                    Object keyvalue = data.get(keyStr);
                                    //Print key and value
                                    builder.append(keyStr + " : " + keyvalue).append("\n");
                                }
                                event.getChannel().sendMessage(builder.toString());
                            } else if (arg[1].startsWith("#")) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                return;
                            } else if (login.has(arg[1])) {
                                JSONObject user = login.getJSONObject(arg[1]);
                                JSONObject data = byteCode.get(user.getString("dataID"));
                                if (data == null) {
                                    event.getChannel().sendMessage("ERROR - Account missing dataID");
                                    return;
                                }
                                StringBuilder builder = new StringBuilder();
                                builder.append("```\n");
                                for (String keyStr : data.keySet()) {
                                    Object keyvalue = data.get(keyStr);
                                    //Print key and value
                                    builder.append(keyStr + " : " + keyvalue).append("\n");
                                }
                                builder.append("\n```");
                                event.getChannel().sendMessage(builder.toString());
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Username `" + arg[1] + "` not found!");
                                return;
                            }
                        } else {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Gets Player Info using uuid, *usage: //gpi <uuid>*");
                        }
                        break;
                    case "oim":
                        if (arg.length > 1) {
                            String message = arg[1];
                            if (arg.length > 2) {
                                message = message + " " + arg [2];
                            } else if (arg.length > 3) {
                                message = message + " " + arg [2] + " " + arg [3];
                            } else if (arg.length > 4) {
                                message = message + " " + arg [2] + " " + arg [3] + " " + arg [4];
                            }
                            Call.onInfoMessage(message);
                        } else {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> sends everyone a info message, *usage: //oim <message>*");
                        }
                        break;
                }
            }
        }
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

    public Role getRole(DiscordApi api, String id){
        Optional<Role> r1 = api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: role not found!");
            return null;
        }
        return r1.get();
    }
    public Boolean hasPermission(Role r, MessageCreateEvent event){
        try {
            if (r == null) {
                if (event.isPrivateMessage()) return false;
                event.getChannel().sendMessage(commandDisabled);
                return false;
            } else if (!event.getMessageAuthor().asUser().get().getRoles(event.getServer().get()).contains(r)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e){
            return false;
        }
    }
}
