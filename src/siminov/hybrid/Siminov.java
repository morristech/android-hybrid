/** 
 * [SIMINOV FRAMEWORK]
 * Copyright [2013] [Siminov Software Solution LLP|support@siminov.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/


package siminov.hybrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import siminov.hybrid.adapter.AdapterHandler;
import siminov.hybrid.events.DatabaseEventHandler;
import siminov.hybrid.events.SiminovEventHandler;
import siminov.hybrid.model.HybridDescriptor;
import siminov.hybrid.model.HybridDescriptor.Adapter;
import siminov.hybrid.model.LibraryDescriptor;
import siminov.hybrid.parsers.HybridDescriptorParser;
import siminov.hybrid.parsers.HybridLibraryDescriptorParser;
import siminov.hybrid.resource.Resources;
import siminov.orm.IInitializer;
import siminov.orm.events.ISiminovEvents;
import siminov.orm.exception.DeploymentException;
import siminov.orm.exception.SiminovException;
import siminov.orm.log.Log;
import siminov.orm.model.ApplicationDescriptor;

/**
 * Exposes methods to deal with SIMINOV HYBRID FRAMEWORK.
 *	<p>
 *		Such As
 *		<p>
 *			1. Initializer: Entry point to the SIMINOV HYBRID.
 *		</p>
 *	
 *
 *	Note*: siminov.hybrid.Siminov extends siminov.orm.Siminov.
 *
 *	</p>
 */
public class Siminov extends siminov.orm.Siminov {

	protected static Resources hybridResources = Resources.getInstance();
	
	protected static boolean isActive = false;

	/**
	 * It is used to check whether SIMINOV HYBRID FRAMEWORK is active or not.
	 * <p>
	 * SIMINOV become active only when its dependent and itself get initialized.
	 * 
	 * @exception If SIMINOV is not active it will throw DeploymentException which is RuntimeException.
	 * 
	 */
	public static void validateSiminov() {
		if(!isActive && !siminov.orm.Siminov.isActive) {
			throw new DeploymentException(Siminov.class.getName(), "validateSiminov", "Siminov Not Active.");
		}
	}
	
	
	public static IInitializer initialize() {
		return new Initializer();
	}
	
	
	/**
	 * It is the entry point to the SIMINOV HYBRID FRAMEWORK.
	 * <p>
	 * When application starts it should call this method to activate SIMINOV HYBRID FRAMEWORK, by providing ApplicationContext and WebView as the parameter.
	 * </p>
	 * 
	 * <p>
	 * Siminov will read all descriptor defined by application, and do necessary processing.
	 * </p>
	 * 
	 * 	EXAMPLE
	 * 	@code {
	 * 

		 	public class Siminov extends DroidGap {
	
				public void onCreate(Bundle savedInstanceState) { 
			
					super.onCreate(savedInstanceState);
					super.init();
					super.appView.getSettings().setJavaScriptEnabled(true);
			
					initializeSiminov();
			
					super.loadUrl("file:///android_asset/www/home.html");
			
				}
			
			
				private void initializeSiminov() {
					siminov.hybrid.Siminov.initialize(getApplicationContext(), this.appView);
				}
			
			}

	 * }
	 * 
	 * @param context Application content.
	 * @param webView WebView.
	 * @exception If any exception occur while deploying application it will through DeploymentException, which is RuntimeException.
	 */
	static void start() {
		
		siminov.orm.Siminov.processApplicationDescriptor();
		processEvents();
		
		
		siminov.orm.Siminov.processDatabaseDescriptors();
		siminov.orm.Siminov.processLibraries();
		siminov.orm.Siminov.processDatabaseMappingDescriptors();

		
		hybridResources.synchronizeMappings();

		
		processHybridDescriptor();
		processLibraries();
		processAdapters();

		
		processHybridServices();
		
	}


	/**
	 * It is used to stop all service started by SIMINOV.
	 * <p>
	 * When application shutdown they should call this. It do following services: 
	 * <p>
	 * 		<pre>
	 * 			<ul>
	 * 				<li> Close all database's opened by SIMINOV.
	 * 				<li> Deallocate all resources held by SIMINOV.
	 * 			</ul>
	 *		</pre>
	 *	</p>
	 * 
	 * @throws SiminovException If any error occur while shutting down SIMINOV.
	 */
	public static void shutdown() {
		
		siminov.orm.Siminov.shutdown();
	}
	
