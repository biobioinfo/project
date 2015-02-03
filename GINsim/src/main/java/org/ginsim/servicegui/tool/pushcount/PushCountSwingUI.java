package org.ginsim.servicegui.tool.pushcount;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.colomoto.logicalmodel.LogicalModel;
import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
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
	
	private final PushCountService service = ServiceManager.getManager().getService(PushCountService.class);
	
	public PushCountSwingUI(RegulatoryGraph graph) {
		super(graph, null, "PushCount", 600,600) ;
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
		try{
		service.getSearcher(model, source, target).doGetResult() ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		closeEvent() ;
		
	}
}
