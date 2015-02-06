package org.colomoto.logicalmodel.tools.pushcount;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of a PushCount computation.
 * It contains, for each state of the target state, the 
 * number of variables from a source state that must be modified to be able 
 * to reach it, and the ancestor of the target state that will be reached 
 * this way
 * @author heinrich
 *
 */
public class PushCountResult {
	
	Map<byte[], Integer> push = new HashMap<byte[], Integer>() ;
	
	Map<byte[], byte[]> pushAncestor = new HashMap<byte[], byte[]>() ;

	/**
	 * Returns the number of varaibles that must be modified to reach state target.
	 * If target is not part of the target states, returns -1.
	 * @param target
	 * @return
	 */
	public int getPush(byte[] target) {
		return push.get(target) ;
	}
	
	/**
	 * Returns the ancestor of target that is reached by choosing the smallest 
	 * perturbation from a source state.
	 * @param target
	 * @return
	 */
	public byte[] getPushAncestor(byte[] target){
		return null;
	}
	
	/**
	 * Returns a string describing this result
	 * @return
	 */
	public String print(){
		String s = "" ;
		for(Map.Entry<byte[], Integer> entry : push.entrySet())
		{
			s += "target : " + printState(entry.getKey()) +"\n" ;
			s += "push = " + entry.getValue() + "\n" ;
			s += "target ancestor = " + printState(pushAncestor.get(entry.getKey())) + "\n\n" ;
		}
		return s ;
	}
	
	public String printState(byte[] state) {
		String s = "" ;
		for(int i = 0 ; i < state.length ; i++)
			s += state[i] ;
		return s ;
	}
}
