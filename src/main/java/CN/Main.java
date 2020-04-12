package CN;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.traits.Entity;
import mindustry.entities.type.Player;
import mindustry.entities.type.Unit;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.plugin.Plugin;
import mindustry.world.blocks.storage.CoreBlock;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.*;
import static mindustry.Vars.player;

public class Main extends Plugin {
    //variables
    private JSONObject settings;

    public static JSONObject adata;
    public static HashMap<String, String> currentLogin = new HashMap<>();
    public static HashMap<String, key> keyList = new HashMap<>();
    public HashMap<String, String> pastLogin = new HashMap<>();
    public HashMap<String, Integer> loginAttempts = new HashMap<>();
    public int halpX;
    public int halpY;
    //discord shat
    private final Long CDT = 300L;
    private DiscordApi api = null;
    private HashMap<Long, String> cooldowns = new HashMap<Long, String>(); //uuid

    public Main() throws InterruptedException {
        try {
            String pureJson = Core.settings.getDataDirectory().child("mods/database/settings.cn").readString();
            adata = new JSONObject(new JSONTokener(pureJson));
            if (!adata.has("settings")){
                Log.err("============");
                Log.err("CRITICAL ERROR - 404");
                Log.err("settings.cn does not contain `settings`");
                Log.err("============");
                return;
            } else {
                settings = adata.getJSONObject("settings");
                Log.info("settings loaded successfully");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("File not found: config\\mods\\database\\settings.cn")){
                Log.err("404 - settings.cn not found!");
                return;
            } else {
                Log.err("Initialization Error!");
                e.printStackTrace();
                return;
            }
        }
        if (adata.has("token")) {
            try {
                api = new DiscordApiBuilder().setToken(adata.getString("token")).login().join();
            } catch (Exception e) {
                if (e.getMessage().contains("READY packet")) {
                    System.out.println("\n[ERR!] discordplugin: invalid token.\n");
                } else {
                    e.printStackTrace();
                }
            }
        }
        //start botThread
        BotThread bt = new BotThread(api, Thread.currentThread(), adata.getJSONObject("settings"));
        bt.setDaemon(false);
        bt.start();

        if (!adata.has("login-info")) {
            Log.err("============");
            Log.err("CRITICAL ERROR - 404");
            Log.err("settings.cn does not contain `login-info`");
            Log.err("============");
            return;
        }
        Cycle c = new Cycle(Thread.currentThread());
        c.setDaemon(false);
        c.start();
        //auto =========================================================================================================
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;

            if (true) { //autoban

            }
            if (true) { //autokick
                if (player.name.equals("IGGGAMES")) {
                    player.con.kick("Invalid name, please choose another name.");
                }

            }
            //final

            if (settings.has("welcome-message")) {
                player.sendMessage(settings.getString("welcome-message"));//<---------------------------------------
            } else {
                Log.err("============");
                Log.err("ERROR - 404");
                Log.err("settings.cn does not contain `welcome-message`");
                Log.err("============");
            }
            if (pastLogin.containsKey(player.uuid)) {
                currentLogin.put(player.uuid, pastLogin.get(player.uuid));
                pastLogin.remove(player.uuid);
                player.sendMessage("[sky]Welcome back!");
            } else {
                player.setTeam(Team.derelict);
                player.updateRespawning();
                player.sendMessage("[yellow] /register or /login to have full access to the server!");
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            pastLogin.put(player.uuid, currentLogin.get(player.uuid));
            currentLogin.remove(player.uuid);
            new Object() {
                String uid = player.uuid;
                private Timer.Task task;

                {
                    task = Timer.schedule(() -> {
                        pastLogin.remove(uid);
                        task.cancel();
                    }, 30 * 60, 1);
                }
            };
        });
        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            Player player = event.player;
            if (player == null) return;
            if (event.breaking) return;
            JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
            if (data.has("bb")) {
                data.put("bb", data.getInt("bb")+1);

                int y = data.getInt("bb") / 10000;
                float z = (float) data.getInt("bb") / 10000;
                if ((float) y == z) {
                    Call.sendMessage("Congratulations to " + player.name + " [white]for building his/her " + y * 10000 + " Block!");
                }
            }

