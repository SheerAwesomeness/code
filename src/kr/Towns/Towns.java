package kr.Towns;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import kr.Kirithia;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Towns {

	private String name;
	private String owner;
	private String motd;
	private String modperms;
	private String adminperms;
	private String defaultperms;
	private String chunkmemberperms;
	private String chunkownerperms;
	private String chunkdefaultperms;
	private HashMap<String, Location> townhomes = new HashMap<String, Location>();
	double totalxp;
	double level;
	double xp;
	boolean open;
	boolean stafftown;
	private List<String> chunks = new LinkedList<String>();
	private List<String> allies = new LinkedList<String>();
	private List<String> enemies = new LinkedList<String>();
	private List<String> members = new LinkedList<String>();
	private List<String> moderators = new LinkedList<String>();
	private List<String> admins = new LinkedList<String>();
	public List<String> towns = new LinkedList<String>();
	private HashMap<String, Towns> whattown = new HashMap<String, Towns>();
	// private HashMap<String, Towns> playerstown = new HashMap<String,
	// Towns>();
	private HashMap<String, LinkedList<String>> chunkmembers = new HashMap<String, LinkedList<String>>();
	private HashMap<String, LinkedList<String>> chunkowners = new HashMap<String, LinkedList<String>>();

	public HashMap<String, Towns> townmap = new HashMap<String, Towns>();

	Kirithia plugin;

	public Towns(Kirithia kr) {
		this.plugin = kr;
	}

	public Towns(String name) {
		this.name = name;
	}

	public Towns(String name, String owner, String motd, String modperms,
			String adminperms, String defaultperms, String chunkmemberperms,
			String chunkownerperms, String chunkdefaultperms, double totalxp,
			double level, boolean open, List<String> chunks,
			List<String> allies, List<String> enemies, List<String> members,
			List<String> moderators, List<String> admins,
			HashMap<String, LinkedList<String>> chunkmembers,
			HashMap<String, LinkedList<String>> chunkowners,
			HashMap<String, Location> homes) {
		this.name = name;
		this.owner = owner;
		this.motd = motd;
		this.modperms = modperms;
		this.adminperms = adminperms;
		this.defaultperms = defaultperms;
		this.chunkmemberperms = chunkmemberperms;
		this.chunkownerperms = chunkownerperms;
		this.chunkdefaultperms = chunkdefaultperms;
		this.totalxp = totalxp;
		this.level = level;
		this.open = open;
		this.chunks = chunks;
		this.allies = allies;
		this.enemies = enemies;
		this.members = members;
		this.moderators = moderators;
		this.admins = admins;
		this.chunkmembers = chunkmembers;
		this.chunkowners = chunkowners;
		this.townhomes = homes;
	}

	public void setupTown(Towns to) {

		// this.townhomes = townhomes;
		for (String namem : to.chunks) {
			whattown.put(namem, to);
		}
		for (String namem : to.members) {
			whattown.put(namem, to);
		}
		townmap.put(to.name.toLowerCase(), to);
		towns.add(to.name.toLowerCase());
	}

	public List<String> getHomeNames() {
		List<String> th = new ArrayList<String>();
		for (String name : townhomes.keySet()) {
			th.add(name);
		}
		return th;
	}

	public void setHome(Location home, String name) {
		if (name == null) {
			name = "home";
		}
		townhomes.put(name.toLowerCase(), home);
	}

	public int getHomeCDTime() {
		if (this.getLevel() < 10) {
			return 1800;
		}
		if (this.getLevel() < 20) {
			return 1500;
		}
		if (this.getLevel() < 30) {
			return 1200;
		}
		return 900;
	}

	public boolean hasHome() {
		if (townhomes.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean anotherHome() {
		int limit = 1;
		if (this.getLevel() > 7) {
			if (this.getLevel() < 15) {
				limit = 2;
			} else if (this.getLevel() < 22) {
				limit = 3;
			} else {
				limit = 4;
			}
		}
		if (this.getHomeNames().size() >= limit) {
			return false;
		}
		return true;
	}

	public void deleteHome(String name) {
		if (name == null) {
			name = "home";

		}
		townhomes.remove(name.toLowerCase());
	}

	public Location getHomeLoc(String name) {
		if (name == null) {
			name = "home";
		}
		if (townhomes.containsKey(name.toLowerCase())) {
			return townhomes.get(name.toLowerCase());
		}
		return null;
	}

	public int getAmountofHomes() {
		return townhomes.size();
	}

	public List<String> getTowns() {
		return towns;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isOpen() {
		return open;
	}

	public void setModPerms(String modperms) {
		this.modperms = modperms;
	}

	public void setAdminPerms(String adminperms) {
		this.adminperms = adminperms;
	}

	public void setDefaultPerms(String defaultperms) {
		this.defaultperms = defaultperms;
	}

	public void setChunkDefaultPerms(String chunkdefaultperms) {
		this.chunkdefaultperms = chunkdefaultperms;
	}

	public void setChunkMemberPerms(String chunkmemberperms) {
		this.chunkmemberperms = chunkmemberperms;
	}

	public void setChunkOwnerPerms(String chunkownerperms) {
		this.chunkownerperms = chunkownerperms;
	}

	public String getModPerms() {
		return modperms;
	}

	public String getAdminPerms() {
		return adminperms;
	}

	public String getDefaultPerms() {
		return defaultperms;
	}

	public String getChunkDefaultPerms() {
		return chunkdefaultperms;
	}

	public String getChunkMemberPerms() {
		return chunkmemberperms;
	}

	public String getChunkOwnerPerms() {
		return chunkownerperms;
	}

	public void setAlly(Towns one, Towns two) {
		one.allies.add(two.getName());
		two.allies.add(one.getName());
		if (one.enemies.contains(two.getName())) {
			one.enemies.remove(two.getName());
		}
		if (two.enemies.contains(one.getName())) {
			two.enemies.remove(one.getName());
		}

	}

	public List<String> getAllies() {
		return allies;
	}

	public List<String> getEnemies() {
		return enemies;
	}

	public void setEnemies(Towns one, Towns two) {
		one.enemies.add(two.getName());
		two.enemies.add(one.getName());
		if (one.allies.contains(two.getName())) {
			one.allies.remove(two.getName());
		}
		if (two.allies.contains(one.getName())) {
			two.allies.remove(one.getName());
		}

	}

	public void setNeutrals(Towns one, Towns two) {
		if (one.allies.contains(two.getName())) {
			one.allies.remove(two.getName());
		}
		if (two.allies.contains(one.getName())) {
			two.allies.remove(one.getName());
		}
		if (one.enemies.contains(two.getName())) {
			one.enemies.remove(two.getName());
		}
		if (two.enemies.contains(one.getName())) {
			two.enemies.remove(one.getName());
		}
	}

	public boolean isAlly(Towns one, Towns two) {
		if (one.allies.contains(two.getName())
				&& two.allies.contains(one.getName())) {
			return true;
		}
		return false;
	}

	public boolean isEnemy(Towns one, Towns two) {
		if (one.enemies.contains(two.getName())
				&& two.enemies.contains(one.getName())) {
			return true;
		}
		return false;
	}

	public boolean isNeutral(Towns one, Towns two) {
		if (!one.enemies.contains(two.getName())
				&& !two.enemies.contains(one.getName())
				&& !one.allies.contains(two.getName())
				&& !two.allies.contains(one.getName())) {
			return true;
		}
		return false;
	}

	public void setStaffTown() {
		this.stafftown = true;
	}

	public boolean isStaffTown() {
		try {
			if (this.stafftown) {
				return true;
			}
		} catch (Exception e) {

		}
		return false;
	}

	public void deleteTown(Towns to) {
		for (String name : towns) {
			Towns t = whattown.get(name);
			try {
				t.allies.remove(name);
			} catch (Exception e) {
			}
			try {

				t.enemies.remove(name);
			} catch (Exception e) {
			}
		}
		for (String name : to.getMembers()) {
			whattown.remove(name);
		}
		towns.remove(to.name.toLowerCase());
		townmap.remove(to.name.toLowerCase());
		setMembers(to, null);
		to.setModerators(null);
		to.setAdmins(null);
		to.setDefaultPerms(null);
		to.setOwner(null);
		for (String coords : to.getChunks()) {
			whattown.remove(coords);

		}
	}

	public Towns(String name, String owner) {
		this.name = name;
		this.owner = owner;
	}

	public void createTown(Towns to) {
		to.members.add(to.owner);
		to.setMOTD("Default MOTD.");
		whattown.put(to.owner, to);
		towns.add(to.name.toLowerCase());
		townmap.put(to.name.toLowerCase(), to);
		to.setChunkDefaultPerms("use ");
		to.setChunkMemberPerms("use chests ");
		to.setChunkOwnerPerms("build use chests ");
		to.setAdminPerms("invite kick promote demote setchunkmembers setchunkowners claim c-u-laim chunkaccess h-s-ome home motd allegiance ");
		to.setModPerms("invite kick home ");
		to.setDefaultPerms("home ");
		to.setLevel(1);
		to.setTownTotalXp(0);
		to.setOpen(false);
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setMOTD(String motd) {
		this.motd = motd;
	}

	public String getMOTD() {
		return motd;
	}

	public void addMember(Towns to, String name) {
		to.members.add(name);
		whattown.put(name, this);
	}

	public void removeMember(Towns to, String name) {
		to.members.remove(name);
		whattown.remove(name);
		to.getModerators().remove(name);
		to.getAdmins().remove(name);
		for (String coords : to.getChunks()) {
			to.removeChunkMember(coords, name);
			to.removeChunkOwner(coords, name);
		}

	}

	public void setMembers(Towns to, List<String> members) {
		if (members == null) {
			to.members.clear();
			return;
		}
		for (String name : to.members) {
			whattown.remove(name);
		}
		to.members = members;
		for (String name : members) {
			whattown.put(name, to);
		}
	}

	public boolean hasChunkPermission(Chunk chunk, Player player,
			String permission) {
		if (getOwner().equalsIgnoreCase(player.getName())){
			return true;
		}
		if (hasPermission(player, "chunkaccess")){
			return true;
		}
		if (getChunkMembers(chunk).contains(player.getName())) {
			if (!getChunkMemberPerms().contains(permission)
					&& !getChunkDefaultPerms().contains(permission)) {
				return false;
			}
		} else if (getChunkOwners(chunk).contains(player.getName())) {
			if (!getChunkOwnerPerms().contains(permission)
					&& !getChunkDefaultPerms().contains(permission)) {
				return false;
			}
		} else if (!getChunkDefaultPerms().contains(permission)) {
			return false;

		}
		return true;
	}

	public boolean hasPermission(Player player, String permission) {
		if (permission.equalsIgnoreCase("unclaim")) {
			permission = "c-u-laim";
		}
		if (permission.equalsIgnoreCase("sethome")) {
			permission = "h-s-ome";
		}
		if (moderators.contains(player.getName())) {
			if (!modperms.contains(permission)
					&& !defaultperms.contains(permission)) {
				return false;
			}
		} else if (admins.contains(player.getName())) {
			if (adminperms.contains(permission)
					&& modperms.contains(permission)
					&& defaultperms.contains(permission)) {
				return false;
			}
		} else if (!defaultperms.contains(permission)
				&& !owner.equalsIgnoreCase(player.getName())) {
			return false;

		}
		return true;
	}

	public List<String> getMembers() {
		return members;
	}

	public void addModerator(String name) {
		if (this.admins.contains(name)) {
			this.admins.remove(name);
		}
		this.moderators.add(name);
	}

	public void removeModerator(String name) {
		this.moderators.remove(name);
	}

	public void setModerators(List<String> moderators) {
		if (moderators == null) {
			this.moderators.clear();
			return;
		}
		for (String name : moderators) {
			if (admins.contains(name)) {
				admins.remove(name);
			}
		}
		this.moderators = moderators;
	}

	public List<String> getModerators() {
		return moderators;
	}

	public void addAdmin(String name) {
		if (this.moderators.contains(name)) {
			this.moderators.remove(name);
		}
		this.admins.add(name);
	}

	public void removeAdmin(String name) {
		this.admins.remove(name);
	}

	public void setAdmins(List<String> admins) {
		if (admins == null) {
			this.admins.clear();
			return;
		}
		for (String name : admins) {
			if (moderators.contains(name)) {
				moderators.remove(name);
			}
		}
		this.admins = admins;
	}

	public List<String> getAdmins() {
		return admins;
	}

	public void addChunk(Towns to, String coords) {
		to.chunks.add(coords);
		whattown.put(coords, to);
	}

	public void addChunk(Towns to, Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		to.chunks.add(coords);
		whattown.put(coords, to);
	}

	public void removeChunk(Towns to, String coords) {
		to.chunks.remove(coords);
		whattown.remove(coords);
		to.chunkowners.remove(coords);
		to.chunkmembers.remove(coords);
	}

	public void removeChunk(Towns to, Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		to.chunks.remove(coords);
		whattown.remove(coords);
		to.chunkowners.remove(coords);
		to.chunkmembers.remove(coords);
	}

	public boolean hasChunk(String coords) {
		if (chunks.contains(coords)) {
			return true;
		}
		return false;
	}

	public List<String> getChunks() {
		return chunks;
	}

	public void addChunkMember(String coords, String name) {
		if (chunkowners.containsKey(coords)) {
			if (chunkowners.get(coords).contains(name)) {
				LinkedList<String> m = chunkowners.get(coords);
				m.remove(name);
				chunkowners.put(coords, m);
			}
		}
		if (chunkmembers.containsKey(coords)) {
			LinkedList<String> m = chunkmembers.get(coords);
			m.add(name);
			chunkmembers.put(coords, m);
		} else {
			List<String> m = Arrays.asList(name);
			LinkedList<String> mn = new LinkedList<String>();
			mn.addAll(m);
			chunkmembers.put(coords, mn);
		}
	}

	public void removeChunkMember(String coords, String name) {
		LinkedList<String> m = chunkmembers.get(coords);
		m.remove(name);
		chunkmembers.put(coords, m);
	}

	public boolean isChunkMember(String coords, String name) {
		if (chunkmembers.containsKey(coords)) {
			if (chunkmembers.get(coords).contains(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean anotherChunk() {
		if (this.chunks.size() < (5 + (this.getLevel() * 4))) {
			return true;
		}
		return false;
	}

	public List<String> getChunkMembers(String coords) {
		if (chunkmembers.containsKey(coords)) {
			return chunkmembers.get(coords);
		}
		return null;
	}

	public List<String> getChunkMembers(Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (chunkmembers.containsKey(coords)) {
			return chunkmembers.get(coords);
		}
		return null;
	}

	public void addChunkOwner(String coords, String name) {
		if (chunkmembers.containsKey(coords)) {
			if (chunkmembers.get(coords).contains(name)) {
				LinkedList<String> m = chunkmembers.get(coords);
				m.remove(name);
				chunkmembers.put(coords, m);
			}
		}
		if (chunkowners.containsKey(coords)) {
			LinkedList<String> m = chunkowners.get(coords);
			m.add(name);
			chunkowners.put(coords, m);
		} else {
			List<String> m = Arrays.asList(name);
			LinkedList<String> mn = new LinkedList<String>();
			mn.addAll(m);
			chunkowners.put(coords, mn);
		}
	}

	public void removeChunkOwner(String coords, String name) {
		LinkedList<String> m = chunkowners.get(coords);
		m.remove(name);
		chunkowners.put(coords, m);
	}

	public boolean isChunkOwner(String coords, String name) {
		if (chunkowners.containsKey(coords)) {
			if (chunkowners.get(coords).contains(name)) {
				return true;
			}
		}
		return false;
	}

	public List<String> getChunkOwners(Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (chunkowners.containsKey(coords)) {
			return chunkowners.get(coords);
		}
		return null;
	}

	public List<String> getChunkOwners(String coords) {
		if (chunkowners.containsKey(coords)) {
			return chunkowners.get(coords);
		}
		return null;
	}

	public List<Player> getOnlineMembers() {
		List<Player> online = new ArrayList<Player>();
		for (String name : this.getMembers()) {
			OfflinePlayer pl = Bukkit.getOfflinePlayer(name);
			if (pl.isOnline()) {
				online.add(((Player) pl));
			}
		}
		return online;
	}

	public List<Player> getOnlineStaff() {
		List<Player> online = new ArrayList<Player>();
		for (String name : this.getMembers()) {
			OfflinePlayer pl = Bukkit.getOfflinePlayer(name);
			if (pl.isOnline()) {
				if (this.moderators.contains(name)
						|| this.admins.contains(name)
						|| this.getOwner().equalsIgnoreCase(name)) {
					online.add(((Player) pl));
				}
			}
		}
		return online;
	}

	public Towns getTownofChunk(String coords) {
		if (whattown.containsKey(coords)) {
			return whattown.get(coords);
		}
		return null;
	}

	public Towns getTownofChunk(Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (whattown.containsKey(coords)) {
			return whattown.get(coords);
		}
		return null;
	}

	public Towns getTownofChunk(ChunkSnapshot chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (whattown.containsKey(coords)) {
			return whattown.get(coords);
		}
		return null;
	}

	public Towns getTown(String name) {
		if (townmap.containsKey(name.toLowerCase())) {
			return townmap.get(name.toLowerCase());
		}

		return null;
	}

	public Towns getTownofPlayer(String name) {
		if (whattown.containsKey(name)) {
			return whattown.get(name);
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setTownTotalXp(int totalxp) {
		this.totalxp = totalxp;
	}

	public boolean addXP(double xp) {
		this.totalxp = totalxp + xp;
		if (this.getXptoLevel() <= 0) {
			while (this.getXptoLevel() <= 0) {
				this.setLevel(this.getLevel() + 1);
			}
			return true;
		}
		return false;
	}

	public double getXptoLevel() {
		double xptolvl = Math.round(400 + (Math.pow(this.getLevel() + 1,
				2.8580424)) * 60);
		double fin = xptolvl - this.totalxp;
		return fin;
	}

	public double getTotalXptoALevel(double level) {
		double xptolvl = Math.round(400 + (Math.pow(level, 2.8580424)) * 60);
		return xptolvl;
	}

	public double getTotalXptoLevel() {
		double xptolvl = Math.round(400 + (Math.pow(this.getLevel() + 1,
				2.8580424)) * 60)
				- Math.round(400 + (Math.pow(this.getLevel(), 2.8580424)) * 60);
		return xptolvl;
	}

	public void setLevel(double level) {
		this.level = level;
	}

	public double getLevel() {
		return level;
	}

	public double getCurrentXp() {
		double prevlvl = Math
				.round(400 + (Math.pow(this.getLevel(), 2.8580424)) * 60);
		if (this.getLevel() == 1) {
			prevlvl = 0;
		}
		double current = this.totalxp - prevlvl;
		return current;
	}

	public double getTotalXp() {
		return totalxp;
	}

}
