package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.HashSet;

import org.mangosdk.spi.ProviderFor;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.sf.javailp.Solver;


@ProviderFor(Service.class)
@Alias("maximalsymbolicsteadystates")
@ServiceStatus(EStatus.RELEASED)
public class MaximalSymbolicSteadyStatesService implements Service {
    
    private RegulatoryGraph regulatoryGraph = null;
    private LogicalModel logicalModel = null;
    private List<Implicant> mddImplicants = null;
    private List<Implicant> primeImplicants = null;
    
    public void setRegulatoryGraph(RegulatoryGraph regulatoryGraph) {
	this.regulatoryGraph = regulatoryGraph;
	this.logicalModel = regulatoryGraph.getModel();
	return;
    }
    
    // public void test() {
    // 	MDDManager mddManager = logicalModel.getMDDManager();
    // 	int[] logicalFunctions = logicalModel.getLogicalFunctions();
	
    // 	// Print MDD manager ///////////////////////////////////////////////////
    // 	System.out.println("");
    // 	System.out.println("MDDManager");
    // 	int node = 0;
    // 	String representation = mddManager.dumpMDD(node);
    // 	System.out.println(representation);
	
    // 	// Print logical functions /////////////////////////////////////////////
    // 	System.out.println("LogicalFunctions");
    // 	for (int i = 0; i < logicalFunctions.length; i++) {
    // 	    node = logicalFunctions[i];
    // 	    //System.out.println(node);
    // 	    representation = mddManager.dumpMDD(node);
    // 	    System.out.println(representation);
    // 	}
	
    // 	// Print MDD variables /////////////////////////////////////////////////
    // 	System.out.println("MDDVariables");
    // 	MDDVariable[] mddVariables = mddManager.getAllVariables();
    // 	for (int i = 0; i < mddVariables.length; i++) {
    // 	    System.out.println(mddVariables[i].toString());
    // 	    //System.out.println(mddManager.getVariableIndex(mddVariables[i]));
    // 	}
    // }
    
    public void run() {	
	// Enumerate and print MDD implicants
	enumerateMDDImplicants();
	System.out.println("");
	System.out.println("MDD implicants:");
	System.out.println(this.mddImplicants);
	
	// Enumerate and print prime implicants
	enumeratePrimeImplicants();
	System.out.println("");
	System.out.println("Prime implicants:");
	System.out.println(this.primeImplicants);
	
	// Solve the Integer Linear Programming
	solveILP();
	
	return;
    }

    
    // Enumeration of MDD implicants ///////////////////////////////////////////
    
    private int numberOfGenes() {
	return this.logicalModel.getLogicalFunctions().length;
    }
    
    private void enumerateMDDImplicants() {
	this.mddImplicants = new ArrayList();
	for (int i = 0; i < numberOfGenes(); i++) {
	    enumerateMDDImplicantsOfLogicalFunction(i);
	}
	return;
    }
    
    private int[] emptyPartialState() {
	int n = numberOfGenes();
	int[] p = new int[n];
	for (int i = 0; i < n; i ++) {
	    p[i] = -1;
	}
	return p;
    }
    
    private void enumerateMDDImplicantsOfLogicalFunction(int i) {
	int[] p = emptyPartialState();
	int c = this.logicalModel.getLogicalFunctions()[i];
	enumerateMDDImplicantsOfMDD(p, c, i);
	return;
    }
    
    private void enumerateMDDImplicantsOfMDD(int[] p, int c, int i) {
	MDDManager mddManager = this.logicalModel.getMDDManager();
	if (mddManager.isleaf(c)) {
	    Implicant implicant = new Implicant(p, c, i);
	    this.mddImplicants.add(implicant);
	} else {
	    MDDVariable mddVariable = mddManager.getNodeVariable(c);
	    for (int v = 0; v < mddVariable.nbval; v ++) {
		int[] q = p.clone();
		q[mddVariable.order] = v;
		int d = mddManager.getChild(c, v);
		enumerateMDDImplicantsOfMDD(q, d, i);
	    }
	}
    }
    
    
    // Enumeration of prime implicants /////////////////////////////////////////
    
    /**
     * Key (c, v) for implicant (p, c, v), used to partition the MDD implicants
     */
    private class Key {
	private final int a;
	private final int b;
	
	public Key(int a, int b) {
	    this.a = a;
	    this.b = b;
	}
	
	@Override
	public boolean equals (Object o) {
	    if (!(o instanceof Key)) {
		return false;
	    } else {
		Key key = (Key) o;
		return ((this.b == key.b) && (this.b == key.b));
	    }
	}
	
