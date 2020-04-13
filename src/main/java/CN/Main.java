package CN;

import arc.Core;
import arc.Events;
import arc.struct.Array;
import arc.util.*;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Items;
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
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.*;
import static mindustry.Vars.data;

public class Main extends Plugin {
    //variables
    private JSONObject settings;

    public static JSONObject adata;
    public static HashMap<String, String> currentLogin = new HashMap<>();
    public static HashMap<String, key> keyList = new HashMap<>();
    public static boolean chat = true;
    public static HashMap<Integer, Player> idTempDatabase = new HashMap<>();
    public static Array<String> pjl = new Array<>();
    public static long milisecondSinceBan = Time.millis();
    public static Array<String> flaggedIP = new Array<>();
    public HashMap<String, String> pastLogin = new HashMap<>();
    public HashMap<String, Integer> loginAttempts = new HashMap<>();
    public int halpX;
    public int halpY;
    public String ruleKey;
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
        byteCode.loadTips();
        //Read rules key
        ruleKey = byteCode.hash(8);
        keyList.put(ruleKey, new key("Server", "readRules", "1"));
        //auto =========================================================================================================
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;

            if (true) { //autoban
                if (player.getInfo().timesKicked * 10 > player.getInfo().timesJoined) {
                    Log.info("[B] {0} : K/10J greater than 1/10", player.uuid);
                    Call.onInfoMessage(player.con, "BANNED: kick / 10 join ratio grater than 1/10");
                    Call.onInfoMessage(player.con, "BANNED: kick / 10 join ratio grater than 1/10");
                    netServer.admins.banPlayer(player.uuid);
                    player.getInfo().timesKicked--;
                    player.con.kick("BANNED: kick / 10 join ratio grater than 1/10",1);
                } else if (player.getInfo().timesKicked >= 10) {
                    Log.info("[B] {0} : k >= 10", player.uuid);
                    Call.onInfoMessage(player.con, "BANNED: times kicked >= 10");
                    Call.onInfoMessage(player.con, "BANNED: times kicked >= 10");
                    netServer.admins.banPlayer(player.uuid);
                    player.getInfo().timesKicked--;
                    player.con.kick("BANNED: times kicked >= 10",1);
                }
            }
            if (true) { //autokick
                if (byteCode.safeName(player.name)) {
                    Log.info("[K] {0} : Invalid Name", player.uuid);
                    player.getInfo().timesKicked--;
                    player.con.kick("Invalid Name!");
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
                if (!currentLogin.containsValue(pastLogin.get(player.uuid))) {
                    currentLogin.put(player.uuid, pastLogin.get(player.uuid));
                    pastLogin.remove(player.uuid);

                    JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
                    if (data.has("readRules") && data.getInt("readRules") == 1) {
                        if (data.has("verified") && data.getInt("verified") == 1) {
                            player.setTeam(Team.sharded);
                            player.updateRespawning();
                            Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                            //pjl
                            Date thisDate = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                            pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                        } else if (data.has("mp") && data.getInt("mp") > 15) {
                            player.setTeam(Team.sharded);
                            player.updateRespawning();
                            Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                            //pjl
                            Date thisDate = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                            pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                        } else {
                            player.sendMessage("[yellow] Wait " + (15 - data.getInt("mp")) + " more minutes or get Verified to be able to play");
                            player.setTeam(Team.derelict);
                            player.updateRespawning();
                        }
                    } else {
                        player.sendMessage("[yellow]Read the /rules to be able to play");
                    }
                } else {
                    player.sendMessage("[scarlet]Error! Account is already in use in this server! If this is not you, contact a Moderator immediately");
                }
            } else {
                player.setTeam(Team.derelict);
                player.updateRespawning();
                player.sendMessage("[yellow] /register or /login to have full access to the server!");
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            if (currentLogin.containsKey(player.uuid)) {
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
            }
        });
        Events.on(EventType.PlayerBanEvent.class, event -> {
            Player player = event.player;
            if (currentLogin.containsKey(player.uuid)) {
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
            }
        });
        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            Player player = event.player;
            if (player == null) return;
            if (event.breaking) return;
            JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
            //auto congratulations
            if (data.has("bb")) {
                data.put("bb", data.getInt("bb")+1);

                int y = data.getInt("bb") / 10000;
                float z = (float) data.getInt("bb") / 10000;
                if ((float) y == z) {
                    Call.sendMessage("Congratulations to " + player.name + " [white]for building his/her " + y * 10000 + " Block!");
                }
            }
            //add xp
            data.put("xp", data.getFloat("xp") + ((float) byteCode.bbXPGainMili(event.tile.block().buildCost/60) / 10000));

