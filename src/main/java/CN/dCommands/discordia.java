package CN.dCommands;

import CN.Main;
import CN.byteCode;

import arc.Events;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;

import mindustry.net.Administration;
import mindustry.world.blocks.storage.CoreBlock;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;

public class discordia implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;
    private DiscordApi api;


    public discordia(JSONObject data, DiscordApi api){
        this.data = data;
        this.api = api;
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

            if (arg[0].startsWith("//") || arg[0].startsWith(data.getString("prefix"))) {
                arg[0] = arg[0].replaceAll("//","").replaceAll(data.getString("prefix"),"");
                switch (arg[0]) {
                    case "gameover":
                        if (rank >= 6) {
                            if (Vars.state.is(GameState.State.menu)) {
                                return;
                            }
                            //inExtraRound = false;
                            Events.fire(new EventType.GameOverEvent(Team.sharded));
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> has ended the game.");
                            Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white]has ended the game.");
                        } else {
                            if (event.isPrivateMessage()) return;
                            event.getChannel().sendMessage(noPermission);
                            return;
                        }
                        break;
                    case "sandbox": //changes sandbox mode on/off, no arguments
                        if (rank >= 6) {
                            if (state.rules.infiniteResources) {
                                state.rules.infiniteResources = false;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white]has [lightgray]Disabled [white]Sandbox Mode.");
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> turned Sandbox Mode off.");
                            } else {
                                state.rules.infiniteResources = true;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white]has [lightgray]Enabled [white]Sandbox Mode.");
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> turned Sandbox Mode on.");
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
                    case "tk": //adds 10k resources to core
                    case "hk": //adds 100k resources to core
                        int amount = 0;
                        if (arg[0].equals("tk")) {
                            amount = 10000;
                        } else {
                            amount = 100000;
                        }
                        Teams.TeamData teamData = state.teams.get(Team.sharded);
                        if (!teamData.hasCore()) {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Your team doesn't have a core!");
                            return;
                        }
                        CoreBlock.CoreEntity core = teamData.cores.first();
                        core.items.add(Items.copper, amount);
                        core.items.add(Items.lead, amount);
                        core.items.add(Items.metaglass, amount);
                        core.items.add(Items.graphite, amount);
                        core.items.add(Items.titanium, amount);
                        core.items.add(Items.thorium, amount);
                        core.items.add(Items.silicon, amount);
                        core.items.add(Items.plastanium, amount);
                        core.items.add(Items.phasefabric, amount);
                        core.items.add(Items.surgealloy, amount);
                        Call.sendMessage("[scarlet]<Admin> [lightgray]" + event.getMessage().getAuthor().getDisplayName() + " [white] has given "+amount/1000+"k resources to core.");
                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> has added "+amount/1000+"k of all resources to core.");
                        break;
                    case "team": //changes team, #id - team
                        if (rank >= 5) {
                            if (arg.length > 2) {
                                if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                    //run
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Player ID '" + arg[1] + "' not found.");
                                    }
                                    Team setTeam;
                                    String setTeamColor;
                                    switch (arg[2]) {
                                        case "sharded":
                                            setTeam = Team.sharded;
                                            setTeamColor = "[accent]";
                                            break;
                                        case "blue":
                                            setTeam = Team.blue;
                                            setTeamColor = "[royal]";
                                            break;
                                        case "crux":
                                            setTeam = Team.crux;
                                            setTeamColor = "[scarlet]";
                                            break;
                                        case "derelict":
                                            setTeam = Team.derelict;
                                            setTeamColor = "[gray]";
                                            break;
                                        case "green":
                                            setTeam = Team.green;
                                            setTeamColor = "[lime]";
                                            break;
                                        case "purple":
                                            setTeam = Team.purple;
                                            setTeamColor = "[purple]";
                                            break;
                                        default:
                                            event.getChannel().sendMessage("[salmon]CT[lightgray]: Available teams: [accent]Sharded, [royal]Blue[lightgray], [scarlet]Crux[lightgray], [lightgray]Derelict[lightgray], [lime]Green[lightgray], [purple]Purple[lightgray].");
                                            return;
                                    }
                                    p.setTeam(setTeam);
                                    p.sendMessage("[salmon]CT[white]: Changed team to " + setTeamColor + arg[2] + "[white].");
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Team set to " + arg[2] + " for " + byteCode.noColors(p.name) + ".");
                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("ID can only contain numbers!");
                                }
                            } else if (arg.length > 1) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, You must specify team, *usage: id - teamName*");
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Changes team for target player, *usage: id - teamName*");
                            }
                        }
                        break;
                    case "gpi":
                        if (rank >= 5) {
                            if (arg.length > 1 && arg[1].length() > 3) {
                                if (arg[1].startsWith("#") && Strings.canParseInt(arg[1].substring(1))) {
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        if (Main.idTempDatabase.containsKey(id)) {
                                            p = Main.idTempDatabase.get(id);
                                        } else {
                                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID '[lightgray]" + id + "[white]'.");
                                            return;
                                        }
                                    }
                                    //get info
                                    JSONObject data = Main.adata.getJSONObject(Main.currentLogin.get(p.uuid));
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
                                } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                                    Administration.PlayerInfo p = netServer.admins.getInfo(arg[1]);
                                    //get info
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID `" + arg[1] + "` not found!");
                                    return;
                                }
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Gets Player Info using uuid, *usage: uuid*");
                            }
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
        } catch (Exception _){
            return false;
        }
    }
}
