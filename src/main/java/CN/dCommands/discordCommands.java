package CN.dCommands;

//mindustry + arc
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.type.Player;
import mindustry.gen.Call;

//javacord

import mindustry.world.modules.ItemModule;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.nio.channels.Channel;
import java.util.List;
import java.util.Optional;

public class discordCommands implements MessageCreateListener {

    private JSONObject data;


    public discordCommands(JSONObject data){
        this.data = data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (data.has("prefix") && data.has("bot_channel_id") && event.getChannel().getIdAsString().equals(data.getString("bot_channel_id"))){
            if (event.getMessageContent().startsWith("..chat ") || event.getMessageContent().startsWith(data.getString("prefix") + "chat ")) {
                //discord -> server
                String[] msg = event.getMessageContent().split(" ", 2);
                Call.sendMessage("[sky]" + event.getMessageAuthor().getName() + " @discord >[] " + msg[1].trim());
            }

            //playerlist
            else if (event.getMessageContent().equalsIgnoreCase("..players") || event.getMessageContent().startsWith(data.getString("prefix") + "players")) {
                StringBuilder lijst = new StringBuilder();
                lijst.append("players: " + Vars.playerGroup.size() + "\n");
                //lijst.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
                for (Player p : Vars.playerGroup.all()) {
                    lijst.append("* " + p.name.trim() + "\n");
                }
                new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
            }
            //info
            else if (event.getMessageContent().equalsIgnoreCase("..info") || event.getMessageContent().startsWith(data.getString("prefix") + "info")) {
                try {
                    StringBuilder lijst = new StringBuilder();
                    lijst.append("map: " + Vars.world.getMap().name() + "\n" + "author: " + Vars.world.getMap().author() + "\n");
                    lijst.append("wave: " + Vars.state.wave + "\n");
                    lijst.append("enemies: " + Vars.state.enemies + "\n");
                    lijst.append("players: " + Vars.playerGroup.size() + '\n');
                    //lijst.append("admins (online): " + Vars.playerGroup.all().count(p -> p.isAdmin));
                    new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
            //info in core
            else if (event.getMessageContent().equalsIgnoreCase("..infores") || event.getMessageContent().startsWith(data.getString("prefix") + "infores")) {
                //event.getChannel().sendMessage("not implemented yet...");
                if (!Vars.state.rules.waves) {
                    event.getChannel().sendMessage("Only available when playing survivalmode!");
                    return;
                } else if (Vars.playerGroup.isEmpty()) {
                    event.getChannel().sendMessage("No players online!");
                } else {
                    StringBuilder lijst = new StringBuilder();
                    lijst.append("amount of items in the core\n\n");
                    ItemModule core = Vars.playerGroup.all().get(0).getClosestCore().items;
                    lijst.append("copper: " + core.get(Items.copper) + "\n");
                    lijst.append("lead: " + core.get(Items.lead) + "\n");
                    lijst.append("graphite: " + core.get(Items.graphite) + "\n");
                    lijst.append("metaglass: " + core.get(Items.metaglass) + "\n");
                    lijst.append("titanium: " + core.get(Items.titanium) + "\n");
                    lijst.append("thorium: " + core.get(Items.thorium) + "\n");
                    lijst.append("silicon: " + core.get(Items.silicon) + "\n");
                    lijst.append("plastanium: " + core.get(Items.plastanium) + "\n");
                    lijst.append("phase fabric: " + core.get(Items.phasefabric) + "\n");
                    lijst.append("surge alloy: " + core.get(Items.surgealloy) + "\n");

                    new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
                }
            }
        }
    }
}
