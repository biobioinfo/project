package org.colomoto.logicalmodel.tools.pushcount;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
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
	public final MDDVariable[] zVars ;

	//Copy of the functions into the tempManager
	private final int[] func ;


	public StateTransitionGraph(LogicalModel model) {
		MDDManager manager = model.getMDDManager() ;

		MDDVariable[] vars = manager.getAllVariables() ;
		varNum = vars.length ;
		String[] tempVars = new String[2*varNum] ;
		for(int i = 0 ; i < tempVars.length ; i++)
			tempVars[i] = "temp " + i ;

		MDDVariableFactory allVars = new MDDVariableFactory() ;

		for(int i = 0 ; i < vars.length ; i++)
			allVars.add(vars[i], vars[i].nbval) ;
		for(int i = 0 ; i < vars.length ; i++)
			allVars.add(tempVars[i], vars[i].nbval) ;
		for(int i = 0 ; i < vars.length ; i++)
			allVars.add(tempVars[i+varNum], vars[i].nbval) ;
		
		tempManager = MDDManagerFactory.getManager(allVars, manager.getLeafCount()) ;

		for(int i = 0 ; i < 3* varNum ; i ++)
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
		zVars = varList.subList(2*varNum, 3*varNum).toArray(new MDDVariable[0]) ;
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
	public int getVariableValue(MDDVariable var){
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
		/*byte[] target1 = new byte[3*varNum] ;
		for(int i = 0 ; i < varNum ; i++)
		{
			target1[i] = target[i] ;
		}*/

		//int mdd = tempManager.nodeFromState(target1, 1) ;
		int mdd = getMddForState(target) ;
		//mdd = AbstractQuantifier.QUANTIFY_MAX.combine(tempManager, yVars, mdd) ;
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
	 * and 0 otherwise. The variables used by this mdd are xVars and yVars to represent x
	 * and y respectively.
	 * @return
	 */
	public int getSynchronousTransitionFunction(){
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
		}
		//Function whose value is true on x,y iff (x,y) is a transition
		int f = MDDBaseOperators.AND.combine(tempManager, fi) ;

		return f ;
	}


	/**
	 * An MDD representing the transition function. The resulting function takes two 
	 * bit-string arguments x and y, and returns 1 if there is a transition from x to y, 
	 * and 0 otherwise. The variables xVars and yVars are used for x and y respectively
	 * @return
	 */
	public int getAsynchronousTransitionFunction(){
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
	public int getPredecessors(int f, int startStates){
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
	public int getBR(int f, int startStates) {
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

	/**
	 * Returns a bdd that represents the states that can be reached from one of 
	 * startStates in exactly one transition
	 * @param f
	 * @param startStates
	 * @return
	 */
	public int getSucessor(int f, int startStates){
		int set = startStates ;//SimpleOperator.substitute(tempManager, xVars, yVars, startStates) ;
		set = SimpleOperator.MIN.combine(tempManager, f, set) ;
		set = MDDQuantifier.QUANTIFY_MAX.combine(tempManager, xVars, set) ;
		set = SimpleOperator.substitute(tempManager, yVars, xVars, set) ;
		return set ;
	}

	/**
	 * Returns the set of states that can be reached from startStates in one or more transitions
	 * @param f : a bdd representing the transition function
	 * @param startStates : a bdd representing a set of initial states
	 * @return
	 */
	public int getFR(int f, int startStates){
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

	/**
	 * Returns an MDD that is one if and only if the x variables are 
	 * int the given state. Does not depend on the y variables.
	 * @param state
	 * @return
	 */
	public int getMddForState(byte[] state){
		byte[] completeState = new byte[3*varNum] ;
		System.arraycopy(state, 0, completeState, 0, state.length) ;

		int mdd = tempManager.nodeFromState(completeState, 1) ;
		mdd = MDDQuantifier.QUANTIFY_MAX.combine(tempManager, yVars, mdd) ;
		mdd = MDDQuantifier.QUANTIFY_MAX.combine(tempManager, zVars, mdd) ;
		return mdd ;
	}

	/**
	 * The the successor of a given state, for synchronous transitions
	 * @param state
	 * @return
	 */
	public byte[] getSynchronousSuccessor(byte[] state) {
		if(state.length != varNum)
			throw new IllegalArgumentException("Wrong length for the input state : " 
					+ state.length + " expected : " + varNum ) ;
		byte[] state2 = new byte[3*varNum] ;
		System.arraycopy(state, 0, state2, 0, varNum) ;
		byte[] res = new byte[varNum] ;
		for(int i = 0 ; i < varNum ; i++)
		{
			int temp = tempManager.reach(func[i], state2) ;
			if(temp > state[i])
				res[i] = (byte) (state[i] + 1) ;
			else 
			{
				if(temp < state[i])
					res[i] = (byte) (state[i] - 1) ;
				else 
					res[i] = state[i] ;
			}
		}
		return res ;
	}

	/**
	 * Returns a state inside the input set of states. 
	 * When the mdd is evaluated with this states, we obtain a non zero value
	 * if stateSet has no satisfying assignment, throws an illegalArgument exception.
	 * @param stateSet
	 * @return
	 */
	public byte[] getOneState(int stateSet){
		Iterator<byte[]> it = AssignmentsEnum.getAssignments(tempManager, stateSet, xVars) ;
		if(! it.hasNext())
			throw new IllegalArgumentException("No satisfying assignement for the input mdd") ;
		else
			return it.next() ;
	}

}
