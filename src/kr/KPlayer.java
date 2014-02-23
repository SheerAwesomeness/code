package kr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;

public class KPlayer {

	Kirithia plugin;
	// base initialised variables
	public String playername;
	private List<String> chunkcoords = new LinkedList<String>();
	public HashMap<String, List<String>> chunkfriends = new HashMap<String, List<String>>();
	long lastlogin = System.currentTimeMillis();
	public List<String> defaultperms = new LinkedList<String>();
	public List<String> friendperms = new LinkedList<String>();
	int reputation = 0;
	int level = 1;
	int melee = 1;
	int archery = 1;
	int defence = 1;
	int xp = 0;
	int maxchunks = 3;

	// temporary variables
	int strength = 0;
	int health = 0;
	int stamina = 0;
	int agility = 0;
	int precision = 0;
	int armour = 0;
	boolean trading = false;
	boolean online = false;
	KPlayer trader;

	// stratifying variables
	private HashMap<String, String> whatplayer = new HashMap<String, String>();
	private HashMap<String, KPlayer> whatkp = new HashMap<String, KPlayer>();

	public KPlayer(Kirithia kr) {
		this.plugin = kr;
	}

	public KPlayer(String playername) {
		this.playername = playername;
	}
	
	public KPlayer(String playername, List<String> chunkcoords, HashMap<String, List<String>> chunkfriends, List<String> defaultperms, List<String> friendperms, int reputation, int level, int melee, int archery, int defence, int xp, int maxchunks) {
		this.playername = playername;
		this.chunkcoords = chunkcoords;
		this.chunkfriends = chunkfriends;
		this.defaultperms = defaultperms;
		this.friendperms = friendperms;
		this.reputation = reputation;
		this.level = level;
		this.xp = xp;
		this.melee = melee;
		this.archery = archery;
		this.defence= defence;
		this.maxchunks = maxchunks;
	}

	public KPlayer create(String playername) {
		KPlayer kp = new KPlayer(playername);
		kp.friendperms.add("use");
		kp.friendperms.add("chests");
		kp.friendperms.add("build");
		kp.maxchunks = 3;
		kp.level = 1;
		kp.melee = 1;
		kp.archery = 1;
		kp.defence = 1;
		whatkp.put(playername.toLowerCase(), kp);
		return kp;
	}
	
	public KPlayer setup(String playername, List<String> chunkcoords, HashMap<String, List<String>> chunkfriends, List<String> defaultperms, List<String> friendperms, int reputation, int level, int melee, int archery, int defence, int xp, int maxchunks) {
		KPlayer kp = new KPlayer(playername, chunkcoords, chunkfriends, defaultperms, friendperms, reputation, level, melee, archery, defence, xp, maxchunks);
		whatkp.put(playername.toLowerCase(), kp);
		for (String coords: chunkcoords){
			whatplayer.put(coords, playername);
		}
		return kp;
	}

	public void setOnline(boolean truefalse){
		online = truefalse;
	}
	
	public boolean isOnline(){
		return online;
	}
	
	public KPlayer KP(String playername) {
		return whatkp.get(playername.toLowerCase());
	}

	public List<String> getChunkCoords(){
		return chunkcoords;
	}
	
	public Collection<KPlayer> getKPlayers(){
		return whatkp.values();
	}
	
	public boolean hasClaimedAny() {
		if (chunkcoords==null){
			return false;
		}
		if (chunkcoords.size() == 0) {
			return false;
		}
		return true;
	}

	public String getName() {
		return playername;
	}

	public int getNumberofChunks() {
		return chunkcoords.size();
	}

	public List<String> getDefaultPerms() {
		return defaultperms;
	}

	public void setDefaultPerm(String perm, boolean truefalse) {
		if (truefalse) {
			if (!defaultperms.contains(perm))
			defaultperms.add(perm);
		} else {
			defaultperms.remove(perm);
		}
	}

	public List<String> getFriendPerms() {
		return friendperms;
	}

	public void setFriendPerm(String perm, boolean truefalse) {
		if (truefalse) {
			if (!friendperms.contains(perm))
			friendperms.add(perm);
		} else {
			friendperms.remove(perm);
		}
	}

