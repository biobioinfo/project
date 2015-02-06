package org.colomoto.logicalmodel.tools.pushcount;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;


/**
 * Used to enumerates all the assignments that leads to 
 * a certain value for a given MDD
 * @author heinrich
 *
 */
public class AssignmentsEnum implements Iterator<byte[]>{
	
	
	private final MDDManager m ;
	private final boolean[] selector ;
	private final MDDVariable[] vars ;
	private final Deque<Integer> mddList = new LinkedList<Integer>() ;
	private byte[] nextAssignment = null ;
	private byte[] nextReturned = null ;
	private boolean hasNext = false;
	
	//Number of variables in the varList argument, that should be taken into
	//account when enumerating the possible states
	private final int listVarNb ;

	
	/**
	 * Returns an iterator over all the variable assignments that produces a 
	 * non zero value
	 * @param m
	 * @param m
	 * @return
	 */
	public static Iterator<byte[]> getAssignments(MDDManager m, int mdd) {
		boolean[] selector = new boolean[m.getLeafCount()] ;
		selector[0] = false ;
		for(int i = 1 ; i < m.getLeafCount() ; i ++)
			selector[i] = true ;
		return getAssignments(m, mdd, selector) ;
	}
	
	/**
	 * Returns an iterator over all the variable assignments that produces a 
	 * non zero value. Only consider the value of the variables 
	 * @param m
	 * @param m
	 * @return
	 */
	public static Iterator<byte[]> getAssignments(MDDManager m, int mdd, MDDVariable[] varList) {
		boolean[] selector = new boolean[m.getLeafCount()] ;
		selector[0] = false ;
		for(int i = 1 ; i < m.getLeafCount() ; i ++)
			selector[i] = true ;
		return getAssignments(m, mdd, selector, varList) ;
	}
	
	

	/**
	 * Returns an iterator over all the variable assignments that produces the given value 
	 * @param m
	 * @param mdd
	 * @param value
	 * @return
	 */
	public static Iterator<byte[]> getAssignments(MDDManager m, int mdd, int value) {
		boolean[] selector = new boolean[m.getLeafCount()] ;
		for(int i = 0 ; i < m.getLeafCount() ; i ++)
			selector[i] = false ;
		selector[value] = true ;
		return getAssignments(m, mdd, selector) ;
	}

	/**
	 * Returns all the assignments x of the BDD, that evaluates to a value i such that 
	 * selector[i] = true
	 * @param m
	 * @param mdd
	 * @param selector
	 * @return
	 */
	public static Iterator<byte[]> getAssignments(final MDDManager m, final int mdd, final boolean[] selector) {
		return getAssignments(m, mdd, selector, m.getAllVariables()) ;
	}
	
	public static Iterator<byte[]> getAssignments(final MDDManager m, final int mdd, 
			final boolean[] selector, final MDDVariable[] varList) {
		return new AssignmentsEnum(m, mdd, selector, varList) ;
	}
	
	protected AssignmentsEnum(MDDManager m, int mdd, boolean[] selector, MDDVariable[] varList) {
		listVarNb = varList.length ;
		this.m = m ;
		m.use(mdd) ;
		this.selector = selector ;
		mddList.addLast(mdd) ; 
		vars = m.getAllVariables() ;
		
		byte[] presentVariables = new byte[vars.length] ;
		for(MDDVariable var : varList)
			presentVariables[var.order] = 1 ;
		
		nextAssignment = new byte[m.getAllVariables().length] ;
		for(int i = 0 ; i < nextAssignment.length ; i++)
		{
			if(presentVariables[i] == 1)
				nextAssignment[i] = -1 ;
			else 
				nextAssignment[i] = -2 ;
		}
		
		init() ;
	}
	
	private void init() {

		
		while(!m.isleaf(mddList.getLast()))
		{
			MDDVariable var = m.getNodeVariable(mddList.getLast()) ;
			nextAssignment[var.order] = 0 ;
			int child = m.getChild(mddList.getLast(), 0) ;
			m.use(child) ;
			mddList.add(child) ;
		}
		while(!mddList.isEmpty() && !selector[mddList.getLast()])
			nextLeaf() ;

		if(! mddList.isEmpty())
			hasNext = true ;
		else 
		{
			hasNext = false ;
			return ;
		}
		
		nextReturned = nextAssignment.clone() ;
		for(int i = 0 ; i < nextReturned.length ; i++)
			if(nextReturned[i] == -1)
				nextReturned[i] = 0 ;
		return ; 
	}

	private void nextLeaf() {
		assert(!mddList.isEmpty()) ;
		//No free, it is a leaf
		mddList.removeLast() ;
		if(mddList.isEmpty())
			return ; 
		
		MDDVariable var = m.getNodeVariable(mddList.getLast()) ;
		while(!mddList.isEmpty() && var.nbval == nextAssignment[var.order] +1)
		{
			nextAssignment[var.order] = -1 ;
			m.free(mddList.removeLast()) ;
			
			if(!mddList.isEmpty())
				var = m.getNodeVariable(mddList.getLast()) ;
		}
		if(mddList.isEmpty())
			return ;
		
		nextAssignment[var.order] += 1 ;
		int child = m.getChild(mddList.getLast(), nextAssignment[var.order]) ;
		m.use(child) ;
		mddList.add(child) ;
		var = m.getNodeVariable(mddList.getLast()) ;
		while(! m.isleaf(mddList.getLast()))
		{
			nextAssignment[var.order] = 0 ;
			child = m.getChild(mddList.getLast(), 0) ;
			m.use(child) ;
			mddList.addLast(child) ;
			var = m.getNodeVariable(mddList.getLast()) ;
		}
	}

	
	
	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public byte[] next() {
 		if(!hasNext)
			throw new NoSuchElementException() ;
 		
		byte[] res = nextReturned.clone() ;
		if(listVarNb != vars.length)
		{
			//Remove the variables that are not taken into account
			byte[] temp = new byte[listVarNb] ;
			int j = 0 ;
			for(int i = 0 ; i< res.length ; i++)
			{
				if(res[i] != -2)
				{
					temp[j] = res[i] ;
					j++ ;
				}
			}
			res = temp ;
		}
		
		
		int j = nextReturned.length - 1 ;

		while(j >= 0 && (nextAssignment[j] != -1 
				|| nextReturned[j] == vars[j].nbval -1))
			j -- ;
		if(j < 0)
		{
			nextLeaf() ;
			while(!mddList.isEmpty() && !selector[mddList.getLast()])
				nextLeaf() ;
			
			if(mddList.isEmpty())
			{
				hasNext = false ;
				return res ;
			}
			
			nextReturned = nextAssignment.clone() ;
			for(int i = 0 ; i < nextReturned.length ; i++)
				if(nextReturned[i] == -1)
					nextReturned[i] = 0 ;
			
		}
		else
		{
			nextReturned[j] += 1 ;
			for(int i = j+1 ; i < nextReturned.length ; i++)
				if(nextAssignment[i] == -1)
					nextReturned[i] = 0 ;	
		}
		return res;		
	}


	
	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Removal from BDD iterators is not allowed") ;

	}
}
