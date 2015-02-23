package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.util.ListIterator;



/**
 * Wrapper class to hold the results of the tool which find the maximal symbolic
 * steady states.
 *
 * @author Baptiste Lefebvre
 */
public class Result {
    
    private List<Implicant> primeImplicants;
    private List<MaximalSymbolicSteadyState> maximalSymbolicSteadyStates;
    
    public Result(List<Implicant> primeImplicants, List<MaximalSymbolicSteadyState> maximalSymbolicSteadyStates) {
	this.primeImplicants = primeImplicants;
	this.maximalSymbolicSteadyStates = maximalSymbolicSteadyStates;
    }
    
    public String toString() {
	String string = "";
	
	string = string + "Prime implicants:\n";
	string = string + "[ <index>:(<partial state>, <value>, <variable>) ]\n";
	ListIterator<Implicant> i1 = this.primeImplicants.listIterator();
	while (i1.hasNext()) {
	    Implicant primeImplicant = i1.next();
	    string = string + primeImplicant.toString() + "\n";
	}
	
	string = string + "\n";
	
	string = string + "Maximal symbolic steady states:\n";
	string = string + "[ <partial state> ( -> <induced partial state>) ]\n";
	ListIterator<MaximalSymbolicSteadyState> i2 = this.maximalSymbolicSteadyStates.listIterator();
	while (i2.hasNext()) {
	    MaximalSymbolicSteadyState maximalSymbolicSteadyState = i2.next();
	    string = string + maximalSymbolicSteadyState.toString() + "\n";
	}
	
	return string;
    }
    
}
