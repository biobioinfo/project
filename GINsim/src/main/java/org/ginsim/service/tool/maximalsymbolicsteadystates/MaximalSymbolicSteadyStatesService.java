package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import org.mangosdk.spi.ProviderFor;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDVariable;


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
	
	// Enumerate ad print prime implicants
	enumeratePrimeImplicants();
	System.out.println("");
	System.out.println("Prime implicants:");
	System.out.println(this.primeImplicants);
	
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

    private Collection<List> partitionMDDImplicants() {
	HashMap<Key, List> partition = new HashMap<Key, List>();
	for (Implicant implicant : this.mddImplicants) {
	    Key key = new Key(implicant.v, implicant.c);
	    if (partition.containsKey(key)) {
		List part = partition.get(key);
		part.add(implicant);
	    } else {
		List part = new ArrayList();
		part.add(implicant);
		partition.put(key, part);
	    }
	}
	return partition.values();
    }
    
    private void enumeratePrimeImplicants() {
	this.primeImplicants = new ArrayList();
	Collection<List> partition = partitionMDDImplicants();
	Iterator<List> iterator = partition.iterator();
	while (iterator.hasNext()) {
	    List part = iterator.next();
	    // TODO: compute the prime implicants from the MDD implicants
	    this.primeImplicants.addAll(part);
	}
	return;
    }
}
