package org.ginsim.service.tool.pushcount;

import java.text.ParseException;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.tools.pushcount.PushCountSearcher;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.operators.MDDBaseOperators;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.mangosdk.spi.ProviderFor;


@ProviderFor(Service.class)
@Alias("pushcount")
@ServiceStatus(EStatus.RELEASED)
public class PushCountService implements Service{

	public PushCountSearcher getSearcher(RegulatoryGraph g, 
			NamedStateList source, NamedStateList target) {
		return getSearcher(g.getModel(), source, target) ;
	}

	public PushCountSearcher getSearcher(LogicalModel model, 
			NamedStateList source, NamedStateList target){
		if(source.size() == 0 || target.size() == 0)
			return null;

		MDDManager m = model.getMDDManager() ;
		int[] sourceMDDs = new int[source.size()] ;
		for(int i = 0 ; i < source.size() ; i++)
			sourceMDDs[i] = source.get(i).getMDD(m) ;

		int[] targetMDDs = new int[target.size()] ;
		for(int i = 0 ; i < target.size() ; i++)
			targetMDDs[i] = target.get(i).getMDD(m) ;

		int sMDD ;
		int tMDD ;
		if(sourceMDDs.length == 1)
			sMDD = sourceMDDs[0] ;
		else 
			sMDD = MDDBaseOperators.OR.combine(m, sourceMDDs) ;

		if(targetMDDs.length == 1)
			tMDD = targetMDDs[0] ;
		else 
			tMDD = MDDBaseOperators.OR.combine(m, targetMDDs) ;

		return getSearcher(model, sMDD, tMDD ) ;
	}

	public PushCountSearcher getSearcher(LogicalModel model, int source, int target) {
		try{
			return new PushCountSearcher(model, source, target);
		} catch (ParseException e) {
			e.printStackTrace() ;
			return null ;
		}
	}
}
