package org.ginsim.service.tool.cluster;

import org.mangosdk.spi.ProviderFor;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.core.service.EStatus;



@ProviderFor(Service.class)
@Alias("cluster")
@ServiceStatus(EStatus.RELEASED)
public class ClusterService implements Service {
    
    public void run() {
	return;
    }
    
}
