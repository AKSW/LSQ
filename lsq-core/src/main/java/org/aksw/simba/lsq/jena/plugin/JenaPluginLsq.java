package org.aksw.simba.lsq.jena.plugin;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginLsq
	implements JenaSubsystemLifecycle {

	public void start() {
		init();
	}

	@Override
	public void stop() {
	}

	
	public static void init() {
		JenaPluginUtils.registerJenaResourceClassesUsingPackageScan(LsqQuery.class.getPackage().getName());
		// TODO Extend mapper proxy with support for generic collection views
		// i.e. Collection<T> getFoo(Class<T>)
		
		
	}
}
