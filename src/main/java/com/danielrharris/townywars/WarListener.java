package main.java.com.danielrharris.townywars;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WarListener
  implements Listener
{
  
  WarListener(TownyWars aThis) {}
  
  @EventHandler
  public void onNationDelete(DeleteNationEvent event){
	  
	  Nation nation = null;
	  War war = null;
	  
	  for(War w : WarManager.getWars())
		  for(Nation n : w.getNationsInWar())
			  if(n.getName().equals(event.getNationName())){
				  nation = n;
				  war = w;
				  break;
			  }
	  
	  if(war == null){
		  for(Rebellion r : Rebellion.getAllRebellions())
		    	if(r.getMotherNation().getName().equals(event.getNationName()))
		    		Rebellion.getAllRebellions().remove(r);
		  return;
	  }
	  
	  WarManager.getWars().remove(war);
	  
	  if(war.getRebellion() != null){
		  Rebellion.getAllRebellions().remove(war.getRebellion());
		  if(war.getRebellion().getRebelnation() != nation)
			  TownyUniverse.getDataSource().deleteNation(war.getRebellion().getRebelnation());
		  else if(war.getRebellion().getMotherNation() != nation)
			  war.getRebellion().peace();
	  }
	  
	  TownyUniverse.getDataSource().saveNations();
	  try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    Player player = event.getPlayer();
    try
    {
      Resident re = TownyUniverse.getDataSource().getResident(player.getName());
      Nation nation = re.getTown().getNation();
      Player plr = Bukkit.getPlayer(re.getName());
      
      War ww = WarManager.getWarForNation(nation);
      if (ww != null)
      {
        player.sendMessage(ChatColor.RED + "Warning: Your nation is at war with " + ww.getEnemy(nation));
        if ((WarManager.hasBeenOffered(ww, nation)) && ((nation.hasAssistant(re)) || (re.isKing()))) {
          player.sendMessage(ChatColor.GREEN + "The other nation has offered peace!");
        }
      }
    }
    catch (Exception ex) {}
  }
  
  @EventHandler
  public void onResidentLeave(TownRemoveResidentEvent event)
  {
    Nation n;
    try
    {
      n = event.getTown().getNation();
    }
    catch (NotRegisteredException ex)
    {
      return;
    }
    War war = WarManager.getWarForNation(n);
    if (war == null) {
      return;
    }
    try {
		if(WarManager.getWarForNation(event.getTown().getNation()).getTownPoints(event.getTown()) > TownyWars.pPlayer)
			war.chargeTownPoints(n, event.getTown(), TownyWars.pPlayer);
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @EventHandler
  public void onResidentAdd(TownAddResidentEvent event)
  {
    Nation n;
    try
    {
      n = event.getTown().getNation();
    }
    catch (NotRegisteredException ex)
    {
      return;
    }
    War war = WarManager.getWarForNation(n);
    if (war == null) {
      return;
    }
    war.chargeTownPoints(n, event.getTown(), -TownyWars.pPlayer);
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @EventHandler
  public void onNationAdd(NationAddTownEvent event)
  {
    War war = WarManager.getWarForNation(event.getNation());
    if (war == null) {
      return;
    }
      war.addNationPoint(event.getNation(), event.getTown());
      war.addNewTown(event.getTown());
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @EventHandler
  public void onNationRemove(NationRemoveTownEvent event)
  {
	  War war = WarManager.getWarForNation(event.getNation());
      if (war == null) {
        return;
      }
      
      war.removeTown(event.getTown(), event.getNation());
      
      try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	 //MAKE FUCKING WORK when a town is disbanded because of lack of funds
    /*if (event.getTown() != WarManager.townremove)
    {
      War war = WarManager.getWarForNation(event.getNation());
      if (war == null) {
        return;
      }
      townadd = event.getTown();
      try
      {
    	  if(event.getNation().getNumTowns() != 0)
    		  event.getNation().addTown(event.getTown());
      }
      catch (AlreadyRegisteredException ex)
      {
        Logger.getLogger(WarListener.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else{
    	 for(Rebellion r : Rebellion.getAllRebellions())
    	    	if(r.isRebelLeader(event.getTown())){
    	    		Rebellion.getAllRebellions().remove(r);
    	    		break;
    	    	}
    	    	else if(r.isRebelTown(event.getTown())){
    	    		r.removeRebell(event.getTown());
    	    		break;
    	    	}
    }
    
    TownyUniverse.getDataSource().saveNations();
    WarManager.townremove = null;*/
  }
  
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event)
  {
    Player plr = event.getEntity();
    EntityDamageEvent edc = event.getEntity().getLastDamageCause();
    if (!(edc instanceof EntityDamageByEntityEvent)) {
      return;
    }
    EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)edc;
    if (!(edbee.getDamager() instanceof Player)) {
      return;
    }
    Player damager = (Player)edbee.getDamager();
    Player damaged = (Player)edbee.getEntity();
    try
    {
      Town tdamagerr = TownyUniverse.getDataSource().getResident(damager.getName()).getTown();
      Nation damagerr = tdamagerr.getNation();
      

      Town tdamagedd = TownyUniverse.getDataSource().getResident(damaged.getName()).getTown();
      Nation damagedd = tdamagedd.getNation();
      
      War war = WarManager.getWarForNation(damagerr);
      if ((war.hasNation(damagedd)) && (!damagerr.getName().equals(damagedd.getName())))
      {
        tdamagedd.pay(TownyWars.pKill, "Death cost");
        tdamagerr.collect(TownyWars.pKill);
      }
      if ((war.hasNation(damagedd)) && (!damagerr.getName().equals(damagedd.getName()))) {
        try
        {
          war.chargeTownPoints(damagedd, tdamagedd, 1);
          int lP = war.getTownPoints(tdamagedd);
          if (lP <= 10 && lP != -1 && WarManager.getWars().contains(war)) {
            plr.sendMessage(ChatColor.RED + "Be careful! Your town only has a " + lP + " points left!");
          }
        }
        catch (Exception ex)
        {
          plr.sendMessage(ChatColor.RED + "An error occured, check the console!");
          ex.printStackTrace();
        }
      }
    }
    catch (Exception ex) {}
    
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
