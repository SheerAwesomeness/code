package kr;

public class CustomMob {
  Kirithia plugin;
  
  
  private String name;
  int level;
  double lowdmg;
  double highdmg;
  double speed;
  private ItemStack head = null;
  private ItemStack chest = null;
  private ItemStack legs = null;
  private ItemStack feet = null;
  private ItemStack hand = null;
  
  //private String dropgroup?
  //decide how the drops work - algorithm or??? 
  
  //TODO store level ranges for mobs in Main class.
  // Only store BASE mobs here, alter values when spawning
  
  //stored
  private HashMap<String, CustomMob> mobstore = new HashMap<String, CustomMob>();
  
  
  
  public CustomMob(Kirithia kz){
    plugin = kz;
  }
  
  public CustomMob(args1, 2, 3, 4, 5, 6, ...){
    //set all
  }
  
    public CustomMob(String name, int level, double lowdmg, double highdmg, double speed){
   this.name = name;
   this.level = level;
   this.lowdmg = lowdmg;
   this.highdmg = highdmg;
   this.speed = speed;
   mobstore.put(name, this);
  }
  
      public CustomMob(String name, int level, double lowdmg, double highdmg, double speed, List<ItemStack> equipment){
     this.name = name;
   this.level = level;
   this.lowdmg = lowdmg;
   this.highdmg = highdmg;
   this.speed = speed;
   head = equipment.get(0);
   chest = equipement.get(1);
   legs = equipment.get(2);
   feet = equipment.get(3);
   hand = equipment.get(4);
    mobstore.put(name, this);
  }
  
  public void setName(String name){
    this.name = name;
  }
  
  public String getName(){
    return name;
  }
  
  public List<ItemStack> getEquipment(){
    List<ItemStack> ls = new ArrayList<ItemStack>();
    ls.addAll(head, chest, legs, feet, hand);
    return ls;
    
  }
  
  public void setHead(ItemStack i){
    head = i;
  }  
  
    public void setChest(ItemStack i){
    chest = i;
  } 
  
    public void setLegs(ItemStack i){
    legs = i;
  } 
  
    public void setFeet(ItemStack i){
    feet = i;
 } 
  
    public void setHand(ItemStack i){
    hand = i;
  } 
  
  public ItemStack getHead(){
    return head;
  }
  
  public ItemStack getChest(){
    return chest;
  }
  
  public ItemStack getLegs(){
    return legs;
    }
    
    public ItemStack getFeet(){
      return feet;
    }
    
    public ItemStack getHand(){
      return hand;
    }
    
    public setDamages(double low, double high){
      lowdmg = low;
      highdmg = high;
    }
    
    public double getRandomDamage(){
      return ((high-low)*Math.random())+low;
    }
    
    public void setSpeed(double speed){
      this.speed = speed;
    }
    
    public double getSpeed(){
      return speed;
    }
    
    public setLevel(int level){
      this.level = level;
    }
  
    public int getLevel(){
      return level;
    }
  
}
