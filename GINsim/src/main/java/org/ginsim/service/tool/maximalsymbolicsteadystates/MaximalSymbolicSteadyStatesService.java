package org.ginsim.service.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.util.ArrayList;

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
    
    public void test(LogicalModel logicalModel) {
	MDDManager mddManager = logicalModel.getMDDManager();
	int[] logicalFunctions = logicalModel.getLogicalFunctions();
	
	// Print MDD manager ///////////////////////////////////////////////////
	System.out.println("");
	System.out.println("MDDManager");
	int node = 0;
	String representation = mddManager.dumpMDD(node);
	System.out.println(representation);
	
	// Print logical functions /////////////////////////////////////////////
	System.out.println("LogicalFunctions");
	for (int i = 0; i < logicalFunctions.length; i++) {
	    node = logicalFunctions[i];
	    //System.out.println(node);
	    representation = mddManager.dumpMDD(node);
	    System.out.println(representation);
	}
	
	// Print MDD variables /////////////////////////////////////////////////
	System.out.println("MDDVariables");
	MDDVariable[] mddVariables = mddManager.getAllVariables();
	for (int i = 0; i < mddVariables.length; i++) {
	    System.out.println(mddVariables[i].toString());
	    //System.out.println(mddManager.getVariableIndex(mddVariables[i]));
	}
    }
    
    public void run(RegulatoryGraph graph) {
	LogicalModel logicalModel = graph.getModel();
	MDDManager mddManager = logicalModel.getMDDManager();
	MDDVariable[] mddVariables = mddManager.getAllVariables();
	int[] logicalFunctions = logicalModel.getLogicalFunctions();
	
	// Enumerate prime implicant ///////////////////////////////////////////
	List primeImplicants = new ArrayList();
	for (int i = 0; i < logicalFunctions.length; i++) {
	    int[] p = new int[mddVariables.length];
	    for (int j = 0; j < p.length; j++) {
		p[j] = -1;
	    }
	    int id = logicalFunctions[i];
	    enumeratePrimeImplicants(mddManager, p, id, id, primeImplicants);
	}
	
	System.out.println("");
	System.out.println("Prime implicants:");
	System.out.println(primeImplicants);
	
	return;
    }
    
    public void enumeratePrimeImplicants(MDDManager mddManager, int[] p, int c, int v, List primeImplicants) {
	if (mddManager.isleaf(c)) {
	    PrimeImplicant primeImplicant = new PrimeImplicant(p, c, v);
	    primeImplicants.add(primeImplicant);
	} else {
	    MDDVariable mddVariable = mddManager.getNodeVariable(c);
	    for (int i = 0; i < mddVariable.nbval; i++) {
		int[] q = p.clone();
		q[mddVariable.order] = i;
		enumeratePrimeImplicants(mddManager, q, mddManager.getChild(c, i), v, primeImplicants);
	    }
	}

	return;
    }
    
}
