package CN.dCommands;

import CN.Main;
import CN.byteCode;

import arc.Events;
import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;

import mindustry.net.Administration;
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
                    case "gameover": //triggers a game over, no arguments
                        if (rank >= 6) {
                            if (Vars.state.is(GameState.State.menu)) {
                                return;
                            }
                            //inExtraRound = false;
                            Events.fire(new EventType.GameOverEvent(Team.crux));
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
                        Teams.TeamData teamData = state.teams.get(Team.sharded);
                        if (!teamData.hasCore()) {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Your team doesn't have a core!");
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
                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> has added 10k of all resources to core.");
                        break;
                    case "team": //changes team, #id - team
                        if (arg.length > 2) {
                            if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                //run
                                int id = Strings.parseInt(arg[1].substring(1));
                                Player p = playerGroup.getByID(id);
                                if (p==null) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Player ID '"+arg[1]+"' not found.");
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
                                player.setTeam(setTeam);
                                event.getChannel().sendMessage("[salmon]CT[white]: Changed team to " + setTeamColor + arg[1] + "[white].");
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Team set to "+arg[1]+" for "+byteCode.noColors(player.name)+".");
                            } else if (arg[1].startsWith("#")) {
                                event.getChannel().sendMessage("ID can only contain numbers!");
                            }
                        } else if (arg.length > 1) {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, You must specify team, *usage: id - teamName*");
                        } else {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Changes team for target player, *usage: id - teamName*");
                        }
                        break;
                    case "gpi":
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
                                String rname = byteCode.nameR(p.name);
                                String dv = "false";
                                if (Main.database.containsKey(p.uuid) && Main.database.get(p.uuid).getVerified()) dv = "true";
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, \nName: " + p.name + "[white]" +
                                        "\nName Raw: " + rname +
                                        "\nTimes Joined: " + p.getInfo().timesJoined +
                                        "\nTimes Kicked: " + p.getInfo().timesKicked +
                                        "\nCurrent IP: " + p.getInfo().lastIP +
                                        "\nUUID: " + p.uuid +
                                        "\nRank: " + Main.database.get(p.uuid).getRank() +
                                        "\nBuildings Built: " + Main.database.get(p.uuid).getBB() +
                                        "\nMinutes Played: " + Main.database.get(p.uuid).getTP() +
                                        "\nGames Played: " + Main.database.get(p.uuid).getGP() +
                                        "\nDiscord Verified?: " + dv +
                                        "\nDiscord Tag: " + Main.database.get(p.uuid).getDiscordTag());
                            } else if (arg[1].startsWith("#")) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                return;
                            } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                                Administration.PlayerInfo p = netServer.admins.getInfo(arg[1]);
                                String rname = byteCode.nameR(p.lastName);
                                String dv = "false";
                                if (Main.database.containsKey(arg[1]) && Main.database.get(arg[1]).getVerified()) dv = "true";
                                event.getChannel().sendMessage("Name: " + p.lastName + "[white]" +
                                        "\nName Raw: " + rname +
                                        "\nTimes Joined: " + p.timesJoined +
                                        "\nTimes Kicked: " + p.timesKicked +
                                        "\nCurrent IP: " + p.lastIP +
                                        "\nUUID: " + arg[1] +
                                        "\nRank: " + Main.database.get(arg[1]).getRank() +
                                        "\nBuildings Built: " + Main.database.get(arg[1]).getBB() +
                                        "\nMinutes Played: " + Main.database.get(arg[1]).getTP() +
                                        "\nGames Played: " + Main.database.get(arg[1]).getGP() +
                                        "\nDiscord Verified?: " + dv +
                                        "\nDiscord Tag: " + Main.database.get(arg[1]).getDiscordTag());
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID `" + arg[1] + "` not found!");
                                return;
                            }
                        } else {
                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Gets Player Info using uuid, *usage: uuid*");
                        }
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
