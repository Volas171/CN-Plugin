package CN;

//-----imports-----//
import arc.Events;
import arc.util.CommandHandler;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.plugin.Plugin;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.*;

import static mindustry.Vars.*;


public class Main extends Plugin {

    private boolean summonEnable = true;
    private boolean reaperEnable = true;
    private boolean lichEnable = true;
    private String mba = "[white]You must be [scarlet]<Admin> [white]to use this command.";

    @Override
    public void registerClientCommands(CommandHandler handler) {
        //-----USERS-----//

        //ping the pong
        handler.<Player>register("ping", "Pings the server", (args, player) -> {
            player.sendMessage("Got Ping!");
        });

        //Summons Entities
        handler.<Player>register("summon","[unit] [Info]", "Summons a [royal]Reaper [gray]at a high cost. do /reaper info", (arg, player) -> {
            String unit = "none";
            //decider section
            if (arg.length != 0) {
                switch (arg[0]) {
                    case "reaper":
                        if (arg.length == 2) {
                            if (player.isAdmin) {
                                switch (arg[1]) {
                                    case "on":
                                        reaperEnable = true;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Reaper [white]turned [lightgray]on[white].");
                                        break;
                                    case "off":
                                        reaperEnable = false;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Reaper [white]turned [lightgray]off[white].");
                                        break;
                                    case "info":
                                        Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]5k \uF837 [#8c7fa9]lead\n[white]4k \uF832 [#8da1e3]titanium[white]\n3.5k \uF831 [#f9a3c7]thorium[white]\n2k \uF82F [#53565c]Silicon[white]\n1.5k \uF82E [#cbd97f]plastanium[white]\n500 \uF82D [#f4ba6e]Phase fabric[white]\n1.25k \uF82C [#f3e979]Surge Alloy");
                                        break;
                                    default:
                                        player.sendMessage("[salmon]Summon[white]: Reaper args contains [lightgray]on[white]/[lightgray]off[white].");
                                        break;
                                }
                            } else if (arg[1].equals("info")){
                                Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]5k \uF837 [#8c7fa9]lead\n[white]4k \uF832 [#8da1e3]titanium[white]\n3.5k \uF831 [#f9a3c7]thorium[white]\n2k \uF82F [#53565c]Silicon[white]\n1.5k \uF82E [#cbd97f]plastanium[white]\n500 \uF82D [#f4ba6e]Phase fabric[white]\n1.25k \uF82C [#f3e979]Surge Alloy");
                                return;
                            } else {
                                player.sendMessage(mba);
                            }
                        } else {
                            unit = "UnitTypes.reaper";
                        }
                        break;
                    case "lich":
                        if (arg.length == 2) {
                            if (player.isAdmin) {
                                switch (arg[1]) {
                                    case "on":
                                        lichEnable = true;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Lich [white]turned [lightgray]on[white].");
                                        break;
                                    case "off":
                                        lichEnable = false;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Lich [white]turned [lightgray]off[white].");
                                        break;
                                    case "info":
                                        Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n3.5k \uF838 [#d99d73]copper\n[white]3.5k \uF837 [#8c7fa9]lead\n[white]2k \uF836 [#ebeef5]metaglass[white]\n[white]1.3k \uF835 [#b2c6d2]graphite[white]\n[white]1.3k \uF832 [#8da1e3]titanium[white]\n1.5k \uF831 [#f9a3c7]thorium[white]\n1.3k \uF82F [#53565c]Silicon[white]\n500 \uF82E [#cbd97f]plastanium[white]\n500 \uF82C [#f3e979]Surge Alloy");
                                        break;
                                    default:
                                        player.sendMessage("[salmon]Summon[white]: Lich args contains [lightgray]on[white]/[lightgray]off[white].");
                                        break;
                                }
                            } else if (arg[1].equals("info")){
                                Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n3.5k \uF838 [#d99d73]copper\n[white]3.5k \uF837 [#8c7fa9]lead\n[white]2k \uF836 [#ebeef5]metaglass[white]\n[white]1.3k \uF835 [#b2c6d2]graphite[white]\n[white]1.3k \uF832 [#8da1e3]titanium[white]\n1.5k \uF831 [#f9a3c7]thorium[white]\n1.3k \uF82F [#53565c]Silicon[white]\n500 \uF82E [#cbd97f]plastanium[white]\n500 \uF82C [#f3e979]Surge Alloy");
                                return;
                            } else {
                                player.sendMessage(mba);
                            }
                        } else {
                            unit = "UnitTypes.lich";
                        }
                        break;
                    case "on":
                        if (player.isAdmin) {
                            summonEnable = true;
                            player.sendMessage("[salmon]Summon[white]: [lightgray]Summon [white]turned [lightgray]on[white].");
                        } else {
                            player.sendMessage(mba);
                        }
                        return;
                    case "off":
                        if (player.isAdmin) {
                            summonEnable = false;
                            player.sendMessage("[salmon]Summon[white]: [lightgray]Summon [white]turned [lightgray]off[white].");
                        } else {
                            player.sendMessage(mba);
                        }
                        return;
                    default:
                        if (player.isAdmin) {
                            player.sendMessage("Summon: arg[0] = reaper, lich, on or off.");
                        } else {
                            player.sendMessage("Summon: arg[0] = reaper or lich.");
                        }
                        break;
                }
            } else {
                player.sendMessage("[salmon]Summon[white]: Summons a [royal]Reaper [white]or [royal]Lich.");
                return;
            }
            //Summon section
            if (!unit.equals("none") && summonEnable) {
                Teams.TeamData teamData = state.teams.get(player.getTeam());
                CoreBlock.CoreEntity core = teamData.cores.first();
                switch (unit) {
                    case "UnitTypes.reaper":
                        if (reaperEnable) {
                            if (core.items.has(Items.copper, 5000) && core.items.has(Items.lead, 5000) && core.items.has(Items.titanium, 4000) && core.items.has(Items.thorium, 3500) && core.items.has(Items.silicon, 2000) && core.items.has(Items.plastanium, 1500) && core.items.has(Items.phasefabric, 500) && core.items.has(Items.surgealloy, 1250)) {
                                //
                                core.items.remove(Items.copper, 5000);
                                core.items.remove(Items.lead, 5000);
                                core.items.remove(Items.titanium, 4000);
                                core.items.remove(Items.thorium, 3500);
                                core.items.remove(Items.silicon, 2000);
                                core.items.remove(Items.plastanium, 1500);
                                core.items.remove(Items.phasefabric, 500);
                                core.items.remove(Items.surgealloy, 1250);
                                //
                                BaseUnit baseUnit = UnitTypes.reaper.create(player.getTeam());
                                baseUnit.set(player.x, player.y);
                                baseUnit.add();
                                ;
                                Call.sendMessage("[white]" + player.name + "[white] has summoned a [royal]Reaper[white].");
                            } else {
                                Call.sendMessage("[salmon]Summon[white]: " + player.name + "[lightgray] tried[white] to summon a [royal]Reaper[white].");
                                player.sendMessage("[salmon]Summon[white]: Not enough resources to spawn [royal]Reaper[white]. Do [lightgray]`/summon reaper info` [white]to see required resources.");
                            }
                        } else {
                            player.sendMessage("[salmon]Summon[white]: [royal]Reaper [white]is disabled.");
                        }
                        break;
                    case "UnitTypes.lich":
                        if (lichEnable) {
                            if (core.items.has(Items.copper, 3500) && core.items.has(Items.lead, 3500) && core.items.has(Items.metaglass, 2000) && core.items.has(Items.graphite, 1250) && core.items.has(Items.titanium, 2500) && core.items.has(Items.thorium, 1750) && core.items.has(Items.silicon, 1250) && core.items.has(Items.plastanium, 500) && core.items.has(Items.surgealloy, 350)) {
                                //
                                core.items.remove(Items.copper, 3500);
                                core.items.remove(Items.lead, 3500);
                                core.items.remove(Items.metaglass, 2000);
                                core.items.remove(Items.graphite, 1250);
                                core.items.remove(Items.titanium, 1250);
                                core.items.remove(Items.thorium, 1500);
                                core.items.remove(Items.silicon, 1250);
                                core.items.remove(Items.plastanium, 500);
                                core.items.remove(Items.surgealloy, 350);
                                //
                                BaseUnit baseUnit = UnitTypes.lich.create(player.getTeam());
                                baseUnit.set(player.x, player.y);
                                baseUnit.add();
                                ;
                                Call.sendMessage("[white]" + player.name + "[white] has summoned a [royal]Lich[white].");
                            } else {
                                Call.sendMessage("[salmon]Summon[white]: " + player.name + "[lightgray] tried[white] to summon a [royal]Lich[white].");
                                player.sendMessage("[salmon]Summon[white]: Not enough resources to spawn [royal]Lich[white]. Do [lightgray]`/summon lich info` [white]to see required resources.");
                            }
                        } else {
                            player.sendMessage("[salmon]Summon[white]: [royal]Lich [white]is disabled.");
                        }
                        break;
                    default:
                        player.sendMessage("ERROR");
                        break;
                }
            } else if (arg.length == 1){
                player.sendMessage("[salmon]Summon[white]: [salmon]Summon[white] is disabled.");
            }
        });

        handler.<Player>register("myteam","[Info]", "Gives team info", (arg, player) -> {
            String x = player.getTeam().name;
            player.sendMessage("Your team is " + x);
        });

        //-----ADMINS-----//

        handler.<Player>register("a","<Command> [1]", "[scarlet]<Admin> [lightgray]- Admin commands", (arg, player) -> {
            if(!player.isAdmin){
                player.sendMessage(mba);
                return;
            }
            switch (arg[0]) {
                //un admin player - un-admins uuid, even if player is offline.
                case "uap":
                    netServer.admins.unAdminPlayer(arg[1]);
                    player.sendMessage("unAdmin: " + arg[1]);
                    break;
                //gameover - triggers gameover for admins team.
                case "gameover":
                    Events.fire(new EventType.GameOverEvent(player.getTeam()));
                    Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + "[white] has ended the game.");
                    break;
                case "inf":
                    Teams.TeamData teamData = state.teams.get(player.getTeam());
                    CoreBlock.CoreEntity core = teamData.cores.first();
                    core.items.add(Items.copper, 1000000);
                    core.items.add(Items.lead, 1000000);
                    core.items.add(Items.metaglass, 1000000);
                    core.items.add(Items.graphite, 1000000);
                    core.items.add(Items.titanium, 1000000);
                    core.items.add(Items.thorium, 1000000);
                    core.items.add(Items.silicon, 1000000);
                    core.items.add(Items.plastanium, 1000000);
                    core.items.add(Items.phasefabric, 1000000);
                    core.items.add(Items.surgealloy, 1000000);
                    Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has given 1mil resources to core.");
                    break;
                //team - changes team.
                case "team":
                    Team setTeam;
                    switch (arg[1]) {
                        case "sharded":
                            setTeam = Team.sharded;
                            break;
                        case "blue":
                            setTeam = Team.blue;
                            break;
                        case "crux":
                            setTeam = Team.crux;
                            break;
                        case "derelict":
                            setTeam = Team.derelict;
                            break;
                        case "green":
                            setTeam = Team.green;
                            break;
                        case "purple":
                            setTeam = Team.purple;
                            break;
                        default:
                            player.sendMessage("[salmon]CT[lightgray]: Available teams: [accent]Sharded, [royal]Blue[lightgray], [scarlet]Crux[lightgray], [lightgray]Derelict[lightgray], [forest]Green[lightgray], [purple]Purple[lightgray].");
                            return;
                    }
                    player.setTeam(setTeam);
                    player.sendMessage("[salmon]CT[white]: Changed team to " + arg[1]);
                    break;
                //test commands.
                case "test":
                    break;
                //if none of the above commands used.
                default:
                    player.sendMessage("\tAvailable Commands:\nuap\ngameover\ninf\nteam");
            }
        });
    }
}

