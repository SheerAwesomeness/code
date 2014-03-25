//Test update
package kr;

public class CustomItem {


public ItemStack setAbility(ItemStack item, KPlayer kpl){
  ItemStack is = item.clone();
  ItemStack im = is.getItemMeta();
  List<String> lore = im.getLore();
  List<String> newlore = new ArrayList<String>();
  if (kpl.hasAbility(im.getDisplayName())){
   newlore.add("§8(§e1§8/§e1§8)");
   newlore.add("§aLearnt.");

  }else{
      newlore.add("§8(§e0§8/§e1§8)");


  }
  newlore.add("§6Ability:");
  newlore.addAll(lore);
   
   im.setLore(newlore);
   is.setItemMeta(im);
   return is;
 
}


}
