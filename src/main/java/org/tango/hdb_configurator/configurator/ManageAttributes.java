//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,
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
// :  $
//
//-======================================================================

package org.tango.hdb_configurator.configurator;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.NamedDevFailed;
import fr.esrf.TangoDs.NamedDevFailedList;
import org.tango.hdb_configurator.common.*;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is able to add/start/stop a list of attributes to a subscriber
 *
 *  <br><br>
     <i>
     <font COLOR="#3b648b">   <!--- DeepSkyBlue4 --->
     //  Example of using the HDB++ configurator API </Font>
     <br> <br>
            package my_package;
            <br>
            <br>
    import org.Tango.hdb_configurator.configurator.HdbAttribute; <br>
    import org.Tango.hdb_configurator.configurator.ManageAttributes; <br>
    import fr.esrf.TangoDs.Except; <br>
    import fr.esrf.Tango.DevFailed; <br>
            <br>
    public class MyAttributeManagement {
        <ul>
        public static void main (String args[])  {
            <ul>
            try {
                <ul>
                <FONT COLOR="#3b648b">
                        //  Create a hdb attribute list<br>
                        //  These attributes are pushed by the device code<br>
                </Font>
                        List&lt;HdbAttribute&gt; hdbAttributes = new ArrayList&lt;HdbAttribute&gt;();<br>
                        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass12", true));<br>
                        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass14", true));<br>
                        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass15", true));<br>
                        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass16", true));<br>
                <br>
                <FONT COLOR="#3b648b">
                        // Add send these attributes to an event subscriber<br>
                </Font>
                        String archiver = "tango/hdb-es/vacuum";<br>
                        ManageAttributes.addAttributes(archiver, hdbAttributes);<br>
                </ul>
            }
            catch (DevFailed e) {
                <ul>
                        Except.print_exception(e);
                </ul>
            }
            </ul>
        }
        </ul>
    }
    </i>
    <br><br>
 *
 * @author verdier
 */

public class ManageAttributes {

