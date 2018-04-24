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
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;


//======================================================
/**
 * A class defining a Hash Table subscriber device name
 * and labels.
 * Key could be device name or its labels
 */
//======================================================
public class SubscriberMap {
    private List<String> labelList = new ArrayList<>();
    private Hashtable<String, Subscriber> label2device = new Hashtable<>();
    private Hashtable<String, String> deviceName2label = new Hashtable<>();
    private List<String> tangoHostList = new ArrayList<>();
    private DeviceProxy configuratorProxy;
    //======================================================
    //======================================================
    public SubscriberMap(String  configuratorDeviceName) throws DevFailed {
        this(new DeviceProxy(configuratorDeviceName));
    }
    //======================================================
    //======================================================
    public SubscriberMap(DeviceProxy configuratorProxy) throws DevFailed {
        this.configuratorProxy = configuratorProxy;
        //  Get Subscriber labels
        List<String[]> labels = TangoUtils.getSubscriberLabels();
        //  Get Subscriber deviceName
        String[] subscriberNames = ArchiverUtils.getSubscriberList();

        String theSubscriberName = System.getenv("Subscriber");
        if (theSubscriberName==null || theSubscriberName.isEmpty()) {
            //  Build all subscribers
            for (String subscriberName : subscriberNames) {
                SplashUtils.getInstance().increaseSplashProgressForLoop(
                        subscriberNames.length, "Building object " + subscriberName);
                put(subscriberName, labels, configuratorProxy);
            }
            StringComparator.sort(labelList);
        }
        else {
            // keep only this one
            for (String[] array : labels) {
                //  Check device name and the on label
                if (array[0].equalsIgnoreCase(theSubscriberName))
                    put(array[0], labels, configuratorProxy);
                else if (array[1].equalsIgnoreCase(theSubscriberName))
                    put(array[0], labels, configuratorProxy);
            }
        }
    }
    //======================================================
    /**
     * Build the map for specified subscribers
     * @param list a list of couple [subscriber, manager] names
     * @throws DevFailed if database read failed
     */
    //======================================================
    public SubscriberMap(List<String[]> list) throws DevFailed {
        //  Get Subscriber labels
        List<String[]> labels = TangoUtils.getSubscriberLabels();

        String theSubscriberName = System.getenv("Subscriber");
        if (theSubscriberName==null || theSubscriberName.isEmpty()) {
            //  Build all subscribers
            for (String[] strings : list) {
                SplashUtils.getInstance().increaseSplashProgressForLoop(
                        list.size(), "Building object " + strings[0]);
                put(strings[0], labels, new DeviceProxy(strings[1]));
            }
            StringComparator.sort(labelList);
        }
    }
    //======================================================
    //======================================================
    public DeviceProxy getConfiguratorProxy() {
        return configuratorProxy;
    }
    //======================================================
    //======================================================
    public void add(Subscriber subscriber) {
        labelList.add(subscriber.getLabel());
        label2device.put(subscriber.getLabel(), subscriber);
        deviceName2label.put(subscriber.getName(), subscriber.getLabel());
    }
    //======================================================
    //======================================================
    private void put(String deviceName, List<String[]> labels, DeviceProxy managerProxy) throws DevFailed {
        try {
            boolean found = false;
            //  Manage full device name
            String tgHost = "";
            if (deviceName.startsWith("tango://")) {
                int index = deviceName.indexOf('/', "tango://".length());
                tgHost = deviceName.substring(0, index) + '/';
            }
            for (String[] label : labels) {
                if (label.length > 1) {
                    String devName = tgHost + label[0].toLowerCase();
                    if (deviceName.toLowerCase().equals(devName)) {
                        label2device.put(label[1], new Subscriber(deviceName, label[1], managerProxy));
                        labelList.add(label[1]);
                        deviceName2label.put(deviceName, label[1]);
                        found = true;
                    }
                } else
                    System.err.println("Syntax problem in \'SubscriberLabel\' property");
            }
            if (!found) {
                label2device.put(deviceName, new Subscriber(deviceName, deviceName, managerProxy));
                labelList.add(deviceName);  //  label is device name
                deviceName2label.put(deviceName, deviceName);
            }
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().showSplash(false);
            e.printStackTrace();
            ErrorPane.showErrorMessage(new JFrame(), e.getMessage(), e);
            SplashUtils.getInstance().showSplash(true);
        }
    }
    //======================================================
    //======================================================
    public int size() {
        return labelList.size();
    }
    //======================================================
    //======================================================
    public List<String> getLabelList() {
        return labelList;
    }
    //======================================================
    //======================================================
    public String getLabel(String deviceName) {
        return deviceName2label.get(deviceName);
    }
    //======================================================
    //======================================================
    public Subscriber getSubscriberByLabel(String label) throws DevFailed {
        Subscriber subscriber = label2device.get(label);
        if (subscriber==null)
            Except.throw_exception("NO_ARCHIVER",
                    "Subscriber \"" + label + "\" not found !");
        return subscriber;
    }
    //======================================================
    //======================================================
    @SuppressWarnings("unused")
    public Subscriber getSubscriberByDevice(String deviceName) throws DevFailed {
        String label = getLabel(deviceName);
        if (label==null)
            Except.throw_exception("NO_ARCHIVER",
                    "Subscriber \"" + deviceName + "\" not found !");
        return getSubscriberByLabel(label);
    }
    //======================================================
    //======================================================
    public List<Subscriber> getSubscriberList() {
        Collection<Subscriber> collection = label2device.values();
        return new ArrayList<>(collection);
    }
    //======================================================
    //======================================================
    @SuppressWarnings("unused")
    public void updateStrategies() throws DevFailed {
        Collection<Subscriber> collection = label2device.values();
        for (Subscriber subscriber : collection)
            subscriber.updateStrategy();
    }
    //======================================================
    //======================================================
    public List<String> getTangoHostList() {
        if (tangoHostList.isEmpty()) {
            Collection<Subscriber> collection = label2device.values();
            for (Subscriber subscriber : collection) {
                List<String> csList = subscriber.getTangoHostList();
                for (String cs : csList)
                    if (!(tangoHostList).contains(cs))
                        tangoHostList.add(cs);
            }
        }
        return tangoHostList;
    }
    //======================================================
    //======================================================
}
