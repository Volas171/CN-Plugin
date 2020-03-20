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
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.io.*;
import java.lang.Thread;
import java.util.Optional;

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
        api.addMessageCreateListener(new discordCommands());
        api.addMessageCreateListener(new discordServerCommands(data));
    }

    public void run(){
        int x = 0;
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
        //
        while (this.mt.isAlive()){
            TextChannel tc = getTextChannel(data.getString("one_info_channel"));
            try{
                Thread.sleep(15 * 1000);
            } catch (Exception e) {
            }
            //update players
            StringBuilder lijst = new StringBuilder();
            //lijst.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
            for (Player p :Vars.playerGroup.all()){
                lijst.append(byteCode.noColors(p.name.trim()) + "\n");
            }
            try {
                File file = new File("players.txt");
                FileWriter out = new FileWriter(file);
                PrintWriter pw = new PrintWriter(out);
                pw.println(lijst.toString());
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
            //update core resources and team info
            Teams.TeamData teamData = state.teams.get(Team.sharded);
            CoreBlock.CoreEntity core = teamData.cores.first();
            if (core == null) {
                return;
            }
            String playerTeam = "Sharded";
            //
            for (Unit u : unitGroup.all()) {
                if(u.getTeam() == player.getTeam()) {
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

            myteam = "Your team is " + playerTeam +
                            "\n\n[accent]Core Resources[white]:" +
                            "\n[white]" + core.items.get(Items.copper) +        " \uF838 [#d99d73]copper" +
                            "\n[white]" + core.items.get(Items.lead) +          " \uF837 [#8c7fa9]lead" +
                            "\n[white]" + core.items.get(Items.metaglass) +     " \uF836 [#ebeef5]metaglass" +
                            "\n[white]" + core.items.get(Items.graphite) +      " \uF835 [#b2c6d2]graphite" +
                            "\n[white]" + core.items.get(Items.titanium) +      " \uF832 [#8da1e3]titanium" +
                            "\n[white]" + core.items.get(Items.thorium) +       " \uF831 [#f9a3c7]thorium" +
                            "\n[white]" + core.items.get(Items.silicon) +       " \uF82F [#53565c]Silicon" +
                            "\n[white]" + core.items.get(Items.plastanium) +    " \uF82E [#cbd97f]plastanium" +
                            "\n[white]" + core.items.get(Items.phasefabric) +   " \uF82D [#f4ba6e]phase fabric" +
                            "\n[white]" + core.items.get(Items.surgealloy) +    " \uF82C [#f3e979]surge alloy" +
                            "\n\n[accent]Team Units: [white]" +
                            "\nDraug Miner Drone: " + draug +
                            "\nSpirit Repair Drone: " + spirit +
                            "\nPhantom Builder Drone: " + phantom +
                            "\nDagger: " + dagger +
                            "\nCrawlers: " + crawler +
                            "\nTitan: " + titan +
                            "\nFortress: " + fortress +
                            "\nEruptor: " + eruptor +
                            "\nChaos Array: " + chaosArray +
                            "\nEradicator: " + eradicator +
                            "\nWraith Fighter: " + wraith +
                            "\nGhoul Bomber: " + ghoul +
                            "\nRevenant: " + revenant +
                            "\nLich: " + lich +
                            "\nReaper: " + reaper +
                            "\nTotal: " + All +
                            "\n";
            try {
                File file = new File("team.txt");
                FileWriter out = new FileWriter(file);
                PrintWriter pw = new PrintWriter(out);
                pw.println(myteam);
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
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
}
