package org.ginsim.service.tool.pushcount;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.operators.MDDBaseOperators;

/**
 * Implicit representation of the state transition graph.
 * Note that this if there are n nodes in the regulatory graph, 
 * this class needs 2n variables. 
 * For now it creates a new MDDManager to add new variables.
 * consequently, all the MDDs that are passed in arguement should 
 * come from this new manager, except if stated otherwise.
 * @author heinrich
 *
 */
public class StateTransitionGraph {

	//Temporary manager with more variables
	private final MDDManager tempManager ;

	//Number of variables in the initial manager
	public final int varNum ;

	//List of the new variables
	private final List<MDDVariable> varList = new ArrayList<MDDVariable>() ;

	public final MDDVariable[] xVars ;
	public final MDDVariable[] yVars ;

	//Copy of the functions into the tempManager
	private final int[] func ;


	public StateTransitionGraph(LogicalModel model) {
		MDDManager manager = model.getMDDManager() ;

		MDDVariable[] vars = manager.getAllVariables() ;
		varNum = vars.length ;
		String[] tempVars = new String[vars.length] ;
		for(int i = 0 ; i < tempVars.length ; i++)
			tempVars[i] = "temp " + i ;

		MDDVariableFactory allVars = new MDDVariableFactory() ;

		for(int i = 0 ; i < vars.length ; i++)
			allVars.add(vars[i], vars[i].nbval) ;
		for(int i = 0 ; i < vars.length ; i++)
			allVars.add(tempVars[i], vars[i].nbval) ;

		tempManager = MDDManagerFactory.getManager(allVars, manager.getLeafCount()) ;

		for(int i = 0 ; i < 2* varNum ; i ++)
			varList.add(tempManager.getVariableForKey(allVars.get(i))) ;

		int[] func0 = model.getLogicalFunctions() ;
		func = new int[func0.length] ;
		try {
			for(int i = 0 ; i<func.length ; i++)
				func[i] = tempManager.parseDump(manager.dumpMDD(func0[i])) ;
		} catch (ParseException e) {
			System.err.println("Error in parse/dump MDD functions") ;
			throw new RuntimeException("Parse/Dump of an MDD failed") ;
		}
		xVars = varList.subList(0, varNum).toArray(new MDDVariable[0]) ;
		yVars = varList.subList(varNum, 2*varNum).toArray(new MDDVariable[0]) ;
	}
	
	
	public MDDManager getManager(){
		return tempManager ;
	}
	
	
	/**
	 * Utility function.
	 * Produces a MDD that returns the value of the variable var
	 * (does not depend on other variables).
	 * @param var
	 * @return
	 */
	int getVariableValue(MDDVariable var){
		int[] values = new int[var.nbval] ;
		for(int i = 0 ; i < var.nbval ; i++)
		{
			values[i] = i ;
		}
		return var.getNode(values) ;
	}
	
	
	/**
	 * Number of transitions needed to go from source to target following transitions
	 * from f.
	 * @param m
	 * @param f
	 * @param source
	 * @param target
	 * @return
	 */
	public int pathDist(int f, byte[] source, byte[] target) {
		assert(target.length == varNum) ;
		byte[] target1 = new byte[2*varNum] ;
		for(int i = 0 ; i < varNum ; i++)
		{
			target1[i] = target[i] ;
		}
		
		int mdd = tempManager.nodeFromState(target1, 1) ;
		mdd = AbstractQuantifier.QUANTIFY_MAX.combine(tempManager, yVars, mdd) ;
		//mdd evaluates to 1 only on target and does not depend on y.
		//See, maybe I need to expand source too....
		while(tempManager.reach(mdd, source) == 0)
		{
			mdd = getPredecessors(f, mdd) ;
		}
		return mdd ;
	}
	
	/**
	 * An MDD representing the transition function. The resulting function takes two 
	 * bit-string arguments x and y, and returns 1 if there is a transition from x to y, 
	 * and 0 otherwise
	 * @return
	 */
	int getSynchronousTransitionFunction(){
		int[] fi = new int[func.length] ;
		
		for(int i = 0 ; i < fi.length ; i ++)
		{
			int xi = getVariableValue(xVars[i]) ;
			int yi = getVariableValue(yVars[i]) ;
			int i1 = MDDBaseOperators.AND.combine(tempManager, 
					SimpleOperator.EQUAL.combine(tempManager, func[i], xi),
					SimpleOperator.EQUAL.combine(tempManager, xi, yi));
			
			int i2 = MDDBaseOperators.AND.combine(tempManager, 
					SimpleOperator.GREATER_THAN.combine(tempManager, func[i], xi),
					SimpleOperator.EQUAL.combine(tempManager, 
							SimpleOperator.ADD.combine(tempManager, xi, 1), yi)) ;
			int i3 = MDDBaseOperators.AND.combine(tempManager, 
					SimpleOperator.LESS_THAN.combine(tempManager, func[i], xi),
					SimpleOperator.EQUAL.combine(tempManager, 
							SimpleOperator.SUB.combine(tempManager, xi, 1), yi)) ;
			fi[i] = MDDBaseOperators.OR.combine(tempManager, new int[]{i1 , i2, i3}) ;
			System.out.println(tempManager.dumpMDD(fi[i])) ;
		}
		//Function whose value is true on x,y iff (x,y) is a transition
		int f = MDDBaseOperators.AND.combine(tempManager, fi) ;
		
		return f ;
	}


