package kr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.Particles.ParticleEffect;
import kr.Towns.AdminTowns;
import kr.Towns.Chunks;
import kr.Towns.Towns;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.Redstone;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

///TODO Make sure to set concurrent home spell cooldowns (or any/all cooldowns for that matter) to player ymls on Disable, and pick em up on Enable :)
// Delete the storage of cooldowns whenever you can!

public class Kirithia extends JavaPlugin implements Listener {

	Server server;
	Plugin plugin;
	static List<String> allowed = new ArrayList<String>();
	static List<String> usingadminchat = new ArrayList<String>();
	static List<String> creatingmine = new ArrayList<String>();
	static List<String> minenames = new ArrayList<String>();
	static List<String> usingtownchat = new ArrayList<String>();
	static List<String> deletedtowns = new ArrayList<String>();
	static List<String> deletedadmintowns = new ArrayList<String>();
	public HashMap<String, String> settingpoints = new HashMap<String, String>();
	public HashMap<String, Location> minepoints = new HashMap<String, Location>();
	public HashMap<String, List<String>> invited = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> allyreq = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> neutralreq = new HashMap<String, List<String>>();
	public HashMap<String, Vector> cannons = new HashMap<String, Vector>();
	public HashMap<String, Integer> cannonsrep = new HashMap<String, Integer>();
	public HashMap<String, Long> spellcds = new HashMap<String, Long>();
	public HashMap<String, String> playerat = new HashMap<String, String>();
	public HashMap<String, List<String>> justfired = new HashMap<String, List<String>>();
	public HashMap<String, String> cannonpermreq = new HashMap<String, String>();
	public HashMap<String, String> cannonmsg = new HashMap<String, String>();
	public HashMap<String, Integer> plpcmax = new HashMap<String, Integer>();
	static List<String> playerstoupdate = new ArrayList<String>();
	static List<String> nofalldmg = new ArrayList<String>();
	static List<Player> casting = new ArrayList<Player>();
	static List<String> allplayers = new ArrayList<String>();
	static HashMap<String, ItemStack> items = new HashMap<String,ItemStack>();
	AdminTowns at = null;
	Towns town = null;
	Chunks ch = null;
	KPlayer kp = null;

	// TODO save interval for player, town, etc data
	// Bukkit.getScheduler().runTaskAsync
	public void save() {

		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
			public void run() {
				YamlConfiguration kplyaml = YamlConfiguration
						.loadConfiguration(new File(plugin.getDataFolder()
								+ File.separator + "players" + File.separator
								+ "players#.yml"));
				List<String> currentplayers = kplyaml.getStringList("players");
				List<String> ptu = new ArrayList<String>();
				for (String name : playerstoupdate) {
					if (!currentplayers.contains(name)) {
						currentplayers.add(name);

					}
					ptu.add(name);
				}
				kplyaml.set("players", currentplayers);
				try {
					kplyaml.save(new File(plugin.getDataFolder()
							+ File.separator + "players" + File.separator
							+ "players#.yml"));
				} catch (IOException e1) {
				}

				for (String name : playerstoupdate) {
					String lname = name.toLowerCase();
					KPlayer kpl = kp.KP(lname);
					kplyaml = YamlConfiguration.loadConfiguration(new File(
							plugin.getDataFolder() + File.separator + "players"
									+ File.separator + lname + ".yml"));
					kplyaml.set("chunkcoords", null);
					kplyaml.set("name", kpl.getName());
					kplyaml.set("chunks", kpl.getChunkCoords());
					if (kpl.getChunkCoords() != null) {
						for (String coords : kpl.getChunkCoords()) {
							kplyaml.set("chunkcoords." + coords + ".friends",
									kpl.getChunkFriends(coords));
						}
					}
					kplyaml.set("defaultperms", kpl.getDefaultPerms());
					kplyaml.set("friendperms", kpl.getFriendPerms());
					kplyaml.set("reputation", kpl.getRep());
					kplyaml.set("level", kpl.getLevel());
					kplyaml.set("melee", kpl.getMelee());
					kplyaml.set("archery", kpl.getArchery());
					kplyaml.set("defence", kpl.getDefence());
					kplyaml.set("xp", kpl.getXP());
					kplyaml.set("maxchunks", kpl.getMaxChunks());
					kplyaml.set("lastlogin", kpl.getLastLogin());
					if (!kpl.isOnline()) {
						ptu.remove(kpl.getName());

					}
					// private List<String> chunkcoords = new
					// LinkedList<String>();
					// private HashMap<String, List<String>> chunkfriends = new
					// HashMap<String, List<String>>();
					// private List<String> defaultperms = new
					// LinkedList<String>();
					// private List<String> friendperms = new
					// LinkedList<String>();
					// int reputation = 0;
					// int level = 1;
					// int melee = 1;
					// //int defence = 1;
					// int xp = 0;
					try {
						kplyaml.save(new File(plugin.getDataFolder()
								+ File.separator + "players" + File.separator
								+ lname + ".yml"));
					} catch (IOException e1) {
					}
				}
				playerstoupdate = ptu;

				YamlConfiguration yaml = YamlConfiguration
						.loadConfiguration(new File(plugin.getDataFolder()
								+ File.separator + "adminchat.yml"));
				yaml.set("Admins-in-chat", allowed);
				try {
					yaml.save(new File(plugin.getDataFolder() + File.separator
							+ "adminchat.yml"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				YamlConfiguration admintownyml = YamlConfiguration
						.loadConfiguration(new File(plugin.getDataFolder()
								+ File.separator + "admintowns"
								+ File.separator + "admintowns#.yml"));
				admintownyml.set("admintowns", at.towns);
				try {
					admintownyml.save(new File(plugin.getDataFolder()
							+ File.separator + "admintowns" + File.separator
							+ "admintowns#.yml"));
				} catch (IOException e) {

					e.printStackTrace();
				}

				for (String tname : at.towns) {
					AdminTowns a = at.getTownbyName(tname);
					admintownyml = YamlConfiguration
							.loadConfiguration(new File(plugin.getDataFolder()
									+ File.separator + "admintowns"
									+ File.separator + a.getName() + ".yml"));
					admintownyml.set("Name", a.getName());
					admintownyml.set("DeniedPerms", a.getDeniedPerms());
					admintownyml.set("DeniedPermMsg", a.getNoPermMessage());
					admintownyml.set("WelcomeMsg", a.getMessage());
					admintownyml.set("Chunks", a.getChunks());
					admintownyml.set("Silent", a.isSilent());
					try {
						admintownyml.save(new File(plugin.getDataFolder()
								+ File.separator + "admintowns"
								+ File.separator + a.getName() + ".yml"));
					} catch (IOException e) {

						e.printStackTrace();
					}
				}

				YamlConfiguration cannonyml = YamlConfiguration
						.loadConfiguration(new File(plugin.getDataFolder()
								+ File.separator + "cannons" + File.separator
								+ "cannons.yml"));
				int n = 0;
				for (String loc : cannons.keySet()) {
					n++;
					cannonyml.set(n + ".location", loc);
					cannonyml.set(n + ".vector", cannons.get(loc).getX() + "x"
							+ cannons.get(loc).getY() + "y"
							+ cannons.get(loc).getZ() + "z");
					cannonyml.set(n + ".repeat", cannonsrep.get(loc));
					if (cannonpermreq.containsKey(loc)) {
						cannonyml.set(n + ".permission", cannonpermreq.get(loc));
					}
					if (cannonmsg.containsKey(loc)) {
						cannonyml.set(n + ".message", cannonmsg.get(loc));
					}

				}
				cannonyml.set("no", n);
				try {
					cannonyml.save(new File(plugin.getDataFolder()
							+ File.separator + "cannons" + File.separator
							+ "cannons.yml"));
				} catch (Exception e) {
				}
				YamlConfiguration townyaml = YamlConfiguration
						.loadConfiguration(new File(plugin.getDataFolder()
								+ File.separator + "towns" + File.separator
								+ "towns#.yml"));
				townyaml.set("towns", town.towns);
				try {
					townyaml.save(new File(plugin.getDataFolder()
							+ File.separator + "towns" + File.separator
							+ "towns#.yml"));
				} catch (IOException e) {

					e.printStackTrace();
				}
				for (String nm : deletedtowns) {
					new File(plugin.getDataFolder() + File.separator + "towns"
							+ File.separator + nm + ".yml").delete();
				}
				for (String nm : deletedadmintowns) {
					new File(plugin.getDataFolder() + File.separator
							+ "admintowns" + File.separator + nm + ".yml")
							.delete();
				}
				Towns t = null;
				for (String townname : town.towns) {
					t = town.getTown(townname);
					townyaml = YamlConfiguration.loadConfiguration(new File(
							plugin.getDataFolder() + File.separator + "towns"
									+ File.separator + townname + ".yml"));
					townyaml.set("Name", t.getName());
					townyaml.set("Owner", t.getOwner());
					townyaml.set("MOTD", t.getMOTD());
					townyaml.set("Level", t.getLevel());
					townyaml.set("TotalXp", t.getTotalXp());
					townyaml.set("Open", t.isOpen());
					townyaml.set("DefaultPerms", t.getDefaultPerms());
					townyaml.set("ModPerms", t.getModPerms());
					townyaml.set("AdminPerms", t.getAdminPerms());
					townyaml.set("ChunkDefaultPerms", t.getChunkDefaultPerms());
					townyaml.set("ChunkMemberPerms", t.getChunkMemberPerms());
					townyaml.set("ChunkOwnerPerms", t.getChunkOwnerPerms());
					townyaml.set("Members", t.getMembers());
					townyaml.set("Moderators", t.getModerators());
					townyaml.set("Admins", t.getAdmins());
					townyaml.set("Allies", t.getAllies());
					townyaml.set("Enemies", t.getEnemies());
					townyaml.set("Chunks", t.getChunks());
					townyaml.set("Homes", t.getHomeNames());
					townyaml.set("tc", null);
					for (String name : t.getHomeNames()) {
						townyaml.set("hl." + name,
								locationToString(t.getHomeLoc(name)));
					}
					for (String coords : t.getChunks()) {

						townyaml.set("tc." + coords + ".members",
								t.getChunkMembers(coords));
						townyaml.set("tc." + coords + ".owners",
								t.getChunkOwners(coords));
					}
					try {
						townyaml.save(new File(plugin.getDataFolder()
								+ File.separator + "towns" + File.separator
								+ townname + ".yml"));
					} catch (IOException e) {

						e.printStackTrace();
					}

				}

				save();
			}

		}, 200);

	}

	public void onEnable() {
		town = new Towns(this);
		at = new AdminTowns(this);
		ch = new Chunks(this);
		kp = new KPlayer(this);
		plugin = this;

		//initialise starter perm items.
		ItemStack is = new ItemStack(Material.FLINT_AND_STEEL);
		ItemMeta im = is.getItemMeta();
		//TODO real mana crystal ;)
		//im.setDisplayName("§bMana Crystal");
		//im.setLore(Arrays.asList("§bMana: §a10/100",
		//		"§7Leveling up and gaining Wisdom",
		//		"§7increases your total Mana."));
		//is.setItemMeta(im);
		//items.put("manacrystal", is);
		im.setDisplayName("§4Coming soon!");
		im.setLore(Arrays.asList("§7Magic will be",
				"§7introduced soon!"));
		is.setItemMeta(im);
		items.put("manacrystal", is);
		
		is = new ItemStack(Material.BOOK);
		im.setDisplayName("§dMy Character");
		im.setLore(Arrays.asList("§7Right click to use.",
						"§7Contains character information",
						"§7and perk and spell selection."));
		is.setItemMeta(im);
		items.put("mycharacter", is);
		
		is = new ItemStack(Material.WALL_SIGN);
		im.setDisplayName("§4Coming soon!");
		im.setLore(Arrays.asList("§7A feature is under",
				"§7development. Expect to see",
				"§7an update soon!"));
		is.setItemMeta(im);
		items.put("comingsoon", is);
		
		
		YamlConfiguration mines = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "mines.yml"));
		if (mines.get("mines")!=null){
			for (String n : mines.getStringList("mines")){
				minenames.add(n);
				minepoints.put(n+"1", locationFromString(mines.getString(n+".1")));
				minepoints.put(n+"2", locationFromString(mines.getString(n+".2")));
			}
		}
		
