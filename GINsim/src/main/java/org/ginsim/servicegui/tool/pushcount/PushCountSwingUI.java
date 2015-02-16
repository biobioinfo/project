package org.ginsim.servicegui.tool.pushcount;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.colomoto.common.task.Task;
import org.colomoto.common.task.TaskListener;
import org.colomoto.common.task.TaskStatus;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.tools.pushcount.PushCountResult;
import org.colomoto.logicalmodel.tools.pushcount.PushCountSearcher;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.ResultsDialog;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatePanel;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.service.tool.pushcount.PushCountService;

public class PushCountSwingUI extends LogicalModelActionDialog {
	private static final long serialVersionUID = 1L;
	
	private JPanel mainPanel = new JPanel() ;
	private InitialStatePanel sourceComp ;
	private NamedStatesHandler sourceHandler ;
	private InitialStatePanel targetComp ;
	private NamedStatesHandler targetHandler ;
	private RegulatoryGraph graph ;
	
	private final PushCountService service = ServiceManager.getManager().getService(PushCountService.class);
	
	public PushCountSwingUI(RegulatoryGraph graph) {
		super(graph, null, "PushCount", 600,600) ;
		this.graph = graph ;
		sourceHandler = new NamedStatesHandler(graph) ;
		targetHandler = new NamedStatesHandler(graph) ;
		sourceComp = new InitialStatePanel(sourceHandler, true) ;
		targetComp = new InitialStatePanel(targetHandler, true) ;
		intialize() ;
	}

	protected void intialize(){
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)) ;
		setMainPanel(mainPanel) ;
		JLabel lab = new JLabel(Txt.t("STR_select_source")) ;
		lab.setAlignmentX(LEFT_ALIGNMENT) ;
		mainPanel.add(lab) ;
		sourceComp.setAlignmentX(LEFT_ALIGNMENT) ;
		mainPanel.add(sourceComp) ;
		lab = new JLabel(Txt.t("STR_select_target")) ;
		lab.setAlignmentX(LEFT_ALIGNMENT) ;
		mainPanel.add(lab) ;
		targetComp.setAlignmentX(LEFT_ALIGNMENT) ;
		mainPanel.add(targetComp) ;
		
		setVisible(true) ;
	}

	@Override
	public void run(LogicalModel model) {
		NamedStateList source = sourceHandler.getInitialStates() ;
		NamedStateList target = targetHandler.getInitialStates() ;
 
		if(source.isEmpty())
		{
			NotificationManager.publishError(graph, "Please enter source states") ;
			return ;
		}
		if(target.isEmpty())
		{
			NotificationManager.publishError(graph, "Please enter target states") ;
		}
		
		PushCountSearcher task = service.getSearcher(model, source, target) ;
		
		final ResultsDialog f = new ResultsDialog(null) ;
		//f.setSize(600, 600) ;
		//f.setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;
		//f.setVisible(true) ;
		
		task.background(new TaskListener() {
			@Override
			public void taskUpdated(Task t) {
				if(t.getStatus() != TaskStatus.FINISHED)
				{
					NotificationManager.publishError(graph, "Task ended with status " + t.getStatus()) ;
				}
				PushCountResult result = (PushCountResult) t.getResult() ;
				if(result == null)
				{
					NotificationManager.publishError(graph, "Computation returned null") ;
					return ; 
				}
				
				f.setResults(result.print()) ;
			}
		}) ;

		closeEvent() ;
		
	}
}
