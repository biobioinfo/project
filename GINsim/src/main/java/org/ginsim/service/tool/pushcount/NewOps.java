package org.ginsim.service.tool.pushcount;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.misc.Pair;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.MDDVariable;
import org.colomoto.mddlib.NodeRelation;
import org.colomoto.mddlib.operators.AbstractOperator;

public class NewOps {

	public static MDDOperator EQUAL = new EqualOperator() ;

	public static MDDOperator MAX = new MaxOperator() ;

	public static MDDOperator MIN = new MinOperator() ;

	public static MDDQuantifier QUANTIFY_MAX = new MaxQuantifier() ;
	
	public static MDDQuantifier QUANTIFY_MIN = new MinQuantifier() ;
	
	
	/**
	 * Returns a new MDD which represents exactly the same function except that 
	 * all variables that are the first elements of the pair are substituted
	 * @param manager
	 * @param substitution
	 * @param mdd
	 * @return
	 */
	public static int substitute(MDDManager manager, List<Pair<MDDVariable,MDDVariable>> substitution,  int mdd){
		
		for(int i = 0 ; i < substitution.size() ; i++)
		{
			if(substitution.get(i).a.nbval != substitution.get(i).b.nbval)
				throw new RuntimeException("MDD : substitution failed, the variables '" + substitution.get(i).a +
						"' and '" + substitution.get(i).b + "' do not have the same number of possible values") ;
		}
		
		Comparator<Pair<MDDVariable, MDDVariable>> comp = new Comparator<Pair<MDDVariable,MDDVariable>>() {

			@Override
			public int compare(Pair<MDDVariable, MDDVariable> p1,
					Pair<MDDVariable, MDDVariable> p2) {
				if(p1.a.after(p2.a))
					return 1;
				else 
					return -1 ;
			}
		};
		Collections.sort(substitution, comp) ;
		return doSubstitute(manager, substitution, mdd, 0) ;
	}


	
	private static int doSubstitute(MDDManager m, List<Pair<MDDVariable, MDDVariable>> sub, 
			int mdd, int startIndex){
		if(sub.size() == startIndex)
			return mdd; 
		if(sub.get(startIndex).a.equals(m.getNodeVariable(mdd)))
		{
			int[] children = m.getChildren(mdd) ;
			int[] newChildren = new int[children.length] ;
			for(int i = 0 ; i < children.length ; i++)
				newChildren[i] = doSubstitute(m, sub, children[i], startIndex +1) ;
			return sub.get(startIndex).b.getNodeFree(newChildren) ;
		}
		else
		{
			if(!sub.get(startIndex).a.after(m.getNodeVariable(mdd)))
				doSubstitute(m, sub, mdd, startIndex+1) ;
			else
			{
				int[] children = m.getChildren(mdd) ;
				int[] newChildren = new int[children.length] ;
				for(int i = 0 ; i < children.length ; i++)
					newChildren[i] = doSubstitute(m, sub, children[i], startIndex) ;
				return m.getNodeVariable(mdd).getNode(newChildren) ;
			}
		}
		return 0 ;
	}



	/**
	 * This operator returns a BDD that evaluates to 1 if and only if the two MDDs evaluate to the same value
	 * @author heinrich
	 *
	 */
	public static class EqualOperator extends AbstractOperator{

		public EqualOperator(){
			super(true) ;
		}

		@Override
		public int combine(MDDManager m, int first, int second) {
			if(first == second)
				return 1 ;
			NodeRelation rel = m.getRelation(first, second) ;
			switch(rel)
			{
			case LL :
				return 0 ;
			default :
				return recurse(m, rel, first, second) ;
			}
		}
		
		@Override
		public int combine(MDDManager m, int[] args){
			int[] results = new int[args.length -1] ; 
			for(int i = 1 ; i < args.length ; i ++)
			{
				results[i-1] = combine(m, args[0], args[i]) ;
			}
			return MIN.combine(m, results) ;
		}
	}


	/**
	 * Max operator on two MDDs. If the two are BDDs, then the result is the same as OR.
	 * @author heinrich
	 *
	 */
	public static class MaxOperator extends AbstractOperator {

		@Override
		public int combine(MDDManager m, int first, int second) {
			if(first == second)
				return first ;
			if(m.getRelation(first, second) == NodeRelation.LL)
			{
				return Math.max(first, second) ;
			}
			else 
			{
				return recurse(m, m.getRelation(first, second), first, second) ;
			}
		}

	}


	/**
	 * Min operator on two MDDs. If the two are BDDs, then the result is the same as AND.
	 * @author heinrich
	 *
	 */
	public static class MinOperator extends AbstractOperator {

		@Override
		public int combine(MDDManager m, int first, int second) {
			if(first == second)
				return first ;
			if(m.getRelation(first, second) == NodeRelation.LL)
			{
				return Math.min(first, second) ;
			}
			else 
			{
				return recurse(m, m.getRelation(first, second), first, second) ;
			}
		}

	}

	public static class MaxQuantifier implements MDDQuantifier  {

		@Override
		public int combine(MDDManager m, MDDVariable var, int mdd) {
			if(m.isleaf(mdd))
				return mdd ;
			MDDVariable headVariable = m.getNodeVariable(mdd) ;
			if(headVariable.equals(var))
			{
				return NewOps.MAX.combine(m, m.getChildren(mdd)) ;
			}
			else 
			{
				int[] children = m.getChildren(mdd) ;
				int[] newChildren = new int[children.length] ;
				for(int i = 0 ; i < children.length ; i ++)
				{
					newChildren[i] = combine(m,var, children[i]) ;
				}
				return var.getNode(newChildren) ;
			}
		}
		
		@Override
		public int combine(MDDManager m , MDDVariable[] vars, int mdd){
			return 0 ;
		}

	}

	public static class MinQuantifier implements MDDQuantifier  {

		@Override
		public int combine(MDDManager m, MDDVariable var, int mdd) {
			if(m.isleaf(mdd))
				return mdd ;
			MDDVariable headVariable = m.getNodeVariable(mdd) ;
			if(headVariable.equals(var))
			{
				return NewOps.MIN.combine(m, m.getChildren(mdd)) ;
			}
			else 
			{
				int[] children = m.getChildren(mdd) ;
				int[] newChildren = new int[children.length] ;
				for(int i = 0 ; i < children.length ; i ++)
				{
					newChildren[i] = combine(m,var, children[i]) ;
				}
				return var.getNode(newChildren) ;
			}
		}
		
		@Override
		public int combine(MDDManager m , MDDVariable[] vars, int mdd){
			return 0 ;
		}


	}

}
