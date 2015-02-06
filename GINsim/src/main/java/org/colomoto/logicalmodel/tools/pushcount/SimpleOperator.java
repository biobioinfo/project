package org.colomoto.logicalmodel.tools.pushcount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.NodeRelation;
import org.colomoto.mddlib.operators.AbstractOperator;

/**
 * Simple utility class for building operators on MDDs. To build a new operator, you must 
 * provide a function op that takes two integers, and returns an integer. If applied on 
 * MDDs a and b, the resulting MDD c will be such that for all assignments x,
 * c(x) = op(a(x), b(x)) ;
 * 
 *  In case the operator is associative, you can apply it on more than two inputs. However, 
 *  if it is not the case, then trying to do so will raise an error.
 * @author heinrich
 *
 */
public class SimpleOperator extends AbstractOperator {

	/**
	 * Returns a bdd that evaluates to 1 iff the two bdds as argument evaluates to the 
	 * same value
	 */
	public static MDDOperator EQUAL = new SimpleOperator(false,new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return a==b ? 1 : 0 ;
		}
	}) ;

	/**
	 * Returns a bdd whose value is the max of the two
	 */
	public static MDDOperator MAX = new SimpleOperator(true, new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return Math.max(a, b);
		}
	}) ;

	/**
	 * Produces a BDD which is the min of the two
	 */
	public static MDDOperator MIN = new SimpleOperator(true, new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return Math.min(a, b);
		}
	}) ;

	/**
	 * Returns a BDD that evaluates to 1 iff the first one evaluates to a value smaller 
	 * than 
	 */
	public static MDDOperator LESS_THAN = new SimpleOperator(false,new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return a < b ? 1 : 0;
		}
	}) ;

	public static MDDOperator GREATER_THAN = new SimpleOperator(false,new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return a > b ? 1 : 0;
		}
	}) ;

	public static MDDOperator ADD = new SimpleOperator(true,new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return a+b;
		}
	}) ;

	public static MDDOperator SUB = new SimpleOperator(false,new IntFunction() {

		@Override
		public int apply(int a, int b) {
			return a-b;
		}
	}) ;



	/**
	 * Returns a new MDD where the variables from the first list 
	 * were substituted with variables from the second.
	 * @param manager
	 * @param oldVars
	 * @param newVars
	 * @param mdd
	 * @return
	 */
	public static int substitute(MDDManager manager, MDDVariable[] oldVars, MDDVariable[] newVars, int mdd){
		if(oldVars.length != newVars.length)
			throw new RuntimeException("MDD : substitution failed, the two arrays must have the same length") ;
		List<VarSubs> subs = new ArrayList<VarSubs>(oldVars.length) ;
		for(int i = 0 ; i < oldVars.length ; i++)
		{
			if(oldVars[i].nbval != newVars[i].nbval)
				throw new RuntimeException("MDD : substitution failed, the variables '" + oldVars[i] +
						"' and '" + newVars[i] + "' do not have the same number of possible values") ;
		}

		for(int i = 0 ; i<oldVars.length ; i++ )
			subs.add(new VarSubs(oldVars[i], newVars[i])) ;
		Collections.sort(subs) ;
		return doSubstitute(manager, subs, mdd, 0) ;
	}

	public static class VarSubs implements Comparable<VarSubs>{
		final MDDVariable oldVar ;
		final MDDVariable newVar ;

		public VarSubs(MDDVariable oldVar, MDDVariable newVar){
			this.oldVar = oldVar ;
			this.newVar = newVar ;
		}

		@Override
		public int compareTo(VarSubs v) {
			if(oldVar.equals(v))
				return 0 ;
			if(oldVar.after(v.oldVar))
				return 1 ;
			else 
				return -1 ;
		}
	}

	private static int doSubstitute(MDDManager m, List<VarSubs> sub, 
			int mdd, int startIndex){
		if(sub.size() == startIndex)
			return mdd; 
		if(m.isleaf(mdd))
			return mdd ;
		if(sub.get(startIndex).oldVar.equals(m.getNodeVariable(mdd)))
		{
			int[] children = m.getChildren(mdd) ;
			int[] newChildren = new int[children.length] ;
			for(int i = 0 ; i < children.length ; i++)
			{
				newChildren[i] = doSubstitute(m, sub, children[i], startIndex +1) ;
			}
			return sub.get(startIndex).newVar.getNodeFree(newChildren) ;
		}
		else
		{
			if(!sub.get(startIndex).oldVar.after(m.getNodeVariable(mdd)))
				return doSubstitute(m, sub, mdd, startIndex+1) ;
			else
			{
				int[] children = m.getChildren(mdd) ;
				int[] newChildren = new int[children.length] ;
				for(int i = 0 ; i < children.length ; i++)
				{
					newChildren[i] = doSubstitute(m, sub, children[i], startIndex) ;
				}
				return m.getNodeVariable(mdd).getNode(newChildren) ;
			}
		}
	}


	/**
	 * Interface for functions that take two integers as argument, and return a new one.
	 * @author heinrich
	 *
	 */
	public static interface IntFunction {
		public int apply(int a, int b) ;
	}

	private IntFunction f ;
	private final boolean assoc ;

	public SimpleOperator(boolean assoc, IntFunction f){
		super(true) ;
		this.f = f ;
		this.assoc = assoc ;
	}

	@Override
	public int combine(MDDManager m, int first, int second) {
		NodeRelation rel = m.getRelation(first, second) ;
		switch(rel)
		{
		case LL :
			int res = f.apply(first, second) ;
			res = Math.min(res, m.getLeafCount() -1) ;
			res = Math.max(res, 0) ;
			return res ;
		default :
			return recurse(m, rel, first, second) ;
		}
	}

	@Override
	public int combine(MDDManager m, int[] bdds) {
		if(assoc)
			return super.combine(m, bdds) ;
		else
			throw new UnsupportedOperationException("Cannot apply non associative operator " +
					"to more than two arguments") ;
	}
}

