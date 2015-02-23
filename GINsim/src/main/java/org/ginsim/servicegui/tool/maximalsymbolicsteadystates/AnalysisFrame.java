package org.ginsim.servicegui.tool.maximalsymbolicsteadystates;

import java.awt.Frame;

import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.common.callable.ProgressListener;
import org.ginsim.service.tool.maximalsymbolicsteadystates.Result;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.service.tool.maximalsymbolicsteadystates.Searcher;
import org.ginsim.common.application.GsException;
import org.ginsim.service.tool.maximalsymbolicsteadystates.MaximalSymbolicSteadyStatesService;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.commongui.dialog.ResultsDialog;



/**
 * Frame displayed to the user when he want to search the symbolic steady states
 *
 * @author Baptiste Lefebvre
 */
public class AnalysisFrame extends StackDialog implements ProgressListener<Result> {
    
    private Frame parent;
    private RegulatoryGraph graph;

    private Searcher searcher;
    private Thread thread;
    private Result result;
    
    public AnalysisFrame(Frame parent, RegulatoryGraph graph) {
	super(parent, "Maximal symbolic steady states", 600, 400);
	// TODO: use Txt.t
	setMessage("Maximal symbolic steady states");
	this.parent = parent;
	this.graph = graph;
    }
    
    @Override
    protected void run() throws GsException {
	// TODO: use Txt.t
	setMessage("Wait!");
	bcancel.setText("Abort");
	brun.setEnabled(false);
	
	MaximalSymbolicSteadyStatesService service = ServiceManager.getManager().getService(MaximalSymbolicSteadyStatesService.class);
	searcher = service.get(graph, this);
	thread = new Thread(searcher);
	thread.start();
	
	return;
    }
    
    @Override
    protected void cancel() {
	thread.interrupt();
	// TODO: check if other administrative calls are needed
	super.cancel();
	return;
    }
    
    
    // ProgressListener implementation /////////////////////////////////////////
    
    @Override
    public void setProgress(int n) {
	setMessage("" + n);
	return;
    }
    
    @Override
    public void setProgress(String s) {
	setMessage(s);
	return;
    }
    
    @Override
    public void milestone(Object data) {
	String s = data.toString();
	setMessage(s);
	return;
    }
    
    @Override
    public void setResult(Result result) {
	// TODO: use Txt.t
	setMessage("Done!");
	this.result = result;
	ResultsDialog resultsDialog = new ResultsDialog(this.parent);
	String results;
	if (null == this.result) {
	    // TODO: use Txt.t
	    results = "Exception (see standard error)";
	} else {
	    results = this.result.toString();
	}
	resultsDialog.setResults(results);
	closeEvent();
	return;
    }
    
}
