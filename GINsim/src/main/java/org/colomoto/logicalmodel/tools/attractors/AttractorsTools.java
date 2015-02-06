package org.colomoto.logicalmodel.tools.attractors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.colomoto.logicalmodel.tools.pushcount.StateTransitionGraph;

public class AttractorsTools {
	
	/**
	 * Wrapper class to hold byte[] states.
	 * Reason is that compare method on java arrays is equivalent to ==, 
	 * and does not check the content. So using Set<byte[]> in the method 
	 * after does not lead to the expected result.
	 * @author heinrich
	 *
	 */
	private static class ArrayWrapper {
		
		public final byte[] t ;
		public ArrayWrapper(byte[] t) {
			this.t = t ;
		}
		
		public boolean equals(Object b) {
			if(b instanceof ArrayWrapper)
			{
				ArrayWrapper a = (ArrayWrapper) b ;
				return Arrays.equals(t, a.t) ;
			}
			return false ;
		}
		
		public int hashCode(){
			return Arrays.hashCode(t);
		}
		
	}

	
	
	/**
	 * Returns true if the given synchronous attractor is a simple loop. 
	 * If the set of states given is not a synchronous attractor, then will throw an 
	 * IllegalArgumentException.
	 * @param syncAtt
	 * @return
	 */
	public static boolean isSimpleLoop(StateTransitionGraph trGraph, List<byte[]> syncAtt) {
		Set<ArrayWrapper> states = new HashSet<ArrayWrapper>();
		for(byte[] state : syncAtt)
			states.add(new ArrayWrapper(state)) ;
		
		byte[] startState = states.iterator().next().t ;
		
		states.remove(new ArrayWrapper(startState)) ;
		byte[] lastState = startState ;
		while(states.size() > 0)
		{
			byte[] newState = trGraph.getSynchronousSuccessor(lastState) ;

			
			if(!states.remove(new ArrayWrapper(newState)))
				throw new IllegalArgumentException("The set of states given is not an attractor") ;
			int diff = 0 ;
			for(int i = 0 ; i < newState.length ; i++)
				diff += Math.abs(newState[i] - lastState[i]) ;
			if(diff >= 2)
				return false ;
			lastState = newState ;
		}
		byte[] newState = trGraph.getSynchronousSuccessor(lastState) ;

		
		if(!Arrays.equals(newState,startState))
			throw new IllegalArgumentException("The set of states given is not an attractor") ;
		
		return true ;
	}
	
}
