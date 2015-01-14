package org.plugin;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.gui.shell.actions.ToolAction;
import org.plugin.TestService;
import org.mangosdk.spi.ProviderFor;


/**
 * Define the action for dummy service
 */
@ProviderFor( ServiceGUI.class)
@GUIFor( TestService.class)
@ServiceStatus( EStatus.RELEASED)
public class TestServiceGUI extends AbstractServiceGUI {

	private static TestService service = ServiceManager.getManager().getService(TestService.class) ;

	public TestServiceGUI() {
	}


	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new DummyAction( graph, this));
		return actions;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 20;
	}


	class DummyAction extends ToolAction {

		private static final long serialVersionUID = -1L;
		private final Graph<?,?> graph;

		public DummyAction(Graph<?,?> graph, ServiceGUI serviceGUI) {
			super( "CoucouAction", "Make coucou", serviceGUI);
			this.graph = graph;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			NotificationManager.publishInformation(graph, "Bonjour toi");
			service.run() ;
		}

	}
}

