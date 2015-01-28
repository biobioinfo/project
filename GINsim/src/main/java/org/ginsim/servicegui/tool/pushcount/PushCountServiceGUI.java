package org.ginsim.servicegui.tool.pushcount;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mangosdk.spi.ProviderFor;

import org.ginsim.common.application.GsException;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.DefaultDialogSize;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatePanel;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatesGUIHelper;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.pushcount.PushCountSearcher;
import org.ginsim.service.tool.pushcount.PushCountService;

/**
 * Service that serves as a GUI for the PushCountService. 
 * It contains the UI that is responsible for displaying 
 * the different options for this service.
 * @author heinrich
 *
 */

@ProviderFor(ServiceGUI.class)
@GUIFor(PushCountService.class)
@ServiceStatus(EStatus.RELEASED)
public class PushCountServiceGUI extends AbstractServiceGUI {
	
	public PushCountServiceGUI(){
	}

	@Override
	public List<Action> getAvailableActions(final Graph<?, ?> graph) {
		if(graph instanceof RegulatoryGraph)
		{
			List<Action> acc = new LinkedList<Action>() ;
			Action action = new ToolAction("STR_pushcount", "STR_pushcount_descr", this) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					new PushCountSwingUI((RegulatoryGraph) graph) ;
				}
			};
			acc.add(action) ;
			return acc ;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 20;
	}

}
