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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ArchiverUtils {
    //======================================================================
    /**
     * Get the subscriber list from configurator
     * @return the subscriber list
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    public static String[] getSubscriberList() throws DevFailed {
        DeviceAttribute deviceAttribute = Utils.getConfiguratorProxy().read_attribute("ArchiverList");
        //  Check if list is coherent.
        //  If no archiver, one could be in attribute only with tango hots
        // (e.g.: tango://tgHost.dsds.dd:10000/)
        String[] archiverNames = deviceAttribute.extractStringArray();
        if (archiverNames.length==1) {
            String archiverName = archiverNames[0];
            StringTokenizer stk=new StringTokenizer(archiverName, "/");
            if (archiverName.startsWith("tango://")) {
                if (stk.countTokens()<5)  {
                    return new String[0];
                }
            }
            else {
                if (stk.countTokens()<2) {
                    return new String[0];
                }
            }
        }
        return archiverNames;
    }
    //======================================================================
    /**
     * Get the attribute (Started or stopped) for specified subscriber
     * @param subscriberProxy    specified subscriber
     * @param attrType          "Started" or "Stopped"
     * @return the attribute (Started or stopped) for specified subscriber
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    public static String[] getAttributeList(DeviceProxy subscriberProxy, String attrType) throws DevFailed {
        DeviceAttribute deviceAttribute = subscriberProxy.read_attribute("Attribute" + attrType + "List");
        return StringComparator.sortArray(deviceAttribute.extractStringArray());
    }
    //======================================================================
    /**
     * read attributes for specified subscriber
     * @param subscriberProxy   specified subscriber
     * @param attributeNames    specified attributes
     * @return the values for specified attribute on specified subscriber
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    public static List<String[]> readStringAttributes(DeviceProxy subscriberProxy,
                                                      String[] attributeNames) throws DevFailed {
        DeviceAttribute[] deviceAttributes = subscriberProxy.read_attribute(attributeNames);
        ArrayList<String[]> list = new ArrayList<>();
        for (DeviceAttribute deviceAttribute : deviceAttributes) {
            if (deviceAttribute.hasFailed())
                list.add(new String[0]);
            else
                list.add(deviceAttribute.extractStringArray());
        }
        return list;
    }
    //======================================================================
    /**
     * Try to lock the device. If already done, wait one second and retry.
     * If it still locked, throws a DevFailed
     * @param proxy    the specified device proxy to be locked.
     * @throws DevFailed if still locked after two tries.
     */
    //======================================================================
    public static void lockDevice(DeviceProxy proxy) throws DevFailed {
        //  If already locked, retry one time after a while
        boolean ok = false;
        for (int oneTry=0 ; !ok && oneTry<2 ; oneTry++) {
            try {
                proxy.lock();
                ok = true;
            }
            catch (DevFailed e) {
                System.err.println(e.errors[0].desc);
                if (e.errors[0].reason.equals("API_DeviceLocked")) {
                    try { Thread.sleep(1000); } catch (InterruptedException ex) { /* */ }
                }
                else
                    throw e;
            }
        }
        System.out.println(proxy.get_name() + " locked");
    }
    //======================================================================
    /*
     * Add an attribute to specified subscriber with default strategy.
     * @param configureProxy configurator device proxy
     * @param subscriberName specified subscriber
     * @param attributeName  specified attribute name
     * @param pushedByCode true if the event could be pushed by device code
     * @param lockIt true if the configurator device must be locked or false if already done.
     * @throws DevFailed in case of read device failed.
     *
    //======================================================================
    public static void addAttribute(DeviceProxy configureProxy,
                                            String subscriberName,
                                            String attributeName,
                                            boolean pushedByCode,
                                            boolean lockIt) throws DevFailed {
        addAttribute(configureProxy, subscriberName, new HdbAttribute(attributeName, pushedByCode, false), lockIt);
    }*/
    //======================================================================
    /**
     * Add an attribute to specified subscriber
     * @param configureProxy configurator device proxy
     * @param subscriberName specified subscriber
     * @param hdbAttribute attribute to be added
     * @param lockIt true if the configurator device must be locked or false if already done.
     * @throws DevFailed in case of read device failed.
     */
    //======================================================================
    public static void addAttribute(DeviceProxy configureProxy,
                                    String subscriberName,
                                    HdbAttribute hdbAttribute,
                                    boolean lockIt) throws DevFailed {
        //  At first lock the device if needed
        if (lockIt) {
            lockDevice(configureProxy);
        }
        if (Utils.isTraceMode()) {
            System.out.println("Add attribute:");
            System.out.println("  - Archiver:\t" + subscriberName);
            System.out.println("  - Attribute:\t" + hdbAttribute.getName());
            System.out.println("  - Strategy:\t" + hdbAttribute.strategyToString());
            System.out.println("  - Push evt:\t" + hdbAttribute.isPushedByCode());
            System.out.println("  - TTL:     \t" + hdbAttribute.getTtlString());
        }
        try {
            //  Configure attributes
            DeviceAttribute[] deviceAttributeList = new DeviceAttribute[5];
            int i=0;
            deviceAttributeList[i++] = new DeviceAttribute("SetAttributeName",   hdbAttribute.getName());
            deviceAttributeList[i++] = new DeviceAttribute("SetArchiver",        subscriberName);
            deviceAttributeList[i++] = new DeviceAttribute("SetStrategy",        hdbAttribute.strategyToString());
            deviceAttributeList[i++] = new DeviceAttribute("SetCodePushedEvent", hdbAttribute.isPushedByCode());
            deviceAttributeList[i] = new DeviceAttribute("SetTTL");
            deviceAttributeList[i].insert_ul(hdbAttribute.getTTL());

            configureProxy.write_attribute(deviceAttributeList);

            //  And send the command to add the attribute.
            configureProxy.command_inout("AttributeAdd");

        }
        catch (DevFailed e) {
            //  Unlock the device before rethrow
            if (lockIt) {
                configureProxy.unlock();
                System.out.println(configureProxy.get_name() + " unlocked");
            }
            throw e;
        }
        //  Unlock the configurator if needed
        if (lockIt) {
            configureProxy.unlock();
            System.out.println(configureProxy.get_name() + " unlocked");
        }
    }
    //======================================================================
    /**
     * Use AttributeAssign to move an attribute to another subscriber
     * @param managerProxy   manager proxy
     * @param attribute      attribute
     * @param subscriberName target subscriber name
     * @throws DevFailed in case of call device failed.
     */
    //======================================================================
    public static void moveAttribute(DeviceProxy managerProxy,
                                     HdbAttribute attribute,
                                     String subscriberName) throws DevFailed {
        // Assign to specified subscriber
        DeviceData  argIn = new DeviceData();
         argIn.insert(new String[] { attribute.getName(),
                 subscriberName, attribute.strategyToString(), Long.toString(attribute.getTTL()) });
        System.out.println(subscriberName + ":\n" + attribute.getName() + " : " + attribute.strategyToString());
        managerProxy.command_inout("AttributeAssign", argIn);
    }
    //======================================================================
    /**
     * Start the archiving for specified attribute on specified subscriber
     * @param subscriberProxy specified subscriber
     * @param attributeName  specified attribute name
     * @throws DevFailed in case of call device failed.
     */
    //======================================================================
    public static void startAttribute(DeviceProxy subscriberProxy, String attributeName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(attributeName);
        subscriberProxy.command_inout("AttributeStart", argIn);
    }
    //======================================================================
    /**
     * Stop the archiving for specified attribute on specified subscriber
     * @param subscriberProxy specified subscriber
     * @param attributeName  specified attribute name
     * @throws DevFailed in case of call device failed.
     */
    //======================================================================
    public static void stopAttribute(DeviceProxy subscriberProxy, String attributeName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(attributeName);
        subscriberProxy.command_inout("AttributeStop", argIn);
    }
    //======================================================================
    /**
     * Pause the archiving for specified attribute on specified subscriber
     * @param subscriberProxy specified subscriber
     * @param attributeName  specified attribute name
     * @throws DevFailed in case of call device failed.
     */
    //======================================================================
    public static void pauseAttribute(DeviceProxy subscriberProxy, String attributeName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(attributeName);
        subscriberProxy.command_inout("AttributePause", argIn);
    }
    //======================================================================
    /**
     * remove specified attribute on specified subscriber
     * @param managerProxy  manager proxy
     * @param attributeName specified attribute name
     * @throws DevFailed in case of call device failed.
     */
    //======================================================================
    public static void removeAttribute(DeviceProxy managerProxy, String attributeName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(attributeName);
        managerProxy.command_inout("AttributeRemove", argIn);
    }
    //======================================================================
    /**
     * Check if the specified attribute is already assign to a subscriber.
     * If yes returns the subscriber name, null otherwise
     * @param configurator     the configurator device proxy.
     * @param attributeName    specified attribute name
     * @return the subscriber name or null if attribute not assign
     * @throws DevFailed in case of connection failed.
     */
    //======================================================================
    public static String getArchiver(DeviceProxy configurator, String attributeName) throws DevFailed {
        DeviceData  argIn = new DeviceData();
        argIn.insert(TangoUtils.fullName(attributeName));
        DeviceData argOut = configurator.command_inout("AttributeGetArchiver", argIn);
        return argOut.extractString();
    }
    //======================================================================
    //======================================================================
    public static String getAttributeStrategy(String attributeName) throws DevFailed {
        DeviceData argIn = new DeviceData();
        argIn.insert(attributeName);
        DeviceData argOut = Utils.getConfiguratorProxy().command_inout("GetAttributeStrategy", argIn);
        return argOut.extractString();
    }
    //======================================================================
    //======================================================================
}
