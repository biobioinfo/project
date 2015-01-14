package org.plugin;

import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.ServiceStatus;
import org.mangosdk.spi.ProviderFor;


/**
 * dummy test for a service
 */
@ProviderFor( Service.class)
@Alias("dummy")
@ServiceStatus(EStatus.RELEASED)
public class TestService implements Service {

	public TestService() {
	}

	public void run() {
		System.out.println("Service running") ;
	}

}
