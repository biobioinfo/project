package org.ginsim.servicegui.tool.maximalsymbolicsteadystates;

import java.awt.Frame;

import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.common.application.GsException;
import org.ginsim.service.tool.maximalsymbolicsteadystates.MaximalSymbolicSteadyStatesService;
import org.ginsim.core.service.ServiceManager;


public class AnalysisFrame extends StackDialog {
    
    private RegulatoryGraph graph;
    
    public AnalysisFrame(Frame frame, RegulatoryGraph graph) {
	super(frame, "Maximal symbolic steady states", 600, 400);
	// TODO: use Txt.t
	setMessage("Maximal symbolic steady states");
	this.graph = graph;
    }
    
    @Override
    protected void run() throws GsException {
	// TODO: use Txt.t
	setMessage("Wait!");
	MaximalSymbolicSteadyStatesService service = ServiceManager.getManager().getService(MaximalSymbolicSteadyStatesService.class);
	service.setRegulatoryGraph(graph);
	service.run();
	return;
    }
    
}
