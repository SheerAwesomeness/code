package kr.Towns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import kr.Kirithia;

import org.bukkit.Chunk;

public class AdminTowns {
	Kirithia plugin;
	private String name;
	private List<String> chunks = new LinkedList<String>();
	public List<String> towns = new LinkedList<String>();
	private List<String> deniedperms = new LinkedList<String>();
	private HashMap<String, AdminTowns> whatat = new HashMap<String, AdminTowns>();
	private String message;
	private String nopermmessage;
	boolean silent = false;
	private List<String> allperms = Arrays.asList("animaldmg", "animalsp", "lever", "button", "door", "pressure", "tripwire", "repeater", "chest", "furnace", "hopper", "build", "enderchest", "trapdoor");

	public AdminTowns(Kirithia kr) {
		this.plugin = kr;
	}

	public AdminTowns(String name) {
		this.name = name;
	}
	
	public void setupAT(AdminTowns at){
		whatat.put(at.name.toLowerCase(), at);
		towns.add(at.name);
		for (String coords: at.getChunks()){
			whatat.put(coords, at);
		}
	}
	
	public void createDefaultAT(AdminTowns at){
		whatat.put(at.name.toLowerCase(), at);
		towns.add(at.name);
		at.message = "Default Message";
		at.nopermmessage = "Default No Perm Message";
	}
	
public void delete(AdminTowns a){
	towns.remove(a.getName());
	whatat.remove(a.getName().toLowerCase());
	for (String coords: a.getChunks()){
		whatat.remove(coords);
		
	}
}
	
	public AdminTowns(String name, List<String> chunks, String message, String nopermmessage, List<String> deniedperms, boolean silent){
		this.name = name;
		this.chunks = chunks;
		this.message = message;
		this.deniedperms = deniedperms;
		this.nopermmessage = nopermmessage;
		this.silent = silent;
	}
	
	public void addChunk(AdminTowns at, Chunk chunk){
		String coords = chunk.getX() + "," + chunk.getZ();
		at.chunks.add(coords);
		whatat.put(coords, at);
	}
	
	public void addChunk (AdminTowns at, String coords){
		at.chunks.add(coords);
		whatat.put(coords, at);
	
	}
	
	public void removeChunk(AdminTowns at, Chunk chunk){
		String coords = chunk.getX() + "," + chunk.getZ();
		at.chunks.remove(coords);
		whatat.remove(coords);
	}
	
	public void removeChunk(AdminTowns at, String coords){
		at.chunks.remove(coords);
		whatat.remove(coords);
	}
	
	public List<String> getChunks(){
		return chunks;
	}
	
	public String getName(){
		return name;
	}
	
	public void setMessage(String msg){
		message = msg;
	}
	
	public String getMessage(){
		return message;
	}
	
	public boolean canUseName(String name){
		if (whatat.containsKey(name.toLowerCase())){
			return false;
		}
		return true;
	}
	
	public void setNoPermMessage(String msg){
		nopermmessage = msg;
	}
	
	public String getNoPermMessage(){
		return nopermmessage;
	}
	
	public List<String> getTownList (){
		return towns;
	}
	
	public AdminTowns getTownbyName(String name){
		if (whatat.containsKey(name.toLowerCase())){
		return whatat.get(name.toLowerCase());
		}
		return null;
	}
	
	public void setSilent(boolean b){
		silent = b;
		
	}
	
	public boolean isSilent(){
		return silent;
	}
	
	public void setDeniedPerms(List<String> denied){
		deniedperms = denied;
	}
	
	public void addDeniedPerm(String perm){
		if (perm.equalsIgnoreCase("all")){
		for (String p: allperms){
			if (!deniedperms.contains("p")){
				deniedperms.add(p);
			}
		}
		}else{
		deniedperms.add(perm);
		}
	}
	
	public void removeDeniedPerm(String perm){
		if (perm.equalsIgnoreCase("all")){
		for (String p: allperms){
			if (deniedperms.contains(p)){
				deniedperms.remove(p);
			}
		}
		}else{
		deniedperms.remove(perm);
		}
	}
	
	public List<String> getDeniedPerms(){
		return deniedperms;
	}
	
	public List<String> getAllPerms(){
		return allperms;
	}
	
}