            /*
            //auto congratulations
                        int y = Main.database.get(p.uuid).getTP() / 60;
                        float z = (float) Main.database.get(p.uuid).getTP()/60;
                        if ((float) y == z) {
                            Call.sendMessage("Congratulations to " + p.name + " [white]for staying active for " + y + " Hours!");
                            Main.liveChat = Main.liveChat + "Congratulations to " + p.name + " [white]for staying active for " + y + " Hours!\n";
                        }
             */
        });
    }
    @Override
    public void registerServerCommands(CommandHandler handler) {

    }
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("register", "<Username> <Password>", "Register your account", (arg, player) -> {
            if (!currentLogin.containsKey(player.uuid)) {
                JSONObject login = adata.getJSONObject("login-info");
                if (!login.has(arg[0])) {
                    Pattern pa = Pattern.compile("[a-zA-Z]", Pattern.CASE_INSENSITIVE);
                    Matcher ma = pa.matcher(arg[1]);
                    boolean a = ma.find();

                    Pattern pn = Pattern.compile("[0-9]");
                    Matcher mn = pn.matcher(arg[1]);
                    boolean number = mn.find();

                    Pattern ps = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
                    Matcher ms = ps.matcher(arg[1]);
                    boolean special = ms.find();

                    if (a && number && special) {
                        //register username and password
                        JSONObject user = new JSONObject();
                        user.put("password", arg[1]);
                        user.put("dataID", byteCode.hash(32)); //give data ID
                        login.put(arg[0], user); //end registration
                        JSONObject data = new JSONObject(); //register user data
                        //date joined
                        Date thisDate = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/Y");
                        data.put("date-joined", dateFormat.format(thisDate));
                        data.put("username",arg[0]);
                        data.put("mp",0);
                        data.put("bb",0);
                        data.put("rank",0);
                        data.put("verified", 0);
                        //create user data and add time joined
                        adata.put(user.getString("dataID"), data);
                        //finishing off
                        player.sendMessage("[lime]Account created Successfully!");
                        player.sendMessage("[sky]Get Verified to have full access to the server or wait 15 minutes.");
                        //login player
                        currentLogin.put(player.uuid, user.getString("dataID"));
                    } else if (!a) {
                        player.sendMessage("Your password must contain a letter");
                    } else if (!number) {
                        player.sendMessage("Your password must contain a number");
                    } else {
                        player.sendMessage("Your password must contain a special character");
                    }
                } else {
                    player.sendMessage("[sky]Username is unavailable");
                }
            } else {
                player.sendMessage("You are already logged in!");
            }
        });
        handler.<Player>register("login", "<Username> <Password>", "Login into your account.", (arg, player) -> {
            if (!currentLogin.containsKey(player.uuid)) {
                if (loginAttempts.containsKey(player.uuid) && loginAttempts.get(player.uuid) > 4) {
                    player.sendMessage("[scarlet]You have attempted too many logins, please try again later.");
                    if (loginAttempts.get(player.uuid) == 5) {
                        loginAttempts.put(player.uuid,loginAttempts.get(player.uuid)+1);
                        new Object() {
                            String uid = player.uuid;
                            private Timer.Task task;

                            {
                                task = Timer.schedule(() -> {
                                    loginAttempts.remove(uid);
                                    task.cancel();
                                }, 30 * 60, 1);
                            }
                        };
                    }
                    return;
                }
                JSONObject login = adata.getJSONObject("login-info");
                if (login.has(arg[0])) {
                    JSONObject user = login.getJSONObject(arg[0]);
                    if (user.get("password").equals(arg[1])) {
                        currentLogin.put(player.uuid, user.getString("dataID"));
                        player.sendMessage("[lime]Login Successful!");
                        if (adata.has(currentLogin.get(player.uuid))) { //if adata has info of player
                            JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
                            if (data.getInt("verified") == 0) {
                                player.sendMessage("[sky]Get Verified to have full access to the server");
                            }
                            if (data.has("verified") && data.getInt("verified") == 1) {
                                player.setTeam(Team.sharded);
                                player.updateRespawning();
                            } else if (data.has("mp") && data.getInt("mp") > 15) {
                                player.setTeam(Team.sharded);
                                player.updateRespawning();
                            }
                        }
                    } else {
                        player.sendMessage("[yellow]Username or Password is incorrect.");
                        if (loginAttempts.containsKey(player.uuid)) {
                            loginAttempts.put(player.uuid, loginAttempts.get(player.uuid)+1);
                        } else {
                            loginAttempts.put(player.uuid, 1);
                        }
                    }
                } else {
                    player.sendMessage("[yellow]Username or Password is incorrect.");
                    if (loginAttempts.containsKey(player.uuid)) {
                        loginAttempts.put(player.uuid, loginAttempts.get(player.uuid)+1);
                    } else {
                        loginAttempts.put(player.uuid, 1);
                    }
                }
            } else {
                player.sendMessage("You are already logged in!");
            }
        });
        handler.<Player>register("key", "<key>", "Enter key to redeem Rank or pi.", (arg, player) -> {
            if (currentLogin.containsKey(player.uuid)) {
                JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
                if (keyList.containsKey(arg[0])) {
                    switch (keyList.get(arg[0]).getAction()) {
                        case "cr":
                            break;
                        case "verify":
                            if (keyList.get(arg[0]).getUsername().equals(data.getString("username"))) {
                                data.put("discord-tag", keyList.get(arg[0]).getValue());
                                data.put("verified", 1);
                                keyList.remove(arg[0]);
                                player.sendMessage("[lime]Successfully verified your account!");
                                player.setTeam(Team.sharded);
                                player.updateRespawning();
                            }
                            break;
                        default:
                            player.sendMessage("---ERROR---");
                            keyList.remove(arg[0]);
                    }
                } else {
                    player.sendMessage("Invalid Key.");
                }
            }
        });
        //calls for help and location
        handler.<Player>register("halp","Calls for help and setups /go", (arg, player) -> {
            halpX = (int) (player.x/8);
            halpY = (int) (player.y/8);
            Call.sendMessage("[coral][[[white]" + player.name + " [coral]]: [white]Need help at ([lightgray]" + halpX + "[white],[lightgray]" + halpY + "[white]). \ndo `[lightgray]/go[white]` to come to me.");
            Log.info("[coral][[[white]"+player.name + " [coral]]: [white]Need help at ([lightgray]" + halpX + "[white],[lightgray]" + halpY + "[white]). do `[lightgray]/go[white]` to come to me.");
        });
        //goes to position
        handler.<Player>register("go","goes to location from /here or /halp", (arg, player) -> {
            player.set(halpX*8,halpY*8);
            player.setNet(halpX*8,halpY*8);
            player.set(halpX*8,halpY*8);
        });
        //calls location
        handler.<Player>register("here","setups /go to go to your location", (arg, player) -> {
            halpX = (int) (player.x/8);
            halpY = (int) (player.y/8);
            Call.sendMessage("[coral][[[white]" + player.name + " [coral]]: [white]Here at ([lightgray]" + halpX + "[white],[lightgray]" + halpY + "[white]). \ndo `[lightgray]/go[white]` to come to me.");
            Log.info("[coral][[[white]" + player.name + " [coral]]: [white]Here at ([lightgray]" + halpX + "[white],[lightgray]" + halpY + "[white]). do `[lightgray]/go[white]` to come to me.");
        });
        //Shows team info
        handler.<Player>register("myteam","[Info]", "Gives team info", (arg, player) -> {
            Teams.TeamData teamData = state.teams.get(player.getTeam());
            if (!teamData.hasCore()) {
                player.sendMessage("Your team doesn't have a core!");
                return;
            }
            CoreBlock.CoreEntity core = teamData.cores.first();
            if (core == null) {
                player.sendMessage("Your team doesn't have a core.");
                return;
            }
            String playerTeam = player.getTeam().name;
            switch (playerTeam) {
                case "sharded":
                    playerTeam = "[accent]" + playerTeam;
                    break;
                case "crux":
                    playerTeam = "[scarlet]" + playerTeam;
                    break;
                case "blue":
                    playerTeam = "[royal]" + playerTeam;
                    break;
                case "derelict":
                    playerTeam = "[gray]" + playerTeam;
                    break;
                case "green":
                    playerTeam = "[lime]" + playerTeam;
                    break;
                case "purple":
                    playerTeam = "[purple]" + playerTeam;
                    break;
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

            Call.onInfoMessage(player.con,
                    "Your team is " + playerTeam +
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
                            "\n Dagger: " + dagger +
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
                            "\n");
        });

        handler.<Player>register("test","<>","aaaaaaaaaaaaaa", (arg, player) -> {
            int y = Integer.getInteger(arg[0]) / 10000;
            float z = (float) Integer.getInteger(arg[0]) / 10000;

            Call.sendMessage(y + " " + z);
            if ((float) y == z) {
                Call.sendMessage("Congratulations to " + player.name + " [white]for building his/her " + y * 10000 + " Block!");
            }
        });

        if (api != null) {
            handler.<Player>register("d", "<text...>", "Sends a message to discord.", (args, player) -> {

                if (!settings.has("dchannel-id")) {
                    player.sendMessage("[scarlet]This command is disabled.");
                } else {
                    TextChannel tc = this.getTextChannel(settings.getString("dchannel-id"));
                    if (tc == null) {
                        player.sendMessage("[scarlet]This command is disabled.");
                        return;
                    }
                    String string = args[0].replace("\\@here","").replaceAll("\\@everyone","@every1").replaceAll("\\@here","@h3r3").replaceAll("\\@(.*)#(.*)","<someone's tag>").replaceAll("<@(.*)>", "<someone's tag>").replaceAll("\\*","\\*").replaceAll("_","\\_").replaceAll("\\|\\|","\\|\\|").replaceAll("~","\\~");

                    tc.sendMessage(byteCode.noColors(player.name) + " *@mindustry* : " + string);
                    player.sendMessage(byteCode.noColors(player.name) + "[sky] to @discord[]: " + args[0]);
                }

            });

            handler.<Player>register("gr", "[player] [reason...]", "Report a griefer by id (use '/gr' to get a list of ids)", (args, player) -> {
                //https://github.com/Anuken/Mindustry/blob/master/core/src/io/anuke/mindustry/core/NetServer.java#L300-L351
                if (!(settings.has("gr-channel-id") && settings.has("mod-role-id"))) {
                    player.sendMessage("[scarlet]This command is disabled.");
                    return;
                }

                //if (true) return; //some things broke in arc and or Vars.playergroup

                for (Long key : cooldowns.keySet()) {
                    if (key + CDT < System.currentTimeMillis() / 1000L) {
                        cooldowns.remove(key);
                        continue;
                    } else if (player.uuid == cooldowns.get(key)) {
                        player.sendMessage("[scarlet]This command is on a 5 minute cooldown!");
                        return;
                    }
                }

                if (args.length == 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("[orange]List or reportable players: \n");
                    for (Player p : Vars.playerGroup.all()) {
                        if (p.isAdmin || p.con == null) continue;

                        builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id).append(")\n");
                    }
                    player.sendMessage(builder.toString());
                } else {
                    Player found = null;
                    if (args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))) {
                        int id = Strings.parseInt(args[0].substring(1));
                        //found = Vars.playerGroup.find(p -> p.id == id);
                        for (Player p: Vars.playerGroup.all()){
                            if (p.id == id){
                                found = p;
                                break;
                            }
                        }
                    } else {
                        for (Player p: Vars.playerGroup.all()){
                            if (p.name.equalsIgnoreCase(args[0])){
                                found = p;
                                break;
                            }
                        }
                        //found = Vars.playerGroup.find(p -> p.name.equalsIgnoreCase(args[0]));
                    }
                    if (found != null) {
                        if (found.isAdmin) {
                            player.sendMessage("[scarlet]Did you really expect to be able to report an admin?");
                        } else if (found.getTeam() != player.getTeam()) {
                            player.sendMessage("[scarlet]Only players on your team can be reported.");
                        } else {
                            TextChannel tc = this.getTextChannel(settings.getString("gr-channel-id"));
                            Role r = this.getRole(settings.getString("mod-role-id"));
                            if (tc == null || r == null) {
                                player.sendMessage("[scarlet]This command is disabled.");
                                return;
                            }
                            //send message
                            if (args.length > 1) {
                                new MessageBuilder()
                                        .setEmbed(new EmbedBuilder()
                                                .setTitle("Griefer online")
                                                .setDescription(r.getMentionTag())
                                                .addField("name", found.name)
                                                .addField("reason", args[1])
                                                .setColor(Color.ORANGE)
                                                .setFooter("Reported by " + player.name))
                                        .send(tc);
                                tc.sendMessage(r.getMentionTag());
                            } else {
                                new MessageBuilder()
                                        .setEmbed(new EmbedBuilder()
                                                .setTitle("Griefer online")
                                                .setDescription(r.getMentionTag())
                                                .addField("name", found.name)
                                                .setColor(Color.ORANGE)
                                                .setFooter("Reported by " + player.name))
                                        .send(tc);
                                tc.sendMessage(r.getMentionTag());
                            }
                            Call.sendMessage(found.name + "[sky] is reported to discord.");
                            cooldowns.put(System.currentTimeMillis() / 1000L, player.uuid);
                        }
                    } else {
                        player.sendMessage("[scarlet]No player[orange] '" + args[0] + "'[scarlet] found.");
                    }
                }
            });
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

    public Role getRole(String id) {
        Optional<Role> r1 = this.api.getRoleById(id);
        if (!r1.isPresent()) {
            System.out.println("[ERR!] discordplugin: adminrole not found!");
            return null;
        }
        return r1.get();
    }
}