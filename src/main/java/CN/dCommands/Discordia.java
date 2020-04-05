package CN.dCommands;

import CN.Main;
import CN.byteCode;

import arc.Events;
import arc.struct.Array;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.core.NetClient;
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

import java.util.Optional;

import static mindustry.Vars.*;

public class Discordia implements MessageCreateListener {
    final long minMapChangeTime = 30L; //30 seconds
    final String commandDisabled = "This command is disabled.";
    final String noPermission = "You don't have permissions to use this command!";

    private JSONObject data;
    private DiscordApi api;


    public Discordia(JSONObject data, DiscordApi api){
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

            String[] arg = event.getMessageContent().split(" ", 5);

            if (arg[0].startsWith("..") || arg[0].startsWith(data.getString("prefix"))) {
                arg[0] = arg[0].replaceAll("\\.\\.","").replaceAll(data.getString("prefix"),"");
                switch (arg[0]) {
                    case "gameover": //triggers a game over, no arguments
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
                                    player.setTeam(setTeam);
                                    player.sendMessage("[salmon]CT[white]: Changed team to " + setTeamColor + arg[1] + "[white].");
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Team set to " + arg[2] + " for " + byteCode.noColors(player.name) + ".");
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
                                    String rname = byteCode.nameR(p.name);
                                    String dv = "false";
                                    if (Main.database.containsKey(p.uuid) && Main.database.get(p.uuid).getVerified())
                                        dv = "true";
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, \nName: " + p.name + "[white]" +
                                            "\nName Raw: " + rname +
                                            "\nTimes Joined: " + p.getInfo().timesJoined +
                                            "\nTimes Kicked: " + p.getInfo().timesKicked +
                                            "\nCurrent IP: ||" + p.getInfo().lastIP +"||"+
                                            "\nUUID: ||" + p.uuid +"||"+
                                            "\nRank: " + Main.database.get(p.uuid).getRank() +
                                            "\nBuildings Built: " + Main.database.get(p.uuid).getBB() +
                                            "\nMinutes Played: " + Main.database.get(p.uuid).getTP() +
                                            "\nGames Played: " + Main.database.get(p.uuid).getGP() +
                                            "\nDiscord Verified?: " + dv +
                                            "\nDiscord Tag: ||" + Main.database.get(p.uuid).getDiscordTag()+"||");
                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                    return;
                                } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                                    Administration.PlayerInfo p = netServer.admins.getInfo(arg[1]);
                                    String rname = byteCode.nameR(p.lastName);
                                    String dv = "false";
                                    if (Main.database.containsKey(arg[1]) && Main.database.get(arg[1]).getVerified())
                                        dv = "true";
                                    event.getChannel().sendMessage("Name: " + p.lastName + "[white]" +
                                            "\nName Raw: " + rname +
                                            "\nTimes Joined: " + p.timesJoined +
                                            "\nTimes Kicked: " + p.timesKicked +
                                            "\nCurrent IP: ||" + p.lastIP +"||"+
                                            "\nUUID: ||" + arg[1] + "||"+
                                            "\nRank: " + Main.database.get(arg[1]).getRank() +
                                            "\nBuildings Built: " + Main.database.get(arg[1]).getBB() +
                                            "\nMinutes Played: " + Main.database.get(arg[1]).getTP() +
                                            "\nGames Played: " + Main.database.get(arg[1]).getGP() +
                                            "\nDiscord Verified?: " + dv +
                                            "\nDiscord Tag: ||" + Main.database.get(arg[1]).getDiscordTag()+"||");
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID `" + arg[1] + "` not found!");
                                    return;
                                }
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Gets Player Info using uuid, *usage: uuid*");
                            }
                        }
                        break;
                    case "pardon": //Un-Bans players
                        if (rank >= 6) {
                            if (arg.length > 1) {
                                if (netServer.admins.isIDBanned(arg[1])) {
                                    netServer.admins.unbanPlayerID(arg[1]);
                                    event.getChannel().sendMessage(" Unbanned player UUID " + arg[1] + ".");
                                } else {
                                    event.getChannel().sendMessage(", UUID " + arg[1] + " wasn't found or isn't banned.");
                                }
                            } else {
                                event.getChannel().sendMessage("Pardon uses uuid to un-ban players.");
                            }
                        }
                        break;
                    case "rpk":
                        if (rank >= 6) {
                            if (arg.length > 1 && arg[1].length() > 3) {
                                if (arg[1].startsWith("#") && Strings.canParseInt(arg[1].substring(1))) {
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID '" + id + "'.");
                                        return;
                                    }
                                    p.getInfo().timesKicked = 0;
                                    p.getInfo().timesJoined = 0;
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Times kicked set to zero for player " + p.getInfo().lastName);
                                    Log.info("<Admin> " + event.getMessage().getAuthor().getName() + " has reset times kicked for " + p.name + " ID " + id);
                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                                    Administration.PlayerInfo p = netServer.admins.getInfo(arg[1]);
                                    p.timesKicked = 0;
                                    p.timesJoined = 0;
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> set Times Kicked to zero for player uuid " + arg[1]);
                                    Log.info("<Admin> " + event.getMessage().getAuthor().getName() + " has reset times kicked for " + p.lastName + " UUID " + arg[1]);
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID " + arg[1] + " not found!");
                                }
                            } else {
                                event.getChannel().sendMessage("Get Player Info, use ID or UUID, to get a player's info");
                            }
                        }
                        break;
                    case "pcc": //Player close connection
                        if (rank >= 5) {
                            if (arg.length > 2) {
                                if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                    //run
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID '" + id + "'.");
                                        return;
                                    }
                                    String reason = arg[2];
                                    switch (arg.length - 1) {
                                        case 3:
                                            reason = arg[2] + " " + arg[3];
                                            break;
                                        case 4:
                                            reason = arg[2] + " " + arg[3] + " " + arg[4];
                                            break;
                                    }
                                    p.getInfo().timesKicked--;
                                    p.con.kick(reason, 1);

                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                    return;
                                }
                            } else if (arg.length > 1) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, You must provide a reason!");
                            } else {
                                event.getChannel().sendMessage("PCC Uses player ID to kick.");
                            }
                        }
                        break;
                    case "unkick":
                        if (rank >= 5) {
                            if (arg.length > 1) {
                                if (netServer.admins.getInfo(arg[1]).lastKicked > Time.millis()) {
                                    netServer.admins.getInfo(arg[1]).lastKicked = Time.millis();
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Un-Kicked player UUID " + arg[1] + ".");
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID " + arg[1] + " wasn't found or isn't kicked.");
                                }
                            } else {
                                event.getChannel().sendMessage("Un-Kick uses uuid to un-kick players.");
                            }
                        }
                        break;
                    case "tp":
                        if (rank >= 5) {
                            if (arg.length > 3) {
                                if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                    //run
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID '" + id + "'.");
                                        return;
                                    }
                                    String x2 = arg[2].replaceAll("[^0-9]", "");
                                    String y2 = arg[3].replaceAll("[^0-9]", "");
                                    if (x2.equals("") || y2.equals("")) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Coordinates must contain numbers!");
                                        return;
                                    }
                                    float x2f = Float.parseFloat(x2);
                                    float y2f = Float.parseFloat(y2);

                                    if (x2f > world.getMap().width) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Your x coordinate is too large. Max: " + world.getMap().width);
                                        return;
                                    }
                                    if (y2f > world.getMap().height) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">[salmon]TP[white]: y must be: 0 <= y <= " + world.getMap().height);
                                        return;
                                    }
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Moved " + p.name + " from (" + p.x / 8 + " , " + p.y / 8 + ") to (" + x2 + " , " + y2 + ").");
                                    player.set(8 * x2f, 8 * y2f);
                                    player.setNet(8 * x2f, 8 * y2f);
                                    player.set(8 * x2f, 8 * y2f);

                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                    return;
                                }
                            } else if (arg.length > 2) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, you must provide Y coordinate!");
                            } else if (arg.length > 1) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, you must provide x coordinate!");
                            } else {
                                event.getChannel().sendMessage("TP uses #ID - x coordinate - y coordinate : to teleport player.");
                            }
                        }
                        break;
                    case "cr": //Changer player rank
                        if (rank >= 6) {
                            if (arg.length > 2) {
                                //setup
                                String uid = null;
                                boolean proceed = false;
                                //run
                                if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID `" + id + "`.");
                                        return;
                                    }
                                    //run
                                    uid = p.uuid;
                                    proceed = true;
                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                                    //run
                                    uid = arg[1];
                                    proceed = true;
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID not found!");
                                }
                                if (proceed && arg[2].length() == 1 && Strings.canParseInt(arg[2])) {
                                    int rankF = Strings.parseInt(arg[2]);
                                    if (Main.database.containsKey(player.uuid) && Main.database.containsKey(uid)) {
                                        if (Main.database.get(uid).getRank() >= rank) {
                                            event.getChannel().sendMessage("[salmon]CR[white]: You don't have permission to change rank!");
                                        } else if (rank > rankF) {
                                            Main.database.get(uid).setRank(rankF);
                                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> Changed rank of `" + uid + "` to " + rankF + ".");

                                        } else if (rank <= rankF) {
                                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, You don't have permission to change rank to " + rankF +
                                                    "\nYou may only change ranks up to " + (rank - 1) + ".");

                                        }
                                    }
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Rank can only contain numbers!");
                                }
                            } else {
                                event.getChannel().sendMessage("Changes rank for selected player.");
                            }
                        }
                        break;
                    case "st":
                        if (rank >= 5) {
                            if (arg.length > 2) {
                                //setup
                                boolean proceed = false;
                                String uid = null;
                                String tag = null;
                                //run
                                if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID `" + id + "`.");
                                        return;
                                    }
                                    uid = p.uuid;
                                    tag = arg[2];
                                    proceed = true;
                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                                    //run
                                    uid = arg[1];
                                    tag = arg[2];
                                    switch (arg.length - 1) {
                                        case 3:
                                            tag = arg[2] + " " + arg[3];
                                            break;
                                        case 4:
                                            tag = arg[2] + " " + arg[3] + " " + arg[4];
                                            break;
                                    }
                                    proceed = true;
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, UUID not found!");
                                }

                                if (proceed) {
                                    if (Main.database.containsKey(uid)) {
                                        if (!tag.contains("#")) {
                                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Discord tag must contain `#`! example: abc123#4567");
                                            return;
                                        } else if (tag.length() <= 5) {
                                            event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Discord tag must be at least 6 digits! example: abc123#4567");
                                            return;
                                        }
                                        Main.database.get(uid).setDiscordTag(tag);
                                        Main.database.get(uid).setVerified(true);
                                        if (Main.database.get(uid).getRank() == 0) Main.database.get(uid).setRank(1);
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> set Discord tag to `" + tag + "` for `" + uid + "`.");
                                    } else {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Player not found in database.");
                                    }
                                }
                            } else if (arg.length > 1) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, You must provide discord tag!");
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Verifies player and adds discord tag using String. example /a #123 abc1234#1234");
                            }
                        }
                        break;
                    case "ban":
                        if (rank >= 5) {
                            if (arg.length > 2) {
                                String reason = arg[2];
                                switch (arg.length - 1) {
                                    case 3:
                                        reason = arg[2] + " " + arg[3];
                                        break;
                                    case 4:
                                        reason = arg[2] + " " + arg[3] + " " + arg[4];
                                        break;
                                }
                                String string = byteCode.ban(arg[1], reason, event.getMessage().getAuthor().getDisplayName());
                                event.getChannel().sendMessage(string);
                                string = string.replace("[B]Success!\n", "");
                                if (data.has("bl_channel_id") && string.startsWith("[")) {
                                    getTextChannel(data.getString("bl_channel_id")).sendMessage(byteCode.noColors(string));
                                    Main.milisecondSinceBan = Time.millis() + 250;
                                }
                            } else if (arg.length > 1) {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, You must provide a reason.");
                            } else {
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Bans a player, ID/UUID - reason");
                            }
                        }
                        break;
                    case "pjl": //list of players joining and leaving
                        if (rank >= 5) {
                            StringBuilder builder = new StringBuilder();
                            if (Main.pjl.size > 25) {
                                for (int i = Main.pjl.size - 25; i < Main.pjl.size; i++) {
                                    builder.append(byteCode.noColors(Main.pjl.get(i))).append("\n");
                                }
                            } else {
                                for (int i = 0; i < Main.pjl.size; i++) {
                                    builder.append(byteCode.noColors(Main.pjl.get(i))).append("\n");
                                }
                            }
                            event.getChannel().sendMessage(builder.toString());
                        }
                        break;
                    case "kill"://kills player
                        if (rank >= 5) {
                            if (arg.length > 1) {
                                if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                                    int id = Strings.parseInt(arg[1].substring(1));
                                    Player p = playerGroup.getByID(id);
                                    if (p == null) {
                                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Could not find player ID '" + id + "'.");
                                        return;
                                    }
                                    p.dead = true;
                                    Call.onInfoToast(p.con, "Killed.", 1);
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> killed " + p.name + ".");
                                } else if (arg[1].startsWith("#")) {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ID can only contain numbers!");
                                } else {
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Must provide id! Example: #1234");
                                }
                            } else {
                                event.getChannel().sendMessage("Kills player using id. Example: /a kill #1234");
                            }
                        }
                        break;
                    case "chat"://turns chan on/off
                        if (rank >= 6) {
                            if (Main.chat) {
                                Main.chat = false;
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> turned chat off.");
                            } else {
                                Main.chat = true;
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + "> turned chat on.");
                            }
                        }
                        break;
                    case "cat"://clear all tag, clear all tags containing user#tag
                        break;
                    case "test": //test commands;

                        break;

                    case "info": //all commands
                        event.getChannel().sendMessage("\tAvailable Commands:" +
                                "\nuap              - Un Admins Player, UUID" +
                                "\ngameover         - Triggers game over." +
                                "\nsandbox          - Infinite Items." +
                                "\ntk              - Adds 10k of every resource to core." +
                                "\nteam             - Changes team, team" +
                                "\ngpi              - Gets Player Info, #ID/UUID" +
                                "\npardon           - Un-Bans a player, UUID" +
                                "\nrpk              - Resets player kick count, #ID/UUID" +
                                "\nbl               - Shows Ban List." +
                                "\npcc              - Closes a player connection." +
                                "\nunkick           - Un-Kicks a player, UUID." +
                                "\ntp               - Teleports player, x - y" +
                                "\nac               - Admin Chat" +
                                "\ncr               - Changes player rank." +
                                "\nst           - Sets discord tag for player, #id/uuid - Tag#Number" +
                                "\nban              - Bans a player, #ID/UUID - reason" +
                                "\npjl              - List of last 50 player joins and leaves." +
                                "\nkill             - Kills player, #ID" +
                                "\nchat             - Toggles chat on or off." +
                                "\ninfo             - Shows all commands and brief description, uuid");
                        break;
                    default:
                        event.getChannel().sendMessage(arg[0] + " is not a command!");
                        return;
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
