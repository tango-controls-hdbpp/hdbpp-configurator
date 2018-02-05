//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision:  $
//
// $Log:  $
//
//-======================================================================


package org.tango.hdb_configurator.common;

import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.ITangoChangeListener;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoEventsAdapter;
import fr.esrf.TangoDs.TangoConst;
import org.tango.hdb_configurator.configurator.ManageAttributes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * This class is a model for a subscriber device.
 * It inherits from a DeviceProxy on the device
 * It manage 3 lists of attributes (started, paused and stopped)
 */
//======================================================
//======================================================
@SuppressWarnings("WeakerAccess")
public class Subscriber extends DeviceProxy {
    public String name;
    protected String label;
    protected String startedFilter;
    protected String stoppedFilter;
    protected String pausedFilter;
    protected List<HdbAttribute> startedAttributes = new ArrayList<>();
    protected List<HdbAttribute> stoppedAttributes = new ArrayList<>();
    protected List<HdbAttribute> pausedAttributes  = new ArrayList<>();
    /** a map <attributeName, HdbAttribute> */
    protected Hashtable<String, HdbAttribute> hdbAttributeMap = new Hashtable<>();
    private Strategy strategy;
    private boolean faulty = false;
    private DeviceProxy configuratorProxy;


    public static final int ATTRIBUTE_STARTED = 0;
    public static final int ATTRIBUTE_PAUSED  = 1;
    public static final int ATTRIBUTE_STOPPED = 2;
    public static final String CLASS_NAME = "HdbEventSubscriber";
    //======================================================
    //======================================================
    public Subscriber(String deviceName, String label, DeviceProxy configuratorProxy) throws DevFailed {
        super(deviceName.toLowerCase());
        this.name  = deviceName.toLowerCase();
        this.label = label;
        this.configuratorProxy = configuratorProxy;
        startedFilter = "*/*/*/*/*";
        stoppedFilter = "*/*/*/*/*";
        pausedFilter  = "*/*/*/*/*";
        updateStrategy();

        //  Subscribe to attribute lists events (one per table started, paused and stopped)
        TangoEventsAdapter  adapter = new TangoEventsAdapter(this);
        ChangeEventListener changeListener = new ChangeEventListener();
        adapter.addTangoChangeListener(changeListener,
                "AttributeList", TangoConst.NOT_STATELESS);
        adapter.addTangoChangeListener(changeListener,
                "AttributeStrategyList", TangoConst.NOT_STATELESS);
        if (System.getenv("NO_TTL")==null)
            adapter.addTangoChangeListener(changeListener,
                    "AttributeTTLList", TangoConst.NOT_STATELESS);
        adapter.addTangoChangeListener(changeListener,
                "AttributeStartedList", TangoConst.NOT_STATELESS);
        adapter.addTangoChangeListener(changeListener,
                "AttributeStoppedList", TangoConst.NOT_STATELESS);
        adapter.addTangoChangeListener(changeListener,
                "AttributePausedList",  TangoConst.NOT_STATELESS);
    }
    //======================================================
    //======================================================
    public DeviceProxy getConfiguratorProxy() {
        return configuratorProxy;
    }
    //======================================================
    //======================================================
    public void setStrategy(Strategy strategy) {
        if (this.strategy!=strategy) {
            this.strategy.clear();
            for (Context context : strategy)
                this.strategy.add(new Context(
                        context.getName(), context.getDescription(), context.isDefault()));
        }
    }
    //======================================================
    //======================================================
    public Strategy getStrategy() {
        return strategy;
    }
    //======================================================
    //======================================================
    public void updateStrategy() throws DevFailed {
        strategy = Strategy.getContextsFromDB(this);
    }
    //======================================================
    //======================================================
    public String getName() {
        return name;
    }
    //======================================================
    //======================================================
    public String getLabel() {
        return label;
    }
    //======================================================
    //======================================================
    public String getStartedFilter() {
        return startedFilter;
    }
    //======================================================
    //======================================================
    public void setStartedFilter(String startedFilter) {
        this.startedFilter = startedFilter;
    }
    //======================================================
    //======================================================
    public String getStoppedFilter() {
        return stoppedFilter;
    }
    //======================================================
    //======================================================
    public void setStoppedFilter(String stoppedFilter) {
        this.stoppedFilter = stoppedFilter;
    }
    //======================================================
    //======================================================
    public String getPausedFilter() {
        return pausedFilter;
    }
    //======================================================
    //======================================================
    public void setPausedFilter(String pausedFilter) {
        this.pausedFilter = pausedFilter;
    }
    //=======================================================
    //=======================================================
    public long getStatisticsResetTime() throws DevFailed {
        DeviceAttribute attribute = read_attribute("StatisticsResetTime");
        if (attribute.hasFailed())
            return 0;
        double nbSeconds = attribute.extractDouble();
        long nbMillis = (long) nbSeconds*1000;
        long now = System.currentTimeMillis();
        return now - nbMillis;
    }
    //=======================================================
    //=======================================================
    public void setContext(String context) throws DevFailed {
        DeviceAttribute deviceAttribute = new DeviceAttribute("Context");
        deviceAttribute.insert(context);
        write_attribute(deviceAttribute);
    }
    //=======================================================
    //=======================================================
    public String getContext() throws DevFailed {
        DeviceAttribute deviceAttribute = read_attribute("Context");
        return deviceAttribute.extractString();
    }
    //=======================================================
    //=======================================================
    public boolean hasFaultyAttribute() {
        try {
            DeviceAttribute attribute = read_attribute("AttributeNokList");
            return !attribute.hasFailed() && attribute.extractStringArray().length != 0;
        }
        catch (DevFailed e) {
            return false;
        }
    }
    //=======================================================
    //=======================================================
    public int getStatisticsTimeWindow() throws DevFailed {
        int value = 2*3600;
        String propertyName = "StatisticsTimeWindow";
        //  Check class property
        DbDatum datum = new DbClass(CLASS_NAME).get_property(propertyName);
        if (!datum.is_empty()) {
            //  Why sometimes value is "Not specified" ???
            try {
                 value = datum.extractLong();
            }
            catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }
        }
        //  Check device property
        datum = get_property(propertyName);
        if (!datum.is_empty()) {
            try {
                value = datum.extractLong();
            }
            catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }
        }
        return value;
    }
    //======================================================
    //======================================================
    List<String> getTangoHostList() {
        List<String> list = new ArrayList<>();
        try {
            String[] attributeList = ArchiverUtils.getAttributeList(this, "");
            for (String attributeName : attributeList) {
                String csName = TangoUtils.getOnlyTangoHost(attributeName);
                if (!list.contains(csName))
                    list.add(csName);
            }
        }
        catch (DevFailed e) {
            System.err.println(e.errors[0].desc);
            //  return an empty list
        }
        return list;
    }
    //======================================================
    //======================================================
    public String toString() {
        return name;
    }
    //======================================================
    //======================================================
    public boolean hasAttribute(int attributeState) {
        return !getAttributeList(attributeState, false).isEmpty();
    }
    //======================================================
    //======================================================
    public List<HdbAttribute> getAttributeList(int attributeState, boolean filtered) {
        switch (attributeState) {
            case ATTRIBUTE_STARTED:
                if (filtered)
                    return Utils.matchFilter(startedAttributes, startedFilter);
                else
                    return startedAttributes;
            case ATTRIBUTE_STOPPED:
                if (filtered)
                    return Utils.matchFilter(stoppedAttributes, stoppedFilter);
                else
                    return stoppedAttributes;
            case ATTRIBUTE_PAUSED:
                if (filtered)
                    return Utils.matchFilter(pausedAttributes, pausedFilter);
                else
                    return pausedAttributes;
        }
        return null; //"Unexpected type list"};
    }
    //======================================================================
    /**
     * Start the archiving for specified attribute on specified subscriber
     * @param attribute  specified attribute
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    public void startAttribute(HdbAttribute attribute) throws DevFailed {
        ManageAttributes.startAttribute(configuratorProxy, attribute);
    }
    //======================================================================
    /**
     * Stop the archiving for specified attribute on specified subscriber
     * @param attribute  specified attribute
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    @SuppressWarnings("unused")
    public  void stopAttribute(HdbAttribute attribute) throws DevFailed {
        ManageAttributes.stopAttribute(configuratorProxy, attribute);
    }
    //======================================================================
    /**
     * Pause the archiving for specified attribute on specified subscriber
     * @param attribute  specified attribute
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    @SuppressWarnings("unused")
    public  void pauseAttribute(HdbAttribute attribute) throws DevFailed {
        ManageAttributes.pauseAttribute(configuratorProxy, attribute);
    }
    //======================================================
    //======================================================
    public String getAttributeStatus(String attributeName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(attributeName);
        DeviceData  argOut = command_inout("AttributeStatus", argIn);
        return argOut.extractString();
    }
    //======================================================
    //======================================================
    public boolean isFaulty() {
        return faulty;
    }
    //======================================================
    /**
     * check if attribute strategies are compatible with the own strategy
     * @param attributeList attribute list to be checked
     * @return true if compatible
     */
    //======================================================
    public List<HdbAttribute> checkAttributeStrategies(List<HdbAttribute> attributeList) throws DevFailed {
        List<HdbAttribute> unCompatibleAttributes = new ArrayList<>();
        for (HdbAttribute attribute : attributeList) {
            //  For each used context
            for (Context attributeContext : attribute) {
                if (attributeContext.isUsed()) {
                    boolean found = false;
                    //  Check if exists in subscriber strategy
                    for (Context context : strategy) {
                        if (context.getName().equalsIgnoreCase(attributeContext.getName()))
                            found = true;
                    }
                    if (!found) {
                        HdbAttribute newAttribute =
                                new HdbAttribute(attribute.getName(), strategy);
                        //  Always
                        newAttribute.get(Strategy.ALWAYS_INDEX).setUsed(true);
                        unCompatibleAttributes.add(newAttribute);
                        break;
                    }
                }
            }
        }
        return unCompatibleAttributes;
    }
    //===============================================================
    //===============================================================
    public void setTTL(List<HdbAttribute> attributeList, long ttl) throws DevFailed {
        for (HdbAttribute attribute : attributeList) {
            DeviceData argIn = new DeviceData();
            argIn.insert(new String[] { attribute.getName(), Long.toString(ttl)});
            command_inout("SetAttributeTTL", argIn);
        }
    }
    //===============================================================
    //===============================================================
    private boolean mustBeRepaint = false;
    public boolean needsRepaint() {
        boolean b = mustBeRepaint;
        mustBeRepaint = false;
        return b;
    }
    //===============================================================
    //===============================================================






    //=====================================================================
    /**
     * Change event listener
     */
    //=====================================================================
    public class ChangeEventListener implements ITangoChangeListener {
        //=================================================================
        private void setError(String message) {
            if (!faulty) {
                startedAttributes = new ArrayList<>();
                pausedAttributes = new ArrayList<>();
                stoppedAttributes = new ArrayList<>();
                startedAttributes.add(new HdbAttribute("!!! " + message));
                stoppedAttributes.add(new HdbAttribute("!!! " + message));
                pausedAttributes.add(new HdbAttribute("!!! " + message));
                faulty = true;
            }
        }
        //=================================================================
        public void change(TangoChangeEvent event) {
            try {
                //	Get the attribute value
                DeviceAttribute attribute = event.getValue();
                if (attribute.hasFailed()) {
                    setError(attribute.getErrStack()[0].desc);
                }
                else
                if (attribute.getQuality()==AttrQuality.ATTR_VALID) {
                    switch (attribute.getName()) {
                        //  Update the 3 lists of attributes (Started, paused and stop)
                        case "AttributeStartedList":
                            startedAttributes =
                                    buildHdbAttributeList(attribute.extractStringArray());
                            faulty = false;
                            break;
                        case "AttributeStoppedList":
                            stoppedAttributes =
                                    buildHdbAttributeList(attribute.extractStringArray());
                            faulty = false;
                            break;
                        case "AttributePausedList":
                            pausedAttributes =
                                    buildHdbAttributeList(attribute.extractStringArray());
                            faulty = false;
                            break;

                        // Update global HdbAttribute list
                        case "AttributeList":
                        case "AttributeStrategyList":
                        case "AttributeTTLList":
                            manageAttributeMap(attribute);
                            break;
                    }
                }
                else {
                    setError(attribute.getName() + " is invalid");
                }
            }
            catch (DevFailed e) {
                setError(e.errors[0].desc);
            }
            catch (Exception e) {
                e.printStackTrace();
                setError(e.getMessage());
            }
        }
        //=================================================================
    }
    //=====================================================================
    //=====================================================================


    private String[] attributeNames = new String[0];
    //=====================================================================
    //=====================================================================
    private void manageAttributeMap(DeviceAttribute deviceAttribute) throws DevFailed {
        switch (deviceAttribute.getName()) {
            case "AttributeList":
                //  Set Strategies
                this.attributeNames = deviceAttribute.extractStringArray();
                //  Create HdbAttributes
                for (String attributeName : attributeNames) {
                    HdbAttribute attribute = hdbAttributeMap.get(attributeName);
                    if (attribute == null) {
                        attribute = new HdbAttribute(attributeName);
                        hdbAttributeMap.put(attributeName, attribute);
                    }
                }
                break;

            case "AttributeStrategyList":
                String[] strategiesStr = deviceAttribute.extractStringArray();
                for (int i=0 ; i<attributeNames.length && i<strategiesStr.length ; i++) {
                    HdbAttribute attribute = hdbAttributeMap.get(attributeNames[i]);
                    if (attribute != null) {
                        //  remove existing strategy and replace by new one
                        attribute.clear();
                        attribute.setStrategy(strategy, strategiesStr[i]);
                    }
                }
                mustBeRepaint = true;
                break;

            case "AttributeTTLList":
                long[] ttlList = deviceAttribute.extractULongArray();
                for (int i=0 ; i<attributeNames.length && i<ttlList.length ; i++) {
                    HdbAttribute attribute = hdbAttributeMap.get(attributeNames[i]);
                    if (attribute != null) {
                        //  Set the TTL value
                        attribute.setTTL(ttlList[i]);
                    }
                }
                mustBeRepaint = true;
                break;
         }
    }
    //=====================================================================
    //=====================================================================
    private List<HdbAttribute> buildHdbAttributeList(String[] attributeNames) {
        List<HdbAttribute> list = new ArrayList<>();
        for (String attributeName : attributeNames) {
            HdbAttribute attribute = hdbAttributeMap.get(attributeName);
            if (attribute!=null)
                list.add(attribute);
        }
        return list;
    }
    //=====================================================================
    //=====================================================================
    public boolean manageAttribute(String attributeName) {
        return (hdbAttributeMap.get(attributeName)!=null);
    }
    //=====================================================================
    //=====================================================================
}

