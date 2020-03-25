package CN.dCommands;

import arc.util.Log;
import mindustry.gen.Call;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import static mindustry.Vars.netServer;

public class aDiscord implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;


    public aDiscord(JSONObject data){
        this.data = data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (data.has("prefix") && data.has("bot_channel_admin_id") && event.getChannel().getIdAsString().equals(data.getString("bot_channel_admin_id"))) {
            //discord -> server
            String[] msg = event.getMessageContent().split(" ", 2);
            //Call.sendMessage("[sky]" + event.getMessageAuthor().getName() + " @discord >[] " + msg[1].trim());
            if (event.getMessageContent().equalsIgnoreCase("..uap") || event.getMessageContent().startsWith(data.getString("prefix") + "uap")) {
                if (data.has("owner_role_id")) {
                    if (msg.length > 1 && msg[1].length() > 0) {
                        netServer.admins.unAdminPlayer(msg[1]);
                        event.getChannel().sendMessage("unAdmin: " + msg[1]);
                    } else {
                        event.getChannel().sendMessage("[salmon]CT[white]: Un Admins Player, do `..uap <UUID>`.");
                    }
                } else {
                    if (event.isPrivateMessage()) return;
                    event.getChannel().sendMessage(commandDisabled);
                    return;
                }
            }
        }
    }
}
