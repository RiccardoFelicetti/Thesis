package org.eclipse.kura.wire.component.decorator.provider;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Objects.nonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.type.TypedValues;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEmitter;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireReceiver;
import org.eclipse.kura.wire.WireRecord;
import org.eclipse.kura.wire.WireSupport;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.wireadmin.Wire;


public class Decorator implements WireReceiver, WireEmitter, ConfigurableComponent{


	 private static final Logger logger = LogManager.getLogger(Decorator.class);
	 
	 //PROPERTIES
	 private static final String DEVICE_ID = "device.id";
	 
	 private static final String POSITION_EN="position.enable";
	 
	 private static final String DEVICE_LAT= "position.lat";
	 private static final String DEVICE_LONG= "position.long";
	 
	 private static final String DEVICE_DESC= "device.desc";
	 //------
	 
	 private volatile WireHelperService wireHelperService;
	 
	 private WireSupport wireSupport;
	 private Map<String, Object> properties;
	 
	 
	 
	 
	 /**
	     * Binds the Wire Helper Service.
	     *
	     * @param wireHelperService
	     *            the new Wire Helper Service
	     */
	    public void bindWireHelperService(final WireHelperService wireHelperService) {
	        if (isNull(this.wireHelperService)) {
	            this.wireHelperService = wireHelperService;
	        }
	    }

	    /**
	     * Unbinds the Wire Helper Service.
	     *
	     * @param wireHelperService
	     *            the new Wire Helper Service
	     */
	    public void unbindWireHelperService(final WireHelperService wireHelperService) {
	        if (this.wireHelperService == wireHelperService) {
	            this.wireHelperService = null;
	        }
	    }
	    
	    /**
	     * OSGi Service Component callback for activation.
	     *
	     * @param componentContext
	     *            the component context
	     * @param properties
	     *            the properties
	     */
	    protected void activate(final ComponentContext componentContext, final Map<String, Object> properties) {
	        logger.debug("Activating Decorator Wire Component...");
	        this.properties = properties;
	        this.wireSupport = this.wireHelperService.newWireSupport(this,
	                (ServiceReference<WireComponent>) componentContext.getServiceReference());
	        logger.debug("Activating Decorator Wire Component... Done");
	    }
	    

	    /**
	     * OSGi Service Component callback for updating.
	     *
	     * @param properties
	     *            the updated properties
	     */
	    public void updated(final Map<String, Object> properties) {
	        logger.debug("Updating Decorator Wire Component...");
	        this.properties = properties;
	        logger.debug("Updating Decorator Wire Component... Done");
	    }

	    
	    /**
	     * OSGi Service Component callback for deactivation.
	     *
	     * @param componentContext
	     *            the component context
	     */
	    protected void deactivate(final ComponentContext componentContext) {
	        logger.debug("Deactivating Decorator Wire Component...");
	        // remained for debugging purposes
	        logger.debug("Deactivating Decorator Wire Component... Done");
	    }
	    
	    
	    