	/**
	 * @permission
	 * @playername
	 * @chunk
	 */
	public boolean hasPermission(String permission, String playername,
			Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (!chunkfriends.containsKey(coords)) {
			if (defaultperms.contains(permission)) {
				return true;
			} else {
				return false;
			}
		}
		if (chunkfriends.get(coords).contains(playername.toLowerCase())) {
			if (friendperms.contains(permission)) {
				return true;
			} else {
				return false;
			}
		} else {
			if (defaultperms.contains(permission)) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void setLastLogin(long lastlogin) {
		this.lastlogin = lastlogin;
	}

	public long getLastLogin() {
		return lastlogin;
	}

	public void setMelee(int melee) {
		this.melee = melee;
	}

	public void setArchery(int archery) {
		this.archery = archery;
	}

	public void setDefence(int defence) {
		this.defence = defence;
	}

	public int getMelee() {
		return melee;
	}

	public int getArchery() {
		return archery;
	}
	
	public int getDefence(){
		return defence;
	}

	public void setStr(int str) {
		strength = str;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setStam(int stamina) {
		this.stamina = stamina;
	}

	public void setAgi(int agi) {
		agility = agi;
	}

	public void setPrec(int prec) {
		precision = prec;
	}

	public void setArmour(int armour) {
		this.armour = armour;
	}

	public void setMaxChunks(int max) {
		this.maxchunks = max;

	}

	public int getMaxChunks() {
		return maxchunks;
	}

	public int getRep() {
		return reputation;

	}

	public int getArmour() {
		return armour;
	}

	public int getAgi() {
		return agility;
	}

	public int getPrec() {
		return precision;
	}

	public int getStam() {
		return stamina;
	}

	public int getHealth() {
		return health;
	}

	public int getStr() {
		return strength;
	}

	public int getLevel() {
		return level;
	}

	public int getXP() {
		return xp;
	}

	public void addRep(int rep) {
		reputation = reputation + rep;
	}

	public void subRep(int rep) {
		reputation = reputation - rep;
	}

	public void addChunk(Chunk chunk, KPlayer KP) {
		String coords = chunk.getX() + "," + chunk.getZ();
		KP.chunkcoords.add(coords);
		whatplayer.put(coords, KP.playername);
	}

	public String getPlayerofChunk(Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (!whatplayer.containsKey(coords)){
			return "";
		}
		return whatplayer.get(coords);
	}

	public String getPlayerofChunk(ChunkSnapshot chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		return whatplayer.get(coords);
	}

	public void removeChunk(String coords, KPlayer KP) {
		whatplayer.remove(coords);
		KP.chunkcoords.remove(coords);
		KP.chunkfriends.remove(coords);
	}

	public void del(KPlayer kp) {
		whatkp.remove(kp.playername.toLowerCase());
	}

	public void removeChunk(Chunk chunk, KPlayer KP) {
		String coords = chunk.getX() + "," + chunk.getZ();
		whatplayer.remove(coords);
		KP.chunkcoords.remove(coords);
		KP.chunkfriends.remove(coords);
	}

	public void addChunkFriend(String coords, String pname) {
		if (!chunkfriends.containsKey(coords)) {
			List<String> ls = Arrays.asList(pname.toLowerCase());
			chunkfriends.put(coords, ls);
		} else {
			List<String> ls = chunkfriends.get(coords);
			ls.add(pname.toLowerCase());
			chunkfriends.put(coords, ls);
		}
	}

	public void addChunkFriend(Chunk chunk, String pname) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (!chunkfriends.containsKey(coords)) {
			List<String> ls = Arrays.asList(pname.toLowerCase());
			chunkfriends.put(coords, ls);
		} else {
			List<String> ls = chunkfriends.get(coords);
			ls.add(pname.toLowerCase());
			chunkfriends.put(coords, ls);
		}
	}

	public List<String> getChunkFriends(Chunk chunk) {
		String coords = chunk.getX() + "," + chunk.getZ();
		return chunkfriends.get(coords);
	}

	public List<String> getChunkFriends(String coords) {
		return chunkfriends.get(coords);
	}

	public boolean isChunkFriend(String coords, String pname) {
		if (chunkfriends.containsKey(coords)) {
			if (chunkfriends.get(coords).contains(pname.toLowerCase()))
				return true;
		}
		return false;
	}

	public boolean isChunkFriend(Chunk chunk, String pname) {
		String coords = chunk.getX() + "," + chunk.getZ();
		if (chunkfriends.containsKey(coords)) {
			if (chunkfriends.get(coords).contains(pname.toLowerCase()))
				return true;
		}
		return false;
	}

	public void removeChunkFriend(String coords, String pname) {
		List<String> ls = chunkfriends.get(coords);
		ls.remove(pname.toLowerCase());

		chunkfriends.put(coords, ls);

	}
	
	public void removeChunkFriend(Chunk chunk, String pname) {
		String coords = chunk.getX() + "," + chunk.getZ();
		List<String> ls = chunkfriends.get(coords);
		ls.remove(pname.toLowerCase());

		chunkfriends.put(coords, ls);

	}

	public boolean canClaimAnother() {
		if (chunkcoords.size() >= maxchunks) {
			return false;
		}
		return true;
	}

	public boolean isInTrade() {
		return trading;
	}

	// TODO trade system ;)
	public void setInTrade(boolean trade) {
		trading = trade;
	}

}