	/**
	 * An MDD representing the transition function. The resulting function takes two 
	 * bit-string arguments x and y, and returns 1 if there is a transition from x to y, 
	 * and 0 otherwise
	 * @return
	 */
	int getAsynchronousTransitionFunction(){
		//We can write f = OR(fi)
		//Where the fi are defined by :
		//fi = AND_{j \neq i} (x_j = y_j) 
		//	AND ((y_i = x_i AND x_i = f_i(x)) 
		//			OR (x_i < f_i(x) AND y_i = x_i +1) 
		//			OR x_i > f_i(x) AND y_i = x_i -1)
		
		int[] fi = new int[func.length] ;
		for(int i = 0 ; i < fi.length ; i ++)
		{
			int[] fij = new int[varNum] ;
			for(int j = 0 ; j < varNum ; j ++)
			{
				if(j != i)
					fij[j] = SimpleOperator.EQUAL.combine(tempManager, 
							getVariableValue(xVars[j]), 
							getVariableValue(yVars[j])) ;
				else
				{
					int xi = getVariableValue(xVars[i]) ;
					int yi = getVariableValue(yVars[i]) ;
					
					int i1 = MDDBaseOperators.AND.combine(tempManager,
							SimpleOperator.EQUAL.combine(tempManager, xi, yi),
							SimpleOperator.EQUAL.combine(tempManager, xi, func[i])) ;
					int i2 = MDDBaseOperators.AND.combine(tempManager,
							SimpleOperator.GREATER_THAN.combine(tempManager, func[i], xi),
							SimpleOperator.EQUAL.combine(tempManager, 
									SimpleOperator.ADD.combine(tempManager, xi, 1),
									yi)) ;
					int i3 = MDDBaseOperators.AND.combine(tempManager,
							SimpleOperator.LESS_THAN.combine(tempManager, func[i], xi),
							SimpleOperator.EQUAL.combine(tempManager, 
									SimpleOperator.SUB.combine(tempManager, xi, 1),
									yi)) ;
					
					fij[j] = MDDBaseOperators.OR.combine(tempManager, new int[]{i1,i2,i3}) ;
				}
			}
			fi[i] = MDDBaseOperators.AND.combine(tempManager, fij) ;
		}
		
		return MDDBaseOperators.OR.combine(tempManager, fi) ;
	}

	
	/**
	 * Returns the predecessors of the given state of states, that is the 
	 * states that can reach a state in startStates in exactly one transition
	 * @param f : the transition function
	 * @param startStates : the set of initial states
	 * @return
	 */
	int getPredecessors(int f, int startStates){
		int set = SimpleOperator.substitute(tempManager, xVars, yVars, startStates) ;
		set = SimpleOperator.MIN.combine(tempManager, f, set) ;
		set= MDDQuantifier.QUANTIFY_MAX.combine(tempManager, yVars, set) ;
		return set ;
	}
	
	/**
	 * Returns the set of states that are backward reachable from a 
	 * state in startStates, ie, that can reach a state in startStates in 
	 * 0, 1 or more transitions.
	 * @param f
	 * @param startStates
	 * @return
	 */
	int getBR(int f, int startStates) {
		int oldSet = 0 ;
		int newSet = startStates ;
		while(oldSet != newSet)
		{
			oldSet = newSet ;
			newSet = getPredecessors(f, newSet) ;
			newSet = MDDBaseOperators.OR.combine(tempManager, newSet, oldSet) ;
			//newSet = SimpleOperator.substitute(tempManager, xVars, yVars, newSet) ;
			//newSet = SimpleOperator.MIN.combine(tempManager, f, newSet) ;
			//newSet = MDDQuantifier.QUANTIFY_MAX.combine(tempManager, yVars, newSet) ;
			//newSet = MDDBaseOperators.OR.combine(tempManager, newSet, oldSet) ;
		}
		return newSet ;
	}
	
	int getSucessor(int f, int startStates){
		int set = startStates ;//SimpleOperator.substitute(tempManager, xVars, yVars, startStates) ;
		set = SimpleOperator.MIN.combine(tempManager, f, set) ;
		set = MDDQuantifier.QUANTIFY_MAX.combine(tempManager, xVars, set) ;
		set = SimpleOperator.substitute(tempManager, yVars, xVars, set) ;
		return set ;
	}
	
	int getFR(int f, int startStates){
		int oldSet = 0 ;
		int newSet = startStates ;
		while(oldSet != newSet)
		{
			oldSet = newSet ;
			newSet = getSucessor(f, newSet) ;
			newSet = MDDBaseOperators.OR.combine(tempManager, newSet, oldSet) ;
		}
		return newSet ;
	}
	
	
}