    private static boolean display = false;
    //===============================================================
    //===============================================================
    public static void setDisplay(boolean b) {
        display = b;
    }
    //===============================================================
    /**
     *  Add a list of attributes to specified subscriber
     * @param configuratorProxy DeviceProxy on configurator manager device
     * @param subscriberName    specified subscriber device name
     * @param hdbAttributes     specified attribute to be added.
     * @throws DevFailed in case of bad subscriber name or connection failed.
     */
    //===============================================================
    public static void addAttributes(DeviceProxy configuratorProxy,
                                     String subscriberName,
                                     List<HdbAttribute> hdbAttributes) throws DevFailed {
        if (hdbAttributes.size()==0)
            return;
        if (display) {
            SplashUtils.getInstance().startSplash();
            SplashUtils.getInstance().setSplashProgress(1, "Adding attributes");
        }
        double step = 100.0/hdbAttributes.size();

        //  And lock configurator before adding attributes
        StringBuilder   errors = new StringBuilder();
        ArchiverUtils.lockDevice(configuratorProxy);
        int cnt = 1;
        for (HdbAttribute hdbAttribute : hdbAttributes) {
            if (display)
                SplashUtils.getInstance().setSplashProgress((int) (step*cnt++), "Adding "+hdbAttribute.getName());
            else
                System.out.println("Adding " + hdbAttribute.getName() + "\tto " + subscriberName);
            try {
                //  Try if device syntax ok
                new AttributeProxy(hdbAttribute.getName());
                //  Add it to archiver
                ArchiverUtils.addAttribute(configuratorProxy, subscriberName, hdbAttribute, false);
            }
            catch (DevFailed e) {
                errors.append(hdbAttribute.getName());
                if (e instanceof NamedDevFailedList) {
                    NamedDevFailedList devFailedList = (NamedDevFailedList) e;
                    for (int i=0 ; i<devFailedList.get_faulty_attr_nb() ; i++) {
                        NamedDevFailed namedDevFailed = devFailedList.elementAt(i);
                        errors.append("\n  (").append(namedDevFailed.err_stack[0].origin).append(" : ")
                                .append(namedDevFailed.err_stack[0].desc).append(")\n");
                    }
                }
                else
                    errors.append("\n  (").append(e.errors[0].origin).append(" : ")
                            .append(e.errors[0].desc).append(")\n");
            }
        }
        if (display)
            SplashUtils.getInstance().stopSplash();
        configuratorProxy.unlock();
        if (errors.length()>0)
            Except.throw_exception("AddingFailed", errors.toString());
    }
    //===============================================================
    /**
     * Start an attribute
     * @param attribute    specified attribute
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void startAttribute(DeviceProxy configuratorProxy, HdbAttribute attribute) throws DevFailed {
        List<HdbAttribute> attributes = new ArrayList<>();
        attributes.add(attribute);
        changeAttributeStates(configuratorProxy, attributes, Subscriber.ATTRIBUTE_STARTED);
    }
    //===============================================================
    /**
     * Start a list of attributes
     * @param attributes    specified attribute list
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void startAttributes(DeviceProxy configuratorProxy, List<HdbAttribute> attributes) throws DevFailed {
        changeAttributeStates(configuratorProxy, attributes, Subscriber.ATTRIBUTE_STARTED);
    }
    //===============================================================
    /**
     * Stop an attribute
     * @param attribute    specified attribute
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void stopAttribute(DeviceProxy configuratorProxy, HdbAttribute attribute) throws DevFailed {
        List<HdbAttribute> attributes = new ArrayList<>();
        attributes.add(attribute);
        changeAttributeStates(configuratorProxy, attributes, Subscriber.ATTRIBUTE_STOPPED);
    }
    //===============================================================
    /**
     * Stop a list of attributes
     * @param attributes    specified attribute list
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void stopAttributes(DeviceProxy configuratorProxy, List<HdbAttribute> attributes) throws DevFailed {
        changeAttributeStates(configuratorProxy, attributes, Subscriber.ATTRIBUTE_STOPPED);
    }
    //===============================================================
    /**
     * Pause an attribute
     * @param attribute    specified attribute
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void pauseAttribute(DeviceProxy configuratorProxy, HdbAttribute attribute) throws DevFailed {
        List<HdbAttribute> attributes = new ArrayList<>();
        attributes.add(attribute);
        changeAttributeStates(configuratorProxy, attributes, Subscriber.ATTRIBUTE_PAUSED);
    }
   //===============================================================
    /**
     * Pause a list of attributes
     * @param attributes    specified attribute list
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void pauseAttributes(DeviceProxy configuratorProxy, List<HdbAttribute> attributes) throws DevFailed {
        changeAttributeStates(configuratorProxy, attributes, Subscriber.ATTRIBUTE_PAUSED);
    }
    //===============================================================
    /**
     * Start/pause/Stop a list of attributes
     * @param attributes    specified attribute list
     * @param action Action to execute (start/pause/stop)
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    private static void changeAttributeStates(DeviceProxy configurator,
            List<HdbAttribute> attributes, int action) throws DevFailed {
        if (action<Subscriber.ATTRIBUTE_STARTED || action>Subscriber.ATTRIBUTE_STOPPED)
            Except.throw_exception("", "Action " + action + " Not Supported");
        String[] strAttributeStates = { "Staring ", " Pause ", "Stopping "};
        String strAction = strAttributeStates[action];

        if (display) {
            SplashUtils.getInstance().startSplash();
            SplashUtils.getInstance().setSplashProgress(10, strAction + "attributes");
        }
        int step = 90/attributes.size();
        if (step<1) step = 1;

        try {
            String  previous = null;
            DeviceProxy archiver = null;
            for (HdbAttribute attribute : attributes) {
                if (display)
                    SplashUtils.getInstance().increaseSplashProgress(step, strAction + attribute);
                else
                    System.out.println(strAction + attribute);
                //  Check if archiver is the same or another one.
                String  archiverName = ArchiverUtils.getArchiver(configurator, attribute.getName());
                if (archiver==null || !archiverName.equals(previous)) {
                    previous = archiverName;
                    archiver = new DeviceProxy(archiverName);
                }
                switch (action) {
                    case Subscriber.ATTRIBUTE_STARTED:
                        ArchiverUtils.startAttribute(configurator,
                                TangoUtils.fullName(attribute.getName()));
                        break;
                    case Subscriber.ATTRIBUTE_STOPPED:
                        ArchiverUtils.stopAttribute(configurator,
                                TangoUtils.fullName(attribute.getName()));
                        break;
                    case Subscriber.ATTRIBUTE_PAUSED:
                        ArchiverUtils.pauseAttribute(configurator,
                                TangoUtils.fullName(attribute.getName()));
                        break;
                }
            }
            if (display)
                SplashUtils.getInstance().stopSplash();
        }
        catch (DevFailed e) {
            if (display)
                SplashUtils.getInstance().stopSplash();
            throw e;
        }
    }
    //===============================================================
    /**
     * Remove a list of attributes to the subscriber
     * @param attributes    specified attribute list
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void removeAttributes(DeviceProxy configuratorProxy, HdbAttribute[] attributes) throws DevFailed {
        List<String> names = new ArrayList<>();
        for (HdbAttribute attribute : attributes)
            names.add(attribute.getName());
        removeAttributes(configuratorProxy, names);
    }
    //===============================================================
    /**
     * Remove a list of attributes to the subscriber
     * @param attributes    specified attribute list
     * @throws DevFailed in case of connection failed.
     */
    //===============================================================
    public static void removeAttributes(DeviceProxy configuratorProxy, List<String> attributes) throws DevFailed {
        if (display) {
            SplashUtils.getInstance().startSplash();
            SplashUtils.getInstance().setSplashProgress(10, "Removing attributes");
        }
        int step = 90/attributes.size();
        if (step<1) step = 1;

        try {
            for (String attribute : attributes) {
                if (display)
                    SplashUtils.getInstance().increaseSplashProgress(step, "Removing "+attribute);
                else
                    System.out.println("Removing " + attribute);
                ArchiverUtils.removeAttribute(configuratorProxy, TangoUtils.fullName(attribute));
            }
            if (display)
                SplashUtils.getInstance().stopSplash();
        }
        catch (DevFailed e) {
            if (display)
                SplashUtils.getInstance().stopSplash();
            throw e;
        }
    }
    //===============================================================
    //===============================================================
    private static DeviceProxy getSubscriber(DeviceProxy configurator, String subscriber) throws DevFailed {
        String[]  subscriberNames = ArchiverUtils.getSubscriberList();
        String fullName = TangoUtils.fullName(subscriber.toLowerCase());
        boolean found = false;
        for (String subscriberName : subscriberNames) {
            if (subscriberName.toLowerCase().equals(fullName)) {
                found = true;
                break;
            }
        }
        if (!found)
            Except.throw_exception("SubscriberNotExists",
                    "Subscriber \"" + subscriber + "\" is not managed by " + configurator.name());
        return new DeviceProxy(subscriber);
    }
    //===============================================================
    //===============================================================
    //===============================================================

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        /*

        List<String> attributes = new ArrayList<>();
        attributes.add("sr/v-rga/c1-cv6000/mass12");
        attributes.add("sr/v-rga/c1-cv6000/mass14");
        attributes.add("sr/v-rga/c1-cv6000/mass15");
        attributes.add("sr/v-rga/c1-cv6000/mass16");
        attributes.add("sr/v-rga/c1-cv6000/mass17");
        attributes.add("sr/v-rga/c1-cv6000/mass18");
        attributes.add("sr/v-rga/c1-cv6000/mass19");

        ArrayList<HdbAttribute> hdbAttributes = new ArrayList<>();
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass12", PUSHED_BY_CODE, START_ARCHIVING));
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass14", PUSHED_BY_CODE, START_ARCHIVING));
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass15", PUSHED_BY_CODE, START_ARCHIVING));
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass16", PUSHED_BY_CODE, START_ARCHIVING));
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass17", PUSHED_BY_CODE, START_ARCHIVING));
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass18", PUSHED_BY_CODE, START_ARCHIVING));
        hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass19", PUSHED_BY_CODE, START_ARCHIVING));

        try {
            ManageAttributes.addAttributes("hdb++/es/2", hdbAttributes);
            ManageAttributes.stopAttributes(hdbAttributes);
        } catch (DevFailed e) {
            Except.print_exception(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        System.exit(0);
    }
}
