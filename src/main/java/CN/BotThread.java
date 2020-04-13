package CN;

import CN.dCommands.discordCommands;
import CN.dCommands.discordServerCommands;
import CN.dCommands.discordia;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.type.Player;
import mindustry.entities.type.Unit;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.world.blocks.storage.CoreBlock;
import org.javacord.api.DiscordApi;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.awt.*;
import java.lang.Thread;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static mindustry.Vars.*;

public class BotThread extends Thread{
    public DiscordApi api;
    private Thread mt;
    private JSONObject data;

    public BotThread(DiscordApi _api, Thread _mt, JSONObject _data) {
        api = _api; //new DiscordApiBuilder().setToken(data.get(0)).login().join();
        mt = _mt;
        data = _data;

        //communication commands
        api.addMessageCreateListener(new discordCommands(data));
        api.addMessageCreateListener(new discordServerCommands(data));
        api.addMessageCreateListener(new discordia(data, api));
    }

    public void run(){
        try {
            Thread.sleep(60 * 1000);
        } catch (Exception e) {
        }

        //
        int draug = 0;
        int spirit = 0;
        int phantom = 0;
        int dagger = 0;
        int crawler = 0;
        int titan = 0;
        int fortress = 0;
        int eruptor = 0;
        int chaosArray = 0;
        int eradicator = 0;
        int wraith = 0;
        int ghoul = 0;;
        int revenant = 0;
        int lich = 0;
        int reaper = 0;

        int All = 0;
        String myteam = "";

        while (this.mt.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            //run stuff
            if (data.has("info_channel_id") && data.has("info_message_id")) {
                TextChannel tc = getTextChannel(data.getString("info_channel_id"));
                //update status
                if (playerGroup.isEmpty()) {
                    setStatus(0);
                } else {
                    setStatus(playerGroup.size());
                }
                //update players
                StringBuilder lijst = new StringBuilder();
                //lijst.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
                if (playerGroup.isEmpty()) {
                    lijst.append("No Players Online\n");
                } else {
                    for (Player p : Vars.playerGroup.all()) {
                        if (Main.currentLogin.containsKey(p.uuid)) lijst.append(byteCode.noColors(p.name.trim()) + "\n");
                    }
                }

                //update core resources and team info
                Teams.TeamData teamData = state.teams.get(Team.sharded);
                if (teamData.hasCore()) {
                    CoreBlock.CoreEntity core = teamData.cores.first();
                    //
                    draug = 0;
                    spirit = 0;
                    phantom = 0;
                    dagger = 0;
                    crawler = 0;
                    titan = 0;
                    fortress = 0;
                    eruptor = 0;
                    chaosArray = 0;
                    eradicator = 0;
                    wraith = 0;
                    ghoul = 0;
                    revenant = 0;
                    lich = 0;
                    reaper = 0;
                    All = 0;
                    //
                    for (Unit u : unitGroup.all()) {
                        if (u.getTeam() == Team.sharded) {
                            if (u.getTypeID().name.equals("draug")) draug = draug + 1;
                            if (u.getTypeID().name.equals("spirit")) spirit = spirit + 1;
                            if (u.getTypeID().name.equals("phantom")) phantom = phantom + 1;
                            if (u.getTypeID().name.equals("dagger")) dagger = dagger + 1;
                            if (u.getTypeID().name.equals("crawler")) crawler = crawler + 1;
                            if (u.getTypeID().name.equals("titan")) titan = titan + 1;
                            if (u.getTypeID().name.equals("fortress")) fortress = fortress + 1;
                            if (u.getTypeID().name.equals("eruptor")) eruptor = eruptor + 1;
                            if (u.getTypeID().name.equals("chaos-array")) chaosArray = chaosArray + 1;
                            if (u.getTypeID().name.equals("eradicator")) eradicator = eradicator + 1;
                            if (u.getTypeID().name.equals("wraith")) wraith = wraith + 1;
                            if (u.getTypeID().name.equals("ghoul")) ghoul = ghoul + 1;
                            if (u.getTypeID().name.equals("revenant")) revenant = revenant + 1;
                            if (u.getTypeID().name.equals("lich")) lich = lich + 1;
                            if (u.getTypeID().name.equals("reaper")) reaper = reaper + 1;
                            All = All + 1;
                        }
                    }

                    myteam = "\n\nCore Resources:" +
                            "\n" + core.items.get(Items.copper) + " copper" +
                            "\n" + core.items.get(Items.lead) + " lead" +
                            "\n" + core.items.get(Items.metaglass) + " metaglass" +
                            "\n" + core.items.get(Items.graphite) + " graphite" +
                            "\n" + core.items.get(Items.titanium) + " titanium" +
                            "\n" + core.items.get(Items.thorium) + " thorium" +
                            "\n" + core.items.get(Items.silicon) + " Silicon" +
                            "\n" + core.items.get(Items.plastanium) + " plastanium" +
                            "\n" + core.items.get(Items.phasefabric) + " phase fabric" +
                            "\n" + core.items.get(Items.surgealloy) + " surge alloy" +
                            "\n\nTeam Units: " +
                            "\n" + draug + " Draug Miner Drone" +
                            "\n" + spirit + " Spirit Repair Drone" +
                            "\n" + phantom + " Phantom Builder Drone" +
                            "\n" + dagger + " Dagger" +
                            "\n" + crawler + " Crawlers" +
                            "\n" + titan + " Titan" +
                            "\n" + fortress + " Fortress" +
                            "\n" + eruptor + " Eruptor" +
                            "\n" + chaosArray + " Chaos Array" +
                            "\n" + eradicator + " Eradicator" +
                            "\n" + wraith + " Wraith Fighter" +
                            "\n" + ghoul + " Ghoul Bomber" +
                            "\n" + revenant + " Revenant" +
                            "\n" + lich + " Lich" +
                            "\n" + reaper + " Reaper" +
                            "\n" + All + " Total" +
                            "\n";
                }
                //update info
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.getHSBColor(21, 84, 50));
                embed.setTitle("Server Info:");
                embed.setUrl("http://cn-discord.ddns.net");
                embed.addField("Map", byteCode.noColors(world.getMap().name()) + " by " + byteCode.noColors(world.getMap().author()));
                embed.addField("Wave: ", "" + state.wave);
                embed.addField("Players Online:", lijst.toString());
                embed.addField("Team Info:", myteam);
                embed.setTimestampToNow();

                try {
                    tc.getMessageById(data.getString("info_message_id")).get().edit(embed);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        api.disconnect();
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

    public Role getRole(String id) {
        Optional<Role> r1 = this.api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: adminrole not found!");
            return null;
        }
        return r1.get();
    }

    public void setStatus(int players) {
        if (players == 0) {
            this.api.updateActivity("with nobody ;-;");
        } else if (players == 1) {
            this.api.updateActivity("with 1 player.");
        } else {
            this.api.updateActivity("with " + players + " players.");
        }
    }

}
