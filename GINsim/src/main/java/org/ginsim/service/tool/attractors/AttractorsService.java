package org.ginsim.service.tool.attractors;

import org.mangosdk.spi.ProviderFor;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.tools.attractors.AttractorsSearcher;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;

@ProviderFor(Service.class)
@Alias("attractors")
@ServiceStatus(EStatus.RELEASED)
public class AttractorsService implements Service {
	
	
	public AttractorsSearcher getSearcher(AttractorsSearcher.Algorithm algo, 
			RegulatoryGraph g, boolean synchronous) {
		return getSearcher(algo, g.getModel(), synchronous) ;
		
	}
	
	public AttractorsSearcher getSearcher(AttractorsSearcher.Algorithm algo, 
			LogicalModel model, boolean synchronous) {
		return new AttractorsSearcher(model, algo, synchronous) ;
	}
}
