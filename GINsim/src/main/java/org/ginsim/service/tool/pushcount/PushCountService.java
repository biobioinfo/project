package org.ginsim.service.tool.pushcount;

import org.colomoto.logicalmodel.LogicalModel;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.mangosdk.spi.ProviderFor;


@ProviderFor(Service.class)
@Alias("dummy")
@ServiceStatus(EStatus.RELEASED)
public class PushCountService implements Service{
	
	public PushCountSearcher getSearcher(RegulatoryGraph g){
		return getSearcher(g.getModel()) ;
	}

	public PushCountSearcher getSearcher(LogicalModel model) {
		return new PushCountSearcher(model);
	}
	
	
	

}