	/**
	 * It process HybridDescriptor.si.xml file defined in Application, and stores in Resources.
	 */
	protected static void processHybridDescriptor() {
		
		HybridDescriptorParser hybridDescriptorParser = new HybridDescriptorParser();
		
		HybridDescriptor jsDescriptor = hybridDescriptorParser.getJSDescriptor();
		if(jsDescriptor == null) {
			Log.logd(Siminov.class.getName(), "processHybridDescriptor", "Invalid JS Descriptor Found.");
			throw new DeploymentException(Siminov.class.getName(), "processHybridDescriptor", "Invalid JS Descriptor Found.");
		}
		
		/*
		 * Add Siminov - JS Library
		 */
		jsDescriptor.addLibraryPath(Constants.HYBRID_DESCRIPTOR_SIMINOV_HYBRID_LIBRARY_PATH);
		
		hybridResources.setHybridDescriptor(jsDescriptor);
	}

	
	/**
	 * It process all LibraryDescriptor.si.xml files defined in Application, and stores in Resources.
	 */
	protected static void processLibraries() {
		
		HybridDescriptor jsDescriptor = hybridResources.getHybridDescriptor();
		Iterator<String> libraryPaths = jsDescriptor.getLibraryPaths();

		while(libraryPaths.hasNext()) {
			String libraryPath = libraryPaths.next();
			
			/*
			 * Parse LibraryDescriptor.
			 */
			HybridLibraryDescriptorParser libraryDescriptorParser = null;
			
			try {
				libraryDescriptorParser = new HybridLibraryDescriptorParser(libraryPath);
			} catch(SiminovException ce) {
				Log.loge(Siminov.class.getName(), "processLibraries", "SiminovException caught while parsing library descriptor, LIBRARY-NAME: " + libraryPath + ", " + ce.getMessage());
				throw new DeploymentException(Siminov.class.getName(), "processLibraries", ce.getMessage());
			}
			
			jsDescriptor.addLibrary(libraryPath, libraryDescriptorParser.getLibraryDescriptor());
		}
		
	}
	
	
	/**
	 * It process all Adapters defined within HybridDescriptor.si.xml or in standalone xml file in Application, and stores in Resources.
	 */
	protected static void processAdapters() {
		
		HybridDescriptor jsDescriptor = hybridResources.getHybridDescriptor();
		
		Iterator<String> libraryPaths = jsDescriptor.getLibraryPaths();
		Iterator<String> adapterPaths = jsDescriptor.getAdapterPaths();
		
		while(libraryPaths.hasNext()) {
			String libraryPath = libraryPaths.next();
			LibraryDescriptor libraryDescriptor = jsDescriptor.getLibraryDescriptorBasedOnPath(libraryPath);

			processAdapters(libraryPath, libraryDescriptor);
		}

		
		processAdapters(adapterPaths);
	}

	
	private static void processAdapters(final Iterator<String> adapterPaths) {
		
		HybridDescriptor jsDescriptor = hybridResources.getHybridDescriptor();
		while(adapterPaths.hasNext()) {
			String adapterPath = adapterPaths.next();
			HybridDescriptorParser hybridDescriptorParser = new HybridDescriptorParser(adapterPath);
			
			Iterator<Adapter> adapters = hybridDescriptorParser.getJSDescriptor().getAdapters();
			while(adapters.hasNext()) {
				Adapter adapter = adapters.next();
				jsDescriptor.addAdapter(adapterPath, adapter);				
			}
		}
	}
	
	
	private static void processAdapters(final String libraryPackageName, final LibraryDescriptor libraryDescriptor) {
		
		Iterator<String> libraryAdapterPaths = libraryDescriptor.getAdapterPaths();
		while(libraryAdapterPaths.hasNext()) {
			String libraryAdapterPath = libraryAdapterPaths.next();
			HybridDescriptorParser hybridDescriptorParser = new HybridDescriptorParser(libraryPackageName, libraryAdapterPath);
			
			Iterator<Adapter> adapters = hybridDescriptorParser.getJSDescriptor().getAdapters();
			while(adapters.hasNext()) {
				Adapter adapter = adapters.next();
				libraryDescriptor.addAdapter(libraryAdapterPath, adapter);				
			}
		}
	}


	/**
	 * It process all Events defined in ApplicationDescriptor.si.xml file in Application, and stores in Resources.
	 */
	protected static void processEvents() {
		
		ApplicationDescriptor applicationDescriptor = ormResources.getApplicationDescriptor();
		
		Collection<String> siminovEvents = new ArrayList<String>();
		Iterator<String> events = applicationDescriptor.getEvents();

		while(events.hasNext()) {
			String event = events.next();
			
			hybridResources.addEvent(event);
			siminovEvents.add(event);
		}

		events = siminovEvents.iterator();
		while(events.hasNext()) {
			String event = events.next();
			applicationDescriptor.removeEvent(event);
		}

		applicationDescriptor.addEvent(SiminovEventHandler.class.getName());
		applicationDescriptor.addEvent(DatabaseEventHandler.class.getName());
		
	}
	
	

	/**
	 * It process and initialize all services needed by SIMINOV HYBRID FRAMEWORK to work functionally.
	 */
	protected static void processHybridServices() {
		AdapterHandler.getInstance();
	}

	
	/**
	 * It Triggers Events that SIMINOV HYBRID FRAMEWORK is initialized properly. 
	 */
	protected static void siminovInitialized() {
		
		isActive = true;
		siminov.orm.Siminov.isActive = true;
		
		ISiminovEvents coreEventHandler = ormResources.getSiminovEventHandler();
		if(ormResources.getSiminovEventHandler() != null) {
			if(siminov.orm.Siminov.firstTimeProcessed) {
				coreEventHandler.firstTimeSiminovInitialized();
			} else {
				coreEventHandler.siminovInitialized();
			}
		} 

	}
	
}
