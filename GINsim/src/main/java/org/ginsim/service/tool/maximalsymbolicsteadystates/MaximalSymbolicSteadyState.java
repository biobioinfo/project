package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.math.BigDecimal;

import org.ojalgo.optimisation.Optimisation;


public class MaximalSymbolicSteadyState {
    
    public int[] p;
    public int[] inducedPartialState;
    
    public MaximalSymbolicSteadyState(List<Implicant> primeImplicants, Optimisation.Result result) {
	for (int a = 0; a < primeImplicants.size(); a++) {
	    long index = (long) a;
	    BigDecimal value = result.get(index);
	    BigDecimal one = new BigDecimal(1.0);
	    if (one == value) {
		Implicant implicant = primeImplicants.get(a);
		// TODO: integrate implicant to maximal symbolic steady state
	    } else {
		// Don't care
	    }
	}
    }
    
}
