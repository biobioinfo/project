package org.colomoto.logicalmodel.tools.pushcount;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDOperator;
import org.colomoto.mddlib.MDDVariable;

public class AbstractQuantifier implements MDDQuantifier {
	
	private final MDDOperator op ; 

	public AbstractQuantifier(MDDOperator op){
		this.op = op ;
	}

	@Override
	public int combine(MDDManager m, MDDVariable var, int mdd) {
		MDDVariable[] vars = new MDDVariable[1] ;
		vars[0] = var ;
		return combine(m, vars, mdd) ;
	}

	@Override
	public int combine(MDDManager m , MDDVariable[] vars, int mdd){
		List<MDDVariable> varList = new LinkedList<MDDVariable>() ;
		for(int i = 0 ; i < vars.length ; i ++)
			varList.add(vars[i]) ;
		Comparator<MDDVariable> comp = new Comparator<MDDVariable>() {

			@Override
			public int compare(MDDVariable v1, MDDVariable v2) {
				if(v1.after(v2))
					return 1 ;
				else
				{
					if(v1.equals(v2))
						return 0 ;
					else
						return -1 ;
				}
			}
		};
		Collections.sort(varList, comp) ;
		return doCombine(m, varList, mdd, 0) ;
	}

	int doCombine(MDDManager m, List<MDDVariable> vars, int mdd, int startIndex) {
		if(m.isleaf(mdd))
			return mdd ;
		
		if(startIndex == vars.size())
			return mdd ;
		
		if(vars.get(startIndex).equals(m.getNodeVariable(mdd)))
		{
			int[] children = m.getChildren(mdd) ;
			for(int i = 0 ; i < children.length ; i++)
				m.use(children[i]) ;
			
			int[] newChildren = new int[children.length] ;
			
			for(int i = 0 ; i < children.length ; i++)
			{
				newChildren[i] = doCombine(m, vars, children[i], startIndex+1) ;
				m.free(children[i]) ;
			}
			return op.combine(m,newChildren) ;
		}
		if(vars.get(startIndex).after(m.getNodeVariable(mdd)))
		{
			int[] children = m.getChildren(mdd) ;
			for(int i = 0 ; i < children.length ; i++)
				m.use(children[i]) ;
			
			int[] newChildren = new int[children.length] ;
			for(int i = 0 ; i < children.length ; i++)
			{
				newChildren[i] = doCombine(m, vars, children[i], startIndex) ;
				m.free(children[i]) ;
			}
			return m.getNodeVariable(mdd).getNodeFree(newChildren) ;
		}
		return doCombine(m, vars, mdd, startIndex+1);
	}
}