	    /** {@inheritDoc} */
	    @Override
	    public void onWireReceive(final WireEnvelope wireEnvelope) {
	    	
	    	requireNonNull(wireEnvelope, "Wire Envelope cannot be null");	    		    	
	    	logger.info("Decorator: Received WireEnvelope from " + wireEnvelope.getEmitterPid());
	    	
	    	//Get Values from properties
	    	String deviceId=getDeviceId();    	
	    	String deviceDesc=getDeviceDesc();    
	    	Double deviceLat=null;
	    	Double deviceLong=null;
	    	Boolean positionEn=getPositionEn();
	    	
	    	//Map for id and description
	    	Map<String,TypedValue<?>> decoratorStringMap= null;
	    	//map for position
	    	Map<String,TypedValue<?>> decoratorPosMap = null;
	    	//Map for records
	    	Map<String,TypedValue<?>> decoratorMap=null;
	    	
	    	//Get values of lat and long
	    	if(positionEn.booleanValue()) {	    		
	    		deviceLat=getDeviceLat();
	    		deviceLong=getDeviceLong();	    		
	    	    //decoratorPosMap= new HashMap<>();
	    	    decoratorMap=new HashMap<>();
	    	}
	    	
	    	//Copy of the received records
	    	List<WireRecord> old_record = new ArrayList<>();
	    	for(WireRecord record: wireEnvelope.getRecords())
	    		old_record.add(record);    	    	
	    	  
	    	//Re-create Map from received record
	    	Set<String> entries=old_record.get(0).getProperties().keySet();
	    	
	    	decoratorMap= new LinkedHashMap<>();
	    	for(String entry:entries) {
	    		decoratorMap.put(entry, old_record.get(0).getProperties().get(entry));
	    	}    	
	    	
	    	
	    	//Add ID on envelope
	    	if(!deviceId.isEmpty()) {
	    		
	    		 if(!nonNull(decoratorStringMap))
	    			 decoratorStringMap=new HashMap<>();
	    		 
	    		 if(!nonNull(decoratorMap)) {
	    			 decoratorMap=new HashMap<>();
	    		 }
	    		 
	    		 final TypedValue<String> id= TypedValues.newStringValue(deviceId);
                 //decoratorStringMap.put("DEVICE_ID", id);
	    		 decoratorMap.put("device_name", id);
	    		 //final WireRecord idWireRecord = new WireRecord(Collections.singletonMap("DEVICE_ID", id));
	    		
	    		 //old_record.add(idWireRecord);	    		 
	    		 		 
	    	}
	    	
	    	//Add Description on envelope
	    	if(!deviceDesc.isEmpty()) {
	    		
	    		
	    		if(!nonNull(decoratorMap)) {
	    			decoratorMap=new HashMap<>();
	    		}
	    		
	    		//decoratorStringMap= new HashMap<>();
	    		final TypedValue<String> desc= TypedValues.newStringValue(deviceDesc);
	    		//decoratorStringMap.put("DEVICE_DESC", desc);	    		
	    		decoratorMap.put("device_desc", desc);
	    		
	    		//final WireRecord descWireRecord = new WireRecord(Collections.singletonMap("DEVICE_DESC", desc));
	    		
	    		//old_record.add(descWireRecord);
	    		
	    	}
	    	
	    	
	    	
	    	
	    	//Add Lat and Long on envelope
	    	if(positionEn.booleanValue()) {	    		
	    		
	    		final TypedValue<Double> lat = TypedValues.newDoubleValue(deviceLat);
	    		final TypedValue<Double> lon = TypedValues.newDoubleValue(deviceLong);
	    		
	    		decoratorMap.put("device_long", lon);
	    		decoratorMap.put("device_lat", lat);
	    		//decoratorPosMap.put("DEVICE_LONG", lon);
	    		//decoratorPosMap.put("DEVICE_LAT", lat);
	    		
	    		
	    		//final WireRecord latWireRecord = new WireRecord(Collections.singletonMap("DEVICE_LAT", lat));
	    		//final WireRecord lonWireRecord = new WireRecord(Collections.singletonMap("DEVICE_LONG", lon));
	    		//old_record.add(latWireRecord);
	            //old_record.add(lonWireRecord);
	    		
	    	}
	    	
	    	/*
	    	if(nonNull(decoratorStringMap)) {
	    	final WireRecord new_DescRecord= new WireRecord(decoratorStringMap);
	    	old_record.add(new_DescRecord);
	    	}
	    	
	    	if(nonNull(decoratorPosMap)) {
	    	final WireRecord new_PosRecord= new WireRecord(decoratorPosMap);	    		    	
	    	old_record.add(new_PosRecord);
	    	}
	    	*/
	    	
	    	//Delete old record and add new record
	    	if(nonNull(decoratorMap) && !decoratorMap.isEmpty()) {
	    		final WireRecord decoratorRecord = new WireRecord(decoratorMap);	
	    		if(old_record.size()>0)
	    		old_record.remove(0);
	    		old_record.add(decoratorRecord);
	    	}
	    	
	    	this.emitEnvelope(new WireEnvelope("Decorator", old_record));
	    	
	    }
	    
	    
	    
	    /** {@inheritDoc} */
	    @Override
	    public void producersConnected(final Wire[] wires) {
	        this.wireSupport.producersConnected(wires);
	    }
	    
	    /** {@inheritDoc} */
	    @Override
	    public void updated(final Wire wire, final Object value) {
	        this.wireSupport.updated(wire, value);
	    }
	
	   
	    /** {@inheritDoc} */
	    @Override
	    public void consumersConnected(final Wire[] wires) {
	        this.wireSupport.consumersConnected(wires);
	    }
	    
	    /** {@inheritDoc} */
	    @Override
	    public Object polled(final Wire wires) {
	        return this.wireSupport.polled(wires);
	    }
	    
	    private void emitEnvelope(WireEnvelope wireEnvelope) {
	    	
	    	logger.info("Decorator: Sending Envelope");	    	
	    	this.wireSupport.emit(wireEnvelope.getRecords());
	    	
	    	
	    }
	    
	    
	    //UTILITIES ----> EXTRACT PROPERTIES
	    
	    private String getDeviceId() {
	    	String device_id = "";
	    	
	    	final Object configuredId = this.properties.get(DEVICE_ID);
	    	if(nonNull(configuredId) && configuredId instanceof String) {
	    		device_id=String.valueOf(configuredId);
	    	}
	    	return device_id;
	    }
	    
	    private String getDeviceDesc() {
	    	String device_desc = "";
	    	
	    	final Object configuredId = this.properties.get(DEVICE_DESC);
	    	if(nonNull(configuredId) && configuredId instanceof String) {
	    		device_desc=String.valueOf(configuredId);
	    	}
	    	return device_desc;
	    }
	    
	    private boolean getPositionEn() {
	    	boolean position_en= false;
	    	
	    	final Object configuredEn = this.properties.get(POSITION_EN);
	    	if(nonNull(configuredEn) && configuredEn instanceof Boolean) {
	    		position_en=(boolean) configuredEn;
	    	}
	    	return position_en;
	    	
	    }
	    
	    private double getDeviceLat() {
	    	double device_lat = 0 ;
	    	
	    	final Object configuredLat = this.properties.get(DEVICE_LAT);
	    	if(nonNull(configuredLat) && configuredLat instanceof Double) {	    		
	    		device_lat= (double) configuredLat;	    		
	    	}
	    	return device_lat;
	    }
	    
	    private Double getDeviceLong() {
	    	double device_long = 0 ;
	    	
	    	final Object configuredLong = this.properties.get(DEVICE_LONG);
	    	if(nonNull(configuredLong) && configuredLong instanceof Double) {
	    		device_long=(Double) configuredLong;
	    	}
	    	return device_long;
	    }
	    
}
