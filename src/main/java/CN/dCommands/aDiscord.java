package CN.dCommands;

import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;

import mindustry.world.blocks.storage.CoreBlock;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.util.Optional;

import static mindustry.Vars.*;

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
        //command
        if (data.has("prefix") && data.has("bot_channel_admin_id") && event.getChannel().getIdAsString().equals(data.getString("bot_channel_admin_id"))) {
            //check properly setup
            if (!data.has("mod_role_id") || !data.has("admin_role_id") || !data.has("owner_role_id") || !data.has("bot_role_id")) {
                if (event.isPrivateMessage()) return;
                event.getChannel().sendMessage(commandDisabled);
                return;
            }
            //get rank
            int rank = 0;
            Role r = getRole(event.getApi(), data.getString("mod_role_id"));
            if (hasPermission(r, event)) {
                rank = 5;
            } else {
                return;
            }
            r = getRole(event.getApi(), data.getString("admin_role_id"));
            if (hasPermission(r, event)) rank = 6;
            r = getRole(event.getApi(), data.getString("owner_role_id"));
            if (hasPermission(r, event)) rank = 7;

            String[] arg = event.getMessageContent().split(" ", 3);

            if (arg[0].startsWith("..") || arg[0].startsWith(data.getString("prefix"))) {
                arg[0] = arg[0].replaceAll("\\.\\.","").replaceAll(data.getString("prefix"),"");
                switch (arg[0]) {
                    case "uap": //to be removed... too risky
                        if (rank == 7) {
                            if (arg.length > 1 && arg[1].length() > 0) {
                                netServer.admins.unAdminPlayer(arg[1]);
                                event.getChannel().sendMessage("unAdmin: " + arg[1]);
                                return;
                            } else {
                                event.getChannel().sendMessage("[salmon]CT[white]: Un Admins Player, do `/a uap <UUID>`.");
                            }
                        } else {
                            if (event.isPrivateMessage()) return;
                            event.getChannel().sendMessage(noPermission);
                            return;
                        }
                        break;
                    case "gameover":
                        if (rank >= 6) {
                            if (Vars.state.is(GameState.State.menu)) {
                                return;
                            }
                            //inExtraRound = false;
                            Events.fire(new EventType.GameOverEvent(Team.crux));
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getName() + "> Game ended.");
                            Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white]has ended the game.");
                        } else {
                            if (event.isPrivateMessage()) return;
                            event.getChannel().sendMessage(noPermission);
                            return;
                        }
                    case "sandbox":
                        if (rank >= 6) {
                            if (state.rules.infiniteResources) {
                                state.rules.infiniteResources = false;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white]has [lightgray]Disabled [white]Sandbox Mode.");
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getName() + "> Sandbox Mode turned off.");
                            } else {
                                state.rules.infiniteResources = true;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white]has [lightgray]Enabled [white]Sandbox Mode.");
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getName() + "> Sandbox Mode turned on.");
                            }
                            for (Player p : playerGroup.all()) {
                                Call.onWorldDataBegin(p.con);
                                netServer.sendWorldData(p);
                                Call.onInfoToast(p.con, "Auto Sync completed.", 5);
                            }
                        } else {
                            if (event.isPrivateMessage()) return;
                            event.getChannel().sendMessage(noPermission);
                            return;
                        }
                        break;
                    case "tk":
                        Teams.TeamData teamData = state.teams.get(Team.sharded);
                        if (!teamData.hasCore()) {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getName() + "> Your team doesn't have a core!");
                            return;
                        }
                        CoreBlock.CoreEntity core = teamData.cores.first();
                        core.items.add(Items.copper, 10000);
                        core.items.add(Items.lead, 10000);
                        core.items.add(Items.metaglass, 10000);
                        core.items.add(Items.graphite, 10000);
                        core.items.add(Items.titanium, 10000);
                        core.items.add(Items.thorium, 10000);
                        core.items.add(Items.silicon, 10000);
                        core.items.add(Items.plastanium, 10000);
                        core.items.add(Items.phasefabric, 10000);
                        core.items.add(Items.surgealloy, 10000);
                        Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white] has given 10k resources to core.");
                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getName() + "> Added 10k of all resources to core.");
                        break;
                    default:
                        event.getChannel().sendMessage(arg[0] + " is not a command!");
                        return;
                }
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
                return false;
            } else {
                return true;
            }
        } catch (Exception _){
            return false;
        }
    }
}
