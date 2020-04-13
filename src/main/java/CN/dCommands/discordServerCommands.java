package CN.dCommands;

import CN.byteCode;
import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.net.Server;


import mindustry.game.EventType.*;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.maps.Map;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.util.Optional;
//change maps
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class discordServerCommands implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;


    public discordServerCommands(JSONObject data){
        this.data = data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (data.has("prefix") && data.has("bot-channel-id") && event.getChannel().getIdAsString().equals(data.getString("bot-channel-id"))) {
            if (event.getMessageContent().equalsIgnoreCase("//gameover") || event.getMessageContent().startsWith(data.getString("prefix") + "gameover")) {
                if (!data.has("admin_role_id")) {
                    if (event.isPrivateMessage()) return;
                    event.getChannel().sendMessage(commandDisabled);
                    return;
                }
                Role r = getRole(event.getApi(), data.getString("admin_role_id"));

                if (!hasPermission(r, event)) return;
                // ------------ has permission --------------
                if (Vars.state.is(GameState.State.menu)) {
                    return;
                }
                //inExtraRound = false;
                Events.fire(new GameOverEvent(Team.crux));
            } else if (event.getMessageContent().equalsIgnoreCase("//maps") || event.getMessageContent().startsWith(data.getString("prefix") + "maps")) {
                StringBuilder mapLijst = new StringBuilder();
                mapLijst.append("List of available maps:\n");
                for (Map m : Vars.maps.customMaps()) {
                    mapLijst.append("* " + m.name() + "/ " + m.width + " x " + m.height + "\n");
                }
                mapLijst.append("Total number of maps: " + Vars.maps.customMaps().size);
                new MessageBuilder().appendCode("", mapLijst.toString()).send(event.getChannel());

            } else if (event.getMessageContent().startsWith("//exit") || event.getMessageContent().startsWith(data.getString("prefix") + "exit")) {
                if (!data.has("owner_role_id")) {
                    if (event.isPrivateMessage()) return;
                    event.getChannel().sendMessage(commandDisabled);
                    return;
                }
                Role r = getRole(event.getApi(), data.getString("owner_role_id"));
                if (!hasPermission(r, event)) return;

                Vars.net.dispose(); //todo: check
                Core.app.exit();
            }
        }

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
                if (event.isPrivateMessage()) return false;
                event.getChannel().sendMessage(noPermission);
                return false;
            } else {
                return true;
            }
        } catch (Exception _){
            return false;
        }
    }


}