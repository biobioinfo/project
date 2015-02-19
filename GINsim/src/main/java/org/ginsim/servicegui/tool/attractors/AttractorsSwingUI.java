package org.ginsim.servicegui.tool.attractors;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.colomoto.common.task.Task;
import org.colomoto.common.task.TaskListener;
import org.colomoto.common.task.TaskStatus;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.tools.attractors.AttractorsSearcher;
import org.colomoto.logicalmodel.tools.attractors.AttractorsSearcher.Algorithm;
import org.ginsim.common.application.Txt;
import org.ginsim.commongui.dialog.ResultsDialog;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.service.tool.attractors.AttractorsService;

public class AttractorsSwingUI extends LogicalModelActionDialog {
	private static final long serialVersionUID = 1L;
	
	private JComboBox algoChooser = 
			new JComboBox(Algorithm.values()) ;
	
	private JLabel reflab = new JLabel(Txt.t("STR_attractors_ref")) ;
	
	private JLabel refLabel = new JLabel(((Algorithm) algoChooser.getSelectedItem()).getReference()) ;
	
	private JComboBox updateMethodChooser = 
			new JComboBox(new String[] {"synchronous", "asynchronous"}) ;
	
	private JPanel mainPanel = new JPanel() ;
	
	private RegulatoryGraph graph ;

	public AttractorsSwingUI(RegulatoryGraph lrg) {
		super(lrg, null, "Attractors", 400, 400);
		this.graph = lrg ;
		init() ;
	}
	
	private void init() {
		setMainPanel(mainPanel) ;
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)) ;
		mainPanel.add(Box.createRigidArea(new Dimension(20, 20))) ;
		JLabel lab = new JLabel(Txt.t("STR_attractors_select_update")) ;
		lab.setAlignmentX(LEFT_ALIGNMENT) ;
		mainPanel.add(lab) ;
		
		mainPanel.add(updateMethodChooser) ;
		updateMethodChooser.setAlignmentX(LEFT_ALIGNMENT) ;
		updateMethodChooser.setMaximumSize(new Dimension(10000, 40)) ;
		
		mainPanel.add(Box.createRigidArea(new Dimension(20, 20))) ;
		
		lab =new JLabel(Txt.t("STR_attractors_select_algo")) ;
		lab.setAlignmentX(LEFT_ALIGNMENT) ;
		mainPanel.add(lab) ;
		
		mainPanel.add(algoChooser) ;
		algoChooser.setAlignmentX(LEFT_ALIGNMENT) ;
		algoChooser.setMaximumSize(new Dimension(10000, 40)) ;
		
		mainPanel.add(Box.createRigidArea(new Dimension(20, 20))) ;
		JPanel pan = new JPanel() ;
		pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS)) ;
		pan.add(reflab) ;
		pan.add(Box.createRigidArea(new Dimension(10, 10))) ;
		pan.add(refLabel) ;
		mainPanel.add(pan) ;
		mainPanel.add(Box.createVerticalGlue()) ;
		pan.setAlignmentX(LEFT_ALIGNMENT) ;
		
		algoChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refLabel.setText(((Algorithm) algoChooser.getSelectedItem()).getReference()) ;
			}
		}) ;
		
	}

	@Override
	public void run(LogicalModel model) {
		boolean synchronous = updateMethodChooser.getSelectedItem().equals("synchronous") ;
		Algorithm algo = (Algorithm) algoChooser.getSelectedItem() ;
		
		AttractorsSearcher s = new AttractorsService().getSearcher(algo, model, synchronous) ;
		s.background(new TaskListener() {
			
			@Override
			public void taskUpdated(Task t) {
				if(t.getStatus() != TaskStatus.FINISHED)
				{
					NotificationManager.publishError(graph, "Task ended with status " + t.getStatus()) ;
					return ; 
				}
				ResultsDialog d = new ResultsDialog(null) ;
				@SuppressWarnings("unchecked")
				List<List<byte[]>> res = (List<List<byte[]>>) t.getResult() ;
				String s = "";
				int i = 1 ;
				for(List<byte[]> att : res)
				{
					s += "Attractor : " + i + "\n" ;
					for(byte[] state : att)
						s += printState(state) + "\n" ;
					s += "\n" ;
					i++ ;
				}
				d.setResults(s) ;
			}
		}) ;
		closeEvent() ;
	}
	
	private String printState(byte[] state){
		String s = "";
		for(byte b : state)
			s += b ;
		return s ;
	}

}