	@Override
	public int hashCode() {
	    return (this.a * 31 + this.b);
	}	
    }
    
    /**
     * Partition MDD implicants (p, c, v) according to (c, v)
     *
     * @return the collection of lists of implicants with identical (c, v)
     */
    private Collection<List> partitionMDDImplicants() {
	HashMap<Key, List> partition = new HashMap<Key, List>();
	for (Implicant implicant : this.mddImplicants) {
	    Key key = new Key(implicant.c, implicant.v);
	    if (partition.containsKey(key)) {
		// Key (c, v) already present in the hashtable
		List part = partition.get(key);
		part.add(implicant);
	    } else {
		// Key (c, v) not present in the hashtable
		List part = new ArrayList();
		part.add(implicant);
		partition.put(key, part);
	    }
	}
	return partition.values();
    }
    
    /**
     * Combine two partial state if possible or throw an exception
     *
     * For examples:
     *              01?1?10   0   1   0   1   0   1   ?
     *          and 00???1?   0   1   1   0   ?   ?   ?
     * combine into 0??1?10   0   1   ?   ?   0   1   ?
     *
     * @param first partial state
     * @param second partial state
     * @return partial state
     */
    private int[] combine(int[] p, int[] q) throws Exception {
	int[] r = p.clone();
	boolean flag = false;
	for (int k = 0; k < p.length; k++) {
	    if (p[k] == -1 && q[k] != -1) {
		throw new Exception("Impossible combination");
	    } else if ((p[k] == 0 && q[k] == 1) || (p[k] == 1 && q[k] == 0)) {
		if (false == flag) {
		    r[k] = -1;
		    flag = true;
		} else {
		    throw new Exception("Impossible combination");
		}
	    } else {
		// Don't care
	    }
	}
	return r;
    }
    
    /**
     * Compute the prime implicants associated with a part of MDD implicants
     *
     * @param List of MDD implicants (p, c, v) with identical c and v
     * @return List of prime implicants (p, c, v) with identical c and v
     *
     * TODO: think about a more efficient way to enumerate prime implicants from
     *       MDD implicants, for example begin with the less symbolic state and
     *       maintain a list for "already primal" implicants to extract them
     *       from the quadratic loop
     */
    private List enumeratePrimeImplicantsOfPart(List mddPart) {
	List primePart = new ArrayList();
	boolean flag = false;
	// For each MDD implicants
	ListIterator<Implicant> iterator_1 = mddPart.listIterator();
	while (iterator_1.hasNext()) {
	    Implicant implicant_1 = iterator_1.next();
	    // Declare temporary set to accumulate more primal implicant
	    Set tempPrimePart = new HashSet<Implicant>();
	    // For each MDD implicants
	    ListIterator<Implicant> iterator_2 = mddPart.listIterator();
	    while (iterator_2.hasNext()) {
		Implicant implicant_2 = iterator_2.next();
		if (implicant_1 != implicant_2) {
		    // Try to find a more primal implicant
		    try {
			int[] p = combine(implicant_1.p, implicant_2.p);
			Implicant implicant = new Implicant(p, implicant_1.c, implicant_2.v);
			tempPrimePart.add(implicant);
		    } catch (Exception exception) {
			// Don't care
		    }
		}
	    }
	    if (tempPrimePart.isEmpty()) {
		// No more primal implicant has been found
		primePart.add(implicant_1);
	    } else {
		// Some more primal implicants have been found
		primePart.addAll(tempPrimePart);
		flag = true;
	    }
	}
	
	if (flag) {
	    return enumeratePrimeImplicantsOfPart(primePart);
	} else {
	    return primePart;
	}
    }
    
    /**
     * Enumerate all the prime implicants
     */
    private void enumeratePrimeImplicants() {
	this.primeImplicants = new ArrayList();
	Collection<List> partition = partitionMDDImplicants();
	Iterator<List> iterator = partition.iterator();
	while (iterator.hasNext()) {
	    List mddPart = iterator.next();
	    List primePart = enumeratePrimeImplicantsOfPart(mddPart);
	    this.primeImplicants.addAll(primePart);
	}
	return;
    }
    
    
    // Solve the Integer Linear Programming
    private void solveILP() {
	SolverFactory factory = new SolverFactoryLpSolve();
	factory.setParameter(Solver.VERBOSE, 0);
	factory.setParameter(Solver.TIMEOUT, 100);
	return;
    }
    
}
