package kr.Towns;

import java.util.HashMap;

import kr.Kirithia;

import org.bukkit.ChunkSnapshot;

public class Chunks {
	Kirithia plugin;

	private HashMap<String, Chunks> chunks = new HashMap<String, Chunks>();
	private HashMap<String, String> chunktypes = new HashMap<String, String>();
	
	public Chunks(Kirithia kr) {
		this.plugin = kr;
	}
	

	
	public void addChunk(ChunkSnapshot chunk, String type){
		String coords = chunk.getX() + "," + chunk.getZ();
		chunktypes.put(coords, type);
		chunks.put(coords, this);
	}
	
	public void addChunk(String coords, String type){
		chunktypes.put(coords, type);
		chunks.put(coords, this);
	}
	
	public void removeChunk(ChunkSnapshot chunk){
		String coords = chunk.getX() + "," + chunk.getZ();
		chunktypes.remove(coords);
		chunks.remove(coords);
	}
	public void removeChunk(String coords){
		chunks.remove(coords);
		chunktypes.remove(coords);
	}


	public void setType(ChunkSnapshot chunk, String type){
		String coords = chunk.getX() + "," + chunk.getZ();
		chunktypes.put(coords, type);
	}
	
	public String getType(ChunkSnapshot chunk){
		String coords = chunk.getX() + "," + chunk.getZ();

		if (chunktypes.get(coords) == null){
			return "wild";
		}
		return 		chunktypes.get(coords);
	}
	
	public String getType(String coords){


		if (chunktypes.get(coords) == null){
			return "wild";
		}
		return 		chunktypes.get(coords);
	}
	
}
