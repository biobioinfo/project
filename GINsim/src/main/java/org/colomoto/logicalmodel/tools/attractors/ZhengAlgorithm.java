package org.colomoto.logicalmodel.tools.attractors;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.colomoto.logicalmodel.tools.pushcount.AssignmentsEnum;
import org.colomoto.logicalmodel.tools.pushcount.MDDQuantifier;
import org.colomoto.logicalmodel.tools.pushcount.SimpleOperator;
import org.colomoto.logicalmodel.tools.pushcount.StateTransitionGraph;
import org.colomoto.mddlib.operators.MDDBaseOperators;

/**
 * Implementation of the algorithm from Zheng et al.
 * Reference : http://journals.plos.org/plosone/article?id=10.1371/journal.pone.0060593
 * Note that the algorithm in the asynchronous case is exactly the same as for 
 * Garg et al. algorithm, only the synchronous part differ. 
 * @author heinrich
 *
 */
public class ZhengAlgorithm {
	
	private StateTransitionGraph trGraph ; 
	
	public ZhengAlgorithm(StateTransitionGraph trGraph){
		this.trGraph = trGraph ;
	}

	public List<List<byte[]>> computeSynchronousAttractors() {
		List<List<byte[]>> attractors = new LinkedList<List<byte[]>>() ;
		
		int fsyn = trGraph.getSynchronousTransitionFunction() ;
		//Substitute x by z (used later in the loop)
		int fsynz = SimpleOperator.substitute(trGraph.getManager(), 
				trGraph.xVars, trGraph.zVars, fsyn) ;
		
		
		int stateSpace = 1 ;
		int deltaj = 0 ; 
		int deltaUnion = 0 ;
		
		int[] varsEqual = new int[trGraph.varNum] ;
		for(int i = 0 ; i < trGraph.varNum ; i++)
		{
			varsEqual[i] = SimpleOperator.EQUAL.combine(trGraph.getManager(), 
					trGraph.getVariableValue(trGraph.xVars[i]), 
					trGraph.getVariableValue(trGraph.yVars[i])) ;
		}
		//identity mdd : returns 1 iff x == y
		int identity = MDDBaseOperators.AND.combine(trGraph.getManager(), varsEqual) ; 
		//fj(x,y) = 1 iff x -> y in exactly j transitions
		//fj is initialized with fj(x,y) = (x == y)
		int fj = identity ;
		
		while(stateSpace != 0)
		{
			//compute f_j+1
			fj = SimpleOperator.substitute(trGraph.getManager(), trGraph.yVars, trGraph.zVars, fj) ;
			fj = MDDBaseOperators.AND.combine(trGraph.getManager(), fj, fsynz) ;
			fj = MDDQuantifier.QUANTIFY_MAX.combine(trGraph.getManager(), trGraph.zVars, fj) ;
			
			deltaj = MDDBaseOperators.AND.combine(trGraph.getManager(), fj, identity) ;
			deltaj = MDDQuantifier.QUANTIFY_MAX.combine(trGraph.getManager(), trGraph.yVars, deltaj) ;
			//deltaj = deltaj \setminus deltaUnion
			deltaj = MDDBaseOperators.AND.combine(trGraph.getManager(),
					deltaj, trGraph.getManager().not(deltaUnion)) ;
			
			int br = trGraph.getBR(fsyn, deltaj) ;
			//stateSpace = stateSpace \setminus br
			stateSpace = MDDBaseOperators.AND.combine(trGraph.getManager(), 
					stateSpace, trGraph.getManager().not(br)) ;
			deltaUnion = MDDBaseOperators.OR.combine(trGraph.getManager(), 
					deltaUnion, deltaj) ;

			int deltacopy = deltaj ;
			
			while(deltacopy != 0)
			{
				int state = trGraph.getMddForState(trGraph.getOneState(deltacopy)) ;
				int att = trGraph.getFR(fsyn, state) ;
				attractors.add(enumStates(att)) ;
				deltacopy = MDDBaseOperators.AND.combine(trGraph.getManager(), deltacopy, 
						trGraph.getManager().not(att)) ;
			}
		}
		return attractors;
	}

	/**
	 * (Copied from class GargAlgorithm)
	 * @return
	 */
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
	 * (copied from class GargAlgorithm)
	 *  
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
	 * (copied from GargAlgorithm class).
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
