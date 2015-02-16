package org.colomoto.logicalmodel.tools.pushcount;


import java.text.ParseException;
import java.util.Iterator;

import org.colomoto.common.task.AbstractTask;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.mddlib.MDDManager;

public class PushCountSearcher extends AbstractTask<PushCountResult> {
	
	private LogicalModel model ;
	
	private final StateTransitionGraph trGraph ;
	
	//BDD representing the source states
	private final int source ;
	
	//BDD representing the target states
	private int target ;

	public PushCountSearcher(LogicalModel model, int source, int target) throws ParseException {
		this.model = model ;
		
		trGraph = new StateTransitionGraph(model) ;
		
		this.source = source ;  
		this.target = target ;
		
	}

	@Override
	protected PushCountResult doGetResult() throws Exception {
		Iterator<byte[]> iter0 = AssignmentsEnum.getAssignments(model.getMDDManager(), target) ;
		int f = trGraph.getAsynchronousTransitionFunction() ;
		
		PushCountResult result = new PushCountResult() ;
		
		while(iter0.hasNext())
		{
			byte[] target = iter0.next();
			int statemdd = model.getMDDManager().nodeFromState(target, 1) ;
			statemdd = trGraph.getManager().parseDump(model.getMDDManager().dumpMDD(statemdd)) ;
			
			int br = trGraph.getBR(f, statemdd) ;
			br = model.getMDDManager().parseDump(trGraph.getManager().dumpMDD(br)) ;
			
			byte[] closestPred = getClosest(f, model.getMDDManager(), source, 
					br, target) ;
			result.pushAncestor.put(target, closestPred) ;
			result.push.put(target, dist(model.getMDDManager(), source, closestPred)) ;
		}
		return result ;
	}
	
	/**
	 * Utility function to print a state on the standard output
	 * @param state
	 */
	private void printState(byte[] state) {
		for(int i = 0 ; i < state.length ; i++)
			System.out.print(state[i]) ;
		System.out.println() ;
	}
	
	/**
	 * Returns the minimum distance from one state of set to state.
	 * The set must not be empty
	 * @param m
	 * @param set
	 * @param state
	 * @return
	 */
	private int dist(MDDManager m, int set, byte[] state){
		Iterator<byte[]> it = AssignmentsEnum.getAssignments(m, set) ;
		int minDist = Integer.MAX_VALUE ;
		while(it.hasNext())
			minDist = Math.min(minDist, dist(state, it.next())) ;
		return minDist ;
	}
	
	/**
	 * The distance between the two states
	 * @param m
	 * @param state1
	 * @param state2
	 * @return
	 */
	private int dist(byte[] state1, byte[] state2){
		assert(state1.length == state2.length) ;
		int d = 0 ;
		for(int i = 0 ; i < state1.length ; i ++)
			d += Math.abs(state1[i] - state2[i]) ;
		return d ;
	}
	
	/**
	 * Returns the state in targetSet that is the closest (in term of push) 
	 * from a state in source.
	 * If there are several at the same distance, then the one that corresponds to 
	 * the shortest path to target is the kept.
	 * The MDD manager corresponds to the manager used to represent the source and 
	 * target sets. 
	 * The transition function is on the other hand connected to the Manager of the trGraph
	 * @return
	 */
	private byte[] getClosest(int f, MDDManager m, int source, int targetSet, byte[] target){
		byte[] res = null ;
		int minDist = m.getLeafCount() * trGraph.varNum ;
		int minPathDist = Integer.MAX_VALUE ;
		Iterator<byte[]> it = AssignmentsEnum.getAssignments(m, targetSet) ;
		while(it.hasNext())
		{
			byte[] next = it.next() ;
			int d = dist(m, source, next) ;
			
			if(minDist >= d)
			{
				int newPathDist = trGraph.pathDist(f, next, target) ; 
				if(minDist > d || newPathDist < minPathDist)
				{
					minDist = d ;
					res = next ;
					minPathDist = newPathDist ;
				}
			}
		}
		return res;
	}
	

}
