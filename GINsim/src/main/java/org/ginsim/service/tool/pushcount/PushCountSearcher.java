package org.ginsim.service.tool.pushcount;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.colomoto.common.task.AbstractTask;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.MDDVariableFactory;
import org.colomoto.mddlib.operators.MDDBaseOperators;

public class PushCountSearcher extends AbstractTask<Object> {
	
	private LogicalModel model ;
	
	//Temporary manager with more variables
	private MDDManager tempManager ;
	//Number of variables in the initial manager
	private int varNum ;
	
	//List of the new variables
	private List<MDDVariable> varList = new ArrayList<MDDVariable>() ;
	
	//Copy of the functions into the tempManager
	int[] tempFunc ;
	
	//BDD representing the source states
	int tempSource ;
	
	//BDD representing the target states
	int tempTarget ;

	public PushCountSearcher(LogicalModel model, int source, int target) throws ParseException {
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
		tempFunc = new int[func.length] ;
		for(int i = 0 ; i<func.length ; i++)
			tempFunc[i] = tempManager.parseDump(manager.dumpMDD(func[i])) ;
		
		tempSource = tempManager.parseDump(manager.dumpMDD(source)) ;
		tempTarget = tempManager.parseDump(manager.dumpMDD(target)) ;
		
	}

	@Override
	public Object doGetResult() throws Exception {
		System.out.println("Running") ;
		int async = getAsynchronousTransitionFunction() ;
		System.out.println("Computed f_async") ;
		int bdd = getPredecessors(async, tempTarget) ;
		System.out.println("Computed predecessors") ;
		bdd = model.getMDDManager().parseDump(tempManager.dumpMDD(bdd)) ;
		
		System.out.println(model.getMDDManager().dumpMDD(bdd)) ;
		for(int i = 0 ; i < varNum ; i++)
			System.out.println(i + " : " + varList.get(i) ) ;
		
		int iter = 0 ;
		Iterator<byte[]> it = AssignmentsEnum.getAssignments(model.getMDDManager(), bdd) ;
		while(it.hasNext())
		{
			byte[] r = it.next() ;
			System.out.print("res: ") ;
			for(int i = 0 ; i < r.length ; i++)
				System.out.print(r[i]) ;
			System.out.println() ;
			iter ++ ;
			if(iter > 100)
				return null; 
		}
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
		MDDVariable[] xVars = varList.subList(0, varNum).toArray(new MDDVariable[0]) ;
		MDDVariable[] yVars = varList.subList(varNum, 2*varNum).toArray(new MDDVariable[0]) ;
		
		
		for(int i = 0 ; i < fi.length ; i ++)
		{
			int xi = getVariableValue(xVars[i]) ;
			int yi = getVariableValue(yVars[i]) ;
			int i1 = MDDBaseOperators.AND.combine(tempManager, 
					SimpleOperator.EQUAL.combine(tempManager, tempFunc[i], xi),
					SimpleOperator.EQUAL.combine(tempManager, xi, yi));
			
			int i2 = MDDBaseOperators.AND.combine(tempManager, 
					SimpleOperator.GREATER_THAN.combine(tempManager, tempFunc[i], xi),
					SimpleOperator.EQUAL.combine(tempManager, 
							SimpleOperator.ADD.combine(tempManager, xi, 1), yi)) ;
			int i3 = MDDBaseOperators.AND.combine(tempManager, 
					SimpleOperator.LESS_THAN.combine(tempManager, tempFunc[i], xi),
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
		
		int[] fi = new int[tempFunc.length] ;
		for(int i = 0 ; i < fi.length ; i ++)
		{
			int[] fij = new int[varNum] ;
			for(int j = 0 ; j < varNum ; j ++)
			{
				if(j != i)
					fij[j] = SimpleOperator.EQUAL.combine(tempManager, 
							getVariableValue(varList.get(j)), 
							getVariableValue(varList.get(varNum + j))) ;
				else
				{
					int xi = getVariableValue(varList.get(i)) ;
					int yi = getVariableValue(varList.get(i + varNum)) ;
					
					int i1 = MDDBaseOperators.AND.combine(tempManager,
							SimpleOperator.EQUAL.combine(tempManager, xi, yi),
							SimpleOperator.EQUAL.combine(tempManager, xi, tempFunc[i])) ;
					int i2 = MDDBaseOperators.AND.combine(tempManager,
							SimpleOperator.GREATER_THAN.combine(tempManager, tempFunc[i], xi),
							SimpleOperator.EQUAL.combine(tempManager, 
									SimpleOperator.ADD.combine(tempManager, xi, 1),
									yi)) ;
					int i3 = MDDBaseOperators.AND.combine(tempManager,
							SimpleOperator.LESS_THAN.combine(tempManager, tempFunc[i], xi),
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
	 * the states described by startStates in 0, 1, or more transitions
	 * @param f : the transition function
	 * @param startStates : the set of initial states
	 * @return
	 */
	int getPredecessors(int f, int startStates){
		int oldSet = 0 ;
		int newSet = startStates ;
		MDDVariable[] xVars = varList.subList(0, varNum).toArray(new MDDVariable[0]) ;
		MDDVariable[] yVars = varList.subList(varNum, 2*varNum).toArray(new MDDVariable[0]) ;
		while(oldSet != newSet)
		{
			oldSet = newSet ;
			newSet = SimpleOperator.substitute(tempManager, xVars, yVars, newSet) ;
			newSet = SimpleOperator.MIN.combine(tempManager, f, newSet) ;
			newSet = MDDQuantifier.QUANTIFY_MAX.combine(tempManager, yVars, newSet) ;
			newSet = MDDBaseOperators.OR.combine(tempManager, newSet, oldSet) ;
		}
			
		return newSet ;
	}

}
