package mallorcatour.bot.interfaces;

import mallorcatour.bot.villainobserver.IVillainListener;
import mallorcatour.brains.IAdvisor;

/**
 * Interface that combines functionality of pokerNN and villain listener
 * @author andriipanasiuk
 *
 */
public interface IVillainModel extends IAdvisor, IVillainListener {
	
}