package org.ginsim.service.tool.pushcount;


import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.html.ListView;

import org.colomoto.common.task.AbstractTask;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.omg.CORBA.NVList;

public class PushCountSearcher extends AbstractTask<Object> {
	
	private LogicalModel model ;
	
	//Temporary manager with more variables
	private MDDManager tempManager ;
	//Number of variables in the initial manager
	private int varNum ;
	
	//List of the new variables
	private List<MDDVariable> varList ;
	
	//Copy of the functions into the tempManager
	int[] tempFunc ;

	public PushCountSearcher(LogicalModel model) throws ParseException {
		this.model = model ;
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
		
		int[] func = model.getLogicalFunctions() ;
		for(int i = 0 ; i<func.length ; i++)
		{
			tempFunc[i] = tempManager.parseDump(manager.dumpMDD(func[i])) ;
		}
		
	}

	@Override
	protected Object doGetResult() throws Exception {
		
		
		return null ;
	}
	
	/**
	 * An MDD representing the transition function. The resulting function takes two 
	 * bit-string arguments x and y, and returns 1 if there is a transition from x to y, 
	 * and 0 otherwise
	 * @return
	 */
	int getSynchronousTransitionFunction(){
		int[] fi = new int[tempFunc.length] ;
		
		for(int i = 0 ; i < fi.length ; i ++)
		{
			MDDVariable var = varList.get(i) ;
			fi[i] = NewOps.EQUAL.combine(tempManager, tempFunc[i], getVariableValue(var)) ;
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
		//fi = AND_{j \neq i} (x_j = y_j) AND (y_i = f_i(x_i))
		
		int[] fi = new int[tempFunc.length] ;
		for(int i = 0 ; i < fi.length ; i ++)
		{
			int[] fij = new int[varNum] ;
			for(int j = 0 ; j < varNum ; i ++)
			{
				if(j != i)
					fij[j] = NewOps.EQUAL.combine(tempManager, 
							getVariableValue(varList.get(j)), 
							getVariableValue(varList.get(varNum + j))) ;
				else
					fij[j] = NewOps.EQUAL.combine(tempManager, 
							getVariableValue(varList.get(varNum + i)), tempFunc[i]) ;
			}
			fi[i] = NewOps.MIN.combine(tempManager, fij) ;
		}
		
		return NewOps.MAX.combine(tempManager, fi) ;
	}
	
	/**
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
	 * Returns an MDD that represents the set of states that can reach one of 
	 * the states described by startStates
	 * @param f : the transition function
	 * @param startStates : the set of initial states
	 * @return
	 */
	int getPredecessors(int f, int startStates){
		int oldSet = 0 ;
		int newSet = startStates ;
		while(oldSet != newSet)
		{
			oldSet = newSet ;
			//newSet = NewOps.QUANTIFY_MIN.
		}
			
		return 0 ;
	}

}
