package org.colomoto.logicalmodel.tools.attractors;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.colomoto.logicalmodel.tools.pushcount.AssignmentsEnum;
import org.colomoto.logicalmodel.tools.pushcount.StateTransitionGraph;
import org.colomoto.mddlib.operators.MDDBaseOperators;

/**
 * Implementation of Garg et al. algorithm to compute attractors in a model.
 * Reference : http://link.springer.com/chapter/10.1007%2F978-3-540-71681-5_5
 * @author heinrich
 *
 */
public class GargAlgorithm {
	
	private final StateTransitionGraph trGraph ;
	
	public GargAlgorithm(StateTransitionGraph trgraph){
		this.trGraph = trgraph ;
	}

	public List<List<byte[]>> computeSynchronousAttractors() {
		List<List<byte[]>> attractors = new LinkedList<List<byte[]>>() ;
		int allStates = 1 ;
		
		int f = trGraph.getSynchronousTransitionFunction() ;
		
		while(allStates != 0)
		{	
			int initState = getInitState(f, allStates) ;
			int att = trGraph.getFR(f, initState) ;
			int br = trGraph.getBR(f, initState) ;
			
			//allStates = allStest \ br
			allStates = MDDBaseOperators.AND.combine(trGraph.getManager(), 
					trGraph.getManager().not(br), allStates);
			
			attractors.add(enumStates(att)) ;
		}
		
		return attractors;
	}
	
	public int getInitState(int f, int states){
		if(states == 0)
			throw new IllegalArgumentException("Cannot return a state from an mdd = 0") ;
		
		byte[] t = trGraph.getOneState(states) ;
		int visited = trGraph.getMddForState(t) ;

		int lastAdded = visited ;
		while(true)
		{
			int newVisited = trGraph.getSucessor(f, visited) ;
			//newAdded = newVisited \setminus visited
			int newAdded = MDDBaseOperators.AND.combine(trGraph.getManager(), newVisited, 
					trGraph.getManager().not(visited)) ; 
			
			if(newAdded == 0) 
				return lastAdded ;
			else
			{
				lastAdded = newAdded ;
				//visited = visited U lastAdded
				visited = MDDBaseOperators.OR.combine(trGraph.getManager(), lastAdded, visited) ;
			}
		}
	}

	public List<List<byte[]>> computeAsynchronousAttractors() {
		int states = 1 ;
		List<List<byte[]>> res = new LinkedList<List<byte[]>>() ;
		
		List<List<byte[]>> synAtts = computeSynchronousAttractors() ;
		int fasyn = trGraph.getAsynchronousTransitionFunction() ;
		
		for(List<byte[]> satt : synAtts)
		{
			if(AttractorsTools.isSimpleLoop(trGraph, satt))
			{
				res.add(satt) ;
				int br = trGraph.getBR(fasyn, trGraph.getMddForState(satt.get(0))) ;
				//states = states \setminus br
				states = MDDBaseOperators.AND.combine(trGraph.getManager(),
						states, trGraph.getManager().not(br)) ;
			}
		}
		res.addAll(naiveAsynchronousAttractors(fasyn, states)) ;
		
		return res ;
	}
	
	/**
	 * Apply the naive exploration to find asynchronous 
	 * @param states
	 * @return
	 */
	public List<List<byte[]>> naiveAsynchronousAttractors(int f, int states) {
		List<List<byte[]>> result = new LinkedList<List<byte[]>>() ;
		while(states != 0)
		{
			byte[] t = trGraph.getOneState(states) ;
			int tmdd = trGraph.getMddForState(t) ;
			int br = trGraph.getBR(f, tmdd) ;
			int fr = trGraph.getFR(f, tmdd) ;
			//set = fr \setminus br
			int set = MDDBaseOperators.AND.combine(trGraph.getManager(), fr, trGraph.getManager().not(br) );
			if(set == 0)
			{
				//fr \subset br => fr is an attractor
				result.add(enumStates(fr)) ;
			}
			//states = states \setminus br 
			states = MDDBaseOperators.AND.combine(trGraph.getManager(),
					states, trGraph.getManager().not(br)) ;
		}
		return result ;
	}
	
	/**
	 * Utility : Returns a list of all the states in the mdd
	 * @param mdd
	 * @return
	 */
	protected List<byte[]> enumStates(int mdd) {
		Iterator<byte[]> it = AssignmentsEnum.getAssignments(trGraph.getManager(), mdd, trGraph.xVars) ;
		List<byte[]> l = new LinkedList<byte[]>() ;
		while(it.hasNext())
			l.add(it.next()) ;
		return l ;
	}

}
