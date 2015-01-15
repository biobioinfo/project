package org.ginsim.servicegui.tool.dummy;

import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.mangosdk.spi.ProviderFor;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.GUIFor;
import org.ginsim.service.tool.dummy.DummyService;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.core.graph.Graph;
import org.ginsim.gui.shell.actions.ToolAction;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.core.notification.NotificationManager;



@ProviderFor(ServiceGUI.class)
@GUIFor(DummyService.class)
@ServiceStatus(EStatus.RELEASED)
public class DummyServiceGUI extends AbstractServiceGUI {
    
    @Override
    public List<Action> getAvailableActions(Graph<?, ?> graph) {
	List<Action> actions = new ArrayList<Action>();
	actions.add(new DummyAction(graph, this));
	return actions;
    }
    
    @Override
    public int getInitialWeight() {
	return W_TOOLS_MAIN + 20;
    }
    
}


class DummyAction extends ToolAction {
    
    private final Graph<?, ?> graph;
    private final DummyService service = ServiceManager.getManager().getService(DummyService.class);

    public DummyAction(Graph<?, ?> graph, ServiceGUI serviceGUI) {
	super("STR_dummy", "STR_dummy_descr", serviceGUI);
	this.graph = graph;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
	NotificationManager.publishInformation(graph, "Dummy.");
	service.run();
	return;
    }
    
}
