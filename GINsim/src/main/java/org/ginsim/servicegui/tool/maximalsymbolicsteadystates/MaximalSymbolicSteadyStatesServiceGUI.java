package org.ginsim.servicegui.tool.maximalsymbolicsteadystates;

import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.mangosdk.spi.ProviderFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.service.tool.maximalsymbolicsteadystates.MaximalSymbolicSteadyStatesService;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.utils.widgets.Frame;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.GraphGUI;



/**
 * Service which serves as a GUI for the MaximalSymbolicSteadyStatesService.
 * It contains the UI that is responsible for displaying the different messages
 * for this service.
 *
 * @author Baptiste Lefebvre
 */
@ProviderFor(ServiceGUI.class)
@GUIFor(MaximalSymbolicSteadyStatesService.class)
@ServiceStatus(EStatus.RELEASED)
public class MaximalSymbolicSteadyStatesServiceGUI extends AbstractServiceGUI {
    
    @Override
    public List<Action> getAvailableActions(Graph<?, ?> graph) {
	if (graph instanceof RegulatoryGraph) {
	    List<Action> actions = new ArrayList<Action>();
	    actions.add(new MaximalSymbolicSteadyStatesAction((RegulatoryGraph)graph, this));
	    return actions;
	} else {
	    return null;
	}
    }
    
    @Override
    public int getInitialWeight() {
	return W_TOOLS_MAIN + 10;
    }
    
}


class MaximalSymbolicSteadyStatesAction extends ToolAction {
    
    private final RegulatoryGraph graph;

    public MaximalSymbolicSteadyStatesAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
	super("STR_maximal_symbolic_steady_states", "STR_maximal_symbolic_steady_states_descr", serviceGUI);
	this.graph = graph;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	// TODO: use Txt.t for the string below
	NotificationManager.publishInformation(graph, "Maximal symbolic steady states.");
	
	Frame frame = GUIManager.getInstance().getFrame(graph);
	GraphGUI<?, ?, ?> gui = null;
	if (frame != null) {
	    gui = GUIManager.getInstance().getGraphGUI(graph);
	    // TODO: see ../reg2dyn/Reg2DynServiceGUI.java
	}
	
	// TODO: add management of the parameters list (for now parameters = {graph})
	AnalysisFrame analysisFrame = new AnalysisFrame(frame, graph);
	analysisFrame.setAssociatedGUI(gui);
	analysisFrame.setVisible(true);
	
	return;
    }
    
}
