package CN;

import CN.dCommands.discordCommands;
import CN.dCommands.discordServerCommands;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.type.Player;
import mindustry.entities.type.Unit;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.world.blocks.storage.CoreBlock;
import org.javacord.api.DiscordApi;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
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
    }

    public void run(){
        int x = 0;
        int players = 0;
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
        //empty embed

        //
        while (this.mt.isAlive()){
            TextChannel tc = getTextChannel(data.getString("one_info_channel"));
            try{
                Thread.sleep(15 * 1000);
            } catch (Exception e) {
            }
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
                    lijst.append(byteCode.noColors(p.name.trim()) + "\n");
                }
            }

            //update core resources and team info
            Teams.TeamData teamData = state.teams.get(Team.sharded);
            if (!teamData.hasCore()) {
                continue;
            }
            CoreBlock.CoreEntity core = teamData.cores.first();
            String playerTeam = "Sharded";
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
             ghoul = 0;;
             revenant = 0;
             lich = 0;
             reaper = 0;
             All = 0;
            //
            for (Unit u : unitGroup.all()) {
                if(u.getTeam() == Team.sharded) {
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

            myteam =        "\n\nCore Resources:" +
                            "\n" + core.items.get(Items.copper) +        " copper" +
                            "\n" + core.items.get(Items.lead) +          " lead" +
                            "\n" + core.items.get(Items.metaglass) +     " metaglass" +
                            "\n" + core.items.get(Items.graphite) +      " graphite" +
                            "\n" + core.items.get(Items.titanium) +      " titanium" +
                            "\n" + core.items.get(Items.thorium) +       " thorium" +
                            "\n" + core.items.get(Items.silicon) +       " Silicon" +
                            "\n" + core.items.get(Items.plastanium) +    " plastanium" +
                            "\n" + core.items.get(Items.phasefabric) +   " phase fabric" +
                            "\n" + core.items.get(Items.surgealloy) +    " surge alloy" +
                            "\n\nTeam Units: " +
                            "\n"+ draug +" Draug Miner Drone" +
                            "\n"+ spirit +" Spirit Repair Drone" +
                            "\n"+ phantom +" Phantom Builder Drone" +
                            "\n"+ dagger +" Dagger" +
                            "\n"+ crawler +" Crawlers" +
                            "\n"+ titan +" Titan" +
                            "\n"+ fortress +" Fortress" +
                            "\n"+ eruptor +" Eruptor" +
                            "\n"+ chaosArray +" Chaos Array" +
                            "\n"+ eradicator +" Eradicator" +
                            "\n"+ wraith +" Wraith Fighter" +
                            "\n"+ ghoul +" Ghoul Bomber" +
                            "\n"+ revenant +" Revenant" +
                            "\n"+ lich +" Lich" +
                            "\n"+ reaper +" Reaper" +
                            "\n"+ All +" Total" +
                            "\n";
            //update info
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.getHSBColor(21,84,50));
            embed.setTitle("Server Info:");
            embed.setUrl("http://cn-discord.ddns.net");
            embed.addField("Map", byteCode.noColors(world.getMap().name()) + " by " + byteCode.noColors(world.getMap().author()));
            embed.addField("Wave: ", ""+state.wave);
            embed.addField("Players Online:",lijst.toString());
            embed.addField("Team Info:",myteam);
            embed.setTimestampToNow();

            try {
                tc.getMessageById(data.getString("info_message_id")).get().edit(embed);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            //save and add a minute
            if (x == 4) {
                x = 0;
                //output PI save file
                try {
                    FileOutputStream fileOut = new FileOutputStream("PDF.cn");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(Main.database);
                    out.flush();
                    out.close();
                    fileOut.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }

                //add 1 minute of play time for each player
                for (Player p : playerGroup.all()) {
                    if (Main.database.containsKey(p.uuid)) {
                        Main.database.get(p.uuid).addTp(1);
                        Call.onInfoToast(p.con,"+1 minutes played.", 3);
                        //auto congratulations
                        int y = Main.database.get(p.uuid).getTP() / 60;
                        float z = (float) Main.database.get(p.uuid).getTP()/60;
                        if ((float) y == z) {
                            Call.sendMessage("Congratulations to " + p.name + " [white]for staying active for " + y + " Hours!");
                        }
                        byteCode.aRank(p.uuid);
                    }
                }
            } else {
                x++;
            }
        }
        if (data.has("serverdown_role_id")){
            Role r = new utilmethods().getRole(api, data.getString("serverdown_role_id"));
            TextChannel tc = new utilmethods().getTextChannel(api, data.getString("serverdown_channel_id"));
            if (r == null || tc ==  null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception _) {}
            } else {
                if (data.has("serverdown_name")){
                    String serverNaam = data.getString("serverdown_name");
                    new MessageBuilder()
                            .append(String.format("%s\nServer %s is down",r.getMentionTag(),((serverNaam != "") ? ("**"+serverNaam+"**") : "")))
                            .send(tc);
                } else {
                    new MessageBuilder()
                            .append(String.format("%s\nServer is down.", r.getMentionTag()))
                            .send(tc);
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
