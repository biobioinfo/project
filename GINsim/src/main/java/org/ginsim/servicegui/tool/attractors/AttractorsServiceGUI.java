package org.ginsim.servicegui.tool.attractors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mangosdk.spi.ProviderFor;

import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.service.tool.attractors.AttractorsService;



@ProviderFor(ServiceGUI.class)
@GUIFor(AttractorsService.class)
@ServiceStatus(EStatus.RELEASED)
public class AttractorsServiceGUI extends AbstractServiceGUI {

	public AttractorsServiceGUI(){
	}

	@Override
	public List<Action> getAvailableActions(final Graph<?, ?> graph) {
		if(graph instanceof RegulatoryGraph)
		{
			List<Action> acc = new LinkedList<Action>() ;
			Action action = new ToolAction("STR_attractors", "STR_attractors_descr", this) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					new AttractorsSwingUI((RegulatoryGraph) graph) ;
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
