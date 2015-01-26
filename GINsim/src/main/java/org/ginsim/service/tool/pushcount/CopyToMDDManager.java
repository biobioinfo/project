package org.ginsim.service.tool.pushcount;

import java.util.HashMap;
import java.util.Map;

import org.colomoto.mddlib.MDDManager;

/**
 * Utility class used to copy a certain MDD from one MDD manager to an other.
 * The primary use of this class it to circumvent the fact that it is not possible to 
 * add new variables into a MDDManager, so the creation of a new one is needed to perform some operations
 * 
 * @author heinrich
 *
 */
public class CopyToMDDManager {
	
	private final MDDManager target ;
	
	//Temporary variables
	private MDDManager source ;
	private Map<Integer, Integer> correspondance = new HashMap<Integer, Integer>();
	
	public CopyToMDDManager(MDDManager target){
		this.target = target ;
	}
	
	/**
	 * Copies the MDD mdd, from the manager source to the manager target
	 * @param source
	 * @param mdd
	 * @return : the identifier for the copied MDD in MDDManager target.
	 */
	public int copy(MDDManager source, int mdd) {
		if(this.source != source)
		{
			this.source = source ;
			correspondance.clear() ;
		}
		if(correspondance.containsKey(mdd))
			return correspondance.get(mdd) ;
		if(source.isleaf(mdd))
		{
		
		}
		
		
		int[] sourceChildren = source.getChildren(mdd) ;
		int[] targetChildren = new int[sourceChildren.length] ;
		for(int i = 0 ; i < sourceChildren.length ; i++)
		{
			
		}
		
		return 0 ;
	}
	
}
