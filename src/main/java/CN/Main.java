package CN;

//-----imports-----//

import arc.Events;
import arc.math.Mathf;
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
import static mindustry.Vars.*;

public class Main extends Plugin {
    public Main(){}

    private boolean reaperEnable = true;

    @Override
    public void registerClientCommands(CommandHandler handler) {
        //-----USERS-----//

        //ping the pong
        handler.<Player>register("ping", "Pings the server", (args, player) -> {
            player.sendMessage("Got Ping!");
        });

        //Spawn reaper at high cost
        handler.<Player>register("reaper","[Info]", "Summons a [royal]Reaper [gray]at a high cost. do /reaper arg info", (arg, player) -> {
            if(arg.length == 1){
                if (!player.isAdmin) {
                    if (arg[1].equals("info")) {
                        Call.onInfoMessage("[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]5k \uF837 [#8c7fa9]lead\n[white]4k \uF832 [#8da1e3]titanium[white]\n3.5k \uF831 [#f9a3c7]thorium[white]\n2k \uF82F [#53565c]Silicon[white]\n1.5k \uF82E [#cbd97f]plastanium[white]\n500 \uF82D [#f4ba6e]Phase fabric[white]\n1.25k \uF82C [#f3e979]Surge Alloy");
                    } else {
                        player.sendMessage("[salmon]Summon[white]: You must be [scarlet]<Admin> [white] to use arguments.");
                    }
                    return;
                }

                switch (arg[0]) {
                    case "off":
                        this.reaperEnable = false;
                        player.sendMessage("[salmon]Summon: Reaper set off.");
                        break;
                    case "on":
                        this.reaperEnable = true;
                        player.sendMessage("[salmon]Summon: Reaper set on.");
                        break;
                    case "admin":
                        BaseUnit baseUnit = UnitTypes.reaper.create(Team.sharded);
                        baseUnit.set(5, 5);
                        baseUnit.add();
                        Call.sendMessage("[salmon]Summon[white]: [scarlet]<Admin> [lightgray]" + player.name + "[white] has summoned a [royal]Reaper [white]at no cost.");
                        break;
                    case "info":
                        player.sendMessage("[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]5k \uF837 [#8c7fa9]lead\n[white]4k \uF832 [#8da1e3]titanium[white]\n3.5k \uF831 [#f9a3c7]thorium[white]\n2k \uF82F [#53565c]Silicon[white]\n1.5k \uF82E [#cbd97f]plastanium[white]\n500 \uF82D [#f4ba6e]Phase fabric[white]\n1.25k \uF82C [#f3e979]Surge Alloy");
                        break;
                    default:
                        player.sendMessage("[scarlet]<Admin> [white]Use args info, on or off.");
                        break;
                }
            } else if (this.reaperEnable) {
                //"[salmon]Summon[white]: [scarlet]///-///WARNING///-///\n/[white]Summon exceeded 15[scarlet]-/\n///-//////-//////-///"
                Teams.TeamData teamData = state.teams.get(Team.sharded);
                CoreBlock.CoreEntity core = teamData.cores.first();
                if (core.items.has(Items.copper, 5000) && core.items.has(Items.lead, 5000) && core.items.has(Items.titanium, 4000) && core.items.has(Items.thorium, 3500) && core.items.has(Items.silicon, 2000) && core.items.has(Items.plastanium, 1500) && core.items.has(Items.phasefabric, 500) && core.items.has(Items.surgealloy, 1250)) {
                    BaseUnit baseUnit = UnitTypes.reaper.create(Team.sharded);
                    baseUnit.set(25, 25);
                    baseUnit.add();
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
                    Call.sendMessage("[white]" + player.name + "[white] has summoned a [royal]Reaper[white].");
                } else {
                    Call.sendMessage("[salmon]Summon[white]: " + player.name + "[lightgray] tried[white] to summon a [royal]Reaper[white].");
                    player.sendMessage("[salmon]Summon[white]: Not enough resources to spawn [royal]Reaper[white].");
                    player.sendMessage("[salmon]Summon[white]: Do [lightgray]`/reaper info` [white]to see required resources.");
                }
            } else {
                player.sendMessage("[salmon]Summon[lightgray]: Reaper is off");
            }
        });

        //-----ADMINS-----//

        //un-admins players
        handler.<Player>register("uap", "<code...>","[scarlet]<Admin> [lightgray]- Code", (arg, player) -> {
            if(!player.isAdmin){
                player.sendMessage("[white]You must be [scarlet]<Admin> [white]to use this command.");
                return;
            }

            netServer.admins.unAdminPlayer(arg[0]);
            player.sendMessage("unAdmin: " + arg[0]);
        });

        //Triggers game over if admin
        handler.<Player>register("agameover", "[scarlet]<Admin> [lightgray]- Game over.", (arg, player) -> {
            if(!player.isAdmin) {
                player.sendMessage("[white]You must be [scarlet]<Admin> [white]to use this command.");
                return;
            }
            Events.fire(new EventType.GameOverEvent(Team.crux));
            Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + "[white] has ended the game.");
        });

        //1 mil resources
        handler.<Player>register("ainf", "[scarlet]<Admin> [lightgray]- " + "1 million resouces", (args, player) -> {
            if(player.isAdmin) {
                Teams.TeamData teamData = state.teams.get(Team.sharded);
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
            } else {
                player.sendMessage("[white]You must be [scarlet]<Admin> [white]to use this command.");
            }
        });

        //change team
        handler.<Player>register("ateam","<team...>", "[scarlet]<Admin> [lightgray]- Changes team", (arg, player) -> {
            if (!player.isAdmin){
                player.sendMessage("[white]You must be [scarlet]<Admin> [white]to use this command.");
                return;
            }

            Team setTeam;
            switch (arg[0]) {
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
                    player.sendMessage("[salmon]CT[lightgray]: Available teams: Sharded, [royal]Blue[lightgray], [scarlet]Crux[lightgray], Derelict[lightgray], [forest]Green[lightgray], [purple]Purple[lightgray].");
                    return;
            }
            player.setTeam(setTeam);
            player.sendMessage("[salmon]CT[white]: Changed team to " + arg[0]);
        });
    }
}

