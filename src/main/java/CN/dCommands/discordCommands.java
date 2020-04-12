package CN.dCommands;

import CN.Main;
import CN.byteCode;
//mindustry + arc
import CN.key;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.gen.Call;

//javacord

import mindustry.world.modules.ItemModule;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.nio.channels.Channel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class discordCommands implements MessageCreateListener {

    private JSONObject data;


    public discordCommands(JSONObject data){
        this.data = data;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (data.has("prefix") && data.has("bot-channel-id") && event.getChannel().getIdAsString().equals(data.getString("bot-channel-id"))){
            String[] arg = event.getMessageContent().split(" ", 4);
            //playerlist
            if (event.getMessageContent().equalsIgnoreCase("//players") || event.getMessageContent().startsWith(data.getString("prefix") + "players")) {
                StringBuilder lijst = new StringBuilder();
                lijst.append("players: " + Vars.playerGroup.size() + "\n");
                //lijst.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
                for (Player p : Vars.playerGroup.all()) {
                    lijst.append("* " + p.name.trim() + "\n");
                }
                new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
            }
            //info
            else if (event.getMessageContent().equalsIgnoreCase("//info") || event.getMessageContent().startsWith(data.getString("prefix") + "info")) {
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
            //resoirces in core
            else if (event.getMessageContent().equalsIgnoreCase("//infores") || event.getMessageContent().startsWith(data.getString("prefix") + "infores")) {
                //event.getChannel().sendMessage("not implemented yet...");
                if (!Vars.state.rules.waves) {
                    event.getChannel().sendMessage("Only available when playing survival mode!");
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
            //get verified
            else if (event.getMessageContent().startsWith(data.getString("prefix") + "verify")) {
                if (Main.adata.has("discord-accounts")) {
                    JSONObject da = Main.adata.getJSONObject("discord-accounts");
                    if (da.has(event.getMessage().getAuthor().getIdAsString())) {
                        event.getChannel().sendMessage("Discord Account already in use!");
                        return;
                    } else {
                        if (arg.length > 1) {
                            JSONObject login = Main.adata.getJSONObject("login-info");
                            if (login.has(arg[1])) {
                                JSONObject user = login.getJSONObject(arg[1]);
                                JSONObject data = Main.adata.getJSONObject(user.getString("dataID"));
                                if (data.has("verified") && data.getInt("verified") == 1) {
                                    event.getChannel().sendMessage("Username not found in database or already verified");
                                    return;
                                }
                                String hash = byteCode.hash(8);
                                Main.keyList.put(hash, new key(arg[1],"verify", event.getMessage().getAuthor().getDiscriminatedName()));
                                da.put(event.getMessage().getAuthor().getIdAsString(), arg[1]);
                                String userID = event.getMessage().getAuthor().getIdAsString();
                                event.getChannel().sendMessage("By getting verified, you agree to the following:" +
                                        "\n```1) having your discord tag saved on server" +
                                        "\n2) having your discord tag sharable in-game" +
                                        "\n3) having your <verified> revoked at any given time ```" +
                                        "\n\ndo ||/key " + hash + "|| to get verified! You have 30s!");
                                new Object() {
                                    private Timer.Task task;

                                    {
                                        task = Timer.schedule(() -> {
                                            if (Main.keyList.containsKey(hash)) {
                                                Main.keyList.remove(hash);
                                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Key Expired");
                                                da.remove(userID);
                                                task.cancel();
                                            } else {
                                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Successfully verified your account!");
                                                task.cancel();
                                            }
                                        }, 30, 1);
                                    }
                                };
                            } else {
                                event.getChannel().sendMessage("Username not found in database or already verified");
                            }
                        } else {
                            event.getChannel().sendMessage("Provide the username of the account that's being activated.");
                            return;
                        }
                    }
                } else {
                    Log.err("============");
                    Log.err("ERROR - 404");
                    Log.err("settings.cn does not contain `discord-accounts`");
                    Log.err("============");
                }


                /*AtomicInteger found = new AtomicInteger();
                Main.database.forEach((k, p) -> {
                    if (p.getDiscordTag().contentEquals(event.getMessage().getAuthor().getDiscriminatedName())) {
                        found.set(found.get() + 1);
                    }
                });
                if (found.get() > 2) {
                    event.getChannel().sendMessage("You too many accounts using this tag! Removing tag from all other accounts...\nUse command again to get verified.");
                    Main.database.forEach((k, p) -> {
                        if (p.getDiscordTag().contentEquals(event.getMessage().getAuthor().getDiscriminatedName())) {
                            p.setVerified(false);
                            p.setDiscordTag("N/A");
                        }
                    });
                } else {
                    String hash = byteCode.hash(6);
                    Main.keyList.put(hash, new key("verify", event.getMessage().getAuthor().getDiscriminatedName()));
                    event.getChannel().sendMessage("By getting verified, you agree to the following:" +
                            "\n```1) having your discord tag saved on server" +
                            "\n2) having your discord tag sharable in-game" +
                            "\n3) having your <verified> revoked at any given time ```" +
                            "\n\ndo ||/key " + hash + "|| to get verified! You have 30s!");

                    new Object() {
                        private Timer.Task task;
                        {
                            task = Timer.schedule(() -> {
                                if (Main.keyList.containsKey(hash)) {
                                    Main.keyList.remove(hash);
                                    event.getChannel().sendMessage("<@"+event.getMessage().getAuthor().getIdAsString()+ ">, Key Expired");
                                    task.cancel();
                                } else {
                                    event.getChannel().sendMessage("<@"+event.getMessage().getAuthor().getIdAsString()+ ">, Success!");
                                    task.cancel();
                                }
                            }, 30, 1);
                        }
                    };
                }
                */
            }
        } else if (!data.has("prefix")) {
            arc.util.Log.err("============");
            arc.util.Log.err("CRITICAL ERROR - 404");
            arc.util.Log.err("settings.cn does not contain `prefix`");
            arc.util.Log.info("use gnsettings to generate settings canvas.");
            Log.err("============");
        }
    }
}
