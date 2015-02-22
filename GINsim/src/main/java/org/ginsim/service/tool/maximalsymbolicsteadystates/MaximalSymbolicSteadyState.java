package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Arrays;

import org.ojalgo.optimisation.Optimisation;


public class MaximalSymbolicSteadyState {
    
    // Set of arc not considered for the maximal symbolic state
    public List<Integer> arcSet = null;
    
    public int[] symbolicState = null;
    public int[] inducedPartialState = null;
    
    public MaximalSymbolicSteadyState(List<Implicant> primeImplicants, Optimisation.Result result) throws Exception {
	// Declare fields
	arcSet = new ArrayList<Integer>();
	Implicant implicant = primeImplicants.get(0);
	int numberOfGenes = implicant.p.length;
	this.symbolicState = new int[numberOfGenes];
	this.inducedPartialState = new int[numberOfGenes];
	for (int i = 0; i < numberOfGenes; i++) {
	    this.symbolicState[i] = -1;
	    this.symbolicState[i] = -1;
	}
	// Initialize fields
	for (int a = 0; a < primeImplicants.size(); a++) {
	    long index = (long) a;
	    BigDecimal value = result.get(index);
	    if (1 == value.intValue()) {
		implicant = primeImplicants.get(a);
		// Initialize partially the symbolic state
		for (int i = 0; i < numberOfGenes; i++) {
		    switch (implicant.p[i]) {
		    case 0:
			this.symbolicState[i] = 0;
			break;
		    case 1:
			this.symbolicState[i] = 1;
			break;
		    case -1:
			break;
		    default:
			throw new Exception("Illegal partial state (illegal value)");
		    }
		}
		// Initialize partially the induced partial state
		this.inducedPartialState[implicant.v] = implicant.c;
	    } else {
		// Initialize partially the arc set
		this.arcSet.add(a);
	    }
	}
    }
    
    public String toString() {
	String string =
	    Arrays.toString(this.symbolicState)
	    + " ( -> "
	    + Arrays.toString(this.inducedPartialState)
	    + ")";
	return string;
    }
    
}
