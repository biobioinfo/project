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
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.netio.BasicLogger;



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
	System.out.println("");
	try {
	    solveILP();
	} catch (Exception exception) {
	    // Don't care
	}
	
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
     * Partition implicants (p, c, v) according to (c, v)
     *
     * @return the collection of lists of implicants with identical (c, v)
     */
    private Collection<List> partitionImplicants(List<Implicant> implicants) {
	HashMap<Key, List> partition = new HashMap<Key, List>();
	for (Implicant implicant : implicants) {
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
	Collection<List> partition = partitionImplicants(this.mddImplicants);
	Iterator<List> iterator = partition.iterator();
	while (iterator.hasNext()) {
	    List mddPart = iterator.next();
	    List primePart = enumeratePrimeImplicantsOfPart(mddPart);
	    this.primeImplicants.addAll(primePart);
	}
	return;
    }
    
    
    // Solve the Integer Linear Programming
    private void solveILP() throws Exception {
	
	Implicant implicant;
	ListIterator<Implicant> implicantIterator;
	
	// Create variables x_a in {0, 1} for every arc a = (p, c, v)
	List<Variable> x = new ArrayList<Variable>();
	implicantIterator = this.primeImplicants.listIterator();
	while (implicantIterator.hasNext()) {
	    // Tattoo prime implicant with the correct index
	    int a = implicantIterator.nextIndex();
	    implicant = implicantIterator.next();
	    implicant.index = a;
	    // Create variable x_a
	    Variable x_a = Variable.make("x_" + a);
	    x_a.lower(0);
	    x_a.upper(1);
	    x_a.weight(1);
	    // Require an integer valued solution
	    x_a.integer(true);
	    // Add variable x to list
	    x.add(x_a);
	}
	// Create variables y0_i in {0, 1] for every variable v_i
	List<Variable> y0 = new ArrayList<Variable>();
	for (int i = 0; i < numberOfGenes(); i++) {
	    // Create variable y0
	    Variable y0_i = Variable.make("y0_" + i);
	    y0_i.lower(0);
	    y0_i.upper(1);
	    y0_i.weight(0);
	    // Require an integer valued solution
	    y0_i.integer(true);
	    // Add variable y0 to list
	    y0.add(y0_i);
	}
	// Create variables y1_i in {0, 1} for every variable v_i
	List<Variable> y1 = new ArrayList<Variable>();
	for (int i = 0; i < numberOfGenes(); i++) {
	    // Create variable y1
	    Variable y1_i = Variable.make("y1_" + i);
	    y1_i.lower(0);
	    y1_i.upper(1);
	    y1_i.weight(0);
	    // Require an integer valued solution
	    y1_i.integer(true);
	    // Add variables y_1 to list
	    y1.add(y1_i);
	}
	
	// Create a model and add variables to it
	ExpressionsBasedModel model = new ExpressionsBasedModel();
	ListIterator<Variable> variableIterator = x.listIterator();
	while (variableIterator.hasNext()) {
	    Variable variable = variableIterator.next();
	    model.addVariable(variable);
	}
	variableIterator = y0.listIterator();
	while (variableIterator.hasNext()) {
	    Variable variable = variableIterator.next();
	    model.addVariable(variable);
	}
	variableIterator = y1.listIterator();
	while (variableIterator.hasNext()) {
	    Variable variable = variableIterator.next();
	    model.addVariable(variable);
	}
	
	// Create constraints  C1: for all c and v_i, yc_i = (Or_{a in Bc_i} x_a)
	// (i.e. | for all a in Bc_i, x_a <= yc_i      (I))
	// (     | 0 <= (Sum_{a in Bc_i} x_a) - yc_i  (II))
	Collection<List> partition = partitionImplicants(this.primeImplicants);
	Iterator<List> partIterator = partition.iterator();
	while (partIterator.hasNext()) {
	    List<Implicant> part = partIterator.next();
	    // Create constraint (II)
	    implicant = part.get(0);
	    Variable  yc_i;
	    switch (implicant.c) {
	    case 0:
		yc_i = y0.get(implicant.v);
		break;
	    case 1:
		yc_i = y1.get(implicant.v);
		break;
	    default:
		throw new Exception("Illegal implicant (illegal third component)");
	    }
	    Expression partExpression = model.addExpression("C1_" + implicant.c + "," + implicant.v);
	    partExpression.lower(0);
	    partExpression.setLinearFactor(yc_i, -1);
	    // Create constraint (I)
	    implicantIterator = part.listIterator();
	    while (implicantIterator.hasNext()) {
		// Create subconstraint for constraint (I)
		implicant = implicantIterator.next();
		Expression expression = model.addExpression("C1_" + implicant.c + "," + implicant.v + "," + implicant.index);
		expression.upper(0);
		Variable x_a = x.get(implicant.index);
		expression.setLinearFactor(x_a, 1);
		expression.setLinearFactor(yc_i, -1);
		// Complete constraint (II)
		partExpression.setLinearFactor(x_a, 1);
	    }
	}
	
	// Create constraints  C2: for all v_i, not(y0_i) or not(y1_i)
	// (i.e. y0_i + y1_i <= 1)
	for (int i = 0; i < numberOfGenes(); i++) {
	    Expression expression = model.addExpression("C2_" + i);
	    expression.upper(1);
	    Variable y0_i = y0.get(i);
	    Variable y1_i = y1.get(i);
	    expression.setLinearFactor(y0_i, 1);
	    expression.setLinearFactor(y1_i, 1);
	}
	
	// Create constraints C3: for all a, for all v_i in U_{T(a)}, not(x_a) or y(T(a)(v_i))_i
	// (i.e. y(T(a)(v_i))_i - x_a >= 0)
	implicantIterator = this.primeImplicants.listIterator();
	while (implicantIterator.hasNext()) {
	    implicant = implicantIterator.next();
	    Variable x_a = x.get(implicant.index);
	    for (int v = 0; v < numberOfGenes(); v++) {
		int c = implicant.p[v];
		if (-1 != c) {
		    Variable  yc_i;
		    switch (c) {
		    case 0:
			yc_i = y0.get(v);
			break;
		    case 1:
			yc_i = y1.get(v);
			break;
		    default:
			throw new Exception("Illegal partial state (illegal value)");
		    }
		    Expression expression = model.addExpression("C3_" + implicant.index + "," + v);
		    expression.lower(0);
		    expression.setLinearFactor(x_a, -1);
		    expression.setLinearFactor(yc_i, 1);
		} else {
		    // Don't care
		}
	    }
	}
	
	// Solve the problem (i.e. maximise the cost)
	Optimisation.Result result = model.maximise();
	
	// Create constraints C4: ...
	// TODO: complete
	
	// Print the result and the model
	BasicLogger.debug();
	BasicLogger.debug(model);
	BasicLogger.debug();
	BasicLogger.debug(result);
	BasicLogger.debug();
		
	return;
    }
    
}
