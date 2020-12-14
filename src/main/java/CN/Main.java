package CN;

import arc.Events;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.json.JSONObject;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.netServer;
import static mindustry.Vars.player;

public class Main extends Plugin {
    //variables
    private JSONObject settings;

    public static HashMap<String, String> currentLogin = new HashMap<>();
    public static HashMap<String, Player> currentKick = new HashMap<>();
    public static HashMap<String, xpg> currentBuild = new HashMap<>();
    public static HashMap<String, key> keyList = new HashMap<>();
    public HashMap<String, Long> noXP = new HashMap<>();
    public static HashMap<Integer, Player> idTempDatabase = new HashMap<>();
    public HashMap<String, String> pastLogin = new HashMap<>();
    public HashMap<String, Integer> loginAttempts = new HashMap<>();
    public static String liveChatQueue = "";
    //discord shat
    private final Long CDT = 300L;
    private DiscordApi api = null;
    private HashMap<Long, String> cooldowns = new HashMap<Long, String>(); //uuid
    private int radi = 15;

    public Main() throws InterruptedException {
        if (byteCode.get("settings") == null) {
            Log.err("Settings Directory or file not found!");
            return;
        } else {
            JSONObject settings = byteCode.get("settings");
            if (settings.has("token"+Administration.Config.port.num())) {
                try {
                    api = new DiscordApiBuilder().setToken(settings.getString("token"+Administration.Config.port.num())).login().join();
                } catch (Exception e) {
                    if (e.getMessage().contains("READY packet")) {
                        Log.err("\ninvalid token.\n");
                        return;
                    } else {
                        e.printStackTrace();
                        return;
                    }
                }

                BotThread bt = new BotThread(api, Thread.currentThread(), byteCode.get("settings"));
                bt.setDaemon(false);
                bt.start();
            } else {
                Log.err("`token"+Administration.Config.port.num()+"` not found, unable to start discord bot.");
            }
        }

        if (byteCode.get("login_info") == null) {
            Log.err("============");
            Log.err("CRITICAL ERROR - 404");
            Log.err("mind_db/ does not contain `login_info.cn`");
            Log.err("============");
            return;
        }
        Cycle c = new Cycle(Thread.currentThread());
        c.setDaemon(false);
        c.start();
        //auto
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            Log.info(byteCode.noColors(player.name)+" : "+player.uuid+" > has connected - spectator");
            settings = byteCode.get("settings");
            if (settings == null){
                Log.err("mind_db/ does not contain `settings.cn`");
                return;
            }
            if (settings.has("welcome_message")) {
                player.sendMessage(settings.getString("welcome_message"));//<_______________________________________
            } else {
                Log.err("============");
                Log.err("ERROR - 404");
                Log.err("settings.cn does not contain `welcome_message`");
                Log.err("============");
            }
            if (pastLogin.containsKey(player.uuid)) {
                if (pastLogin.get(player.uuid) == null || !byteCode.has(pastLogin.get(player.uuid))) {
                    pastLogin.remove(player.uuid);
                    return;
                }
                currentLogin.put(player.uuid, pastLogin.get(player.uuid));
                pastLogin.remove(player.uuid);
                player.sendMessage("[sky]Welcome back!");
                Call.sendMessage("[accent]"+byteCode.noColors(player.name)+" has connected.");
                Log.info(byteCode.noColors(player.name)+" : "+player.uuid+" > has connected");
            } else {
                player.setTeam(Team.derelict);
                player.updateRespawning();
                player.sendMessage("[yellow] /register or /login to have full access to the server!");
            }
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            if (player.getInfo().lastKicked > Time.millis()) {
                JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                if (data == null) {
                    Log.err("Account for uuid `"+player.uuid+"` missing data id `"+currentLogin.get(player.uuid)+"`!");
                    return;
                }
                if (data.has("timesKicked")) {
                    data.put("timesKicked", data.getInt("timesKicked") + 1);
                } else {
                    data.put("timesKicked", 1);
                }
                currentLogin.remove(player.uuid);
                currentKick.put(player.name, player);
            }

            if (player.getInfo().banned) {
                if (currentLogin.containsKey(player.uuid)) {
                    JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                    if (data == null) {
                        Log.err("Account for uuid `"+player.uuid+"` missing data id `"+currentLogin.get(player.uuid)+"`!");
                        return;
                    }
                    if (data.has("banned")) {
                        data.put("banned", data.getInt("banned") +1);
                    } else {
                        data.put("banned", 1);
                    }
                    data.put("rank", 0);
                    currentLogin.remove(player.uuid);
                }
            }

            if (currentLogin.containsKey(player.uuid)) {
                idTempDatabase.put(player.id, player);
                pastLogin.put(player.uuid, currentLogin.get(player.uuid));
                currentLogin.remove(player.uuid);
                Call.sendMessage("[accent]" + byteCode.noColors(player.name) + " has disconnected.");
                Log.info(byteCode.noColors(player.name) + " : " + player.uuid + " > has disconnected");
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
            } else {
                Log.info(byteCode.noColors(player.name)+" : "+player.uuid+" > has disconnected - spectator");
            }
        });
        Events.on(EventType.PlayerBanEvent.class, event -> {
            Player player = event.player;
            if (currentLogin.containsKey(player.uuid)) {
                JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                if (data == null) {
                    Log.err("Account for uuid `"+player.uuid+"` missing data id `"+currentLogin.get(player.uuid)+"`!");
                    return;
                }
                if (data.has("banned")) {
                    data.put("banned", data.getInt("banned") +1);
                } else {
                    data.put("Banned", 1);
                }
                data.put("rank", 0);
                currentLogin.remove(player.uuid);
            }
        });
        Events.on(EventType.GameOverEvent.class, event -> {
            new Object() {
                private Timer.Task task;
                {
                    task = Timer.schedule(() -> {
                        for (Player p : Vars.playerGroup.all()) {
                            if (!Main.currentLogin.containsKey(p.uuid)) {
                                p.con.close();
                            }
                        }
                        task.cancel();
                    }, 12, 1);
                }
            };
        });
        Events.on(EventType.WorldLoadEvent.class, event -> {
            for (Player p : Vars.playerGroup.all()) {
                if (!Main.currentLogin.containsKey(p.uuid)) {
                    p.con.close();
                }
            }
            settings = byteCode.get("settings");
            if (settings == null) {
                Log.err("mind_db/ does not contain `settings.cn`");
                return;
            }
            if (settings.has("coreProtection_radius")) {
                radi = settings.getInt("coreProtection_radius");
            } else {
                Log.err("settings.cn does not contain key `coreProtection_radius`");
            }

            Vars.netServer.admins.addActionFilter(action -> {
                player = action.player;
                if (player == null) return true;
                String uuid = player.uuid;
                if (uuid == null) return true;
                JSONObject data = byteCode.get(Main.currentLogin.get(uuid));
                if (data == null) {Log.err(uuid + " doesnt have dataID " + Main.currentLogin.get(uuid)); player.sendMessage("ERROR - No dataID"); return false;}
                settings = byteCode.get("settings");
                if (settings == null) {
                    Log.err("mind_db/ does not contain `settings.cn`");
                    return true;
                }
                if (action.type == Administration.ActionType.rotate) {
                    if (data.has("rank") && data.getInt("rank") >= 2) return true;
                    return false;
                }
                return true; //thx fuzz
            });

            if (!settings.has("lc_id"+Administration.Config.port.num())) Log.err("settings.cn does not contain key lc_id"+Administration.Config.port.num());
        });
        Events.on(EventType.BlockBuildBeginEvent.class, event -> {
            if (event.breaking) currentBuild.remove(event.tile.x+","+event.tile.y);
            currentBuild.put(event.tile.x+","+event.tile.y, new xpg(event.tile.cblock(), Time.millis()));
        });
        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            Player player = event.player;
            if (player == null) return;
            if (event.breaking) {
                if (!noXP.containsKey(player.uuid)) noXP.put(player.uuid, (long) (Time.millis() + (1000 / 2.51 * event.tile.cblock().buildCost / 60)));
                if (currentBuild.containsKey(player.uuid)) currentBuild.remove(player.uuid);
                return;
            }
            if (currentLogin.containsKey(player.uuid)) {
                if (byteCode.has(currentLogin.get(player.uuid))) {
                    JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                    if (data == null) {
                        Log.err("mind_db/ does not contain `"+currentLogin.get(player.uuid)+".cn`");
                        player.sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing account data");
                        return;
                    } else {
                        //bb
                        if (data.has("bb")) {
                            byteCode.putInt(currentLogin.get(player.uuid), "bb", data.getInt("bb")+1);
                        } else {
                            byteCode.putInt(currentLogin.get(player.uuid), "bb", 1);
                        }
                        //xp
                        if (currentBuild.containsKey(event.tile.x+","+event.tile.y)) {
                            if (currentBuild.get(event.tile.x + "," + event.tile.y).getBlock() == event.tile.cblock()) {
                                if (Time.millis() - currentBuild.get(event.tile.x + "," + event.tile.y).getTime() > (event.tile.cblock().buildCost / 60) / 2.6 * 1000) {
                                    if (data.has("xp")) {
                                        data.put("xp", data.getFloat("xp") + (float) byteCode.bbXPGainMili(event.tile.cblock().buildCost / 60) / 10000);
                                    } else {
                                        data.put("xp", (float) byteCode.bbXPGainMili(event.tile.block().buildCost / 60) / 10000);
                                    }
                                } else {
                                    if (data.has("xp")) {
                                        data.put("xp", data.getFloat("xp") + (float) byteCode.bbXPGainMili((float) (Time.millis() - currentBuild.get(event.tile.x + "," + event.tile.y).getTime()) / 1000) / 10000);
                                    } else {
                                        data.put("xp", (float) byteCode.bbXPGainMili((float) (Time.millis() - currentBuild.get(event.tile.x + "," + event.tile.y).getTime()) / 1000) / 10000);
                                    }
                                }
                                if (data.has("lvl")) {
                                    if (byteCode.xpn(data.getInt("lvl") + 1) < data.getFloat("xp")) {
                                        Call.onInfoToast(player.con, "[lime]Leveled Up!", 10);
                                        data.put("lvl", data.getInt("lvl") + 1);
                                    }
                                } else {
                                    data.put("lvl", 0);
                                }
                                byteCode.save(currentLogin.get(player.uuid), data);
                            }
                        } else {
                            if (noXP.containsKey(player.uuid) && noXP.get(player.uuid) > Time.millis()) {
                                return;
                            }
                            //noXP.put(player.uuid, (long) (Time.millis() + (1000 / 2.51 * event.tile.block().buildCost / 60)));
                            if (data.has("xp")) {data.put("xp", data.getFloat("xp") + ((float) byteCode.bbXPGainMili(event.tile.block().buildCost / 60) / 10000));} else {data.put("xp", ((float) byteCode.bbXPGainMili(event.tile.block().buildCost / 60) / 10000));}
                            if (!data.has("lvl")) data.put("lvl", 1);
                            if (byteCode.xpn(data.getInt("lvl") + 1) < data.getFloat("xp")) {
                                Call.onInfoToast(player.con, "[lime]Leveled Up!", 10);
                                data.put("lvl", data.getInt("lvl") + 1);
                            }
                            byteCode.save(currentLogin.get(player.uuid), data);
                        }
                    }
                }
            }
        });
        Events.on(EventType.ServerLoadEvent.class, event -> {
            netServer.admins.addChatFilter((player, text) -> null);
            currentBuild.clear();
            currentLogin.clear();
            pastLogin.clear();
        });
        Events.on(EventType.PlayerChatEvent.class, event -> {
            Player player = event.player;
            if (!event.message.startsWith("/")) {
                if (currentLogin.containsKey(player.uuid)) {
                    JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                    if (data == null) {
                        Log.err("mind_db/ does not contain `"+currentLogin.get(player.uuid)+".cn`");
                        player.sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing account data");
                        return;
                    }
                    if (data.has("lvl") && data.has("rank")) {
                        Call.sendMessage(byteCode.tag(data.getInt("rank"),data.getInt("lvl")) + " " + player.name + " [white]> " + byteCode.censor(event.message));
                        Log.info(byteCode.noColors(byteCode.tag(data.getInt("rank"),data.getInt("lvl")) + " " + player.name + " [white]> " + event.message));
                        liveChatQueue += "\n<"+data.getInt("lvl")+"> " + byteCode.dec(byteCode.noColors(player.name)) + " > " + byteCode.dec(byteCode.noColors(byteCode.censor(event.message.replace("\\@here","").replaceAll("\\@everyone","@every1").replaceAll("\\@here","@h3r3").replaceAll("\\@(.*)#(.*)","<someone's tag>").replaceAll("<@(.*)>", "<someone's tag>"))));
                    } else {
                        Call.sendMessage(player.name + " [white]> " + byteCode.censor(event.message));
                        Log.err("============");
                        Log.err("ERROR - 404");
                        Log.err("`" + player.uuid + "` does not contain `lvl` or `data`");
                        Log.err("============");
                    }
                } else {
                    Call.sendMessage("[lightgray]<SPECTATOR> []"+player.name + " [white]> " + byteCode.censor(event.message));
                    Log.info("[lightgray]<SPECTATOR> []"+player.name + " [white]> " + byteCode.noColors(event.message));
                    liveChatQueue += "\n<S0> " + byteCode.dec(byteCode.noColors(player.name)) + " > " + byteCode.dec(byteCode.noColors(byteCode.censor(event.message.replace("\\@here","").replaceAll("\\@everyone","@every1").replaceAll("\\@here","@h3r3").replaceAll("\\@(.*)#(.*)","<someone's tag>").replaceAll("<@(.*)>", "<someone's tag>"))));
                }

            }
        });
    }
    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("settings", "sends all settings as message", arg -> {
            settings = byteCode.get("settings");
            if (settings == null) {
                Log.err("mind_db/ does not contain `settings.cn`");
                return;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("\n");
            for (String keyStr : settings.keySet()) {
                Object keyvalue = settings.get(keyStr);
                //Print key and value
                builder.append(keyStr + " : " + keyvalue).append("\n");
            }
            Log.info(builder.toString());
        });
        handler.register("get","<fileName>", "gets everything inside the json.cn", arg -> {
            JSONObject data = byteCode.get(arg[0]);
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                builder.append("\n");
                for (String keyStr : data.keySet()) {
                    Object keyvalue = data.get(keyStr);
                    //Print key and value
                    builder.append(keyStr + " : " + keyvalue).append("\n");
                }
                Log.info(builder.toString());
            }
        });
        handler.register("make","<fileName>", "puts a basic object in into the filename.cn", arg -> {
            JSONObject object = new JSONObject();
            object.put("Key", "Value");
            Log.info(byteCode.make(arg[0],object));
        });
        handler.register("putstr","<fileName> <key> <value...>", "puts a key and value into the filename.cn", arg -> {
            Log.info(byteCode.putStr(arg[0], arg[1], arg[2]));
        });
        handler.register("putint","<fileName> <key> <value>", "puts a key and value into the filename.cn", arg -> {
            int i = Strings.parseInt(arg[2]);
            Log.info(byteCode.putInt(arg[0], arg[1], i));
        });
        handler.register("remove","<fileName> <key>", "removes a key in the filename.cn", arg -> {
            Log.info(byteCode.remove(arg[0], arg[1]));
        });
        handler.register("debug", "check all critical info is present", arg -> {
            Log.info("Step 1 - core files are present");
            Log.info("settings.cn is present : " + byteCode.has("settings"));
            Log.info("discord_accounts.cn is present : " + byteCode.has("discord_accounts"));
            Log.info("login_info.cn is present : " + byteCode.has("login_info"));

            Log.info("Step 2 - account has DataID");
            int accounts = 0;
            int dataIDs = 0;
            StringBuilder missing = new StringBuilder();
            missing.append("Accounts missing dataID:\n");
            JSONObject login = byteCode.get("login_info");
            JSONObject user;
            if (login == null) {
                Log.err("mind_db/ does not contain `login_info.cn`");
            } else {
                for (String keyStr : login.keySet()) {
                    user = login.getJSONObject(keyStr);
                    accounts++;
                    if (byteCode.get(user.getString("dataID")) == null) {
                        missing.append(keyStr + " : " + user.getString("dataID"));
                    } else {
                        dataIDs++;
                    }
                }
                Log.info("Found " + accounts + " accounts and " + dataIDs + " data IDs.");
                Log.info(missing.toString());
            }
        });
        handler.register("cr", "<rankInt>", "changes rank of player", arg -> {
            if (Strings.canParseInt(arg[0])) {
                String hash = byteCode.hash(8);
                keyList.put(hash, new key(null, "cr", arg[0]));
                Log.info("do `/key " + hash + "` to receive rank change");
            } else {
                Log.err("rank must contain numbers!");
            }
        });
        handler.register("badlist", "<badword>", "adds bad word to badlist", arg -> {
            if (byteCode.has("badList.cn")) {
                if (byteCode.get("badList").has(arg[0])) {
                    Log.err("badList already contains " + arg[0]);
                    return;
                }
                byteCode.putStr("badList", arg[0], "bad");
            }
        });
    }
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("register", "<Username> <Password>", "Register your account", (arg, player) -> {
            if (!currentLogin.containsKey(player.uuid)) {
                JSONObject login = byteCode.get("login_info");
                if (login == null) {
                    Log.err("mind_db/ does not contain `login_info.cn`");
                    player.sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing login_info.cn");
                    return;
                }
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
                        login.put(arg[0], user);
                        byteCode.save("login_info", login);
                        //end registration
                        JSONObject data = new JSONObject(); //register user data
                        //date joined
                        Date thisDate = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/Y");
                        data.put("date_joined", dateFormat.format(thisDate));
                        data.put("username",arg[0]);
                        data.put("mp",0);
                        data.put("bb",0);
                        data.put("rank",1);
                        data.put("verified", 0);
                        //create user data and add time joined
                        byteCode.make(user.getString("dataID"), data);
                        //finishing off
                        player.sendMessage("[lime]Account created Successfully!");
                        player.sendMessage("[sky]Wait [gold]10 minutes[] or get []Verified []to have full access to the server. (See Discord)"+ (settings.getString("discord_text")) );
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
                JSONObject login = byteCode.get("login_info");
                if (login == null) {
                    Log.err("mind_db/ does not contain `login_info.cn`");
                    player.sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing login_info.cn");
                    return;
                }
                if (login.has(arg[0])) {
                    JSONObject user = login.getJSONObject(arg[0]);
                    if (user.get("password").equals(arg[1])) {
                        if (user.has("rank") && user.getInt("rank") == 0) {
                            if (user.has("Banned")) {
                                if (user.has("banID") && user.has("banReason")) {
                                    String reason = user.getString("banReason");
                                    String bid = user.getString("banID");
                                    player.getInfo().timesKicked--;
                                    player.con.kick("Your Account is [scarlet]Banned[], and so are you." +
                                            "\nBan Reason: " + reason +
                                            "\nBan ID: " + bid);
                                } else {
                                    player.getInfo().timesKicked--;
                                    player.con.kick("Your Account is [scarlet]Banned[], and so are you.");
                                }
                            } else {
                                player.getInfo().timesKicked--;
                                player.con.kick("Your Account is [scarlet]Banned[], and so are you.");
                            }
                        } else {
                            Main.currentLogin.forEach((k, p) -> {
                                if (p == user.getString("dataID")) {
                                    player.sendMessage("You are already logged in! If this is not you, contact a mindustry Admin");
                                    return;
                                }
                            });
                            currentLogin.put(player.uuid, user.getString("dataID"));
                            player.sendMessage("[lime]Login Successful!");
                            //spawn player
                            JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                            if (data == null) {
                                Log.err("mind_db/ does not contain `"+currentLogin.get(player.uuid)+".cn`");
                                player.sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing account data");
                                return;
                            }
                            if (data.has("verified") && data.getInt("verified") == 1) {
                                player.setTeam(Team.sharded);
                                player.updateRespawning();
                                Call.sendMessage("[accent]"+byteCode.noColors(player.name)+" has connected.");
                                Log.info(byteCode.noColors(player.name)+" : "+player.uuid+" > has connected");
                            } else if (data.has("mp") && data.getInt("mp") >= 15) {
                                player.setTeam(Team.sharded);
                                player.updateRespawning();
                                Call.sendMessage("[accent]"+byteCode.noColors(player.name)+" has connected.");
                                Log.info(byteCode.noColors(player.name)+" : "+player.uuid+" > has connected");
                            } else {
                                if (data.has("mp")) {
                                    int x = 10 - data.getInt("mp");
                                    player.sendMessage("[sky]Get Verified, or wait "+x+" more minutes, to have full access to the server. ");
                                } else {
                                    player.sendMessage("[sky]Get Verified, or wait 10 more minutes, to have full access to the server. ");
                                    byteCode.putInt(currentLogin.get(player.uuid), "mp", 1);
                                }
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
                player.sendMessage("You are already logged in! If this is not you, contact a mindustry Admin");
            }
        });
        handler.<Player>register("key", "<key>", "Enter key to redeem Rank or cake.", (arg, player) -> {
            if (currentLogin.containsKey(player.uuid)) {
                JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                if (data == null) {
                    Log.err("mind_db/ does not contain `"+currentLogin.get(player.uuid)+".cn`");
                    player.sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing Account data");
                    return;
                }
                if (keyList.containsKey(arg[0])) {
                    switch (keyList.get(arg[0]).getAction()) {
                        case "cr":
                            if (data.has("rank") && data.getInt("rank") < Strings.parseInt(keyList.get(arg[0]).getValue())) {
                                byteCode.putInt(currentLogin.get(player.uuid),"rank", Strings.parseInt(keyList.get(arg[0]).getValue()));
                                player.sendMessage("Rank updated to "+ keyList.get(arg[0]).getValue());
                            } else {
                                player.sendMessage("unable to change rank");
                            }
                            keyList.remove(arg[0]);
                            break;
                        case "verify":
                            if (keyList.get(arg[0]).getUsername().equals(data.getString("username"))) {
                                byteCode.putStr(currentLogin.get(player.uuid), "discord_tag", keyList.get(arg[0]).getValue());
                                byteCode.putInt(currentLogin.get(player.uuid), "verified", 1);
                                if (data.getInt("rank") == 1) byteCode.putInt(currentLogin.get(player.uuid), "rank", 2);
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
            } else {
                player.sendMessage("[scarlet]/register or /login to use this command!");
            }
        });
        handler.<Player>register("stats", "Displays your basic info", (arg, player) -> {
            if (currentLogin.containsKey(player.uuid)) {
                if (byteCode.has(currentLogin.get(player.uuid))) {
                    JSONObject data = byteCode.get(currentLogin.get(player.uuid));
                    if (data.has("rank")) player.sendMessage("Rank : " + data.getInt("rank"));
                    if (data.has("bb")) player.sendMessage("Buildings Built : " + data.getInt("bb"));
                    if (data.has("mp")) player.sendMessage("Minutes Played : " + data.getInt("mp"));
                    if (data.has("discord_tag")) player.sendMessage("Discord Tag : " + data.getString("discord_tag"));
                    player.sendMessage("Info about current UUID");
                    player.sendMessage("Times Joined : "+player.getInfo().timesJoined);
                    player.sendMessage("Current ID : "+player.id);
                    player.sendMessage("Name Raw : [[#"+player.color+"]"+byteCode.nameR(player.name));
                    player.sendMessage("xp : " + data.getFloat("xp"));
                    player.sendMessage("lvl : " + data.getInt("lvl"));
                    player.sendMessage("lvl : " + data.getInt("lvl"));
                } else {
                    Call.onKick(player.con,"ERROR - No dataID");
                }
            } else {
                player.sendMessage("[scarlet]/register or /login to use this command!");
            }
        });
        if (api != null) {
            handler.<Player>register("d", "<text...>", "Sends a message to discord.", (args, player) -> {

                if (!settings.has("dchannel_id")) {
                    player.sendMessage("[scarlet]This command is disabled.");
                } else {
                    TextChannel tc = this.getTextChannel(settings.getString("dchannel_id"));
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
                    builder.append("[orange]List of reportable players: \n");
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
                            player.sendMessage(found.name + "[sky] is reported to discord.");
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