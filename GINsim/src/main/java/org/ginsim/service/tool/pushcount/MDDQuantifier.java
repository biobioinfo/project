package org.ginsim.service.tool.pushcount;

import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;


/**
 * Interface for quantifiers over a variable
 * @author heinrich
 *
 */
public interface MDDQuantifier {
	
	public static MDDQuantifier QUANTIFY_MAX = new AbstractQuantifier(SimpleOperator.MAX) ;

	public static MDDQuantifier QUANTIFY_MIN = new AbstractQuantifier(SimpleOperator.MIN) ;

	/**
	 * Quantifies over the variable var
	 * @param m
	 * @param var
	 * @param mdd
	 * @return the new MDD where the variable var was quantified out.
	 */
	public int combine(MDDManager m, MDDVariable var, int mdd) ;

	/**
	 * Quatifies over all the given variables
	 * @param m
	 * @param var
	 * @param mdd
	 * @return a new MDD where the variables vars where quantified out.
	 */
	public int combine(MDDManager m, MDDVariable[] vars, int mdd) ;

	
}