		YamlConfiguration kpyaml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "players" + File.separator
						+ "players#.yml"));
		List<String> players = kpyaml.getStringList("players");
		allplayers = players;

		for (String name : players) {
			kpyaml = YamlConfiguration.loadConfiguration(new File(plugin
					.getDataFolder()
					+ File.separator
					+ "players"
					+ File.separator + name + ".yml"));
			HashMap<String, List<String>> chunkhash = new HashMap<String, List<String>>();
			for (String coords : kpyaml.getStringList("chunks")) {
				ch.addChunk(coords, "player");
				chunkhash.put(
						coords,
						kpyaml.getStringList("chunkcoords." + coords
								+ ".friends"));
			}
			KPlayer kpl = kp.setup(kpyaml.getString("name"),
					kpyaml.getStringList("chunks"),
					(HashMap<String, List<String>>) chunkhash,
					kpyaml.getStringList("defaultperms"),
					kpyaml.getStringList("friendperms"),
					kpyaml.getInt("reputation"), kpyaml.getInt("level"),
					kpyaml.getInt("melee"), kpyaml.getInt("archery"),
					kpyaml.getInt("defence"), kpyaml.getInt("xp"),
					kpyaml.getInt("maxchunks"));
		}

		for (Player pl : Bukkit.getOnlinePlayers()) {
			kp.KP(pl.getName()).setOnline(true);
			playerstoupdate.add(pl.getName());
		}

		YamlConfiguration cannonyml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "cannons" + File.separator
						+ "cannons.yml"));
		int i = 0;
		String loc = "";
		while (i < cannonyml.getInt("no")) {

			i++;
			loc = cannonyml.getString(i + ".location");
			String vec = cannonyml.getString(i + ".vector");
			Vector v = new Vector(Double.parseDouble(vec.substring(0,
					vec.indexOf("x"))), Double.parseDouble(vec.substring(
					vec.indexOf("x") + 1, vec.indexOf("y"))),
					Double.parseDouble(vec.substring(vec.indexOf("y") + 1,
							vec.indexOf("z"))));
			cannons.put(loc, v);
			cannonsrep.put(loc, cannonyml.getInt(i + ".repeat"));
			if (cannonyml.contains(i + ".permission")) {
				cannonpermreq.put(loc, cannonyml.getString(i + ".permission"));
			}
			if (cannonyml.contains(i + ".message")) {
				cannonmsg.put(loc, cannonyml.getString(i + ".message"));
			}
		}
		YamlConfiguration admintownyml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "admintowns" + File.separator
						+ "admintowns#.yml"));
		try {
			for (Object townname : admintownyml.getList("admintowns")) {
				admintownyml = YamlConfiguration.loadConfiguration(new File(
						plugin.getDataFolder() + File.separator + "admintowns"
								+ File.separator + townname + ".yml"));
				at.setupAT(new AdminTowns(admintownyml.getString("Name"),
						admintownyml.getStringList("Chunks"), admintownyml
								.getString("WelcomeMsg"), admintownyml
								.getString("DeniedPermMsg"), admintownyml
								.getStringList("DeniedPerms"), admintownyml
								.getBoolean("Silent")));
				for (String coords : admintownyml.getStringList("Chunks")) {
					ch.addChunk(coords, "admin");
				}

			}
		} catch (Exception e) {

		}
		YamlConfiguration townyaml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "towns" + File.separator
						+ "towns#.yml"));
		for (Object townname : townyaml.getList("towns")) {
			YamlConfiguration townsep = YamlConfiguration
					.loadConfiguration(new File(plugin.getDataFolder()
							+ File.separator + "towns" + File.separator
							+ townname + ".yml"));
			HashMap<String, LinkedList<String>> chunkmembers = new HashMap<String, LinkedList<String>>();
			HashMap<String, LinkedList<String>> chunkowners = new HashMap<String, LinkedList<String>>();
			for (String coords : townsep.getStringList("Chunks")) {
				LinkedList<String> cm = new LinkedList<String>();
				LinkedList<String> co = new LinkedList<String>();
				cm.addAll(townsep.getStringList("tc." + coords + ".members"));
				co.addAll(townsep.getStringList("tc." + coords + ".owners"));
				chunkmembers.put(coords, cm);
				chunkowners.put(coords, co);
				ch.addChunk(coords, "town");
			}
			HashMap<String, Location> homes = new HashMap<String, Location>();
			for (String home : townsep.getStringList("Homes")) {
				homes.put(home,
						locationFromString(townsep.getString("hl." + home)));
			}
			town.setupTown(new Towns(townsep.getString("Name"), townsep
					.getString("Owner"), townsep.getString("MOTD"), townsep
					.getString("ModPerms"), townsep.getString("AdminPerms"),
					townsep.getString("DefaultPerms"), townsep
							.getString("ChunkMemberPerms"), townsep
							.getString("ChunkOwnerPerms"), townsep
							.getString("ChunkDefaultPerms"), townsep
							.getDouble("TotalXp"), townsep.getDouble("Level"),
					townsep.getBoolean("Open"),
					townsep.getStringList("Chunks"), townsep
							.getStringList("Allies"), townsep
							.getStringList("Enemies"), townsep
							.getStringList("Members"), townsep
							.getStringList("Moderators"), townsep
							.getStringList("Admins"), chunkmembers,
					chunkowners, homes));
			allyreq.put(townsep.getString("Name"), new ArrayList<String>());
			neutralreq.put(townsep.getString("Name"), new ArrayList<String>());
		}
		server = getServer();

		server.getPluginManager().registerEvents(this, this);
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(
				plugin.getDataFolder() + File.separator + "adminchat.yml"));
		List<String> blug = yaml.getStringList("Admins-in-chat");
		if (blug != null) {
			for (String name : blug) {
				allowed.add(name);
			}
		}
		save();

	}

	public void onDisable() {
		new File(plugin.getDataFolder()
				+ File.separator + "mines.yml").delete();
		YamlConfiguration mines = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "mines.yml"));
		mines.set("mines", minenames);
		for (String n: minenames){
			mines.set(n+".1", locationToString(minepoints.get(n+"1")));
			mines.set(n+".2", locationToString(minepoints.get(n+"2")));
		}
		try {
			mines.save(new File(plugin.getDataFolder() + File.separator + "mines.yml"));
		} catch (IOException e1) {
		}
		
		
		YamlConfiguration kplyaml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "players" + File.separator
						+ "players#.yml"));
		List<String> currentplayers = kplyaml.getStringList("players");
		for (String name : playerstoupdate) {
			if (!currentplayers.contains(name)) {
				currentplayers.add(name);
			}
		}
		kplyaml.set("players", currentplayers);
		try {
			kplyaml.save(new File(plugin.getDataFolder() + File.separator
					+ "players" + File.separator + "players#.yml"));
		} catch (IOException e1) {
		}

		for (String name : playerstoupdate) {

			String lname = name.toLowerCase();
			KPlayer kpl = kp.KP(lname);
			kplyaml = YamlConfiguration.loadConfiguration(new File(plugin
					.getDataFolder()
					+ File.separator
					+ "players"
					+ File.separator + lname + ".yml"));
			kplyaml.set("chunkcoords", null);
			kplyaml.set("name", kpl.getName());
			kplyaml.set("chunks", kpl.getChunkCoords());
			if (kpl.getChunkCoords() != null) {
				for (String coords : kpl.getChunkCoords()) {
					kplyaml.set("chunkcoords." + coords + ".friends",
							kpl.getChunkFriends(coords));
				}
			}

			kplyaml.set("defaultperms", kpl.getDefaultPerms());
			kplyaml.set("friendperms", kpl.getFriendPerms());
			kplyaml.set("reputation", kpl.getRep());
			kplyaml.set("level", kpl.getLevel());
			kplyaml.set("melee", kpl.getMelee());
			kplyaml.set("archery", kpl.getArchery());
			kplyaml.set("defence", kpl.getDefence());
			kplyaml.set("xp", kpl.getXP());
			kplyaml.set("maxchunks", kpl.getMaxChunks());
			kplyaml.set("lastlogin", kpl.getLastLogin());
			// private List<String> chunkcoords = new
			// LinkedList<String>();
			// private HashMap<String, List<String>> chunkfriends = new
			// HashMap<String, List<String>>();
			// private List<String> defaultperms = new
			// LinkedList<String>();
			// private List<String> friendperms = new
			// LinkedList<String>();
			// int reputation = 0;
			// int level = 1;
			// int melee = 1;
			// //int defence = 1;
			// int xp = 0;
			try {
				kplyaml.save(new File(plugin.getDataFolder() + File.separator
						+ "players" + File.separator + lname + ".yml"));
			} catch (IOException e1) {
			}
		}

		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(
				plugin.getDataFolder() + File.separator + "adminchat.yml"));
		yaml.set("Admins-in-chat", allowed);
		try {
			yaml.save(new File(plugin.getDataFolder() + File.separator
					+ "adminchat.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		YamlConfiguration admintownyml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "admintowns" + File.separator
						+ "admintowns#.yml"));
		admintownyml.set("admintowns", at.towns);
		try {
			admintownyml.save(new File(plugin.getDataFolder() + File.separator
					+ "admintowns" + File.separator + "admintowns#.yml"));
		} catch (IOException e) {

			e.printStackTrace();
		}

		for (String tname : at.towns) {
			AdminTowns a = at.getTownbyName(tname);
			admintownyml = YamlConfiguration.loadConfiguration(new File(plugin
					.getDataFolder()
					+ File.separator
					+ "admintowns"
					+ File.separator + a.getName() + ".yml"));
			admintownyml.set("Name", a.getName());
			admintownyml.set("DeniedPerms", a.getDeniedPerms());
			admintownyml.set("DeniedPermMsg", a.getNoPermMessage());
			admintownyml.set("WelcomeMsg", a.getMessage());
			admintownyml.set("Chunks", a.getChunks());
			admintownyml.set("Silent", a.isSilent());
			try {
				admintownyml.save(new File(plugin.getDataFolder()
						+ File.separator + "admintowns" + File.separator
						+ a.getName() + ".yml"));
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		YamlConfiguration cannonyml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "cannons" + File.separator
						+ "cannons.yml"));
		int n = 0;
		for (String loc : cannons.keySet()) {
			n++;
			cannonyml.set(n + ".location", loc);
			cannonyml.set(n + ".vector", cannons.get(loc).getX() + "x"
					+ cannons.get(loc).getY() + "y" + cannons.get(loc).getZ()
					+ "z");
			cannonyml.set(n + ".repeat", cannonsrep.get(loc));
			if (cannonpermreq.containsKey(loc)) {
				cannonyml.set(n + ".permission", cannonpermreq.get(loc));
			}
			if (cannonmsg.containsKey(loc)) {
				cannonyml.set(n + ".message", cannonmsg.get(loc));
			}

		}
		cannonyml.set("no", n);
		try {
			cannonyml.save(new File(plugin.getDataFolder() + File.separator
					+ "cannons" + File.separator + "cannons.yml"));
		} catch (Exception e) {
		}
		YamlConfiguration townyaml = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "towns" + File.separator
						+ "towns#.yml"));
		townyaml.set("towns", town.towns);
		try {
			townyaml.save(new File(plugin.getDataFolder() + File.separator
					+ "towns" + File.separator + "towns#.yml"));
		} catch (IOException e) {

			e.printStackTrace();
		}
		for (String nm : deletedtowns) {
			new File(plugin.getDataFolder() + File.separator + "towns"
					+ File.separator + nm + ".yml").delete();
		}
		for (String nm : deletedadmintowns) {
			new File(plugin.getDataFolder() + File.separator + "admintowns"
					+ File.separator + nm + ".yml").delete();
		}
		Towns t = null;
		for (String townname : town.towns) {
			t = town.getTown(townname);
			townyaml = YamlConfiguration.loadConfiguration(new File(plugin
					.getDataFolder()
					+ File.separator
					+ "towns"
					+ File.separator + townname + ".yml"));
			townyaml.set("Name", t.getName());
			townyaml.set("Owner", t.getOwner());
			townyaml.set("MOTD", t.getMOTD());
			townyaml.set("Level", t.getLevel());
			townyaml.set("TotalXp", t.getTotalXp());
			townyaml.set("Open", t.isOpen());
			townyaml.set("DefaultPerms", t.getDefaultPerms());
			townyaml.set("ModPerms", t.getModPerms());
			townyaml.set("AdminPerms", t.getAdminPerms());
			townyaml.set("ChunkDefaultPerms", t.getChunkDefaultPerms());
			townyaml.set("ChunkMemberPerms", t.getChunkMemberPerms());
			townyaml.set("ChunkOwnerPerms", t.getChunkOwnerPerms());
			townyaml.set("Members", t.getMembers());
			townyaml.set("Moderators", t.getModerators());
			townyaml.set("Admins", t.getAdmins());
			townyaml.set("Allies", t.getAllies());
			townyaml.set("Enemies", t.getEnemies());
			townyaml.set("Chunks", t.getChunks());
			townyaml.set("Homes", t.getHomeNames());
			townyaml.set("tc", null);
			for (String name : t.getHomeNames()) {
				townyaml.set("hl." + name, locationToString(t.getHomeLoc(name)));
			}
			for (String coords : t.getChunks()) {

				townyaml.set("tc." + coords + ".members",
						t.getChunkMembers(coords));
				townyaml.set("tc." + coords + ".owners",
						t.getChunkOwners(coords));
			}
			try {
				townyaml.save(new File(plugin.getDataFolder() + File.separator
						+ "towns" + File.separator + townname + ".yml"));
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public int getMaxPrivates(Player pl) {
		int o = 3;
		if (plpcmax.containsKey(pl.getName())) {
			o = plpcmax.get(pl.getName());
		}
		return o;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player p = null;
		KPlayer kpl = null;
		if (sender instanceof Player) {
			p = (Player) sender;
			kpl = kp.KP(p.getName());
		} else {
			sender.sendMessage("Must be player!");
			return true;
		}
		if (label.equalsIgnoreCase("chunk") || label.equalsIgnoreCase("chunks")) {
			if (sender.hasPermission("kirithia.chunk")) {
				if (args.length == 0) {
					p.sendMessage("§7-----------------------------------------------------");
					p.sendMessage("§7---------------§6Kirithian Chunks Help§7---------------");
					p.sendMessage("§7-----------------------------------------------------");
					p.sendMessage("  §6Claim private chunks to build in or store items safely!");
					p.sendMessage("- §e/chunk claim: §7Claims the chunk you are in.");
					p.sendMessage("- §e/chunk unclaim: §7Unclaims the chunk you are in.");
					p.sendMessage("- §e/chunk addFriend <name>: §7Adds a player to a chunk.");
					p.sendMessage("- §e/chunk removeFriend <name>: §7Removes a player from chunk.");
					p.sendMessage("- §e/chunk info: §7Information on chunk and chunk friends.");
					p.sendMessage("- §e/chunk perm: §7Chunk permissions commands.");
					return false;

				}
				if (args[0].equalsIgnoreCase("claim")) {
					Chunk c = p.getLocation().getChunk();
					if (ch.getType(c.getChunkSnapshot()) != "wild") {
						if (kp.getPlayerofChunk(p.getLocation().getChunk())
								.equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou already own this chunk!");
						} else {
							p.sendMessage("§cThis chunk is already owned. Check §e/chunk info");
						}
						return false;
					}
					if (!kpl.hasClaimedAny()
							&& getCooldownRemaining("firstchunk", p.getName()) > 0) {
						p.sendMessage("§cYou can not claim a home chunk for another "
								+ toHourMinSec(getCooldownRemaining(
										"firstchunk", p.getName())));
						return false;
					}

					if (!kpl.canClaimAnother()) {
						p.sendMessage("§cYou have already claimed your maximum of "
								+ kpl.getMaxChunks()
								+ " chunks. §bkirithia.com/shop §cto increase your limit!");
						return false;
					}
					int X = c.getX();
					int Z = c.getZ();
					if (kpl.hasClaimedAny()
							&& !kp.getPlayerofChunk(
									c.getWorld().getChunkAt(X - 1, Z)).equals(
									p.getName())
							&& !kp.getPlayerofChunk(
									c.getWorld().getChunkAt(X + 1, Z)).equals(
									p.getName())
							&& !kp.getPlayerofChunk(
									c.getWorld().getChunkAt(X, Z - 1)).equals(
									p.getName())
							&& !kp.getPlayerofChunk(
									c.getWorld().getChunkAt(X, Z + 1)).equals(
									p.getName())) {
						// no adjacent chunks claimed and this isn't first claim
						p.sendMessage("§cYou can only claim chunks that are next to your other chunks!");
						return false;
					}
					setCooldown("firstchunk", p.getName(), 10800);
					ch.addChunk(c.getChunkSnapshot(), "player");

					// TODO decide about max private system - preferably create
					// a console setMax command for donors
					kp.addChunk(c, kpl);

					p.sendMessage("§aYou claimed the chunk you are standing in! Type §e/chunk §afor commands!");
					return false;
				}

				if (args[0].equalsIgnoreCase("unclaim")) {
					Chunk c = p.getLocation().getChunk();

					if (ch.getType(c.getChunkSnapshot()) != "player") {
						p.sendMessage("§cYou do not own this chunk! Check §e/chunk info");
						return false;
					} else if (!kp.getPlayerofChunk(c).equals(p.getName())) {
						p.sendMessage("§cYou do not own this chunk! Check §e/chunk info");
						return false;
					}

					if (args.length > 1) {
						if (!args[1].equalsIgnoreCase("confirm")) {
							p.sendMessage("§cDid you mean §e/chunk unclaim§c?");
							return false;
						}
					}
					if (args.length == 1) {

						if (kpl.getNumberofChunks() == 1
								&& getCooldownRemaining("firstchunk",
										p.getName()) > 0) {
							p.sendMessage("§cYou will not be able to claim a home chunk for "
									+ toHourMinSec(getCooldownRemaining(
											"firstchunk", p.getName())));
							p.sendMessage("§cType §e/chunk unclaim confirm §cto continue with the unclaim!");
							return false;
						}
					} else {
						kp.removeChunk(c, kpl);
						ch.removeChunk(c.getChunkSnapshot());
						p.sendMessage("§aChunk successfully unclaimed!");
						return false;
					}
					kp.removeChunk(c, kpl);
					ch.removeChunk(c.getChunkSnapshot());
					p.sendMessage("§aChunk successfully unclaimed!");
					return false;

				}

				if (args[0].equalsIgnoreCase("addfriend")) {
					Chunk c = p.getLocation().getChunk();

					if (ch.getType(c.getChunkSnapshot()) != "player") {
						p.sendMessage("§cYou do not own this chunk! You can only add friends to a chunk you own.");
						return false;
					} else if (!kp.getPlayerofChunk(c).equals(p.getName())) {
						p.sendMessage("§cYou do not own this chunk! You can only add friends to a chunk you own.");
						return false;
					}
					if (args.length < 2) {
						p.sendMessage("§cUse like this! §e/chunk addFriend <playername>");
						return false;
					}
					Player add = Bukkit.getPlayer(args[1]);
					if (add != null) {
						if (!kpl.isChunkFriend(c, add.getName())) {
							if (add.getName().equals(p.getName())) {
								p.sendMessage("§cYou can not add yourself as a friend to this chunk!");
								return false;
							}
							kpl.addChunkFriend(c, add.getName());
							p.sendMessage("§aAdded §e" + add.getName()
									+ " §ato the chunk you are standing in!");
							return false;
						} else {
							p.sendMessage("§e"
									+ add.getName()
									+ " §chas already been added to this chunk!");
							return false;
						}

					} else {
						p.sendMessage("§cThis player is not online!");
						p.sendMessage("§cIf you wish to add an offline player, use §e/chunk addOfflineFriend <playername>");
						return false;
					}

				}
				if (args[0].equalsIgnoreCase("addofflinefriend")) {
					Chunk c = p.getLocation().getChunk();
					if (ch.getType(c.getChunkSnapshot()) != "player") {
						p.sendMessage("§cYou do not own this chunk! You can only add friends to a chunk you own.");
						return false;
					} else if (!kp.getPlayerofChunk(c).equals(p.getName())) {
						p.sendMessage("§cYou do not own this chunk! You can only add friends to a chunk you own.");
						return false;
					}
					if (args.length < 2) {
						p.sendMessage("§cUse like this! §e/chunk addOfflineFriend <playername>");
						return false;
					}
					if (!kpl.isChunkFriend(c, args[1])) {
						if (args[1].equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou can not add yourself as a friend to this chunk!");
							return false;
						}
						kpl.addChunkFriend(c, args[1]);
						p.sendMessage("§aAdded §e" + args[1]
								+ " §ato the chunk you are standing in!");
						return false;
					} else {
						p.sendMessage("§e" + args[1]
								+ " §chas already been added to this chunk!");
						return false;
					}
				}

				if (args[0].equalsIgnoreCase("removefriend")) {
					Chunk c = p.getLocation().getChunk();
					if (ch.getType(c.getChunkSnapshot()) != "player") {
						p.sendMessage("§cYou do not own this chunk! You can remove friends from a chunk you own.");
						return false;
					} else if (!kp.getPlayerofChunk(c).equals(p.getName())) {
						p.sendMessage("§cYou do not own this chunk! You can remove friends from a chunk you own.");
						return false;
					}
					if (args.length < 2) {
						p.sendMessage("§cUse like this! §e/chunk removeFriend <playername>");
						return false;
					}
					if (kpl.isChunkFriend(c, args[1])) {
						kpl.removeChunkFriend(c, args[1]);
						p.sendMessage("§aSuccessfully removed §e" + args[1]
								+ " §afrom the chunk you are standing in!");
						return false;
					} else {
						p.sendMessage("§e" + args[1]
								+ " §chas not been added to this chunk!");
						return false;
					}
				}

				if (args[0].equalsIgnoreCase("info")) {
					Chunk chunk = p.getLocation().getChunk();
					ChunkSnapshot c = chunk.getChunkSnapshot();
					if (ch.getType(c).equals("wild")) {
						p.sendMessage("§eThis chunk is §aunclaimed§e. You are in §7The Wild§e!");
						return false;
					}
					if (ch.getType(c).equals("admin")) {
						AdminTowns att = at.getTownbyName(c.getX() + ","
								+ c.getZ());
						if (att.getMessage().equalsIgnoreCase("none")) {
							p.sendMessage("§eYou are in an §cAdmin Town§e!");
							return false;
						}
						p.sendMessage("§eYou are in an §cAdmin Town§e!");
						p.sendMessage(att.getMessage());
						return false;
					}
					if (ch.getType(c).equals("town")) {
						Towns tt = town.getTownofChunk(c);
						p.sendMessage("§eYou are in the territory of the town §b"
								+ tt.getName() + "§e!");
						return false;
					}
					if (ch.getType(c).equals("player")) {
						String name = kp.getPlayerofChunk(c);
						if (name.equals(p.getName())) {
							p.sendMessage("§eYou own this chunk!");
							String friends = "§cnone";
							if (!kpl.getChunkFriends(chunk).isEmpty()) {
								friends = kpl.getChunkFriends(chunk).toString();
								friends = friends.replace("[", "");
								friends = friends.replace("]", "");
							}
							p.sendMessage("§eChunk Friends: §a" + friends);
							return false;
						}
						p.sendMessage("§eYou are in §d" + name
								+ "§e's Private Chunk!");
						return false;
					}
				}

				if (args[0].equalsIgnoreCase("perm")
						|| args[0].equalsIgnoreCase("perms")) {
					if (args.length == 1) {
						p.sendMessage("§7-----------------------------------------------------");
						p.sendMessage("§7--------------§6Private Chunk Permissions§7--------------");
						p.sendMessage("§7-----------------------------------------------------");
						p.sendMessage("- §e/chunk perm §ainfo: §7Information about each permission.");
						p.sendMessage("- §e/chunk perm §fdefault: §7Lists the default permissions.");
						p.sendMessage("- §e/chunk perm §6friends: §7Lists the member permissions.");
						p.sendMessage("§7-----------------------------------------------------");
						return false;
					}
					if (!args[1].equalsIgnoreCase("info")
							&& !args[1].equalsIgnoreCase("default")
							&& !args[1].equalsIgnoreCase("friend")
							&& !args[1].equalsIgnoreCase("friends")
							&& !args[1].equalsIgnoreCase("freinds")
							&& !args[1].equalsIgnoreCase("freind")) {
						p.sendMessage("§7-----------------------------------------------------");
						p.sendMessage("§7--------------§6Private Chunk Permissions§7--------------");
						p.sendMessage("§7-----------------------------------------------------");
						p.sendMessage("- §e/chunk perm §ainfo: §7Information about each permission.");
						p.sendMessage("- §e/chunk perm §fdefault: §7Lists the default permissions.");
						p.sendMessage("- §e/chunk perm §6friends: §7Lists the member permissions.");
						p.sendMessage("§7-----------------------------------------------------");
						return false;
					}
					if (args[1].equalsIgnoreCase("info")) {
						p.sendMessage("§7-----------------------------------------------------");
						p.sendMessage("- §buse - §7Allows the use of levers, doors etc...");
						p.sendMessage("- §bchests - §7Allows the use of chests, furnaces, hoppers etc...");
						p.sendMessage("- §bbuild - §7Allows placing and removing of blocks.");
						p.sendMessage("§7-----------------------------------------------------");
						return false;
					}
					if (args[1].equalsIgnoreCase("default")) {
						if (args.length == 2) {
							List<String> perl = kpl.getDefaultPerms();
							String msg = "§cnone";
							if (!perl.isEmpty()) {
								msg = "§a" + perl.toString();
								msg = msg.replace("[", "");
								msg = msg.replace("]", "");
							}

							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("§fDefault §bPermissions: " + msg);
							p.sendMessage("§fTo set: §e/chunk perm default <permission> allow/deny");
							p.sendMessage("§7-----------------------------------------------------");
							return false;
						}
						if (!args[2].equalsIgnoreCase("use")
								&& !args[2].equalsIgnoreCase("build")
								&& !args[2].equalsIgnoreCase("chest")
								&& !args[2].equalsIgnoreCase("chests")) {
							p.sendMessage("§e"
									+ args[2]
									+ " §cis not a valid permission! §euse, chests, build");
							return false;
						}
						String perm = args[2].toLowerCase();
						if (perm.equals("chest")) {
							perm = "chests";
						}
						if (args.length < 4) {
							p.sendMessage("§e/chunk perm default " + args[2]
									+ " allow/deny");
							p.sendMessage("§aAllow §for §cdeny§f?");
							return false;
						}
						boolean yesno = false;
						String msgbool = "§cdenied";
						if (!args[3].equalsIgnoreCase("allow")
								&& !args[3].equalsIgnoreCase("deny")) {
							p.sendMessage("§e/chunk perm default " + args[2]
									+ " allow/deny");
							p.sendMessage("§aAllow §for §cdeny§f?");
							return false;
						}
						if (args[3].equalsIgnoreCase("allow")) {
							yesno = true;
							msgbool = "§aallowed";
						}
						kpl.setDefaultPerm(perm, yesno);

						p.sendMessage("§aThe permission §b" + perm
								+ " §ais now " + msgbool
								+ " §afor non-friends (§fDefaults§a).");
						return false;
					}

					if (args[1].equalsIgnoreCase("friend")
							|| args[1].equalsIgnoreCase("friends")
							|| args[1].equalsIgnoreCase("freinds")
							|| args[1].equalsIgnoreCase("freind")) {
						if (args.length == 2) {
							List<String> perl = kpl.getFriendPerms();
							String msg = "§cnone";
							if (!perl.isEmpty()) {
								msg = "§a" + perl.toString();
								msg = msg.replace("[", "");
								msg = msg.replace("]", "");
							}

							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("§6Friend §bPermissions: " + msg);
							p.sendMessage("§fTo set: §e/chunk perm friends <permission> allow/deny");
							p.sendMessage("§7-----------------------------------------------------");
							return false;
						}
						if (!args[2].equalsIgnoreCase("use")
								&& !args[2].equalsIgnoreCase("build")
								&& !args[2].equalsIgnoreCase("chest")
								&& !args[2].equalsIgnoreCase("chests")) {
							p.sendMessage("§e"
									+ args[2]
									+ " §cis not a valid permission! §euse, chests, build");
							return false;
						}
						String perm = args[2].toLowerCase();
						if (perm.equals("chest")) {
							perm = "chests";
						}
						if (args.length < 4) {
							p.sendMessage("§e/chunk perm friends " + args[2]
									+ " allow/deny");
							p.sendMessage("§aAllow §for §cdeny§f?");
							return false;
						}
						boolean yesno = false;
						String msgbool = "§cdenied";
						if (!args[3].equalsIgnoreCase("allow")
								&& !args[3].equalsIgnoreCase("deny")) {
							p.sendMessage("§e/chunk perm default " + args[2]
									+ " allow/deny");
							p.sendMessage("§aAllow §for §cdeny§f?");
							return false;
						}
						if (args[3].equalsIgnoreCase("allow")) {
							yesno = true;
							msgbool = "§aallowed";
						}
						kpl.setFriendPerm(perm, yesno);

						p.sendMessage("§aThe permission §b" + perm
								+ " §ais now " + msgbool
								+ " §afor §6Friends§a.");
						return false;
					}

				}

			}
		}
		if (label.equalsIgnoreCase("mine")) {
			if (!sender.hasPermission("kirithia.mine")) {
				return false;
			}
			if (args.length == 0) {
				p.sendMessage("§cLike this: §e/mine create");
				p.sendMessage("§cLike this: §e/mine name <name>");
				p.sendMessage("§cLike this: §e/mine delete <name>");
				p.sendMessage("§cLike this: §e/mine info");
				p.sendMessage("§cLike this: §e/mine list");
				return false;
			}
			if (args[0].equalsIgnoreCase("create")) {
				if (!creatingmine.contains(p.getName())){
					p.sendMessage("§eNow left click and right click the corners, don't use worldedit wand.");
					creatingmine.add(p.getName());
					return false;
				}else{
					p.sendMessage("§eMine-Create mode disabled.");
					creatingmine.remove(p.getName());
					return false;
					
				}
			}
			if (args[0].equalsIgnoreCase("name")) {
				if (args.length == 1) {
					p.sendMessage("§cLike this: §e/mine name <name>");
					return false;
				}
				if (!settingpoints.containsKey(p.getName()+"right")||!settingpoints.containsKey(p.getName()+"left")){
					p.sendMessage("§cYou must set points first! §e/mine create");
					return false;
				}
				if (minenames.contains(args[1].toLowerCase())){
					p.sendMessage("§cName taken. Choose another.");
					return false;
				}
				String[] array = settingpoints.get(p.getName()+"right").split(",");
				minepoints.put(args[1].toLowerCase()+"1", new Location(p.getWorld(), Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2])));
				array = settingpoints.get(p.getName()+"left").split(",");
				minepoints.put(args[1].toLowerCase()+"2", new Location(p.getWorld(), Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2])));
				p.sendMessage("§aCreated Mine §e"+args[1]);
				minenames.add(args[1].toLowerCase());
				creatingmine.remove(p.getName());
				return false;
			}
			if (args[0].equalsIgnoreCase("delete")) {
				if (args.length == 1) {
					p.sendMessage("§cLike this: §e/mine delete <name>");
					return false;
				}
				if (!minenames.contains(args[1].toLowerCase())){
					p.sendMessage("§cThat mine does not exist! §e/mine list");
					return false;
				}
				minenames.remove(args[1].toLowerCase());
				minepoints.remove(args[1].toLowerCase()+"1");
				minepoints.remove(args[1].toLowerCase()+"2");
				p.sendMessage("§aMine Removed: §e"+args[1]);
				return false;
			}
			if (args[0].equalsIgnoreCase("list")) {
				p.sendMessage(minenames.toString());
				return false;
			}
		}

			
		if (label.equalsIgnoreCase("cannon")) {

			if (sender.hasPermission("kirithia.cannon")) {
				if (args.length == 0) {
					p.sendMessage("§cLike this: §e/cannon info");
					p.sendMessage("§cLike this: §e/cannon delete");
					p.sendMessage("§cLike this: §e/cannon create <x> <y> <z> <repeattimes> <optional:permission> <opt: message>");
					return false;
				}
				if (args[0].equalsIgnoreCase("create")) {
					if (!p.getLocation().getBlock().getType()
							.equals(Material.STONE_PLATE)) {
						p.sendMessage("§cYou must be standing on a stone pressure plate!");
						return false;
					}
					if (args.length < 4) {
						p.sendMessage("§cLike this: §e/cannon create <x> <y> <z>");
						return false;
					}
					String loc = p.getLocation().getBlockX() + ","
							+ p.getLocation().getBlockY() + ","
							+ p.getLocation().getBlockZ();
					try {
						Vector velocity = new Vector(
								Double.parseDouble(args[1]),
								Double.parseDouble(args[2]),
								Double.parseDouble(args[3]));
						cannons.put(loc, velocity);
						p.sendMessage("Cannon created!");
						if (args.length > 4) {
							cannonsrep.put(loc, Integer.parseInt(args[4]));
						} else {
							cannonsrep.put(loc, 1);
						}
					} catch (Exception e) {
						p.sendMessage("§c<x> <y> <z> and <repeattimes> must be numbers! <repeattimes> must be a whole number!");
					}
					if (args.length > 5) {
						cannonpermreq.put(loc, args[5]);
					}
					if (args.length > 6) {
						int i = 6;
						String msg = "";
						while (i < args.length) {
							msg = msg + args[i] + " ";
							i++;
						}
						msg = msg.replace("&", "§");
						cannonmsg.put(loc, msg);

					}
				}
				if (args[0].equalsIgnoreCase("delete")) {
					String loc = p.getLocation().getBlockX() + ","
							+ p.getLocation().getBlockY() + ","
							+ p.getLocation().getBlockZ();
					if (cannons.containsKey(loc)) {
						cannons.remove(loc);
						cannonsrep.remove(loc);
						cannonpermreq.remove(loc);
						cannonmsg.remove(loc);
						p.sendMessage("§cCannon deleted!");
						return false;
					} else {
						p.sendMessage("§cYou are not standing in a cannonous position! :L");
						return false;
					}
				}
				if (args[0].equalsIgnoreCase("info")) {
					String loc = p.getLocation().getBlockX() + ","
							+ p.getLocation().getBlockY() + ","
							+ p.getLocation().getBlockZ();
					if (cannons.containsKey(loc)) {
						p.sendMessage("§aCannon Info: §eX:§a "
								+ cannons.get(loc).getX() + " §eY:§a "
								+ cannons.get(loc).getY() + " §eZ:§a "
								+ cannons.get(loc).getZ() + " §eRepeats: §a"
								+ cannonsrep.get(loc));
						if (cannonpermreq.containsKey(loc)) {
							p.sendMessage("§ePermission: §a"
									+ cannonpermreq.get(loc));
						}
						if (cannonmsg.containsKey(loc)) {
							p.sendMessage("§eNo-Perm message: §r"
									+ cannonmsg.get(loc));
						}
					} else {
						p.sendMessage("§cYou are not standing in a cannonous position! :L");
						return false;
					}
				}
			}
		}
		if (label.equalsIgnoreCase("admin")) {
			if (sender.hasPermission("kirithia.admin")) {
				if (args.length == 0) {
					p.sendMessage("§e/admin chat/town/addxp <townname> <amt>");
					return false;
				}
				if (args[0].equalsIgnoreCase("addrep")) {
					if (args.length < 3) {
						p.sendMessage("§e/admin addrep <playername> <amount>");
						return false;
					}
					if (kp.KP(args[1]) == null) {
						p.sendMessage("§cThat player has never logged on...");
						return false;
					}
					kpl = kp.KP(args[1]);
					try {
						kpl.addRep(Integer.parseInt(args[2]));
						p.sendMessage("§aAdded " + args[2] + " Repuation to "
								+ args[1]);
						return false;
					} catch (Exception e) {
						p.sendMessage("§c<amount> must be a number!!!");
						return false;
					}
				}
				if (args[0].equalsIgnoreCase("getrep")) {
					if (args.length < 2) {
						p.sendMessage("§e/admin addrep <playername>");
						return false;
					}
					if (kp.KP(args[1]) == null) {
						p.sendMessage("§cThat player has never logged on...");
						return false;
					}
					kpl = kp.KP(args[1]);
					p.sendMessage("§e" + args[1] + " §ahas §b" + kpl.getRep()
							+ " §aRepuation");
					return false;
				}
				if (args[0].equalsIgnoreCase("town")) {
					if (args.length < 2) {

						p.sendMessage("§7--------------§6Admin Towns Help§7----------------------");
						p.sendMessage("§7-----------------------------------------------------");
						p.sendMessage("- §e/admin town create <name>: §7Creates and selects a new.");
						p.sendMessage("- §e/admin town sel <name>: §7Switches which town you use.");
						p.sendMessage("- §e/admin town un/claim: §7Un/Claims land.");
						p.sendMessage("- §e/admin town un/claim <r>: §7Un/Claims in a radius.");
						p.sendMessage("- §e/admin town setmsg <m>: §7Sets welcome msg.");
						p.sendMessage("- §e/admin town setpmsg <m>: §7Sets no perm msg.");
						p.sendMessage("- §e/admin town perm: §7Sets player perms for town chunks.");
						p.sendMessage("- §e/admin town silent true/false: §7No enter/exit msgs.");
						p.sendMessage("- §e/admin town list: §7Lists admin town names.");
						p.sendMessage("- §e/admin town info: §7Info on chunk and town.");
						return false;

					}
					if (args[1].equalsIgnoreCase("silent")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}
						if (args.length < 3) {
							p.sendMessage("§cSilent true or false? /admin town silent true/false");
							return false;
						}
						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));
						if (args[2].equalsIgnoreCase("true")) {
							a.setSilent(true);
							p.sendMessage("§aSet Town silent to: " + args[2]);
							return false;
						}
						if (args[2].equalsIgnoreCase("false")) {
							a.setSilent(false);
							p.sendMessage("§aSet Town silent to: " + args[2]);
							return false;
						}

						p.sendMessage("§cMust be true or false!...");

						return false;
					}
					if (args[1].equalsIgnoreCase("list")) {
						p.sendMessage("§aAdmin Towns: §e"
								+ at.getTownList().toString());
					}
					if (args[1].equalsIgnoreCase("perm")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}
						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));
						if (args.length < 3) {
							p.sendMessage("§c/admin town perm deny <perm>");
							p.sendMessage("§c/admin town perm allow <perm>");
							p.sendMessage("§c/admin town perm list");
							p.sendMessage("§c/admin town perm listall");
							return false;
						}
						if (args[2].equalsIgnoreCase("deny")) {
							if (args.length < 4) {
								p.sendMessage("§c/admin town perm deny <perm>");
								p.sendMessage("§cNeed a perm! /admin town perm listall");
								return false;
							}
							if (!at.getAllPerms().contains(
									args[3].toLowerCase())
									&& !args[3].equalsIgnoreCase("all")) {
								p.sendMessage("§cNot a real perm! /admin town perm listall");
								return false;
							}
							if (a.getDeniedPerms().contains(
									args[3].toLowerCase())) {
								p.sendMessage("§cThis perm is already denied.");
								return false;
							}
							a.addDeniedPerm(args[3].toLowerCase());
							p.sendMessage("§a" + args[3] + " denied.");
							return false;
						}
						if (args[2].equalsIgnoreCase("allow")) {
							if (args.length < 4) {
								p.sendMessage("§c/admin town perm allow <perm>");
								p.sendMessage("§cNeed a perm! /admin town perm listall");
								return false;
							}
							if (!at.getAllPerms().contains(
									args[3].toLowerCase())
									&& !args[3].equalsIgnoreCase("all")) {
								p.sendMessage("§cNot a real perm! /admin town perm listall");
								return false;
							}
							if (!a.getDeniedPerms().contains(
									args[3].toLowerCase())
									&& !args[3].equalsIgnoreCase("all")) {
								p.sendMessage("§cThis perm is already allowed.");
								return false;
							}
							a.removeDeniedPerm(args[3].toLowerCase());
							p.sendMessage("§a" + args[3] + " allowed again.");
							return false;
						}
						if (args[2].equalsIgnoreCase("list")) {
							p.sendMessage("§aAll denied perms: "
									+ a.getDeniedPerms());
						}
						if (args[2].equalsIgnoreCase("listall")) {
							p.sendMessage("§aAll perms: " + at.getAllPerms());
						}
					}
					if (args[1].equalsIgnoreCase("create")) {
						if (args.length < 3) {
							p.sendMessage("§cYou need to specify a town name. /admin town create <name>");
							return false;
						}
						if (!at.canUseName(args[2])) {
							p.sendMessage("§cAn admin town with that name already exists!");
							return false;
						}
						at.createDefaultAT(new AdminTowns(args[2]));
						playerat.put(p.getName(), args[2]);
						p.sendMessage("§aAdmin Town: §e" + args[2]
								+ " §acreated and selected!");
						return false;
					}

					if (args[1].equalsIgnoreCase("sel")) {
						if (args.length < 3) {
							p.sendMessage("§cYou need to specify a town to select. /admin town sel <name>");
							return false;
						}
						if (at.getTownbyName(args[2]) == null) {
							p.sendMessage("§cThe admin town \"" + args[2]
									+ "\" does not exist! Try /admin town list");
							return false;
						}
						AdminTowns a = at.getTownbyName(args[2]);
						playerat.put(p.getName(), args[2]);
						p.sendMessage("§aYou have selected the town: §e"
								+ a.getName());
						return false;
					}

					if (args[1].equalsIgnoreCase("setmsg")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}
						if (args.length < 3) {
							p.sendMessage("§cYou need to specify a message. /admin town setmsg <m>");
							return false;
						}
						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));
						int i = 2;
						String msg = "";
						while (i < args.length) {
							msg = msg + args[i] + " ";
							i++;
						}
						msg = msg.replace("&", "§");
						a.setMessage(msg);
						p.sendMessage("§aSet town welcome msg :)");
						return false;
					}

					if (args[1].equalsIgnoreCase("setpmsg")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}
						if (args.length < 3) {
							p.sendMessage("§cYou need to specify a message. /admin town setpmsg <m>");
							return false;
						}
						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));
						int i = 2;
						String msg = "";
						while (i < args.length) {
							msg = msg + args[i] + " ";
							i++;
						}
						msg = msg.replace("&", "§");
						a.setMessage(msg);
						p.sendMessage("§aSet town no permission msg :)");
						return false;
					}

					if (args[1].equalsIgnoreCase("claim")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}
						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));
						if (args.length > 2) {
							int r = 0;
							try {
								r = Integer.parseInt(args[2]);
							} catch (Exception e) {
								p.sendMessage("§c<r> must be a number!");
								return false;
							}
							if (r == 0) {
								return false;
							}
							int side = 1 + 2 * r;
							int unclaimed = 0;
							World w = p.getWorld();
							Chunk c = w.getChunkAt(p.getLocation().getChunk()
									.getX()
									- r, p.getLocation().getChunk().getZ() - r);
							int X = c.getX();
							int Z = c.getZ();

							for (int i = 0; i < side; i++) {

								for (int b = 0; b < side; b++) {

									if (ch.getType(X + "," + Z) != "wild") {
										unclaimed++;
									} else {

										at.addChunk(a, X + "," + Z);
										ch.addChunk(X + "," + Z, "admin");
									}

									Z++;
								}
								X++;
								Z = Z - side;

							}
							p.sendMessage("§aClaimed §e"
									+ (Math.pow(side, 2) - unclaimed)
									+ " §achunks for the admin town:"
									+ a.getName());
							p.sendMessage("§c"
									+ unclaimed
									+ " §cchunks were already claimed in some way or another.");
							return false;
						}

						if (ch.getType(p.getLocation().getChunk()
								.getChunkSnapshot()) != "wild") {
							p.sendMessage("§cYou can not claim this chunk.");
							return false;
						}
						p.sendMessage("§cChunk claimed.");
						at.addChunk(a, p.getLocation().getChunk());

						ch.addChunk(p.getLocation().getChunk()
								.getChunkSnapshot(), "admin");
					}
					if (args[1].equalsIgnoreCase("unclaim")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}

						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));

						if (args.length > 2) {
							int r = 0;
							try {
								r = Integer.parseInt(args[2]);
							} catch (Exception e) {
								p.sendMessage("§c<r> must be a number!");
								return false;
							}
							if (r == 0) {
								return false;
							}
							int side = 1 + 2 * r;
							int unable = 0;
							World w = p.getWorld();
							Chunk c = w.getChunkAt(p.getLocation().getChunk()
									.getX()
									- r, p.getLocation().getChunk().getZ() - r);
							int X = c.getX();
							int Z = c.getZ();

							for (int i = 0; i < side; i++) {

								for (int b = 0; b < side; b++) {

									if (at.getTownbyName(X + "," + Z) == null) {
										unable++;
									} else if (!at.getTownbyName(X + "," + Z)
											.equals(a)) {
										unable++;

									} else {

										at.removeChunk(a, X + "," + Z);
										ch.removeChunk(X + "," + Z);
									}
									p.sendMessage(X + "," + Z);
									Z++;
								}
								X++;
								Z = Z - side;

							}
							p.sendMessage("§aUnclaimed §e"
									+ (Math.pow(side, 2) - unable)
									+ " §achunks from the admin town:"
									+ a.getName());
							p.sendMessage("§c"
									+ unable
									+ " §cchunks were not claimed by this admin town.");
							return false;
						}

						if (at.getTownbyName(p.getLocation().getChunk().getX()
								+ "," + p.getLocation().getChunk().getZ()) == null) {
							p.sendMessage("§cThis admin town does not own this land.");
							return false;
						}
						if (!at.getTownbyName(
								p.getLocation().getChunk().getX() + ","
										+ p.getLocation().getChunk().getZ())
								.equals(a)) {
							p.sendMessage("§cThis admin town does not own this land.");
							return false;
						}
						p.sendMessage("§cChunk unclaimed.");
						at.removeChunk(a, p.getLocation().getChunk());

						ch.removeChunk(p.getLocation().getChunk()
								.getChunkSnapshot());
						return false;
					}
					if (args[1].equalsIgnoreCase("info")) {
						if (ch.getType(p.getLocation().getChunk()
								.getChunkSnapshot()) != "admin") {
							p.sendMessage("§cThis is not an admin town chunk.");
							return false;
						}
						AdminTowns a = at.getTownbyName(p.getLocation()
								.getChunk().getX()
								+ "," + p.getLocation().getChunk().getZ());
						p.sendMessage("§a" + a.getName() + "'s Chunk.");
					}
					if (args[1].equalsIgnoreCase("delete")) {
						if (!playerat.containsKey(p.getName())) {
							p.sendMessage("§cYou must select a town!. /admin town sel <name>");
							return false;
						}
						AdminTowns a = at.getTownbyName(playerat.get(p
								.getName()));

						if (args.length == 2) {
							p.sendMessage("§cAre you sure you want to do this? "
									+ a.getName() + " will be gone forever.");
							p.sendMessage("§cType §4/admin town delete confirmyes§c if you are sure.");
						} else {
							if (args[2].equalsIgnoreCase("confirmyes")) {

								deletedadmintowns
										.add(a.getName().toLowerCase());
								for (String coords : a.getChunks()) {
									ch.removeChunk(coords);
								}

								at.delete(a);
								playerat.remove(p.getName());
								p.sendMessage("§cAdmin Town deleted forever.");

							}
						}
						return false;
					}
					// single unclaim and then multi both.

				}
				if (args[0].equalsIgnoreCase("chat")) {
					if (args.length == 1) {
						if (allowed.contains(p.getName())) {
							if (usingadminchat.contains(p.getName())) {
								usingadminchat.remove(p.getName());
								p.sendMessage("§cYou are no longer talking in admin chat!");
							} else {
								usingadminchat.add(p.getName());
								p.sendMessage("§aYou are now talking in admin chat!");
							}
						}
					} else if (args.length > 1) {
						if (args[1].equalsIgnoreCase("add") && args.length == 3) {
							try {
								String name = Bukkit.getPlayer(args[2])
										.getName();
								if (!allowed.contains(name)) {
									allowed.add(name);
									p.sendMessage("§aYou added " + name
											+ " to admin chat!");
								} else {
									p.sendMessage("§c"
											+ name
											+ " is already in admin chat! Silly!");
								}

							} catch (Exception e) {
								p.sendMessage("§cError.");
							}
						} else if (args[1].equalsIgnoreCase("remove")
								&& args.length == 3) {
							try {
								String name = Bukkit.getPlayer(args[2])
										.getName();
								if (allowed.contains(name)) {
									allowed.remove(name);
									p.sendMessage("§aYou removed " + name
											+ " from admin chat!");
								} else {
									p.sendMessage("§c" + name
											+ " is not in admin chat! Silly!");
								}
								try {
									usingadminchat.remove(name);
								} catch (Exception e) {

								}
							} catch (Exception e) {
								p.sendMessage("§cError.");
							}
						} else if (args[1].equalsIgnoreCase("list")) {
							try {
								String list = "Allowed: ";
								for (String name : allowed) {
									list = list + name + ", ";
								}
								p.sendMessage(list);
							} catch (Exception e) {
								p.sendMessage("§cError.");
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("addxp")) {
					if (town.getTown(args[1].toLowerCase()) == null) {
						p.sendMessage("§cThe town§e " + args[1]
								+ " §cdoes not exist!");
						return false;
					}
					Towns t = town.getTown(args[1].toLowerCase());
					if (t.addXP(Double.parseDouble(args[2]))) {
						sendTownChat(null,
								"§dThe Town has leveled up! " + t.getName()
										+ " is now level " + (int) t.getLevel()
										+ "!", t);
					}
				}
			}
			return false;
		}
		// town commands
		// ################################
		// ###################
		if (label.equalsIgnoreCase("town")) {
			if (args.length != 0) {
				if (sender.hasPermission("kirithia.town")) {
					if (args[0].equalsIgnoreCase("sethome")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "sethome")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}

						if (t.getLevel() < 8) {
							// check for if the chunk is in their town
							Chunk c = p.getLocation().getChunk();
							if (town.getTownofChunk(c) == null) {
								p.sendMessage("§cYou can only set town homes in land your town owns!");
								return false;
							}
							if (!town.getTownofChunk(c).equals(t)) {
								p.sendMessage("§cYou can only set town homes in land your town owns!");
								return false;
							}

							if (checkSafe(p.getLocation())) {
								t.setHome(p.getLocation(), null);
								p.sendMessage("§aYou set the §eTown Home§a to your location!");
								return false;

							} else {
								p.sendMessage("§cThis is not a suitable location! This position may be dangerous.");
								return false;
							}

						}

						// double home.
						if (!t.anotherHome()
								&& !t.getHomeNames().contains(
										args[1].toLowerCase())) {
							p.sendMessage("§cYour town already has it's limit of "
									+ t.getAmountofHomes() + " homes.");
							p.sendMessage("§cUse §e/town delhome §cto choose a home to remove.");
							return false;
						}
						if (args.length < 2) {
							// home commands
							p.sendMessage("§cUse like this! §e/town sethome <name>");

							return false;
						}
						Chunk c = p.getLocation().getChunk();
						if (town.getTownofChunk(c) == null) {
							p.sendMessage("§cYou can only set town homes in land your town owns!");
							return false;
						}
						if (!town.getTownofChunk(c).equals(t)) {
							p.sendMessage("§cYou can only set town homes in land your town owns!");
							return false;
						}

						if (checkSafe(p.getLocation())) {
							t.setHome(p.getLocation(), args[1]);
							p.sendMessage("§aYou set the §eTown Home§b "
									+ args[1] + "§a to your location!");
							return false;

						} else {
							p.sendMessage("§cThis is not a suitable location! This position may be dangerous.");
							return false;
						}

					}
					if (args[0].equalsIgnoreCase("delhome")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "sethome")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}

						if (t.getLevel() < 8) {
							p.sendMessage("§cAs your town can only yet have one town home, your §e/town sethome §cwill overwrite it.");
							return false;
						}
						if (args.length < 2) {
							String str = "";
							for (String name : t.getHomeNames()) {
								str = str + name + " ";
							}
							p.sendMessage("§eTown homes: §b" + str);
							p.sendMessage("§cUse like this: §e/town home <home-name>");

							return false;
						}
						if (!t.getHomeNames().contains(args[1].toLowerCase())) {
							p.sendMessage("§e" + args[1]
									+ " §cis not a valid town home!");
							String str = "";
							for (String name : t.getHomeNames()) {
								str = str + name + " ";
							}
							p.sendMessage("§eTown homes: §b" + str);
							return false;
						}
						t.deleteHome(args[1].toLowerCase());
						p.sendMessage("§e" + args[1]
								+ " §csuccessfully removed!");
					}
					if (args[0].equalsIgnoreCase("home")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "home")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (!t.hasHome()) {
							p.sendMessage("§cYour town does not have a home!");
							return false;
						}
						if (t.getLevel() < 8) {
							if (!p.getWorld().equals(
									t.getHomeLoc(null).getWorld())) {
								p.sendMessage("§cYou are not in the same world!");
								return false;
							}
						}
						if (!canNowUseSpell("home", p.getName())) {

							p.sendMessage("§cYou must wait for "
									+ toHourMinSec(getCooldownRemaining("home",
											p.getName()))
									+ " before you can use this spell again!");
							return false;
						}
						if (t.getLevel() < 8) {
							t.getHomeLoc(null).getChunk().load();
							if (checkSafe(t.getHomeLoc(null))) {
								// p.teleport(t.getHomeLoc(null));
								if (casting.contains(p)) {
									p.sendMessage("§cYou are already casting a spell!");
									return false;
								}
								p.sendMessage("§eYou are casting a spell. Do not move or the spell will be cancelled!");

								ParticleEffect.PORTAL.display(p.getLocation(),
										0, 0, 0, 4, 400);

								homeAnimation(p, p.getLocation(), 0,
										t.getHomeLoc(null), "the Town Home");
								return false;
							} else {
								p.sendMessage("§cThe Home Location is not safe!");
								return false;
							}
						} else {
							if (args.length < 2) {
								String str = "";
								for (String name : t.getHomeNames()) {
									str = str + name + " ";
								}
								p.sendMessage("§eTown homes: §b" + str);
								p.sendMessage("§cUse like this: §e/town home <home-name>");

								return false;
							}
							if (!t.getHomeNames().contains(
									args[1].toLowerCase())) {
								p.sendMessage("§e" + args[1]
										+ " §cis not a valid town home!");
								String str = "";
								for (String name : t.getHomeNames()) {
									str = str + name + " ";
								}
								p.sendMessage("§eTown homes: §b" + str);
								return false;
							}
							if (!p.getWorld().equals(
									t.getHomeLoc(args[1].toLowerCase())
											.getWorld())) {
								p.sendMessage("§cYou are not in the same world!");

								return false;
							}
							t.getHomeLoc(args[1].toLowerCase()).getChunk()
									.load();
							if (checkSafe(t.getHomeLoc(args[1].toLowerCase()))) {
								// p.teleport(t.getHomeLoc(null));
								if (casting.contains(p)) {
									p.sendMessage("§cYou are already casting a spell!");
									return false;
								}
								p.sendMessage("§eYou are casting a spell. Do not move or the spell will be cancelled!");
								ParticleEffect.PORTAL.display(p.getLocation(),
										0, 0, 0, 4, 400);
								homeAnimation(p, p.getLocation(), 0,
										t.getHomeLoc(args[1].toLowerCase()),
										"the Town Home - " + args[1]);
								return false;
							} else {
								p.sendMessage("§cThe Home Location is not safe!");
								return false;
							}
						}

					}
					if (args[0].equalsIgnoreCase("perm")
							|| args[0].equalsIgnoreCase("perms")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (args.length < 2) {
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("§7-------------------§6Town Permissions§7-------------------");
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("- §e/town perm §ainfo: §7Information about each permission.");
							p.sendMessage("- §e/town perm §fdefault: §7Lists the default rank permissions.");
							p.sendMessage("- §e/town perm §bmod: §7Lists the moderator rank permissions.");
							p.sendMessage("- §e/town perm §cadmin: §7Lists the admin rank permissions.");
							p.sendMessage("§7-----------------------------------------------------");
							return false;
						}
						if (args[1].equalsIgnoreCase("admin")) {
							if (args.length < 4) {
								p.sendMessage("§cAdmin Permissions:");
								p.sendMessage("§fGreen: §aAllowed§f, §fRed: §cDenied");
								p.sendMessage("§7-----------------------------------------------------");
								String allperms = "invite kick motd promote demote c-u-laim claim setchunkmembers setchunkowners chunkaccess h-s-ome home allegiance";
								String perms = t.getAdminPerms();
								String rem = "";
								while (perms.contains(" ")) {
									rem = perms
											.substring(0, perms.indexOf(" "));
									perms = perms
											.substring(perms.indexOf(" ") + 1);
									allperms = allperms.replace(rem, "");
								}
								String allowed = t.getAdminPerms();
								allowed = allowed
										.replace("c-u-laim", "unclaim");
								allowed = allowed.replace("h-s-ome", "sethome");
								allperms = allperms.replace("c-u-laim",
										"unclaim");
								allperms = allperms.replace("h-s-ome",
										"sethome");
								String finalmsg = "§a" + allowed + "§c"
										+ allperms;
								while (finalmsg.contains("  ")) {
									finalmsg = finalmsg.replace("  ", " ");
								}
								p.sendMessage("§bPermissions: " + finalmsg);
								p.sendMessage("/§etown perm admin <permission> §aallow§f/§cdeny§f: §7Set permissions.");
								return false;
							} else {

								if (!args[2].equalsIgnoreCase("invite")
										&& !args[2].equalsIgnoreCase("kick")
										&& !args[2].equalsIgnoreCase("motd")
										&& !args[2].equalsIgnoreCase("promote")
										&& !args[2].equalsIgnoreCase("demote")
										&& !args[2].equalsIgnoreCase("unclaim")
										&& !args[2].equalsIgnoreCase("claim")
										&& !args[2]
												.equalsIgnoreCase("setchunkmembers")
										&& !args[2]
												.equalsIgnoreCase("setchunkowners")
										&& !args[2]
												.equalsIgnoreCase("chunkaccess")
										&& !args[2].equalsIgnoreCase("sethome")
										&& !args[2].equalsIgnoreCase("home")

										&& !args[2]
												.equalsIgnoreCase("allegiance")) {
									p.sendMessage("§cInvalid permission!");
									p.sendMessage("§cPermissions: §binvite, kick, promote, demote, claim, unclaim, setchunkmembers, setchunkowners, chunkaccess, sethome, home, motd, allegiance");
									return false;
								}
								if (args.length > 3) {
									if (!args[3].equalsIgnoreCase("allow")
											&& !args[3]
													.equalsIgnoreCase("deny")) {
										p.sendMessage("§cUse like this: §e/town perm admin "
												+ args[2] + " §aallow§f/§cdeny");
										return false;
									} else {
										if (!p.getName().equalsIgnoreCase(
												t.getOwner())) {
											p.sendMessage("§cOnly the Town Mayor can use these commands.");
											return false;
										}
										if (args[3].equalsIgnoreCase("allow")) {
											String permission = args[2];
											if (permission
													.equalsIgnoreCase("unclaim")) {
												permission = "c-u-laim";
											}
											if (permission
													.equalsIgnoreCase("sethome")) {
												permission = "h-s-ome";
											}
											if (!t.getAdminPerms().contains(
													permission)) {
												t.setAdminPerms(t
														.getAdminPerms()
														+ permission + " ");
												p.sendMessage("§aAdmins permission allowed: §b"
														+ args[2].toLowerCase());
											} else {
												p.sendMessage("§cAdmins already have the §b"
														+ args[2].toLowerCase()
														+ "§c permission.");
											}
											return false;
										}
										if (args[3].equalsIgnoreCase("deny")) {
											String permission = args[2];
											if (permission
													.equalsIgnoreCase("unclaim")) {
												permission = "c-u-laim";
											}
											if (permission
													.equalsIgnoreCase("sethome")) {
												permission = "h-s-ome";
											}
											if (t.getAdminPerms().contains(
													permission)) {
												String permss = t
														.getAdminPerms();
												permss = permss.replace(
														permission + " ", "");
												t.setAdminPerms(permss);
												p.sendMessage("§aAdmins permission §cdenied§a: §b"
														+ args[2].toLowerCase());
											} else {
												p.sendMessage("§cAdmins don't have the §b"
														+ args[2].toLowerCase()
														+ "§c permission.");
											}
											return false;

										}

									}
								}
							}
						}
						if (args[1].equalsIgnoreCase("mod")
								|| args[1].equalsIgnoreCase("moderator")) {
							if (args.length < 4) {
								p.sendMessage("§bMod Permissions:");
								p.sendMessage("§fGreen: §aAllowed§f, §fRed: §cDenied");
								p.sendMessage("§7-----------------------------------------------------");
								String allperms = "invite kick motd promote demote c-u-laim claim setchunkmembers setchunkowners chunkaccess h-s-ome home allegiance";
								String perms = t.getModPerms();
								String rem = "";
								while (perms.contains(" ")) {
									rem = perms
											.substring(0, perms.indexOf(" "));
									perms = perms
											.substring(perms.indexOf(" ") + 1);
									allperms = allperms.replace(rem, "");
								}
								String allowed = t.getModPerms();
								allowed = allowed
										.replace("c-u-laim", "unclaim");
								allowed = allowed.replace("h-s-ome", "sethome");
								allperms = allperms.replace("c-u-laim",
										"unclaim");
								allperms = allperms.replace("h-s-ome",
										"sethome");
								String finalmsg = "§a" + allowed + "§c"
										+ allperms;
								while (finalmsg.contains("  ")) {
									finalmsg = finalmsg.replace("  ", " ");
								}
								p.sendMessage("§bPermissions: " + finalmsg);
								p.sendMessage("/§etown perm mod <permission> §aallow§f/§cdeny§f: §7Set permissions.");
								return false;
							} else {

								if (!args[2].equalsIgnoreCase("invite")
										&& !args[2].equalsIgnoreCase("kick")
										&& !args[2].equalsIgnoreCase("motd")
										&& !args[2].equalsIgnoreCase("promote")
										&& !args[2].equalsIgnoreCase("demote")
										&& !args[2].equalsIgnoreCase("unclaim")
										&& !args[2].equalsIgnoreCase("claim")
										&& !args[2]
												.equalsIgnoreCase("setchunkmembers")
										&& !args[2]
												.equalsIgnoreCase("setchunkowners")
										&& !args[2]
												.equalsIgnoreCase("chunkaccess")
										&& !args[2].equalsIgnoreCase("sethome")
										&& !args[2].equalsIgnoreCase("home")

										&& !args[2]
												.equalsIgnoreCase("allegiance")) {
									p.sendMessage("§cInvalid permission!");
									p.sendMessage("§cPermissions: §binvite, kick, promote, demote, claim, unclaim, setchunkmembers, setchunkowners, chunkaccess, sethome, home, motd, allegiance");
									return false;
								}
								if (args.length > 3) {
									if (!args[3].equalsIgnoreCase("allow")
											&& !args[3]
													.equalsIgnoreCase("deny")) {
										p.sendMessage("§cUse like this: §e/town perm mod "
												+ args[2] + " §aallow§f/§cdeny");
										return false;
									} else {
										if (!p.getName().equalsIgnoreCase(
												t.getOwner())) {
											p.sendMessage("§cOnly the Town Mayor can use these commands.");
											return false;
										}
										if (args[3].equalsIgnoreCase("allow")) {
											String permission = args[2];
											if (permission
													.equalsIgnoreCase("unclaim")) {
												permission = "c-u-laim";
											}
											if (permission
													.equalsIgnoreCase("sethome")) {
												permission = "h-s-ome";
											}
											if (!t.getModPerms().contains(
													permission)) {
												t.setModPerms(t.getModPerms()
														+ permission + " ");
												p.sendMessage("§aMods permission allowed: §b"
														+ args[2].toLowerCase());
											} else {
												p.sendMessage("§cMods already have the §b"
														+ args[2].toLowerCase()
														+ "§c permission.");
											}
											return false;
										}
										if (args[3].equalsIgnoreCase("deny")) {
											String permission = args[2];
											if (permission
													.equalsIgnoreCase("unclaim")) {
												permission = "c-u-laim";
											}
											if (permission
													.equalsIgnoreCase("sethome")) {
												permission = "h-s-ome";
											}
											if (t.getModPerms().contains(
													permission)) {
												String permss = t.getModPerms();
												permss = permss.replace(
														permission + " ", "");
												t.setModPerms(permss);
												p.sendMessage("§aMods permission §cdenied§a: §b"
														+ args[2].toLowerCase());
											} else {
												p.sendMessage("§cMods don't have the §b"
														+ args[2].toLowerCase()
														+ "§c permission.");
											}
											return false;

										}

									}
								}
							}
						}
						if (args[1].equalsIgnoreCase("default")) {
							if (args.length < 4) {
								p.sendMessage("§eDefault Permissions:");
								p.sendMessage("§fGreen: §aAllowed§f, §fRed: §cDenied");
								p.sendMessage("§7-----------------------------------------------------");
								String allperms = "invite kick motd promote demote c-u-laim claim setchunkmembers setchunkowners chunkaccess h-s-ome home allegiance";
								String perms = t.getDefaultPerms();
								String rem = "";
								while (perms.contains(" ")) {
									rem = perms
											.substring(0, perms.indexOf(" "));
									perms = perms
											.substring(perms.indexOf(" ") + 1);
									allperms = allperms.replace(rem, "");
								}
								String allowed = t.getDefaultPerms();
								allowed = allowed
										.replace("c-u-laim", "unclaim");
								allowed = allowed.replace("h-s-ome", "sethome");
								allperms = allperms.replace("c-u-laim",
										"unclaim");
								allperms = allperms.replace("h-s-ome",
										"sethome");
								String finalmsg = "§a" + allowed + "§c"
										+ allperms;
								while (finalmsg.contains("  ")) {
									finalmsg = finalmsg.replace("  ", " ");
								}
								p.sendMessage("§bPermissions: " + finalmsg);
								p.sendMessage("/§etown perm default <permission> §aallow§f/§cdeny§f: §7Set permissions.");
								return false;
							} else {

								if (!args[2].equalsIgnoreCase("invite")
										&& !args[2].equalsIgnoreCase("kick")
										&& !args[2].equalsIgnoreCase("motd")
										&& !args[2].equalsIgnoreCase("promote")
										&& !args[2].equalsIgnoreCase("demote")
										&& !args[2].equalsIgnoreCase("unclaim")
										&& !args[2].equalsIgnoreCase("claim")
										&& !args[2]
												.equalsIgnoreCase("setchunkmembers")
										&& !args[2]
												.equalsIgnoreCase("setchunkowners")
										&& !args[2]
												.equalsIgnoreCase("chunkaccess")
										&& !args[2].equalsIgnoreCase("sethome")
										&& !args[2].equalsIgnoreCase("home")

										&& !args[2]
												.equalsIgnoreCase("allegiance")) {
									p.sendMessage("§cInvalid permission!");
									p.sendMessage("§cPermissions: §binvite, kick, promote, demote, claim, unclaim, setchunkmembers, setchunkowners, chunkaccess, sethome, home, motd, allegiance");
									return false;
								}
								if (args.length > 3) {
									if (!args[3].equalsIgnoreCase("allow")
											&& !args[3]
													.equalsIgnoreCase("deny")) {
										p.sendMessage("§cUse like this: §e/town perm default "
												+ args[2] + " §aallow§f/§cdeny");
										return false;
									} else {
										if (!p.getName().equalsIgnoreCase(
												t.getOwner())) {
											p.sendMessage("§cOnly the Town Mayor can use these commands.");
											return false;
										}
										if (args[3].equalsIgnoreCase("allow")) {
											String permission = args[2];
											if (permission
													.equalsIgnoreCase("unclaim")) {
												permission = "c-u-laim";
											}
											if (permission
													.equalsIgnoreCase("sethome")) {
												permission = "h-s-ome";
											}
											if (!t.getDefaultPerms().contains(
													permission)) {
												t.setDefaultPerms(t
														.getDefaultPerms()
														+ permission + " ");
												p.sendMessage("§aDefaults permission allowed: §b"
														+ args[2].toLowerCase());
											} else {
												p.sendMessage("§cDefaults already have the §b"
														+ args[2].toLowerCase()
														+ "§c permission.");
											}
											return false;
										}
										if (args[3].equalsIgnoreCase("deny")) {
											String permission = args[2];
											if (permission
													.equalsIgnoreCase("unclaim")) {
												permission = "c-u-laim";
											}
											if (permission
													.equalsIgnoreCase("sethome")) {
												permission = "h-s-ome";
											}
											if (t.getDefaultPerms().contains(
													permission)) {
												String permss = t
														.getDefaultPerms();
												permss = permss.replace(
														permission + " ", "");
												t.setDefaultPerms(permss);
												p.sendMessage("§aDefaults permission §cdenied§a: §b"
														+ args[2].toLowerCase());
											} else {
												p.sendMessage("§cDefaults don't have the §b"
														+ args[2].toLowerCase()
														+ "§c permission.");
											}
											return false;

										}

									}
								}
							}
						}
						if (args[1].equalsIgnoreCase("info")) {
							if (args.length < 3) {
								p.sendMessage("§cUse like this: §e/town perm info §b<permission>");
								p.sendMessage("§cPermissions: §binvite, kick, motd, promote, demote, claim, unclaim, setchunkmembers, setchunkowners, chunkaccess, sethome, home, allegiance");
								// invitekickpromotedemotesetchunkmemberssetchunkownersclaimc-u-laimchunkaccessh-s-omehomemotdallegiance
								return false;
							} else {
								switch (args[2]) {
								case "invite":
									p.sendMessage("§bInvite: §aAllows the group to invite other players to the town.");
									break;
								case "kick":
									p.sendMessage("§bKick: §aAllows the group to remove other players from the town.");
									break;
								case "motd":
									p.sendMessage("§bMotD: §aAllows the group to set the Town Message of the Day.");
									break;
								case "promote":
									p.sendMessage("§bPromote: §aAllows the group to promote players in the town to any ranks below them.");
									break;
								case "demote":
									p.sendMessage("§bDemote: §aAllows the group to demote other players who are a lower rank than themselves.");
									break;
								case "claim":
									p.sendMessage("§bClaim: §aAllows the group to claim land for the town.");
									break;
								case "unclaim":
									p.sendMessage("§bUnclaim: §aAllows the group to unclaim ANY town land!");
									break;
								case "setchunkmembers":
									p.sendMessage("§bSetChunkMembers: §aAllows the group to add or remove members of any town chunk.");
									break;
								case "setchunkowners":
									p.sendMessage("§bSetChunkOwners: §aAllows the group to add or remove owners of any town chunk.");
									break;
								case "chunkaccess":
									p.sendMessage("§bChunkAccess: §aAllows the group access to anything in ALL town land.");
									break;
								case "sethome":
									p.sendMessage("§bSetHome: §aAllows the group to set and delete homes for the town.");
									break;
								case "home":
									p.sendMessage("§bHome: §aAllows the group to travel to the town home with /town home.");
									break;
								case "allegiance":
									p.sendMessage("§bAllegiance: §aAllows the group to enemy, ally or set a neutral standing with other towns.");
									break;
								default:
									p.sendMessage("§cInvalid permission!");
									p.sendMessage("§cPermissions: §binvite, kick, promote, demote, claim, unclaim, setchunkmembers, setchunkowners, chunkaccess, sethome, home, allegiance");
									break;
								}
							}
						}
						// town perms mod/erator
						// town perm mod <perm> <yes/no>
						// town perm info <perm>

						// only when setting
						if (!t.getOwner().equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("setmotd")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "motd")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}

						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town motd <message>");

							return false;
						}
						int i = 1;
						String MOTD = "";
						while (i < args.length) {
							MOTD = MOTD + args[i] + " ";
							i++;
						}
						t.setMOTD(MOTD);
						sendTownChat(null, "§d" + p.getName()
								+ " changed the MOTD: " + MOTD, t);
						return false;
					}
					if (args[0].equalsIgnoreCase("motd")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						p.sendMessage("§dMOTD: " + t.getMOTD());
						return false;
					}
					if (args[0].equalsIgnoreCase("create")) {

						if (town.getTownofPlayer(p.getName()) != null) {
							p.sendMessage("§cYou're already in a town! §e/town leave");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town create <name>");
							return false;
						}
						if (kp.KP(p.getName()).getRep() < 2000) {
							p.sendMessage("§cYou need at least §b2000 §cReputation to create a town! §e/repuation");
							return false;
						}
						// go over rep, make sure it's ok TODO
						String attemptedname = args[1].replace(" ", "");

						if (attemptedname.length() > 20) {
							p.sendMessage("§cTown name must not exceed 20 characters (letters)!");
							return false;
						}
						if (attemptedname.length() < 3) {
							p.sendMessage("§cTown name must be atleast 3 characters (letters)!");
							return false;
						}
						String namecheck = attemptedname.replace("_",
								"neverguessthishugestringlmao");

						if (!namecheck.matches("[a-zA-Z]+")) {

							p.sendMessage("§cTown name must only contain letters (with _ for any spaces)!");
							return false;
						}
						String finalname = namecheck.replace(
								"neverguessthishugestringlmao", " ");

						boolean exist = false;
						for (String current : town.getTowns()) {
							if (current.toLowerCase().equalsIgnoreCase(
									finalname)) {
								exist = true;
							}
						}
						if (!exist) {
							town.createTown(new Towns(finalname, p.getName()));
							allyreq.put(finalname, new ArrayList<String>());
							neutralreq.put(finalname, new ArrayList<String>());
							p.sendMessage("§aTown: "
									+ "§e"
									+ finalname
									+ " §acreated successfully! §aUse §e/town help §afor commands!");
							kp.KP(p.getName()).subRep(2000);
							// TODO make sure rep is okay!!
							return false;
						} else {
							p.sendMessage("§cThis town name has already been used!");
							return false;
						}

					}
					if (args[0].equalsIgnoreCase("invite")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "invite")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}

						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town invite <name>");
							return false;
						}

						if (Bukkit.getPlayer(args[1]) != null) {
							Player invited = Bukkit.getPlayer(args[1]);
							if (town.getTownofPlayer(invited.getName()) == null) {
								invitePlayer(p, invited,
										town.getTownofPlayer(p.getName()));
								p.sendMessage("§aYou invited §e"
										+ invited.getName()
										+ " §ato your town.");
								return false;
							} else {
								if (town.getTownofPlayer(p.getName())
										.equals(town.getTownofPlayer(invited
												.getName()))) {
									p.sendMessage("§e"
											+ invited.getName()
											+ " §cis already a member of your town!");
									return false;
								}
								p.sendMessage("§e" + invited.getName()
										+ " §cis already a member of a town!");
								return false;
							}
						} else {
							p.sendMessage("§c'" + args[1] + "' is not online.");
							return false;
						}

					}
					if (args[0].equalsIgnoreCase("join")) {
						if (town.getTownofPlayer(p.getName()) != null) {
							p.sendMessage("§cYou're already in a town! §e/town leave");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town join <name>");
							return false;
						}
						if (town.getTown(args[1].toLowerCase()) == null) {
							p.sendMessage("§cThe town§e " + args[1]
									+ " §cdoes not exist!");
							return false;
						}
						Towns t = town.getTown(args[1].toLowerCase());
						if (!t.isOpen()) {
							if (invited.containsKey(p.getName())) {
								if (invited.get(p.getName()).contains(
										t.getName())) {
									town.addMember(t, p.getName());
									sendTownChat(null, "§d" + p.getName()
											+ " has joined the town!", t);
									return false;
								}
								p.sendMessage("§cYou must be invited to this town!");
							}
							p.sendMessage("§cYou must be invited to this town!");
						} else {
							town.addMember(t, p.getName());
							sendTownChat(null, "§d" + p.getName()
									+ " has joined the town!", t);
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("claim")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}

						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "claim")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (town.getTownofChunk(p.getLocation().getChunk()) != null) {
							if (town.getTownofChunk(p.getLocation().getChunk())
									.equals(t)) {
								p.sendMessage("§cYour town already owns this land.");
								return false;

							}
							p.sendMessage("§cYou can not claim this chunk.");
							return false;
						}
						if (ch.getType(p.getLocation().getChunk()
								.getChunkSnapshot()) != "wild") {
							p.sendMessage("§cYou can not claim this chunk.");
							return false;
						}
						if (!t.anotherChunk()) {
							p.sendMessage("§cYour town is at it's size limit. A higher town level means more chunks can be claimed.");
							return false;
						}
						town.addChunk(t, p.getLocation().getChunk());

						ch.addChunk(p.getLocation().getChunk()
								.getChunkSnapshot(), "town");

						for (Player pl : t.getOnlineStaff()) {
							pl.sendMessage("§d" + p.getName()
									+ " claimed a chunk for your town at X:"
									+ (int) p.getLocation().getX() + " Z:"
									+ (int) p.getLocation().getZ());
						}
						return false;
					}

					if (args[0].equalsIgnoreCase("unclaim")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}

						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "unclaim")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (town.getTownofChunk(p.getLocation().getChunk()) != null) {
							if (!town
									.getTownofChunk(p.getLocation().getChunk())
									.equals(t)) {
								p.sendMessage("§cYour town does not own this land!");
								return false;

							}

						} else {
							p.sendMessage("§cYour town does not own this land!");
							return false;
						}

						town.removeChunk(t, p.getLocation().getChunk());
						ch.removeChunk(p.getLocation().getChunk()
								.getChunkSnapshot());

						for (Player pl : t.getOnlineStaff()) {
							pl.sendMessage("§d" + p.getName()
									+ " unclaimed a chunk from your town at X:"
									+ (int) p.getLocation().getX() + " Z:"
									+ (int) p.getLocation().getZ());
						}
						return false;
					}

					if (args[0].equalsIgnoreCase("chat")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						if (usingtownchat.contains(p.getName())) {
							usingtownchat.remove(p.getName());
							p.sendMessage("§cYou are no longer speaking in Town Chat!");
							return false;
						} else {
							usingtownchat.add(p.getName());
							if (usingadminchat.contains(p.getName())) {
								usingadminchat.remove(p.getName());
							}
							p.sendMessage("§aYou are now speaking in Town Chat!");
							return false;
						}

					}
					if (args[0].equalsIgnoreCase("kick")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "kick")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town kick <name>");
							return false;
						}
						String rank = "default";
						if (t.getOwner().equalsIgnoreCase(p.getName())) {
							rank = "o";
						}
						if (t.getModerators().contains(p.getName())) {
							rank = "m";
						}
						if (t.getAdmins().contains(p.getName())) {
							rank = "a";
						}
						if (args[1].equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou can not kick yourself...");
							return false;
						}
						boolean find = false;
						for (String name : t.getMembers()) {
							if (name.equalsIgnoreCase(args[1])) {
								find = true;
								if (rank.equals("default")) {
									p.sendMessage("§cYou can not kick a player the same rank as you or higher.");
									return false;
								}
								if (rank.equals("m")
										&& (t.getModerators().contains(name)
												|| t.getAdmins().contains(name) || t
												.getOwner().equalsIgnoreCase(
														name))) {
									p.sendMessage("§cYou can not kick a player the same rank as you or higher.");
									return false;
								}
								if (rank.equals("a")
										&& (t.getAdmins().contains(name) || t
												.getOwner().equalsIgnoreCase(
														name))) {
									p.sendMessage("§cYou can not kick a player the same rank as you or higher.");
									return false;
								}
								town.removeMember(t, name);
								sendTownChat(null, "§d" + name
										+ " has been kicked from the town by "
										+ p.getName() + "!", t);
								OfflinePlayer pl = Bukkit
										.getOfflinePlayer(name);
								if (pl != null) {
									if (pl.isOnline()) {
										((Player) pl)
												.sendMessage("§cYou have been kicked from the town by "
														+ p.getName() + "!");
									}
								}
							}
						}
						if (!find) {
							p.sendMessage("§cThe player '" + args[1]
									+ "' is not a member of your town.");
							return false;
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("givemayor")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.getOwner().equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cOnly the Mayor can do this!");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town givemayor <name>");
							return false;
						}
						if (args[1].equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou already are the Mayor...");
							return false;
						}
						boolean find = false;
						for (String name : t.getMembers()) {
							if (name.equalsIgnoreCase(args[1])) {
								find = true;
								if (t.getModerators().contains(name)) {
									t.removeModerator(name);
								}
								if (t.getAdmins().contains(name)) {
									t.removeAdmin(name);
								}
								t.setOwner(name);
								t.addAdmin(p.getName());
								sendTownChat(null, "§d" + name
										+ " is the new Mayor of the town!", t);
								p.sendMessage("§cYou are now an admin of what used to be your town.");
								OfflinePlayer pl = Bukkit
										.getOfflinePlayer(name);
								if (pl != null) {
									if (pl.isOnline()) {
										((Player) pl)
												.sendMessage("§aYou are now the Mayor! Use §e/town help §afor useful commands!");
									}
								}
							}
						}
						if (!find) {
							p.sendMessage("§cThe player '" + args[1]
									+ "' is not a member of your town.");
							return false;
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("promote")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "promote")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town promote <name>");
							return false;
						}
						String rank = "default";
						if (t.getOwner().equalsIgnoreCase(p.getName())) {
							rank = "o";
						}

						if (t.getAdmins().contains(p.getName())) {
							rank = "a";
						}
						if (args[1].equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou can not promote yourself...");
							return false;
						}
						if (rank == "default") {
							p.sendMessage("§cYou can not promote a player to, or at the same rank as you or higher.");
							return false;
						}
						boolean find = false;
						for (String name : t.getMembers()) {
							if (name.equalsIgnoreCase(args[1])) {
								find = true;
								if (t.getAdmins().contains(name)) {
									p.sendMessage("§c" + name
											+ " is already an admin.");
									return false;
								}
								if (rank.equals("a")
										&& (t.getAdmins().contains(name) || t
												.getOwner().equalsIgnoreCase(
														name))) {
									p.sendMessage("§cYou can not promote a player to, or at the same rank as you or higher.");
									return false;
								}
								rank = "Moderator";
								if (!t.getModerators().contains(name)) {
									t.addModerator(name);
								} else {
									t.addAdmin(name);
									rank = "Admin";
								}
								sendTownChat(null, "§d" + name
										+ " has been promoted to Town " + rank
										+ " by " + p.getName() + "!", t);
								OfflinePlayer pl = Bukkit
										.getOfflinePlayer(name);
								if (pl != null) {
									if (pl.isOnline()) {
										((Player) pl)
												.sendMessage("§aYou have been promoted to Town "
														+ rank + "!");
									}
								}
							}
						}
						if (!find) {
							p.sendMessage("§cThe player '" + args[1]
									+ "' is not a member of your town.");
							return false;
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("demote")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "demote")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town demote <name>");
							return false;
						}
						if (args[1].equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou can not demote yourself...");
							return false;
						}
						if (!t.getAdmins().contains(p.getName())
								&& !t.getOwner().equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou can not demote a player at the same rank as you or higher.");
							return false;
						}
						boolean find = false;
						for (String name : t.getMembers()) {
							if (name.equalsIgnoreCase(args[1])) {
								find = true;
								if (t.getAdmins().contains(name)
										&& !t.getOwner().equalsIgnoreCase(
												p.getName())) {
									p.sendMessage("§cYou can not demote a player at the same rank as you or higher.");
									return false;
								}

								String rank = "Default";
								if (t.getModerators().contains(name)) {
									t.removeModerator(name);
								} else if (t.getAdmins().contains(name)) {
									t.removeAdmin(name);
									t.addModerator(name);
									rank = "Moderator";
								} else {
									p.sendMessage("§cThis player is a Town Default...");
									return false;
								}
								sendTownChat(null, "§d" + name
										+ " has been demoted to Town " + rank
										+ " by " + p.getName() + ".", t);
								OfflinePlayer pl = Bukkit
										.getOfflinePlayer(name);
								if (pl != null) {
									if (pl.isOnline()) {
										((Player) pl)
												.sendMessage("§aYou have been demoted to Town "
														+ rank + ".");
									}
								}
							}
						}
						if (!find) {
							p.sendMessage("§cThe player '" + args[1]
									+ "' is not a member of your town.");
							return false;
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("leave")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (t.getOwner().equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cYou must pass on the Mayor title before leaving - §e/town givemayor <name>, §cOr you can §4/town disband!");
							return false;
						}
						town.removeMember(t, p.getName());
						sendTownChat(null, "§d" + p.getName()
								+ " has left the town!", t);
						p.sendMessage("§cYou are no longer part of "
								+ t.getName() + "!");
						return false;
					}
					if (args[0].equalsIgnoreCase("disband")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.getOwner().equalsIgnoreCase(p.getName())) {
							p.sendMessage("§cOnly the Mayor can do this!");
							return false;
						}
						if (args.length == 1) {
							p.sendMessage("§cAre you sure you want to do this? The whole town will be gone forever.");
							p.sendMessage("§cType §4/town disband confirmyes§c if you are sure.");
						} else {
							if (args[1].equalsIgnoreCase("confirmyes")) {
								sendTownChat(null, "§c§l" + t.getName()
										+ " has been disbanded by the Mayor ("
										+ t.getOwner()
										+ "). You are no longer in a town.", t);
								deletedtowns.add(t.getName().toLowerCase());
								for (String coords : t.getChunks()) {
									ch.removeChunk(coords);
								}
								allyreq.remove(t.getName());
								neutralreq.remove(t.getName());
								town.deleteTown(t);

								p.sendMessage("§cTown deleted forever.");

							}
						}
						return false;
					}
					if (args[0].equalsIgnoreCase("info")) {

						if (args.length == 1
								&& town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cPlease specify a town name! §e/town info <name>§c.");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (args.length == 1) {
							p.sendMessage("§7--------------------------------------------------");
							p.sendMessage("§bTown Name: §6" + t.getName());
							p.sendMessage("§7--------------------------------------------------");
							p.sendMessage("§bLevel: §6" + (int) t.getLevel());
							p.sendMessage("§bSize: §6" + t.getChunks().size());
							p.sendMessage("§bPopulation: §6"
									+ t.getMembers().size());
							String allies;
							if (t.getAllies().isEmpty()) {
								allies = "none";
							} else {
								allies = t.getAllies().toString();
								allies = allies.replace("[", "");
								allies = allies.replace("]", "");
							}
							String enemies;
							if (t.getEnemies().isEmpty()) {
								enemies = "none";
							} else {
								enemies = t.getEnemies().toString();
								enemies = enemies.replace("[", "");
								enemies = enemies.replace("]", "");
							}
							p.sendMessage("§bAllies: §a" + allies);
							p.sendMessage("§bEnemies: §c" + enemies);
							String online = "";
							String offline = "";
							boolean firstonline = true;
							boolean firstoffline = true;
							for (String name : t.getMembers()) {
								String finalname = "";
								if (t.getOwner().equalsIgnoreCase(name)) {
									finalname = "§5[§6Mayor§5] ";
								}
								if (t.getAdmins().contains(name)) {
									finalname = "§5[§cA§5] ";

								}
								if (t.getModerators().contains(name)) {
									finalname = "§5[§bM§5] ";

								}
								OfflinePlayer pl = Bukkit
										.getOfflinePlayer(name);

								if (pl.isOnline()) {
									if (firstonline) {
										online = finalname + "§a" + name;
										firstonline = false;
									} else {
										online = online + ", " + finalname
												+ "§a" + name;
									}
								} else {
									if (firstoffline) {
										offline = finalname + "§c" + name;
										firstoffline = false;
									} else {
										offline = offline + ", " + finalname
												+ "§c" + name;
									}
								}
							}
							p.sendMessage("§bOnline Members: " + online);
							p.sendMessage("§bOffline Members: " + offline);
							p.sendMessage("§bMOTD: §6\"" + t.getMOTD() + "\"");
							if (t.isOpen()) {

								p.sendMessage("§bJoin Status: §6Open to all.");
							} else {
								p.sendMessage("§bJoin Status: §6Invite only.");
							}
							p.sendMessage("§7--------------------------------------------------");
							return false;
						}
						if (town.getTown(args[1].toLowerCase()) == null) {
							p.sendMessage("§cThe town§e " + args[1]
									+ " §cdoes not exist!");
							return false;
						}
						t = town.getTown(args[1].toLowerCase());
						p.sendMessage("§7--------------------------------------------------");
						p.sendMessage("§bTown Name: §6" + t.getName());
						p.sendMessage("§7--------------------------------------------------");
						p.sendMessage("§bLevel: §6" + (int) t.getLevel());
						p.sendMessage("§bSize: §6" + t.getChunks().size());
						p.sendMessage("§bPopulation: §6"
								+ t.getMembers().size());
						String allies;
						if (t.getAllies().isEmpty()) {
							allies = "none";
						} else {
							allies = t.getAllies().toString();
							allies = allies.replace("[", "");
							allies = allies.replace("]", "");
						}
						String enemies;
						if (t.getEnemies().isEmpty()) {
							enemies = "none";
						} else {
							enemies = t.getEnemies().toString();
							enemies = enemies.replace("[", "");
							enemies = enemies.replace("]", "");
						}
						p.sendMessage("§bAllies: §a" + allies);
						p.sendMessage("§bEnemies: §c" + enemies);
						String online = "";
						String offline = "";
						boolean firstonline = true;
						boolean firstoffline = true;
						for (String name : t.getMembers()) {
							String finalname = "";
							if (t.getOwner().equalsIgnoreCase(name)) {
								finalname = "§5[§6Mayor§5] ";
							}
							if (t.getAdmins().contains(name)) {
								finalname = "§5[§cA§5] ";

							}
							if (t.getModerators().contains(name)) {
								finalname = "§5[§bM§5] ";

							}

							OfflinePlayer pl = Bukkit.getOfflinePlayer(name);
							if (pl.isOnline()) {
								if (firstonline) {
									online = finalname + "§a" + name;
									firstonline = false;
								} else {
									online = online + ", " + finalname + "§a"
											+ name;
								}
							} else {
								if (firstoffline) {
									offline = finalname + "§c" + name;
									firstoffline = false;
								} else {
									offline = offline + ", " + finalname + "§c"
											+ name;
								}
							}
						}
						p.sendMessage("§bOnline Members: " + online);
						p.sendMessage("§bOffline Members: " + offline);
						p.sendMessage("§bMOTD: §6\"" + t.getMOTD() + "\"");
						if (t.isOpen()) {

							p.sendMessage("§bJoin Status: §6Open to all.");
						} else {
							p.sendMessage("§bJoin Status: §6Invite only.");
						}
						p.sendMessage("§7--------------------------------------------------");
						return false;
					}
					if (args[0].equalsIgnoreCase("help")) {
						if (args.length == 1) {
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("§7--------------§6Kirithian Towns Help (1/4)§7--------------");
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("- §e/town info <name>: §7Displays information on a town.");
							p.sendMessage("- §e/town create <name>: §7Creates a town.");
							p.sendMessage("- §e/town invite <name>: §7Invites a player to your town.");
							p.sendMessage("- §e/town join <name>: §7Request membership of a town.");
							p.sendMessage("- §e/town leave: §7Leaves a town.");
							p.sendMessage("- §e/town chat: §7Toggles town chat.");
							p.sendMessage("- §7Type §b/town help 2§7 for the next page of help.");
							return false;
						} else {
							switch (args[1]) {
							case "1":
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("§7--------------§6Kirithian Towns Help (1/4)§7--------------");
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("- §e/town info <name>: §7Displays information on a town.");
								p.sendMessage("- §e/town create <name>: §7Creates a town.");
								p.sendMessage("- §e/town invite <name>: §7Invites a player to your town.");
								p.sendMessage("- §e/town join <name>: §7Request membership of a town.");
								p.sendMessage("- §e/town leave: §7Leaves a town.");
								p.sendMessage("- §e/town chat: §7Toggles town chat.");
								p.sendMessage("- §7Type §b/town help 2§7 for the next page of help.");
								return false;
							case "2":
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("§7--------------§6Kirithian Towns Help (2/4)§7--------------");
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("- §e/town motd: §7Displays the town Message of the Day.");
								p.sendMessage("- §e/town setmotd <message>: §7Sets the town MotD.");
								p.sendMessage("- §e/town home: §7Town home teleportation commands.");
								p.sendMessage("- §e/town sethome: §7Set a town teleportation point.");
								p.sendMessage("- §e/town level: §7Displays information about town XP.");
								p.sendMessage("- §e/town kick <name>: §7Kick a player out of the town.");
								p.sendMessage("- §7Type §b/town help 3§7 for the next page of help.");
								return false;
							case "3":
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("§7--------------§6Kirithian Towns Help (3/4)§7--------------");
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("- §e/town claim: §7Claim a chunk of land for the town.");
								p.sendMessage("- §e/town unclaim: §7Unclaim a chunk.");
								p.sendMessage("- §e/town chunk: §7Town chunk commands.");
								p.sendMessage("- §e/town perm: §7Town permissions.");
								p.sendMessage("- §e/town promote <name>: §7Promote a player in the town.");
								p.sendMessage("- §e/town demote <name>: §7Demote a player in the town.");
								p.sendMessage("- §7Type §b/town help 4§7 for the last page of help.");
								return false;
							case "4":
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("§7--------------§6Kirithian Towns Help (4/4)§7--------------");
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("- §e/town ally <name>: §7Request an alliance with a town.");
								p.sendMessage("- §e/town neutral <name>: §7Request neutrality with a town.");
								p.sendMessage("- §e/town enemy <name>: §7Declare war on a town.");
								p.sendMessage("- §e/town givemayor <name>: §7Pass on the mayor title.");
								p.sendMessage("- §e/town disband: §7Delete the town forever.");
								return false;
							default:
								p.sendMessage("§cInvalid Town Help Page Number.");
								return false;
							}
						}

					}
					if (args[0].equalsIgnoreCase("chunk")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (args.length > 1) {
							if (args[1].equalsIgnoreCase("info")) {
								if (town.getTownofChunk(p.getLocation()
										.getChunk()) != null) {
									if (!town.getTownofChunk(
											p.getLocation().getChunk()).equals(
											t)) {
										p.sendMessage("§cYour town does not own this land!");
										return false;

									}

								} else {
									p.sendMessage("§cYour town does not own this land!");
									return false;
								}
								String coords = p.getLocation().getChunk()
										.getX()
										+ ","
										+ p.getLocation().getChunk().getZ();
								p.sendMessage("§6Chunk at X:§b"
										+ p.getLocation().getBlockX()
										+ " §6Z:§b"
										+ p.getLocation().getBlockZ());
								String members = " §e";
								if (t.getChunkMembers(coords) != null) {
									for (String name : t
											.getChunkMembers(coords)) {
										members = members + name + " ";
									}
								}
								if (members == " §e") {
									members = " §enone";
								}
								String owners = " §e";
								if (t.getChunkOwners(coords) != null) {
									for (String name : t.getChunkOwners(coords)) {
										owners = owners + name + " ";
									}
								}
								if (owners == " §e") {
									owners = " §enone";
								}

								p.sendMessage("§bMembers:" + members + "");
								p.sendMessage("§bOwners:" + owners + "");
								return false;

							}
							if (args[1].equalsIgnoreCase("addmember")) {
								if (!t.hasPermission(p, "setchunkmembers")) {
									p.sendMessage("§cYou do not have permission to do that for your town.");
									return false;
								}
								if (town.getTownofChunk(p.getLocation()
										.getChunk()) != null) {
									if (!town.getTownofChunk(
											p.getLocation().getChunk()).equals(
											t)) {
										p.sendMessage("§cYour town does not own this land!");
										return false;

									}

								} else {
									p.sendMessage("§cYour town does not own this land!");
									return false;
								}
								if (args.length < 3) {
									p.sendMessage("§cUse like this! §e/town chunk addmember <name>");
									return false;
								}
								OfflinePlayer pl = null;
								boolean find = false;
								for (String name : t.getMembers()) {
									if (name.equalsIgnoreCase(args[2])) {
										find = true;
										pl = Bukkit.getOfflinePlayer(name);
									}

								}
								if (!find) {
									p.sendMessage("§cThe player '" + args[2]
											+ "' is not a member of your town.");
									return false;
								}
								String coords = p.getLocation().getChunk()
										.getX()
										+ ","
										+ p.getLocation().getChunk().getZ();
								if (t.isChunkMember(coords, pl.getName())) {
									p.sendMessage("§e"
											+ args[2]
											+ " §cis already a member of this chunk!");
									return false;
								}
								t.addChunkMember(coords, pl.getName());
								p.sendMessage("§e" + args[2]
										+ "§a is now a member of this chunk.");
								return false;
							}
							if (args[1].equalsIgnoreCase("addowner")) {
								if (!t.hasPermission(p, "setchunkowners")) {
									p.sendMessage("§cYou do not have permission to do that for your town.");
									return false;
								}
								if (town.getTownofChunk(p.getLocation()
										.getChunk()) != null) {
									if (!town.getTownofChunk(
											p.getLocation().getChunk()).equals(
											t)) {
										p.sendMessage("§cYour town does not own this land!");
										return false;

									}

								} else {
									p.sendMessage("§cYour town does not own this land!");
									return false;
								}
								if (args.length < 3) {
									p.sendMessage("§cUse like this! §e/town chunk addowner <name>");
									return false;
								}
								OfflinePlayer pl = null;
								boolean find = false;
								for (String name : t.getMembers()) {
									if (name.equalsIgnoreCase(args[2])) {
										find = true;
										pl = Bukkit.getOfflinePlayer(name);
									}

								}
								if (!find) {
									p.sendMessage("§cThe player '" + args[2]
											+ "' is not a member of your town.");
									return false;
								}
								String coords = p.getLocation().getChunk()
										.getX()
										+ ","
										+ p.getLocation().getChunk().getZ();
								if (t.isChunkOwner(coords, pl.getName())) {
									p.sendMessage("§e"
											+ args[2]
											+ " §cis already an owner of this chunk!");
									return false;
								}
								t.addChunkOwner(coords, pl.getName());
								p.sendMessage("§e" + args[2]
										+ "§a is now an owner of this chunk.");
								return false;
							}
							if (args[1].equalsIgnoreCase("removemember")) {
								if (!t.hasPermission(p, "setchunkmembers")) {
									p.sendMessage("§cYou do not have permission to do that for your town.");
									return false;
								}
								if (town.getTownofChunk(p.getLocation()
										.getChunk()) != null) {
									if (!town.getTownofChunk(
											p.getLocation().getChunk()).equals(
											t)) {
										p.sendMessage("§cYour town does not own this land!");
										return false;

									}

								} else {
									p.sendMessage("§cYour town does not own this land!");
									return false;
								}
								if (args.length < 3) {
									p.sendMessage("§cUse like this! §e/town chunk removemember <name>");
									return false;
								}
								boolean find = false;
								OfflinePlayer pl = null;
								for (String name : t.getMembers()) {
									if (name.equalsIgnoreCase(args[2])) {
										find = true;
										pl = Bukkit.getOfflinePlayer(name);
									}

								}
								if (!find) {
									p.sendMessage("§cThe player '" + args[2]
											+ "' is not a member of your town.");
									return false;
								}
								String coords = p.getLocation().getChunk()
										.getX()
										+ ","
										+ p.getLocation().getChunk().getZ();
								if (!t.isChunkMember(coords, pl.getName())) {
									p.sendMessage("§e"
											+ args[2]
											+ " §cis not a member of this chunk!");
									return false;
								}
								t.removeChunkMember(coords, pl.getName());
								ch.removeChunk(p.getLocation().getChunk()
										.getChunkSnapshot());
								p.sendMessage("§e"
										+ args[2]
										+ " §ais no longer a member of this chunk!");
								return false;
							}
							if (args[1].equalsIgnoreCase("removeowner")) {
								if (!t.hasPermission(p, "setchunkowners")) {
									p.sendMessage("§cYou do not have permission to do that for your town.");
									return false;
								}
								if (town.getTownofChunk(p.getLocation()
										.getChunk()) != null) {
									if (!town.getTownofChunk(
											p.getLocation().getChunk()).equals(
											t)) {
										p.sendMessage("§cYour town does not own this land!");
										return false;

									}

								} else {
									p.sendMessage("§cYour town does not own this land!");
									return false;
								}
								if (args.length < 3) {
									p.sendMessage("§cUse like this! §e/town chunk removeowner <name>");
									return false;
								}
								boolean find = false;
								OfflinePlayer pl = null;
								for (String name : t.getMembers()) {
									if (name.equalsIgnoreCase(args[2])) {
										find = true;
										pl = Bukkit.getOfflinePlayer(name);
									}

								}
								if (!find) {
									p.sendMessage("§cThe player '" + args[2]
											+ "' is not a member of your town.");
									return false;
								}
								String coords = p.getLocation().getChunk()
										.getX()
										+ ","
										+ p.getLocation().getChunk().getZ();
								if (!t.isChunkMember(coords, pl.getName())) {
									p.sendMessage("§e"
											+ args[2]
											+ " §cis not an owner of this chunk!");
									return false;
								}
								t.removeChunkMember(coords, pl.getName());
								ch.removeChunk(p.getLocation().getChunk()
										.getChunkSnapshot());
								p.sendMessage("§e"
										+ args[2]
										+ " §ais no longer an owner of this chunk!");
								return false;
							}

							if (args[1].equalsIgnoreCase("perms")
									|| args[1].equalsIgnoreCase("perm")) {
								if (args.length > 2) {
									if (args[2].equalsIgnoreCase("info")) {
										if (args.length < 4) {
											p.sendMessage("§cUse like this: §e/town chunk perm info §b<permission>");
											p.sendMessage("§cPermissions: §bchests, build, use");
											return false;
										} else {
											switch (args[3]) {
											case "use":
												p.sendMessage("§bUse: §aAllows the use of buttons, levers, doors etc.");
												return false;

											case "chests":
												p.sendMessage("§bChests: §aAllows opening of unlocked chests.");
												return false;

											case "build":
												p.sendMessage("§bBuild: §aAllows placing and removing of blocks.");
												return false;

											default:
												p.sendMessage("§cInvalid permission!");
												p.sendMessage("§cPermissions: §bchests, build, use");
												return false;

											}
										}
									}

									if (args[2].equalsIgnoreCase("default")) {
										if (args.length < 5) {
											p.sendMessage("§bChunk Default Permissions:");
											p.sendMessage("§fGreen: §aAllowed§f, §fRed: §cDenied");
											p.sendMessage("§7-----------------------------------------------------");
											String allperms = "use chests build ";
											String perms = t
													.getChunkDefaultPerms();
											String rem = "";
											while (perms.contains(" ")) {
												rem = perms.substring(0,
														perms.indexOf(" "));
												perms = perms.substring(perms
														.indexOf(" ") + 1);
												allperms = allperms.replace(
														rem, "");
											}
											String finalmsg = "§a"
													+ t.getChunkDefaultPerms()
													+ "§c" + allperms;
											while (finalmsg.contains("  ")) {
												finalmsg = finalmsg.replace(
														"  ", " ");
											}
											p.sendMessage("§bPermissions: "
													+ finalmsg);
											p.sendMessage("/§etown chunk perm default <permission> §aallow§f/§cdeny§f: §7Set permissions.");
											return false;
										} else {
											if (!args[3]
													.equalsIgnoreCase("use")
													&& !args[3]
															.equalsIgnoreCase("build")
													&& !args[3]
															.equalsIgnoreCase("chests")) {
												p.sendMessage("§cInvalid permission!");
												p.sendMessage("§cPermissions: §bchests, build, use");
												return false;
											}
											if (args.length > 4) {
												if (!args[4]
														.equalsIgnoreCase("allow")
														&& !args[4]
																.equalsIgnoreCase("deny")) {
													p.sendMessage("§cUse like this: §e/town chunk perm default "
															+ args[3]
															+ " §aallow§f/§cdeny");
													return false;
												} else {
													if (args[4]
															.equalsIgnoreCase("allow")) {
														if (!t.getOwner()
																.equals(p
																		.getName())) {
															p.sendMessage("§cOnly the §6Mayor §chas access to these commands!");
															return false;
														}
														String permission = args[3];
														if (!t.getChunkDefaultPerms()
																.contains(
																		permission)) {
															t.setChunkDefaultPerms(t
																	.getChunkDefaultPerms()
																	+ permission
																	+ " ");
															p.sendMessage("§aChunk default permission allowed: §b"
																	+ args[3]
																			.toLowerCase());
														} else {
															p.sendMessage("§cChunk default already have the §b"
																	+ args[3]
																			.toLowerCase()
																	+ "§c permission.");
														}
														return false;
													}
													if (args[4]
															.equalsIgnoreCase("deny")) {
														if (!t.getOwner()
																.equals(p
																		.getName())) {
															p.sendMessage("§cOnly the §6Mayor §chas access to these commands!");
															return false;
														}
														String permission = args[3];

														if (t.getChunkDefaultPerms()
																.contains(
																		permission)) {
															String permss = t
																	.getChunkDefaultPerms();
															permss = permss
																	.replace(
																			permission
																					+ " ",
																			"");
															t.setChunkDefaultPerms(permss);
															p.sendMessage("§aChunk default permission §cdenied§a: §b"
																	+ args[3]
																			.toLowerCase());
														} else {
															p.sendMessage("§cChunk default don't have the §b"
																	+ args[3]
																			.toLowerCase()
																	+ "§c permission.");
														}
														return false;

													}
												}
											}

										}

									}
									if (args[2].equalsIgnoreCase("member")) {
										if (args.length < 5) {
											p.sendMessage("§bChunk Member Permissions:");
											p.sendMessage("§fGreen: §aAllowed§f, §fRed: §cDenied");
											p.sendMessage("§7-----------------------------------------------------");
											String allperms = "use chests build ";
											String perms = t
													.getChunkMemberPerms();
											String rem = "";
											while (perms.contains(" ")) {
												rem = perms.substring(0,
														perms.indexOf(" "));
												perms = perms.substring(perms
														.indexOf(" ") + 1);
												allperms = allperms.replace(
														rem, "");
											}
											String finalmsg = "§a"
													+ t.getChunkMemberPerms()
													+ "§c" + allperms;
											while (finalmsg.contains("  ")) {
												finalmsg = finalmsg.replace(
														"  ", " ");
											}
											p.sendMessage("§bPermissions: "
													+ finalmsg);
											p.sendMessage("/§etown chunk perm member <permission> §aallow§f/§cdeny§f: §7Set permissions.");
											return false;
										} else {
											if (!args[3]
													.equalsIgnoreCase("use")
													&& !args[3]
															.equalsIgnoreCase("build")
													&& !args[3]
															.equalsIgnoreCase("chests")) {
												p.sendMessage("§cInvalid permission!");
												p.sendMessage("§cPermissions: §bchests, build, use");
												return false;
											}
											if (args.length > 4) {
												if (!args[4]
														.equalsIgnoreCase("allow")
														&& !args[4]
																.equalsIgnoreCase("deny")) {
													p.sendMessage("§cUse like this: §e/town chunk perm member "
															+ args[3]
															+ " §aallow§f/§cdeny");
													return false;
												} else {
													if (args[4]
															.equalsIgnoreCase("allow")) {
														String permission = args[3];
														if (!t.getChunkMemberPerms()
																.contains(
																		permission)) {
															t.setChunkMemberPerms(t
																	.getChunkMemberPerms()
																	+ permission
																	+ " ");
															p.sendMessage("§aChunk members permission allowed: §b"
																	+ args[3]
																			.toLowerCase());
														} else {
															p.sendMessage("§cChunk members already have the §b"
																	+ args[3]
																			.toLowerCase()
																	+ "§c permission.");
														}
														return false;
													}
													if (args[4]
															.equalsIgnoreCase("deny")) {
														String permission = args[3];

														if (t.getChunkMemberPerms()
																.contains(
																		permission)) {
															String permss = t
																	.getChunkMemberPerms();
															permss = permss
																	.replace(
																			permission
																					+ " ",
																			"");
															t.setChunkMemberPerms(permss);
															p.sendMessage("§aChunk members permission §cdenied§a: §b"
																	+ args[3]
																			.toLowerCase());
														} else {
															p.sendMessage("§cChunk members don't have the §b"
																	+ args[3]
																			.toLowerCase()
																	+ "§c permission.");
														}
														return false;

													}
												}
											}

										}
									}
									if (args[2].equalsIgnoreCase("owner")) {
										if (args.length < 5) {
											p.sendMessage("§bChunk owner Permissions:");
											p.sendMessage("§fGreen: §aAllowed§f, §fRed: §cDenied");
											p.sendMessage("§7-----------------------------------------------------");
											String allperms = "use chests build ";
											String perms = t
													.getChunkOwnerPerms();
											String rem = "";
											while (perms.contains(" ")) {
												rem = perms.substring(0,
														perms.indexOf(" "));
												perms = perms.substring(perms
														.indexOf(" ") + 1);
												allperms = allperms.replace(
														rem, "");
											}
											String finalmsg = "§a"
													+ t.getChunkOwnerPerms()
													+ "§c" + allperms;
											while (finalmsg.contains("  ")) {
												finalmsg = finalmsg.replace(
														"  ", " ");
											}
											p.sendMessage("§bPermissions: "
													+ finalmsg);
											p.sendMessage("/§etown chunk perm owner <permission> §aallow§f/§cdeny§f: §7Set permissions.");
											return false;
										} else {
											if (!args[3]
													.equalsIgnoreCase("use")
													&& !args[3]
															.equalsIgnoreCase("build")
													&& !args[3]
															.equalsIgnoreCase("chests")) {
												p.sendMessage("§cInvalid permission!");
												p.sendMessage("§cPermissions: §bchests, build, use");
												return false;
											}
											if (args.length > 4) {
												if (!args[4]
														.equalsIgnoreCase("allow")
														&& !args[4]
																.equalsIgnoreCase("deny")) {
													p.sendMessage("§cUse like this: §e/town chunk perm owner "
															+ args[3]
															+ " §aallow§f/§cdeny");
													return false;
												} else {
													if (args[4]
															.equalsIgnoreCase("allow")) {
														String permission = args[3];
														if (!t.getChunkOwnerPerms()
																.contains(
																		permission)) {
															t.setChunkOwnerPerms(t
																	.getChunkOwnerPerms()
																	+ permission
																	+ " ");
															p.sendMessage("§aChunk owners permission allowed: §b"
																	+ args[3]
																			.toLowerCase());
														} else {
															p.sendMessage("§cChunk owners already have the §b"
																	+ args[3]
																			.toLowerCase()
																	+ "§c permission.");
														}
														return false;
													}
													if (args[4]
															.equalsIgnoreCase("deny")) {
														String permission = args[3];

														if (t.getChunkOwnerPerms()
																.contains(
																		permission)) {
															String permss = t
																	.getChunkOwnerPerms();
															permss = permss
																	.replace(
																			permission
																					+ " ",
																			"");
															t.setChunkOwnerPerms(permss);
															p.sendMessage("§aChunk owners permission §cdenied§a: §b"
																	+ args[3]
																			.toLowerCase());
														} else {
															p.sendMessage("§cChunk owners don't have the §b"
																	+ args[3]
																			.toLowerCase()
																	+ "§c permission.");
														}
														return false;

													}
												}
											}

										}
									}
									p.sendMessage("§7-----------------------------------------------------");
									p.sendMessage("§7-------------------§6Land Permissions§7-------------------");
									p.sendMessage("§7-----------------------------------------------------");
									p.sendMessage("- §e/town chunk perm §ainfo: §7Information about each permission.");
									p.sendMessage("- §e/town chunk perm §fdefault: §7Lists the default permissions.");
									p.sendMessage("- §e/town chunk perm §bmember: §7Lists the member permissions.");
									p.sendMessage("- §e/town chunk perm §cowner: §7Lists the owner permissions.");
									p.sendMessage("§7-----------------------------------------------------");
									return false;
								}
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("§7-------------------§6Land Permissions§7-------------------");
								p.sendMessage("§7-----------------------------------------------------");
								p.sendMessage("- §e/town chunk perm §ainfo: §7Information about each permission.");
								p.sendMessage("- §e/town chunk perm §fdefault: §7Lists the default permissions.");
								p.sendMessage("- §e/town chunk perm §bmember: §7Lists the member permissions.");
								p.sendMessage("- §e/town chunk perm §cowner: §7Lists the owner permissions.");
								p.sendMessage("§7-----------------------------------------------------");
								return false;
							}
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("§7-------------------§6Chunk Commands§7-------------------");
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("- §e/town chunk §6info: §7Lists members and owners of the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §aperm: §7Chunk permission commands.");
							p.sendMessage("- §e/town chunk §faddmember§e <name>: §7Adds a member to the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §faddowner§e <name>: §7Adds an owner to the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §cremovemember §e<name>: §7Removes a member from the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §cremoveowner §e<name>: §7Removes an owner from the chunk you are standing in.");
							p.sendMessage("§7-----------------------------------------------------");
							return false;
						} else {
							// chunk help
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("§7-------------------§6Chunk Commands§7-------------------");
							p.sendMessage("§7-----------------------------------------------------");
							p.sendMessage("- §e/town chunk §6info: §7Lists members and owners of the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §aperm: §7Chunk permission commands.");
							p.sendMessage("- §e/town chunk §faddmember§e <name>: §7Adds a member to the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §faddowner§e <name>: §7Adds an owner to the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §cremovemember §e<name>: §7Removes a member from the chunk you are standing in.");
							p.sendMessage("- §e/town chunk §cremoveowner §e<name>: §7Removes an owner from the chunk you are standing in.");
							p.sendMessage("§7-----------------------------------------------------");
							return false;
						}

					}
					if (args[0].equalsIgnoreCase("ally")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "allegiance")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town ally <townname>");
							return false;
						}
						if (town.getTown(args[1].toLowerCase()) == null) {
							p.sendMessage("§cThe town§e " + args[1]
									+ " §cdoes not exist!");
							return false;
						}
						Towns req = town.getTown(args[1].toLowerCase());
						if (town.isAlly(t, req)) {
							p.sendMessage("§cYou are already §aallies with §e"
									+ req.getName() + "§c!");
							return false;
						}

						if (allyreq.get(req.getName()).contains(t.getName())) {
							for (Player pl : req.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §aThe town §e"
										+ req.getName()
										+ " §ahas accepted your alliance request!");
							}
							for (Player pl : t.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §e"
										+ p.getName() + " §a has accepted §e"
										+ req.getName()
										+ "'s §aalliance request!");
							}
							town.setAlly(t, req);
							return false;
						}

						if (!allyreq.get(t.getName()).contains(req.getName())) {
							for (Player pl : req.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §aThe town §e"
										+ t.getName()
										+ " §ahas requested an alliance. Type §e/town ally "
										+ t.getName() + " §ato accept.");
							}
							for (Player pl : t.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §e"
										+ p.getName() + " §ahas sent §e"
										+ req.getName()
										+ " §aan alliance request!");
							}
							List<String> l = allyreq.get(t.getName());
							l.add(req.getName());
							allyreq.put(t.getName(), l);
							final Towns freq = req;
							final Towns ft = t;
							Bukkit.getScheduler().scheduleSyncDelayedTask(this,
									new Runnable() {
										public void run() {
											allyreq.get(ft.getName()).remove(
													freq.getName());
										}
									}, 1200);
							return false;
						}

						p.sendMessage("§cYou have already sent an alliance request to this town.");
						return false;
					}
					if (args[0].equalsIgnoreCase("enemy")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "allegiance")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town enemy <townname>");
							return false;
						}
						if (town.getTown(args[1].toLowerCase()) == null) {
							p.sendMessage("§cThe town§e " + args[1]
									+ " §cdoes not exist!");
							return false;
						}
						Towns req = town.getTown(args[1].toLowerCase());
						if (town.isEnemy(t, req)) {
							p.sendMessage("§cYou are already enemies with §e"
									+ req.getName() + "§c!");
							return false;
						}
						for (Player pl : req.getOnlineStaff()) {
							pl.sendMessage("§5[§6Allegiance§5] §cThe town §e"
									+ t.getName()
									+ " §chas declared you as an enemy!");
						}
						for (Player pl : t.getOnlineStaff()) {
							pl.sendMessage("§5[§6Allegiance§5] §e"
									+ p.getName()
									+ " §chas declared the town §e"
									+ req.getName() + " §cas an enemy!");
						}
						town.setEnemies(t, req);
						return false;
					}
					if (args[0].equalsIgnoreCase("neutral")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());
						if (!t.hasPermission(p, "allegiance")) {
							p.sendMessage("§cYou do not have permission to do that for your town.");
							return false;
						}
						if (args.length < 2) {
							p.sendMessage("§cUse like this! §e/town neutral <townname>");
							return false;
						}
						if (town.getTown(args[1].toLowerCase()) == null) {
							p.sendMessage("§cThe town§e " + args[1]
									+ " §cdoes not exist!");
							return false;
						}
						Towns req = town.getTown(args[1].toLowerCase());
						if (town.isNeutral(t, req)) {
							p.sendMessage("§cYou are already §eneutral with §e"
									+ req.getName() + "§c!");
							return false;
						}

						if (town.isAlly(t, req)) {
							for (Player pl : req.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §aYou now have a neutral standing with §e"
										+ req.getName() + "§a!");
							}
							for (Player pl : t.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §e"
										+ req.getName()
										+ " §aset a §eneutral §astanding with you. You are no longer allies.");
							}
							town.setNeutrals(t, req);
							return false;

						}

						if (neutralreq.get(req.getName()).contains(t.getName())) {
							for (Player pl : req.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §aThe town §e"
										+ req.getName()
										+ " §ahas accepted your neutral request!");
							}
							for (Player pl : t.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §e"
										+ p.getName() + " §ahas accepted §e"
										+ req.getName()
										+ "'s §eneutral §arequest!");
							}
							town.setNeutrals(t, req);
							return false;
						}

						if (!neutralreq.get(t.getName())
								.contains(req.getName())) {
							for (Player pl : req.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §aThe town §e"
										+ t.getName()
										+ " §ahas requested neutrality. Type §e/town neutral "
										+ t.getName() + " §ato accept.");
							}
							for (Player pl : t.getOnlineStaff()) {
								pl.sendMessage("§5[§6Allegiance§5] §e"
										+ p.getName() + " §ahas sent §e"
										+ req.getName()
										+ "§a a neutral request!");
							}
							List<String> l = neutralreq.get(t.getName());
							l.add(req.getName());
							neutralreq.put(t.getName(), l);
							final Towns freq = req;
							final Towns ft = t;
							Bukkit.getScheduler().scheduleSyncDelayedTask(this,
									new Runnable() {
										public void run() {
											neutralreq.get(ft.getName())
													.remove(freq.getName());
										}
									}, 1200);
							return false;
						}

						p.sendMessage("§cYou have already sent a neutral request to this town.");
						return false;
					}
					if (args[0].equalsIgnoreCase("level")) {
						if (town.getTownofPlayer(p.getName()) == null) {
							p.sendMessage("§cYou're not in a town!");
							return false;
						}
						Towns t = town.getTownofPlayer(p.getName());

						Double pc = 50 * (t.getCurrentXp() / t
								.getTotalXptoLevel());
						if (t.getLevel() == 1) {
							pc = 50 * (t.getCurrentXp() / t
									.getTotalXptoALevel(t.getLevel() + 1));
						}
						int greens = (int) Math.round(pc);
						int i = 0;
						String bar = "§6[";
						while (i < 50) {
							if (i < greens) {
								bar = bar + "§a|";
							} else {
								bar = bar + "§c|";
							}
							i++;
						}
						bar = bar + "§6]";
						p.sendMessage("§f-----------------------------------------------------");
						p.sendMessage("§f---------------------§6Town Level§f---------------------");
						p.sendMessage("§f-----------------------------------------------------");
						p.sendMessage("§bCurrent Level: §6"
								+ (int) t.getLevel());
						p.sendMessage("§bCurrent XP: §6" + (int) t.getTotalXp());
						if (t.getLevel() == 1) {
							p.sendMessage("§60 "
									+ bar
									+ " §6"
									+ (int) t.getTotalXptoALevel(t.getLevel() + 1));
						} else {
							p.sendMessage("§6"
									+ (int) t.getTotalXptoALevel(t.getLevel())
									+ " "
									+ bar
									+ " §6"
									+ (int) t.getTotalXptoALevel(t.getLevel() + 1));
						}
						p.sendMessage("§f-----------------------------------------------------");

					}
					p.sendMessage("§7-----------------------------------------------------");
					p.sendMessage("§7--------------§6Kirithian Towns Help (1/4)§7--------------");
					p.sendMessage("§7-----------------------------------------------------");
					p.sendMessage("- §e/town info <name>: §7Displays information on a town.");
					p.sendMessage("- §e/town create <name>: §7Creates a town.");
					p.sendMessage("- §e/town invite <name>: §7Invites a player to your town.");
					p.sendMessage("- §e/town join <name>: §7Request membership of a town.");
					p.sendMessage("- §e/town leave: §7Leaves a town.");
					p.sendMessage("- §e/town chat: §7Toggles town chat.");
					p.sendMessage("- §7Type §b/town help 2§7 for the next page of help.");
					return false;

				} else {
					p.sendMessage("§cYou do not have permission for any town commands.");
					return false;
				}

			} else {
				p.sendMessage("§7-----------------------------------------------------");
				p.sendMessage("§7--------------§6Kirithian Towns Help (1/4)§7--------------");
				p.sendMessage("§7-----------------------------------------------------");
				p.sendMessage("- §e/town info <name>: §7Displays information on a town.");
				p.sendMessage("- §e/town create <name>: §7Creates a town.");
				p.sendMessage("- §e/town invite <name>: §7Invites a player to your town.");
				p.sendMessage("- §e/town join <name>: §7Request membership of a town.");
				p.sendMessage("- §e/town leave: §7Leaves a town.");
				p.sendMessage("- §e/town chat: §7Toggles town chat.");
				p.sendMessage("- §7Type §b/town help 2§7 for the next page of help.");
				return false;
			}
		}
		return false;

	}

	public void sendTownChat(Player sender, String message, Towns town) {
		if (sender == null) {
			for (Player p : town.getOnlineMembers()) {
				p.sendMessage(message);
			}
			return;
		}
		String m = "§5" + sender.getName() + ": " + message;
		if (town.getModerators().contains(sender.getName())) {
			m = "§5[§bM§5] " + m;

		}
		if (town.getAdmins().contains(sender.getName())) {
			m = "§5[§cA§5] " + m;
		}
		if (town.getOwner().equalsIgnoreCase(sender.getName())) {
			m = "§5[§6Mayor§5] " + m;
		}
		for (Player p : town.getOnlineMembers()) {
			p.sendMessage(m);
		}

	}

	public void invitePlayer(Player inviter, Player player, Towns town) {
		if (invited.get(player.getName()) != null) {
			if (invited.get(player.getName()).contains(town.getName())) {
				return;
			}
			List<String> ls = invited.get(player.getName());
			ls.add(town.getName());
			invited.put(player.getName(), ls);
		} else {
			List<String> ls = new ArrayList<String>();
			ls.add(town.getName());
			invited.put(player.getName(), ls);
		}
		player.sendMessage("§bYou have been invited to §e" + town.getName()
				+ " §bby §e" + inviter.getName() + ".");
		player.sendMessage("§bType §e/town join " + town.getName()
				+ " §bin the next 30 seconds to accept and join.");
		final Player p = player;
		final Towns t = town;
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (invited.get(p.getName()).size() > 1) {
					List<String> ls = invited.get(p.getName());
					ls.remove(t.getName());
					invited.put(p.getName(), ls);
				} else {
					invited.remove(p.getName());
				}
			}
		}, 600);

	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.getMessage().startsWith("/")) {
			return;
		}
		if (usingadminchat.contains(e.getPlayer().getName())) {
			e.setCancelled(true);
			for (String name : allowed) {
				OfflinePlayer pl = Bukkit.getOfflinePlayer(name);
				if (pl.isOnline()) {

					((Player) pl).sendMessage("§b[§cA§b] §f"
							+ e.getPlayer().getName() + "§7: §b"
							+ e.getMessage());
				}
			}
		} else if (usingtownchat.contains(e.getPlayer().getName())) {
			e.setCancelled(true);
			sendTownChat(e.getPlayer(), e.getMessage(),
					town.getTownofPlayer(e.getPlayer().getName()));
		}
	}





	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (e.getFrom().getChunk().equals(e.getTo().getChunk())) {
			return;
		} else {
			ChunkSnapshot chunkto = e.getTo().getChunk().getChunkSnapshot();
			ChunkSnapshot chunkfrom = e.getFrom().getChunk().getChunkSnapshot();
			Player p = e.getPlayer();

			if (ch.getType(chunkfrom).equals("town")
					&& ch.getType(chunkto).equals("town")) {
				Towns from = town.getTownofChunk(chunkfrom);
				Towns to = town.getTownofChunk(chunkto);
				if (from.equals(to)) {
					return;
				} else {
					p.sendMessage("§bTerritory of §e" + to.getName() + "§b!");
					return;
				}

			}
			if (ch.getType(chunkfrom).equals("player")
					&& ch.getType(chunkto).equals("player")) {
				String namefrom = kp.getPlayerofChunk(chunkfrom);
				String nameto = kp.getPlayerofChunk(chunkto);
				if (namefrom.equals(nameto)) {
					return;
				} else {
					p.sendMessage("§e" + nameto + "'s §6Private Chunk");
					return;
				}
			}
			if (ch.getType(chunkfrom).equals("admin")
					&& ch.getType(chunkto).equals("admin")) {
				AdminTowns from = at.getTownbyName(chunkfrom.getX() + ","
						+ chunkfrom.getZ());
				AdminTowns to = at.getTownbyName(chunkto.getX() + ","
						+ chunkto.getZ());
				if (from.equals(to)) {
					return;
				} else {
					if (to.isSilent() || from.isSilent()) {
						return;
					}
					if (to.getMessage().equalsIgnoreCase("none")) {
						return;
					}
					p.sendMessage(to.getMessage());
					return;
				}

			}
			if (ch.getType(chunkfrom).equals(ch.getType(chunkto))) {
				return;
			}
			if (ch.getType(chunkto).equals("wild")) {
				p.sendMessage("§7The Wild.");
				return;
			}
			if (ch.getType(chunkto).equals("town")) {
				p.sendMessage("§bTerritory of §e"
						+ town.getTownofChunk(chunkto).getName() + "§b!");
				return;
			}
			if (ch.getType(chunkto).equals("admin")) {
				AdminTowns to = at.getTownbyName(chunkto.getX() + ","
						+ chunkto.getZ());
				if (to.getMessage().equalsIgnoreCase("none")) {
					return;
				}
				p.sendMessage(to.getMessage());
				return;
			}
			if (ch.getType(chunkto).equals("player")) {
				String nameto = kp.KP(kp.getPlayerofChunk(chunkto)).getName();
				p.sendMessage("§e" + nameto + "'s §6Private Chunk");
				return;
			}
		}
	}

	@EventHandler
	public void onDmg(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (e.getCause().equals(DamageCause.FALL)) {
				if (nofalldmg.contains(p.getName())) {
					nofalldmg.remove(p.getName());
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onEntityBreak(HangingBreakByEntityEvent e) {
		Chunk c = e.getEntity().getLocation().getChunk();
		ChunkSnapshot cs = c.getChunkSnapshot();
		Player p = null;
		if (e.getRemover() instanceof Player) {
			p = (Player) e.getRemover();
		} else if (e.getRemover() instanceof Projectile) {
			Projectile pr = (Projectile) e.getRemover();
			if (pr.getShooter() instanceof Player) {
				p = (Player) pr.getShooter();
			} else {
				if (!ch.getType(cs).equals("wild")) {
					e.setCancelled(true);
				}
			}
		} else {
			if (!ch.getType(cs).equals("wild")) {
				e.setCancelled(true);
			}
		}
		if (e.getEntity() instanceof ItemFrame
				|| e.getEntity() instanceof Painting) {

			if (ch.getType(cs).equals("admin")) {
				AdminTowns a = at.getTownbyName(c.getX() + "," + c.getZ());
				if (a.getDeniedPerms().contains("build")) {
					e.setCancelled(true);
					if (Math.random() < 0.1) {
						p.sendMessage("§cYou can not build or break blocks here!");
					}
				}
				return;
			}
			if (ch.getType(cs).equals("town")) {
				Towns t = town.getTownofChunk(c);
				if (!t.hasChunkPermission(c, p, "build")) {
					if (Math.random() < 0.1) {
						p.sendMessage("§cYou can not build or break blocks here!");
					}
					e.setCancelled(true);
					return;
				}

			}
			if (ch.getType(cs).equals("player")) {
				KPlayer kpl = kp.KP(kp.getPlayerofChunk(c));
				if (kpl.getName().equals(p.getName())) {
					return;
				}
				if (!kpl.isChunkFriend(c, p.getName())) {
					if (!kpl.getDefaultPerms().contains("build")) {
						p.sendMessage("§cYou can not build or break blocks here!");
						e.setCancelled(true);

					}
					return;
				}
				if (!kpl.getFriendPerms().contains("build")) {
					p.sendMessage("§cYou can not build or break blocks here!");
					e.setCancelled(true);
					return;

				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		final Block b = e.getClickedBlock();
		if (b == null) {
			return;
		}
		Player p = e.getPlayer();
		if (!p.isOp()) {
			
		
		if (e.getAction().equals(Action.RIGHT_CLICK_AIR)
				|| e.getAction().equals(Action.LEFT_CLICK_AIR)) {
			return;
			// TODO any spells or abilities on interact?
		}
		Material m = e.getClickedBlock().getType();
		// "lever", "button", "door", "pressure", "tripwire", "repeater",
		// "chest", "furnace", "hopper", "build", "enderchest", "trapdoor")
		if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		if (m.equals(Material.LEVER) || m.equals(Material.WOOD_BUTTON)
				|| m.equals(Material.STONE_BUTTON)
				|| m.equals(Material.WOOD_DOOR)
				|| m.equals(Material.WOOD_PLATE)
				|| m.equals(Material.STONE_PLATE)
				|| m.equals(Material.IRON_PLATE)
				|| m.equals(Material.GOLD_PLATE) || m.equals(Material.TRIPWIRE)
				|| m.equals(Material.TRIPWIRE_HOOK)
				|| m.equals(Material.DIODE_BLOCK_OFF)
				|| m.equals(Material.DIODE_BLOCK_ON)
				|| m.equals(Material.REDSTONE_COMPARATOR_OFF)
				|| m.equals(Material.REDSTONE_COMPARATOR_ON)
				|| m.equals(Material.CHEST) || m.equals(Material.TRAPPED_CHEST)
				|| m.equals(Material.FURNACE) || m.equals(Material.HOPPER)
				|| m.equals(Material.HOPPER_MINECART)
				|| m.equals(Material.MINECART)
				|| m.equals(Material.ENDER_CHEST)
				|| m.equals(Material.TRAP_DOOR)) {

			Chunk c = e.getClickedBlock().getLocation().getChunk();
			ChunkSnapshot cs = c.getChunkSnapshot();
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if (ch.getType(cs).equals("wild")) {
					return;
				}
				if (ch.getType(cs).equals("admin")) {
					AdminTowns a = at.getTownbyName(c.getX() + "," + c.getZ());
					List<String> denied = a.getDeniedPerms();
					if (m.equals(Material.LEVER)) {
						if (denied.contains("lever")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use levers here!");
							return;
						}
					}
					if (m.equals(Material.WOOD_BUTTON)
							|| m.equals(Material.STONE_BUTTON)) {
						if (denied.contains("button")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use buttons here!");
							return;
						}
					}
					if (m.equals(Material.WOOD_DOOR)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use doors here!");
							return;
						}
					}
					if (m.equals(Material.WOOD_PLATE)
							|| m.equals(Material.STONE_PLATE)
							|| m.equals(Material.IRON_PLATE)
							|| m.equals(Material.GOLD_PLATE)) {
						String l = b.getX() + "," + b.getY() + "," + b.getZ();

						if (!cannons.containsKey(l)) {

							if (denied.contains("pressure")) {
								e.setCancelled(true);
								p.sendMessage("§cYou can not use pressure plates here!");
								return;
							}
						}
					}
					if (m.equals(Material.TRIPWIRE)
							|| m.equals(Material.TRIPWIRE_HOOK)) {
						if (denied.contains("tripwire")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use tripwire here!");
							return;
						}
					}
					if (m.equals(Material.DIODE_BLOCK_OFF)
							|| m.equals(Material.DIODE_BLOCK_ON)
							|| m.equals(Material.REDSTONE_COMPARATOR_OFF)
							|| m.equals(Material.REDSTONE_COMPARATOR_ON)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not alter repeaters here!");
							return;
						}
					}
					if (m.equals(Material.CHEST)
							|| m.equals(Material.TRAPPED_CHEST)) {
						if (denied.contains("door")) {
							if (denied.contains("chestview")) {
								e.setCancelled(true);
								p.sendMessage("§cYou can not access chests here!");
								return;
							} else {
								// TODO chest view but not alter contents
							}
						}
					}
					if (m.equals(Material.FURNACE)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use furnaces here!");
							return;
						}
					}
					if (m.equals(Material.HOPPER)
							|| m.equals(Material.HOPPER_MINECART)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use hoppers here!");
							return;
						}
					}
					if (m.equals(Material.MINECART)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use minecarts here!");
							return;
						}
					}
					if (m.equals(Material.ENDER_CHEST)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not access enderchests here!");
							return;
						}
					}
					if (m.equals(Material.TRAP_DOOR)) {
						if (denied.contains("door")) {
							e.setCancelled(true);
							p.sendMessage("§cYou can not use trap-doors here!");
							return;
						}
					}

				}
				if (ch.getType(cs).equals("town")) {
					Towns t = town.getTownofChunk(c);
					if (m.equals(Material.CHEST)) {
						if (!t.hasChunkPermission(c, p, "chests")) {
							p.sendMessage("§cYou can't use chests here!");
							e.setCancelled(true);
							return;
						}
					}
					if (!t.hasChunkPermission(c, p, "use")) {
						if (Math.random() < 0.1) {
							p.sendMessage("§cYou can't use this here!");
						}
						e.setCancelled(true);
						return;
					}

				}
				if (ch.getType(cs).equals("player")) {
					KPlayer kpl = kp.KP(kp.getPlayerofChunk(c));
					if (kpl.getName().equals(p.getName())) {
						return;
					}
					if (m.equals(Material.CHEST)) {
						if (!kpl.isChunkFriend(c, p.getName())) {
							if (!kpl.getDefaultPerms().contains("chests")) {
								p.sendMessage("§cYou can not use chests here!");
								e.setCancelled(true);

							}
							return;
						}
						if (!kpl.getFriendPerms().contains("chests")) {
							p.sendMessage("§cYou can not use chests here!");
							e.setCancelled(true);
							return;
						}
					}
					if (!kpl.isChunkFriend(c, p.getName())) {
						if (!kpl.getDefaultPerms().contains("use")) {
							p.sendMessage("§cYou can not use this here!");
							e.setCancelled(true);

						}
						return;
					}
					if (!kpl.getFriendPerms().contains("use")) {
						p.sendMessage("§cYou can not use this here!");
						e.setCancelled(true);
						return;
					}
				}
				return;
			}
		}
	}

		if (b.getType() == Material.STONE_PLATE) {
			if (e.getPlayer().isOp()) {
				try {
					if (e.getPlayer().getItemInHand().getType()
							.equals(Material.BEDROCK)) {
						return;
					}
				} catch (Exception ex) {

				}
			}

			final String loc = b.getX() + "," + b.getY() + "," + b.getZ();
			final Player pl = e.getPlayer();
			if (cannons.containsKey(loc)) {
				if (justfired.containsKey(pl.getName())) {
					if (justfired.get(pl.getName()).contains(loc)) {
						e.setCancelled(true);
						return;
					}
				}
				if (justfired.containsKey(pl.getName())) {
					List<String> ls = justfired.get(p.getName());
					ls.add(loc);
					justfired.put(pl.getName(), ls);
				} else {
					List<String> ls = new ArrayList<String>();
					ls.add(loc);
					justfired.put(pl.getName(), ls);
				}
				if (cannonpermreq.containsKey(loc)) {
					if (!pl.hasPermission(cannonpermreq.get(loc))) {
						if (cannonmsg.containsKey(loc)) {
							pl.sendMessage(cannonmsg.get(loc));
						}
						Bukkit.getScheduler().scheduleSyncDelayedTask(this,
								new Runnable() {
									public void run() {
										List<String> ls = justfired.get(pl
												.getName());
										ls.remove(loc);
										if (ls.isEmpty()) {
											justfired.remove(pl.getName());
										} else {
											justfired.put(pl.getName(), ls);
										}
									}
								}, 100);
						e.setCancelled(true);
						return;
					}
				}

				Bukkit.getScheduler().scheduleSyncDelayedTask(this,
						new Runnable() {
							public void run() {
								List<String> ls = justfired.get(pl.getName());
								ls.remove(loc);
								if (ls.isEmpty()) {
									justfired.remove(pl.getName());
								} else {
									justfired.put(pl.getName(), ls);
								}

							}
						}, 20);

				final Vector v = cannons.get(loc);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this,
						new Runnable() {
							public void run() {
								pl.setVelocity(v);
							}
						}, 1);
				repeatvel(v, pl, cannonsrep.get(loc));
				p.getWorld().playSound(pl.getLocation(), Sound.EXPLODE, 5, 1);
				ParticleEffect.SMOKE.display(pl.getLocation(), 0, 0, 0, 1, 20);
				ParticleEffect.EXPLODE.display(pl.getLocation(), 0, 0, 0, 1, 3);

				e.setCancelled(true);

			}

		}
		if (creatingmine.contains(p.getName())){
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				String loc = b.getLocation().getBlockX()+","+b.getLocation().getBlockY()+","+b.getLocation().getBlockZ();
				settingpoints.put(p.getName()+"right", loc);
				p.sendMessage("§aSet right click point to "+loc);
				p.sendMessage("§aOnce both points are set, type §e/mine name <name>");
				e.setCancelled(true);
				return;
			}
			if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
				String loc = b.getLocation().getBlockX()+","+b.getLocation().getBlockY()+","+b.getLocation().getBlockZ();
				settingpoints.put(p.getName()+"left", loc);
				p.sendMessage("§aSet left click point to "+loc);
				p.sendMessage("§aOnce both points are set, type §e/mine name <name>");
				e.setCancelled(true);
				return;
			}
		}

	}

	public void repeatvel(final Vector v, final Player p, final int times) {
		if (times > 0) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					p.setVelocity(v);
					repeatvel(v, p, times - 1);
				}
			}, 4);
		} else {
			nofalldmg.add(p.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					nofalldmg.remove(p.getName());
				}
			}, 300);
		}
	}

	public String getChunkType(Chunk chunk) {

		return ch.getType(chunk.getChunkSnapshot());
	}

	public boolean checkSafe(Location loc) {
		Location lc = loc.clone();
		World w = lc.getWorld();
		Block b = w.getBlockAt(lc);
		Material t = b.getType();
		if (!t.equals(Material.AIR) && !t.equals(Material.TORCH)
				&& !t.equals(Material.REDSTONE_TORCH_OFF)
				&& !t.equals(Material.REDSTONE_TORCH_ON)
				&& !t.equals(Material.PAINTING)
				&& !t.equals(Material.ITEM_FRAME)
				&& !t.equals(Material.WALL_SIGN)) {
			return false;
		}
		lc.setY(lc.getY() - 1);

		b = w.getBlockAt(lc);
		t = b.getType();

		if (t.equals(Material.AIR) || t.equals(Material.LAVA)
				|| t.equals(Material.WATER) || t.equals(Material.CACTUS)
				|| t.equals(Material.TORCH)
				|| t.equals(Material.REDSTONE_TORCH_OFF)
				|| t.equals(Material.REDSTONE_TORCH_ON)
				|| t.equals(Material.PAINTING) || t.equals(Material.ITEM_FRAME)
				|| t.equals(Material.WALL_SIGN)) {
			return false;
		}

		lc.setY(lc.getY() + 2);
		b = w.getBlockAt(lc);
		t = b.getType();
		if (!t.equals(Material.AIR) && !t.equals(Material.TORCH)
				&& !t.equals(Material.REDSTONE_TORCH_OFF)
				&& !t.equals(Material.REDSTONE_TORCH_ON)
				&& !t.equals(Material.PAINTING)
				&& !t.equals(Material.ITEM_FRAME)
				&& !t.equals(Material.WALL_SIGN)) {
			return false;
		}

		return true;
	}

	public void homeAnimation(final Player p, final Location loc,
			final int count, final Location tel, final String dest) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (!p.isOnline()) {
					return;
				}
				if (p.getLocation().getX() == loc.getX()) {
					if (!casting.contains(p)) {
						casting.add(p);
					}
					int i = count;
					i++;
					if (i < 18) {
						ParticleEffect.PORTAL.display(p.getLocation(), 0, 0, 0,
								4, 400);
					} else {

						if (i > 30) {
							// footCircleAnimation(loc, ParticleEffect.SPELL,
							// null, null, null, 40, 10, 5, 3);
							ParticleEffect.INSTANT_SPELL.display(
									p.getLocation(), 0, 1, 0, 4, 100);
						}
					}

					i++;
					if (i < 36) {
						homeAnimation(Bukkit.getPlayerExact(p.getName()), loc,
								i, tel, dest);
					} else {
						try {
							casting.remove(p);
						} catch (Exception e) {

						}
						tel.getChunk().load();
						p.teleport(tel);
						p.sendMessage("§aYou successfully teleported to "
								+ dest + "!");
						spellcds.put(p.getName() + "home",
								System.currentTimeMillis()
										+ (town.getTownofPlayer(p.getName())
												.getHomeCDTime() * 1000));
					}
				} else {
					p.sendMessage("§cSpell cancelled.");
					try {
						casting.remove(p);
					} catch (Exception e) {

					}
				}
			}
		}, 5);
	}

	public void setCooldown(String spellname, String playername, int seconds) {
		spellcds.put(playername + spellname, System.currentTimeMillis()
				+ (seconds * 1000));
	}

	public long getCooldownRemaining(String spellname, String playername) {
		if (!spellcds.containsKey(playername + spellname)) {
			return 0;
		}
		if (spellcds.get(playername + spellname) > System.currentTimeMillis()) {
			Double msleft = (double) (spellcds.get(playername + spellname) - System
					.currentTimeMillis());
			Double sleft = msleft / 1000;

			return Math.round(sleft);
		} else {
			spellcds.remove(playername + spellname);
			return 0;
		}

	}

	public boolean canNowUseSpell(String spellname, String playername) {
		if (!spellcds.containsKey(playername + spellname)) {
			return true;
		}
		if (spellcds.get(playername + spellname) > System.currentTimeMillis()) {
			return false;
		}
		spellcds.remove(playername + spellname);
		return true;
	}

	public String toHourMinSec(long time) {
		int t = (int) time;
		int hour = 0;
		int min = 0;
		int sec = 0;
		String returntime = "";
		String plural = "";
		if (t >= 3600) {
			hour = t / 3600;
			t = t - (hour * 3600);
			if (hour == 1) {
				plural = "hour";
			} else {
				plural = "hours";
			}
			returntime = hour + " " + plural + ", ";
		}
		if (t >= 60) {
			min = t / 60;
			t = t - (min * 60);
			if (min == 1) {
				plural = "minute";
			} else {
				plural = "mins";
			}
			returntime = returntime + min + " " + plural;
		}
		if (t > 0) {
			sec = t;
			if (sec == 1) {
				plural = "second";
			} else {
				plural = "seconds";
			}
			returntime = returntime + " and " + sec + " " + plural;
		}
		if (returntime.equals("")) {
			returntime = "1 second";
		}
		return returntime;

	}

	public void footCircleAnimation(Location centre, ParticleEffect one,
			ParticleEffect two, ParticleEffect three, ParticleEffect four,
			int ticks, int solidity, int thickness, int radius) {

		for (int i = 1; i <= 20; i++) {

			double angle = ((2 * Math.PI) / 20) * i;

			int x = (int) (centre.getX() + Math.cos(angle));
			int z = (int) (centre.getZ() + Math.sin(angle));

			Location temploc = new Location(centre.getWorld(), x,
					centre.getY(), z);
			Location playloc = centre.clone();
			for (int c = 1; c <= solidity; c++) {
				playloc.add(temploc.toVector());
				try {

					// ParticleEffect.sendToLocation(one, playloc, 0, 1, 0, 1,
					// thickness);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public String locationToString(Location loc) {
		if (loc == null) {
			return null;
		}
		return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY()
				+ ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
	}

	private Location locationFromString(String location) {
		if (location == null) {
			return null;
		}
		String[] parts = location.split(";");
		World w = getServer().getWorld(parts[0]);
		Location loc = null;
		try {
			loc = new Location(w, Double.parseDouble(parts[1]),
					Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
			if (parts.length >= 5) {
				loc.setYaw(Float.parseFloat(parts[4]));
			}
			if (parts.length == 6) {
				loc.setPitch(Float.parseFloat(parts[5]));
			}
		} catch (IllegalArgumentException ex) {

			return null;
		}
		return loc;
	}



	@EventHandler
	public void stopDragonDamage(EntityExplodeEvent event) // Listen for the
															// event...
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockplace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (p.isOp()) {
			return;
		}
		Chunk c = e.getBlock().getLocation().getChunk();
		ChunkSnapshot cs = c.getChunkSnapshot();
		// TODO check if block is emergency block and let it gooooooo :3
		if (ch.getType(cs).equals("wild")) {
			e.setCancelled(true);
			if (Math.random() < 0.1) {
				p.sendMessage("§cYou can only build in claimed chunks!");
			}
			return;
		}
		if (ch.getType(cs).equals("admin")) {
			for (String name : minenames){
				if (isInside(e.getBlock().getLocation(), minepoints.get(name+"1"), minepoints.get(name+"2"))){
					return;
				}
			}
			AdminTowns a = at.getTownbyName(c.getX() + "," + c.getZ());
			if (a.getDeniedPerms().contains("build")) {
				e.setCancelled(true);
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can not build or break blocks here!");
				}
			}
			return;
		}
		if (ch.getType(cs).equals("town")) {
			Towns t = town.getTownofChunk(c);
			if (!t.hasChunkPermission(c, p, "build")) {
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can not build or break blocks here!");
				}
				e.setCancelled(true);
				return;
			}

		}
		if (ch.getType(cs).equals("player")) {
			KPlayer kpl = kp.KP(kp.getPlayerofChunk(c));
			if (kpl.getName().equals(p.getName())) {
				return;
			}
			if (!kpl.isChunkFriend(c, p.getName())) {
				if (!kpl.getDefaultPerms().contains("build")) {
					if (Math.random() < 0.1) {
						p.sendMessage("§cYou can not build or break blocks here!");
					}
					e.setCancelled(true);

				}
				return;
			}
			if (!kpl.getFriendPerms().contains("build")) {
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can not build or break blocks here!");
				}
				e.setCancelled(true);
				return;

			}
		}
	}

	@EventHandler
	public void onBlockbreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (p.isOp()) {
			return;
		}
		Chunk c = e.getBlock().getLocation().getChunk();
		ChunkSnapshot cs = c.getChunkSnapshot();
		Material bt = e.getBlock().getType();
		if (ch.getType(cs).equals("wild")) {
			if (!bt.equals(Material.LOG) && !bt.equals(Material.LOG_2)
					&& !bt.equals(Material.LONG_GRASS)
					&& !bt.equals(Material.LEAVES)
					&& !bt.equals(Material.LEAVES_2)
					&& !bt.equals(Material.BROWN_MUSHROOM)
					&& !bt.equals(Material.RED_MUSHROOM)
					&& !bt.equals(Material.CACTUS)
					&& !bt.equals(Material.DEAD_BUSH)
					&& !bt.equals(Material.DOUBLE_PLANT)
					&& !bt.equals(Material.IRON_ORE)
					&& !bt.equals(Material.COAL_ORE)
					&& !bt.equals(Material.LAPIS_ORE)
					&& !bt.equals(Material.NETHER_WARTS)
					&& !bt.equals(Material.DIAMOND_ORE)
					&& !bt.equals(Material.PUMPKIN)
					&& !bt.equals(Material.MELON)
					&& !bt.equals(Material.RED_ROSE)
					&& !bt.equals(Material.REDSTONE_ORE)
					&& !bt.equals(Material.SUGAR_CANE_BLOCK)
					&& !bt.equals(Material.SUGAR_CANE)
					&& !bt.equals(Material.SAPLING)
					&& !bt.equals(Material.SPONGE) && !bt.equals(Material.VINE)
					&& !bt.equals(Material.WATER_LILY)
					&& !bt.equals(Material.WOOD)
					&& !bt.equals(Material.YELLOW_FLOWER)) {
				e.setCancelled(true);
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can only break plants, logs, wood, leaves and ores in the Wild!");
				}
			}
			return;
		}
		if (ch.getType(cs).equals("admin")) {
			for (String name : minenames){
				if (isInside(e.getBlock().getLocation(), minepoints.get(name+"1"), minepoints.get(name+"2"))){
					return;
				}
			}
			AdminTowns a = at.getTownbyName(c.getX() + "," + c.getZ());
			if (a.getDeniedPerms().contains("build")) {
				e.setCancelled(true);
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can not build or break blocks here!");
				}
			}
			return;
		}
		if (ch.getType(cs).equals("town")) {
			Towns t = town.getTownofChunk(c);
			if (!t.hasChunkPermission(c, p, "build")) {
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can not build or break blocks here!");
				}
				e.setCancelled(true);
				return;
			}

		}
		if (ch.getType(cs).equals("player")) {
			KPlayer kpl = kp.KP(kp.getPlayerofChunk(c));
			if (kpl.getName().equals(p.getName())) {
				return;
			}
			if (!kpl.isChunkFriend(c, p.getName())) {
				if (!kpl.getDefaultPerms().contains("build")) {
					if (Math.random() < 0.1) {
						p.sendMessage("§cYou can not build or break blocks here!");
					}
					e.setCancelled(true);

				}
				return;
			}
			if (!kpl.getFriendPerms().contains("build")) {
				if (Math.random() < 0.1) {
					p.sendMessage("§cYou can not build or break blocks here!");
				}
				e.setCancelled(true);
				return;

			}
		}
	}

	@EventHandler
	public void onLeafDecay(LeavesDecayEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void fireDamageControl(BlockIgniteEvent event) {
		if (!event.getCause().equals(IgniteCause.FLINT_AND_STEEL)){
		//if (event.getNewState().getType() == Material.FIRE) {
			if (ch.getType(
					event.getBlock().getLocation().getChunk()
							.getChunkSnapshot()).equals("wild")) {
				if (Math.random() < 0.7)
					event.setCancelled(true);
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void fireDamageControl(BlockBurnEvent event) {
		if (ch.getType(
				event.getBlock().getLocation().getChunk()
						.getChunkSnapshot()).equals("wild")) {
			if (Math.random() < 0.7)
				event.setCancelled(true);
		}else{
			event.setCancelled(true);
		}
	}
	
	public void repeatingClaim(final int side, final Chunk c,
			final AdminTowns a, final int unclaimed, final Player p, final int i) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Chunk cc = c.getWorld().getChunkAt(c.getX(), c.getZ());
				AdminTowns as = at.getTownbyName(a.getName());
				int uc = unclaimed;
				for (int b = 0; b < side; b++) {

					if (ch.getType(cc.getChunkSnapshot()) != "wild") {
						uc++;
					} else {

						at.addChunk(as, cc);
						p.sendMessage("" + cc.getX() + "," + cc.getZ());
						ch.addChunk(cc.getChunkSnapshot(), "admin");
					}
					cc = cc.getWorld().getChunkAt(cc.getX(), cc.getZ() + 1);
				}
				int g = i + 1;
				if (g < side) {
					cc = cc.getWorld().getChunkAt(cc.getX() + 1,
							cc.getZ() - side);

					repeatingClaim(side, cc, as, uc, p, g);
				} else {
					p.sendMessage("§aClaimed §e"
							+ (Math.pow(side, 2) - unclaimed)
							+ " §achunks for the admin town:" + a.getName());
					p.sendMessage("§c"
							+ unclaimed
							+ " §cchunks were already claimed in some way or another.");
				}
			}

		}, 2);

	}

	public String IStoString(List<ItemStack> ilist, ItemStack i) {
		String alltostring = "";
		if (ilist != null) {
			for (ItemStack is : ilist) {
				if (is != null) {
					if (is.hasItemMeta()) {
						alltostring = alltostring
								+ is.getItemMeta().getLore().toString();
					}
				}
			}
		}
		if (i != null) {
			if (i.hasItemMeta()) {
				alltostring = alltostring
						+ i.getItemMeta().getLore().toString();
			}
		}
		return alltostring;
	}
	
	public String fromPInv(PlayerInventory PI){
		List<ItemStack> li = new ArrayList<ItemStack>();
		li.add(PI.getBoots());
		li.add(PI.getHelmet());
		li.add(PI.getChestplate());
		li.add(PI.getLeggings());
		return IStoString(li, null);
	}

	public void setAttAsync(final String alltostring, final KPlayer kpl) {
		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
			public void run() {
				String alts = alltostring.replace("+", "#");
				while (alts.contains("§")) {
					int index = alts.indexOf("§");
					String sub1 = alts.substring(index + 2);
					String sub2 = alts.substring(0, index);
					alts = sub2 + sub1;
				}
				Pattern strp = Pattern.compile("(Strength #)(\\d+)");
				Matcher strm = strp.matcher(alts);
				Pattern precp = Pattern.compile("(Precision #)(\\d+)");
				Matcher precm = precp.matcher(alts);
				// Pattern wisp = Pattern.compile("(Wisdom #)(\\d+)");
				// Matcher wism = wisp.matcher(all);
				// Pattern intp = Pattern.compile("(Intellect #)(\\d+)");
				// Matcher intm = intp.matcher(all); TODO add spell attributes.
				Pattern agip = Pattern.compile("(Agility #)(\\d+)");
				Matcher agim = agip.matcher(alts);
				Pattern hpp = Pattern.compile("(Health #)(\\d+)");
				Matcher hpm = hpp.matcher(alts);
				Pattern stap = Pattern.compile("(Stamina #)(\\d+)");
				Matcher stam = stap.matcher(alts);
				Pattern armp = Pattern.compile("(Armour: )(\\d+)");
				Matcher armm = armp.matcher(alts);
				int Strength = 0;
				int Agility = 0;
				int Precision = 0;
				int Health = 0;
				int Stamina = 0;
				int Armour = 0;
				while (strm.find()) {
					int total1 = Integer.parseInt(strm.group(2));
					Strength = Strength + total1;
				}

				while (precm.find()) {
					int total2 = Integer.parseInt(precm.group(2));
					Precision = Precision + total2;
				}

				// while (wism.find()) {
				// int total3 = Integer.parseInt(wism.group(2));
				// finaltotal3 = total3 + finaltotal3;
				// }

				// while (intm.find()) {
				// int total4 = Integer.parseInt(intm.group(2));
				// finaltotal4 = total4 + finaltotal4;
				// }

				while (agim.find()) {
					int total5 = Integer.parseInt(agim.group(2));
					Agility = Agility + total5;
				}
				while (hpm.find()) {
					int total6 = Integer.parseInt(hpm.group(2));
					Health = Health + total6;
				}
				while (stam.find()) {
					int total7 = Integer.parseInt(stam.group(2));
					Stamina = Stamina + total7;
				}
				while (armm.find()) {
					int total8 = Integer.parseInt(armm.group(2));
					Armour = Armour + total8;
				}
				kpl.setAgi(Agility);
				kpl.setArmour(Armour);
				kpl.setStam(Stamina);
				kpl.setHealth(Health);
				kpl.setPrec(Precision);
				kpl.setStr(Strength);
			}

		});

	}

	public Map<String, Integer> getAttributes(List<ItemStack> ilist, ItemStack i) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		String alltostring = "";
		if (ilist != null) {
			for (ItemStack is : ilist) {
				if (is != null) {
					if (is.hasItemMeta()) {
						alltostring = alltostring
								+ is.getItemMeta().getLore().toString();
					}
				}
			}
		}
		if (i != null) {
			if (i.hasItemMeta()) {
				alltostring = alltostring
						+ i.getItemMeta().getLore().toString();
			}
		}
		if (alltostring.equals("")) {
			return ret;
		}
		alltostring.replace("+", "#");
		Pattern strp = Pattern.compile("(Strength #)(\\d+)");
		Matcher strm = strp.matcher(alltostring);
		Pattern precp = Pattern.compile("(Precision #)(\\d+)");
		Matcher precm = precp.matcher(alltostring);
		// Pattern wisp = Pattern.compile("(Wisdom #)(\\d+)");
		// Matcher wism = wisp.matcher(all);
		// Pattern intp = Pattern.compile("(Intellect #)(\\d+)");
		// Matcher intm = intp.matcher(all); TODO add spell attributes.
		Pattern agip = Pattern.compile("(Agility #)(\\d+)");
		Matcher agim = agip.matcher(alltostring);
		Pattern hpp = Pattern.compile("(Health #)(\\d+)");
		Matcher hpm = hpp.matcher(alltostring);
		Pattern stap = Pattern.compile("(Stamina #)(\\d+)");
		Matcher stam = stap.matcher(alltostring);
		Pattern armp = Pattern.compile("(Armour: )(\\d+)");
		Matcher armm = armp.matcher(alltostring);
		int Strength = 0;
		int Agility = 0;
		int Precision = 0;
		int Health = 0;
		int Stamina = 0;
		int Armour = 0;
		while (strm.find()) {
			int total1 = Integer.parseInt(strm.group(2));
			Strength = Strength + total1;
		}

		while (precm.find()) {
			int total2 = Integer.parseInt(precm.group(2));
			Precision = Precision + total2;
		}

		// while (wism.find()) {
		// int total3 = Integer.parseInt(wism.group(2));
		// finaltotal3 = total3 + finaltotal3;
		// }

		// while (intm.find()) {
		// int total4 = Integer.parseInt(intm.group(2));
		// finaltotal4 = total4 + finaltotal4;
		// }

		while (agim.find()) {
			int total5 = Integer.parseInt(agim.group(2));
			Agility = Agility + total5;
		}
		while (hpm.find()) {
			int total6 = Integer.parseInt(hpm.group(2));
			Health = Health + total6;
		}
		while (stam.find()) {
			int total7 = Integer.parseInt(stam.group(2));
			Stamina = Stamina + total7;
		}
		while (armm.find()) {
			int total8 = Integer.parseInt(armm.group(2));
			Armour = Armour + total8;
		}
		ret.put("Strength", Strength);
		ret.put("Agility", Agility);
		ret.put("Precision", Precision);
		ret.put("Health", Health);
		ret.put("Stamina", Stamina);
		ret.put("Armour", Armour);
		return ret;
	}

	@EventHandler
	public void closeInv(InventoryCloseEvent e) {
		final Player pl = (Player) e.getPlayer();
		KPlayer kpl = kp.KP(pl.getName());
		setAttAsync(fromPInv(pl.getInventory()),kpl);
		Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				pl.setMaxHealth(kp.KP(pl.getName()).getActualHP());
			}
		}, 5);
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent e) {
		if (usingadminchat.contains(e.getPlayer().getName())) {
			usingadminchat.remove(e.getPlayer().getName());
		}
		if (usingtownchat.contains(e.getPlayer().getName())) {
			usingtownchat.remove(e.getPlayer().getName());
		}
		Player p = e.getPlayer();
		KPlayer kpl = kp.KP(p.getName());
		kpl.setOnline(false);
	}
	
	@EventHandler
	public void onLogin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		if (!allplayers.contains(p.getName().toLowerCase())) {
			kp.create(p.getName());
			allplayers.add(p.getName().toLowerCase());
			
			return;
		}
		if (!playerstoupdate.contains(p.getName()))
			playerstoupdate.add(p.getName());
		KPlayer kpl = kp.KP(p.getName());
		kpl.setOnline(true);
		setAttAsync(fromPInv(p.getInventory()),kpl);
		Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				p.setMaxHealth(kp.KP(p.getName()).getActualHP());
			}
		}, 5);
	}
	
	public void repeatPlayParticles(List<Location>)
	
	
	public void particleHelix(){
	
	}
	
	
	public void particleFloor(){
		
	}
	
	public void particleCircle(Player p, double height, ParticleEffect pe, double radius, double amount, int repeat, int offsetX, int offsetY, int offsetZ, int count, int speed){
		Location loc = p.getLocation();
		loc = loc.add(0, height, 0);
		pe.display(loc, offsetX, offsetY, offsetZ, speed, count);
		double angleinc = 360/amount;
		for (int i =1; i<=amount; i++){
			Location l = new Location(p.getWorld(), (radius*Math.cos(Math.toRadians(angleinc*i)))+loc.getX(), loc.getY(), loc.getZ()+(radius*Math.sin(Math.toRadians(angleinc*i))));
			pe.display(l, offsetX, offsetY, offsetZ, speed, count);
		}
	}
	
	
    public static Boolean isInside(Location loc, Location corner1, Location corner2) {
        double xMin = 0;
        double xMax = 0;
        double yMin = 0;
        double yMax = 0;
        double zMin = 0;
        double zMax = 0;
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
 
        xMin = Math.min(corner1.getX(), corner2.getX());
        xMax = Math.max(corner1.getX(), corner2.getX());
 
        yMin = Math.min(corner1.getY(), corner2.getY());
        yMax = Math.max(corner1.getY(), corner2.getY());
 
        zMin = Math.min(corner1.getZ(), corner2.getZ());
        zMax = Math.max(corner1.getZ(), corner2.getZ());
 
        return (x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax);
    }
	
	@EventHandler
	public void regen(EntityRegainHealthEvent e) {
		//
	}

	public Chunks getChunkClass() {
		return ch;
	}

	public Towns getTownClass() {
		return town;
	}

	public KPlayer getKPlayerClass() {
		return kp;
	}

	public AdminTowns getAdminTownsClass() {
		return at;
	}

}
