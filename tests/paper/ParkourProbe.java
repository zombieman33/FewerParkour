import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.block.BlockBreakEvent;
import java.lang.reflect.Proxy;
import java.util.UUID;
import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.*;
import me.zombieman.dev.fewerparkour.manager.*;
import me.zombieman.dev.fewerparkour.listeners.SelectorListener;
public class ParkourProbe extends JavaPlugin {
 public void onEnable(){Bukkit.getScheduler().runTask(this,()->{try{run();getLogger().info("PARKOUR_PROBE_PASS: create command, selection start/checkpoint, exit, setspawn, persisted YAML, tab completion");}catch(Throwable t){getLogger().log(java.util.logging.Level.SEVERE,"PARKOUR_PROBE_FAIL",t);}finally{Bukkit.shutdown();}});}
 void check(boolean v,String msg){if(!v)throw new AssertionError(msg);}
 void run() throws Exception {
 var p=(FewerParkour)Bukkit.getPluginManager().getPlugin("FewerParkour");check(p!=null&&p.isEnabled(),"enabled");check(new java.io.File(p.getDataFolder(),"parkourData").isDirectory(),"course directory initialized before first create");check(p.getDescription().getDepend().contains("PlaceholderAPI"),"hard dependency declared");
 var w=Bukkit.getWorlds().getFirst();var id=UUID.fromString("00000000-0000-0000-0000-000000002789");
 Player a=(Player)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Player.class},(o,m,args)->switch(m.getName()){
 case "getUniqueId"->id; case "getName"->"ParkourProbe";case "getWorld"->w;case "getLocation"->new Location(w,0,70,0);case "hasPermission","isOp","isOnline"->true;case "hashCode"->id.hashCode();case "equals"->o==args[0];case "toString"->"ParkourProbe";default->{if(m.getReturnType()==boolean.class)yield false;if(m.getReturnType()==int.class)yield 0;yield null;}});
 PlayerData.createFile(p,a);var cmd=p.getCommand("parkour");
 cmd.execute(a,"parkour",new String[]{"create","probe","GOLD_BLOCK"});check(ParkourManager.parkourExists(p,"probe"),"course created");
 var listener=new SelectorListener(p);
 for(int x:new int[]{0,4,8}){var b=w.getBlockAt(x,70,0);b.setType(Material.STONE);listener.onBlockBreak(new BlockBreakEvent(b,a));}
 var c=ParkourManager.getParkourConfig(p,"probe");check(c.isSet("start"),"start");check(c.getStringList("checkpoints").size()==2,"checkpoints");
 cmd.execute(a,"parkour",new String[]{"exit"});check(PlayerData.getPlayerDataConfig(p,id).getString("parkourSelectionMode")==null,"exit selection");
 cmd.execute(a,"parkour",new String[]{"setspawn","probe"});
 var saved=org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(ParkourManager.getParkourFile(p,"probe"));check(saved.isSet("start")&&saved.getStringList("checkpoints").size()==2,"disk persistence");
 check(cmd.tabComplete(a,"parkour",new String[]{"cre"}).contains("create"),"completion");
 Bukkit.getPluginManager().disablePlugin(p);check(!p.isEnabled()&&Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"),"parkour disables before dependency");
 }
}