            if (byteCode.xpn(data.getInt("lvl")+1) < data.getFloat("xp")) {
                Call.onInfoToast(player.con,"[lime]Leveled Up!", 10);
                data.put("lvl", data.getInt("lvl") + 1);
            }

            //event.tile.block().stats.
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
        Events.on(EventType.ServerLoadEvent.class, event -> {
            netServer.admins.addChatFilter((player, text) -> null);
        });
        Events.on(EventType.PlayerChatEvent.class, event -> {
            Player player = event.player;
            if (!event.message.startsWith("/")) {
                if (chat || player.isAdmin) {
                    if (currentLogin.containsKey(player.uuid)) {
                        JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
                        if (data.has("lvl") && data.has("rank")) {
                            Call.sendMessage(byteCode.tag(data.getInt("rank"),data.getInt("lvl")) + " " + player.name + " [white]> " + byteCode.censor(event.message));
                        } else {
                            Call.sendMessage(player.name + " [white]> " + byteCode.censor(event.message));
                            Log.err("============");
                            Log.err("ERROR - 404");
                            Log.err("`" + player.uuid + "` does not contain `lvl` or `data`");
                            Log.err("============");
                        }
                    } else {
                        Call.sendMessage("[lightgray]<SPECTATOR> []"+player.name + " [white]> " + byteCode.censor(event.message));
                    }
                } else {
                    player.sendMessage("[lightgray]Chat is Disabled.");
                }
            }
        });
    }
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("clearplayer", "<uuid>", "description", arg -> {
            if (netServer.admins.getInfo(arg[0]).timesJoined > 0) {
                netServer.admins.getInfo(arg[0]).timesKicked = 0;
                netServer.admins.getInfo(arg[0]).lastKicked = Time.millis();
            }
        });
        handler.register("badlist", "<word>", "description", arg -> {
            JSONObject badlist = adata.getJSONObject("badList");
            if (badlist.has(arg[0])) {
                Log.err("badList already containg `{}`!", arg[0]);
            } else {
                badlist.put(arg[0], "bad");
            }
            try {
                File file = new File("config\\mods\\database\\settings.cn");
                FileWriter out = new FileWriter(file, false);
                PrintWriter pw = new PrintWriter(out);
                pw.println(Main.adata.toString());
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
            Log.info("Succesfully added {0} to badList!", arg[0]);
        });
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

                    if (a && number && special && arg[1].length() >= 8) {
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
                        data.put("lvl",0);
                        data.put("xp",0.0000);
                        data.put("verified", 0);
                        //create user data and add time joined
                        adata.put(user.getString("dataID"), data);
                        //finishing off
                        player.sendMessage("[lime]Account created Successfully! Now try to /login");
                        player.sendMessage("[yellow]Requirements (0/2):");
                        player.sendMessage("[yellow]> Get Verified to have full access to the server or wait 15 minutes.");
                        player.sendMessage("[yellow]> Read the /rules");
                        player.sendMessage("[sky]Complete all the requirements to be able to play.");
                    } else if (!a) {
                        player.sendMessage("[scarlet]Your password must contain a letter");
                    } else if (!number) {
                        player.sendMessage("[scarlet]Your password must contain a number");
                    } else if (arg[1].length() < 8) {
                        player.sendMessage("[scarlet]Your password must be at least 8 characters long");
                    } else {
                        player.sendMessage("[scarlet]Your password must contain a special character");
                    }
                } else {
                    player.sendMessage("[yellow]Username is unavailable");
                }
            } else {
                player.sendMessage("[scarlet]You are already logged in!");
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
                        if (!currentLogin.containsValue(user.getString("dataID"))) {
                            currentLogin.put(player.uuid, user.getString("dataID"));
                            player.sendMessage("[lime]Login Successful!");
                            if (adata.has(currentLogin.get(player.uuid))) { //if adata has info of player
                                JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
                                if (data.has("readRules") && data.getInt("readRules") == 1) {
                                    if (data.has("verified") && data.getInt("verified") == 1) {
                                        player.setTeam(Team.sharded);
                                        player.updateRespawning();
                                        Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                                        //pjl
                                        Date thisDate = new Date();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                                        pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                                    } else if (data.has("mp") && data.getInt("mp") > 15) {
                                        player.setTeam(Team.sharded);
                                        player.updateRespawning();
                                        Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                                        //pjl
                                        Date thisDate = new Date();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                                        pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                                    } else if (data.has("mp")) {
                                        player.sendMessage("[yellow] Wait " + (15 - data.getInt("mp")) + " more minutes or get Verified to be able to play");
                                    }
                                } else {
                                    player.sendMessage("[yellow]Read the /rules to be able to play");
                                }
                            }
                        } else {
                            player.sendMessage("[scarlet]Error! Account is already in use in this server! If this is not you, contact a Moderator immediately");
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
                                if (!data.has("rank") || data.getInt("rank") == 0) data.put("rank", 1);
                                keyList.remove(arg[0]);
                                player.sendMessage("[lime]Successfully verified your account!");
                                if (data.has("readRules") && data.getInt("readRules") == 1) {
                                    player.setTeam(Team.sharded);
                                    player.updateRespawning();
                                    Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                                    //pjl
                                    Date thisDate = new Date();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                                    pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                                } else {
                                    player.sendMessage("[yellow]Read the /rules to be able to play");
                                }
                            }
                            break;
                        case "readRules":
                            if (data.has("readRules")) {
                                if (data.getInt("readRules") == 1) {
                                    player.sendMessage("[scarlet]You already read the Rules!");
                                    if (data.has("verified") && data.getInt("verified") == 1) {
                                        player.setTeam(Team.sharded);
                                        player.updateRespawning();
                                        Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                                        //pjl
                                        Date thisDate = new Date();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                                        pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                                    } else if (data.has("mp") && data.getInt("mp") > 15) {
                                        player.setTeam(Team.sharded);
                                        player.updateRespawning();
                                        Call.sendMessage("[accent]"+byteCode.noColors(player.name) + " has connected.");
                                        //pjl
                                        Date thisDate = new Date();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
                                        pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
                                    } else {
                                        player.sendMessage("[yellow] Wait " + (15 - data.getInt("mp")) + " more minutes or get Verified to be able to play");
                                    }
                                    return;
                                }
                            }
                            data.put("readRules", 1);
                            player.sendMessage("[lime]You read the Rules!");
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
        handler.<Player>register("rules","Sends you the server rules", (arg, player) -> {
            player.sendMessage("Rules:\n" +
                    "\nGeneral Rules:\n" +
                    "1) Grief or Spam.\n" +
                    "2) Ban/Kick evade.\n" +
                    "3) Use exploits.\n" +
                    "4) Nuke/Explode core, just /rtv to vhange maps.\n" +
                    "5) Use multiple accounts.\n" +
                    "6) Making and/or using a AFK machine will result in kick/ban.\n" +
                    "\nDiscord Rules:\n" +
                    "1) Name must be similar to that of in-game.\n" +
                    "2) Spamming Caps or messages can result in ban.\n" +
                    "3) Posting of NSFW and/or Personal information is strictly prohibited.\n" +
                    "4) Insulting and/or bullying is not tolerated.\n" +
                    "5) Exploiting bots will result in ban.\n" +
                    "6) Planning to Grief, Raid or harass will result in ban.\n" +
                    "\nServer Specific rules:\nautoban:\n" +
                    "You will be auto banned if:\n" +
                    "1) If kicked more 10 times.\n" +
                    "2) If kick to join ratio is more than 1/10\n" +
                    "\nSurvival:\n" +
                    "1) Pixel art permitted but must be at most 32x32. Pixel art may be removed at any point by anyone. Spamming will result in ban.\n" +
                    "2) Thorium reactors must be far away from core.\n" +
                    "3) No more than 50 draught miners per map.\n" +
                    "4) Absolutely no exploits allowed; whether beneficial or not.\n" +
                    "5) Power sources must be dioded." +
                    "\nSandbox:\n" +
                    "1) Spamming will result in ban.\n" +
                    "2) Builds must be constrained to 1 box. (48x48)\n" +
                    "3) Trying to lag/crash server will result in ban.\n" +
                    "4) Any machine that purposely makes fire or explosion will result in ban.\n\n" +
                    "Now that you read the rules, do `[lightgray]/key " + ruleKey + "[]` to confirm you read the rules.");
        });
        //Shows player info
        handler.<Player>register("stats","Shows your stats", (arg, player) -> {
            if (currentLogin.containsKey(player.uuid)) {
                JSONObject data = adata.getJSONObject(currentLogin.get(player.uuid));
                int txptl = byteCode.xpn(data.getInt("lvl") + 1) - byteCode.xpn(data.getInt("lvl"));
                int xpil = (int) data.getFloat("xp") - byteCode.xpn(data.getInt("lvl"));
                int ten = xpil / (txptl / 10);

                StringBuilder builder = new StringBuilder();
                builder.append("Total XP: " + data.getFloat("xp"));
                builder.append("\nLevel: " + data.getInt("lvl"));
                builder.append("\nRank: " + data.getInt("rank") + " - " + byteCode.tagName(data.getInt("rank")));
                builder.append("\n<");
                for (int i = 0; i < ten; i++) {
                    builder.append("/");
                }
                for (int i = 0; i < 10 - ten; i++) {
                    builder.append("-");
                }
                builder.append(">");
                builder.append("\n" + xpil + "XP / " + txptl + "XP until next level").append("\n");
                builder.append("name : ").append(player.name).append("\n");
                builder.append("times joined : ").append(player.getInfo().timesJoined).append("\n");
                builder.append("times kicked : ").append(player.getInfo().timesKicked).append("\n");
                builder.append("uuid : ").append(player.uuid).append("\n");
                for (String keyStr : data.keySet()) {
                    Object keyvalue = data.get(keyStr);
                    //Print key and value
                    if (keyStr.equals("lvl"))  continue;
                    if (keyStr.equals("rank")) continue;
                    if (keyStr.equals("xp")) continue;
                    builder.append("[white]"+keyStr + ": [lightgray]" + keyvalue).append("\n");
                }
                player.sendMessage(builder.toString());
            } else {
                player.sendMessage("[scarlet]/login or /register to use this command!");
            }
        });
        //list of all players
        handler.<Player>register("players","list of all players", (arg, player) -> {
            StringBuilder builder = new StringBuilder();
            builder.append("[accent]List of players: \n");
            for (Player p : playerGroup.all()) {
                if (currentLogin.containsKey(p.uuid)) {
                    JSONObject data = Main.adata.getJSONObject(currentLogin.get(p.uuid));
                    builder.append(byteCode.tag(data.getInt("rank"),data.getInt("lvl"))).append(" ").append(byteCode.nameR(p.name)).append(" [white]: #").append(p.id);
                }
            }
            player.sendMessage(builder.toString());
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
        handler.<Player>register("myteam", "Gives team info", (arg, player) -> {
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
            int players = 0;
            for (Player p : playerGroup.all()) {
                if (p.getTeam() == player.getTeam()) {
                    players++;
                }
            }
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
                            "\n[]Your team has " + players + " players" +
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
        handler.<Player>register("test","<something>","aaaaaaaaaaaaaa", (arg, player) -> {
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
                if (!(settings.has("gr_channel_id") && settings.has("mod_role_id"))) {
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
                            TextChannel tc = this.getTextChannel(settings.getString("gr_channel_id"));
                            Role r = this.getRole(settings.getString("mod_role_id"));
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