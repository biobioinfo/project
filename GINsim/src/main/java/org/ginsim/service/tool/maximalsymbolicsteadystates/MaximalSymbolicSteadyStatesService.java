package org.ginsim.service.tool.maximalsymbolicsteadystates;

import org.mangosdk.spi.ProviderFor;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.common.callable.ProgressListener;



/**
 * Main method for the MaximalSymbolicSteadyStatesService plugin.
 *
 * @author Baptiste Lefebvre
 */
@ProviderFor(Service.class)
@Alias("maximalsymbolicsteadystates")
@ServiceStatus(EStatus.RELEASED)
public class MaximalSymbolicSteadyStatesService implements Service {
    
    public Searcher get(RegulatoryGraph regulatoryGraph, ProgressListener<Result> progressListener) {
      Searcher searcher = new Searcher(regulatoryGraph, progressListener);
      return searcher;
    }
    
}
