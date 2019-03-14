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
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


public class TangoUtils {
    private static boolean ttlRead = false;
    private static long defaultTTL = 31; // days
    public static final String deviceHeader = "tango://";
    //======================================================================
    //======================================================================
    public static String getDefaultTangoHost() throws DevFailed {
        //return ApiUtil.get_db_obj().get_tango_host();
        return getTangoHost(new TangoUrl().getTangoHost());
    }
    //======================================================================
    //======================================================================
    public static String getEventTangoHost() throws DevFailed {
        String eventTangoHost = System.getProperty("EVENT_TANGO_HOST");
        if (eventTangoHost==null)
            eventTangoHost = System.getenv("EVENT_TANGO_HOST");
        if (eventTangoHost==null || eventTangoHost.isEmpty()) {
            return getTangoHost(ApiUtil.getTangoHost());
        }
        else
            return getTangoHost(eventTangoHost.toLowerCase());
    }
    //======================================================================
    //======================================================================
    public static String getTangoHost(String tangoHost) throws DevFailed {

        //  Check syntax
        int p = tangoHost.indexOf('.');
        if (p<0)
            p = tangoHost.indexOf(':');
        if (p<0)
            Except.throw_exception("SyntaxError", "Bad syntax for TANGO_HOST (host:port)");

        //  Check if it is a TANGO_HOST
        String newTangoHost = ApiUtil.get_db_obj(tangoHost).get_tango_host();

        //  Check if alias
        String hostName = tangoHost.substring(0, p);
        if (newTangoHost.startsWith(hostName + '.'))
            return newTangoHost;    //  Not an alias
        else {
            //  substitute alias to real name
            p = newTangoHost.indexOf('.');
            if (p>0) {
                //  With FQDN, can add alias to FQDN
                return hostName + newTangoHost.substring(p);
            }
            else {
                try {
                    //  Get FQDN and append to alias name
                    String alias = InetAddress.getByName(hostName).getCanonicalHostName();
                    //System.out.println(hostName);
                    int idx = alias.indexOf('.');
                    String fqdn = "";
                    if (idx>0) {
                        fqdn = alias.substring(idx);
                    }
                    p = tangoHost.indexOf(':');
                    return hostName+fqdn+tangoHost.substring(p);
                }
                catch (UnknownHostException e) {
                    Except.throw_exception(e.toString(), e.toString());
                }
                return "";  //  Cannot occur
            }
        }
    }
    //======================================================================
    //======================================================================
    public static String fullName(String attributeName) throws DevFailed {
        if (attributeName.startsWith(deviceHeader)) {
            return checkFQDN(attributeName);
        }
        return deviceHeader + getDefaultTangoHost() +"/" + attributeName;
    }
    //======================================================================
    //======================================================================
    private static String  checkFQDN(String attributeName) {
        //  get tango host name
        int idx = attributeName.indexOf(':', deviceHeader.length());
        String host = attributeName.substring(deviceHeader.length(), idx);
        if (host.contains("."))
            return attributeName; // FQDN is inside

        //  get the canonical name and keep only FQDN
        try {
            String alias = InetAddress.getByName(host).getCanonicalHostName();
            int idx2 = alias.indexOf('.');
            String fqdn = "";
            if (idx2<0)
                System.err.println("Cannot parse FQDN !");
            else {
                fqdn = alias.substring(idx2);
                System.out.println("----------> " + fqdn);
            }
            return deviceHeader + host + fqdn + attributeName.substring(idx);
        }
        catch (UnknownHostException e) {
            System.err.println(e.toString());
            return attributeName;
        }
    }
    //======================================================================
    /**
     * Get a list of TANGO_HOST which have restricted access.
     * And compare with EVENT_TANGO_ACCESS to know if limited or not
     * @return true if access are limited
     * @throws DevFailed in case of database access failed
     */
    //======================================================================
    public static boolean hasLimitedAccess() throws DevFailed {
        String expert = System.getenv("EXPERT_MODE");
        if (expert != null && expert.equals("false")) {
            return true;
        }
        String eventTangoHost = getEventTangoHost();
        DbDatum datum = ApiUtil.get_db_obj().get_property("HDB++", "LimitedAccessTangoHosts");
        if (datum.is_empty())
            return false; // everybody has full access
        String[] tangoHosts = datum.extractStringArray();
        for (String tangoHost : tangoHosts) {
            if (eventTangoHost.equals(tangoHost) || eventTangoHost.startsWith(tangoHost))
                return true;
        }
        return false;
    }
    //======================================================================
    //======================================================================
    public static String fullName(String tangoHost, String name) {
        return deviceHeader + tangoHost +"/" + name;
    }
    //======================================================================
    //======================================================================
    public static String getOnlyTangoHost(final String fullName) {
        String tangoHost = fullName;
        if (fullName.startsWith(deviceHeader)) {
            int idx = fullName.indexOf('/', deviceHeader.length());
            tangoHost = fullName.substring(deviceHeader.length(), idx);
        }
        return tangoHost;
    }
    //======================================================================
    //======================================================================
    public static String getOnlyDeviceName(final String fullName) {
        String deviceName = fullName;
        if (fullName.startsWith(deviceHeader)) {
            int idx = fullName.indexOf('/', deviceHeader.length());
            deviceName = fullName.substring(idx+1);
        }
        return deviceName;
    }
    //======================================================================
    //======================================================================
    public static String[] getAttributeFields(String  attributeName) {
        if (attributeName.startsWith(deviceHeader)) {
            int idx = attributeName.indexOf('/', deviceHeader.length());
            attributeName = attributeName.substring(idx+1);
        }
        StringTokenizer stk = new StringTokenizer(attributeName, "/");
        String[]    fields = new String[4];
        fields[0] = stk.nextToken(); // domain
        fields[1] = stk.nextToken(); // family
        fields[2] = stk.nextToken(); // member
        fields[3] = stk.nextToken(); // attribute
        return  fields;
    }
    //======================================================================
    //======================================================================
    public static String[] getDomains(String tangoHost) throws DevFailed {
        return ApiUtil.get_db_obj(tangoHost).get_device_domain("*");
    }
    //======================================================================
    //======================================================================
    public static String[] getFamilies(String tangoHost, String domain) throws DevFailed {
        return ApiUtil.get_db_obj(tangoHost).get_device_family(domain + "/*");
    }
    //======================================================================
    //======================================================================
    public static String[] getMembers(String tangoHost, String path) throws DevFailed {
        return ApiUtil.get_db_obj(tangoHost).get_device_member(path + "/*");
    }
    //======================================================================
    //======================================================================
    public static String[] getDeviceAttributes(String tangoHost, String deviceName) throws DevFailed {
        return new DeviceProxy(fullName(tangoHost, deviceName)).get_attribute_list();
    }
    //======================================================================
    //======================================================================
    public static String getConfiguratorDeviceName() throws DevFailed {
        String    deviceName = System.getProperty("HdbManager");
        if (deviceName==null || deviceName.isEmpty()) {
            deviceName = System.getenv("HdbManager");
        }
        if (deviceName==null || deviceName.isEmpty())
            Except.throw_exception("DeviceNotDefined",
                    "No Configuration Manager Found in environment");
        return deviceName;
    }
    //======================================================================
    //======================================================================
    public static String getEventProperties(String tangoHost, String attributeName) throws DevFailed {

        AttributeProxy attributeProxy = new AttributeProxy(fullName(tangoHost, attributeName));
        int idlVersion = attributeProxy.get_idl_version();
        if (idlVersion < 3) {
            return ("Device_" + idlVersion + "Impl not supported.");
        }

        //	ok. Get config
        AttributeInfoEx info = attributeProxy.get_info_ex();
        StringBuilder   sb = new StringBuilder();
        if (info.events != null) {
            sb.append("Archive event properties:\n");
            if (info.events.arch_event != null) {
                sb.append(" abs_change: ").append(info.events.arch_event.abs_change).append("\n");
                sb.append(" rel_change: ").append(info.events.arch_event.rel_change).append("\n");
                sb.append(" period    : ").append(info.events.arch_event.period);
            } else {
                sb.append(" rel_change: ").append(TangoConst.Tango_AlrmValueNotSpec).append("\n");
                sb.append(" abs_change: ").append(TangoConst.Tango_AlrmValueNotSpec).append("\n");
                sb.append(" period    : ").append(TangoConst.Tango_AlrmValueNotSpec);
            }
        }
        return sb.toString();
    }
    //======================================================================
    //======================================================================
    public static String getAttPollingInfo(String tangoHost, String attributeName) throws DevFailed {
        String header = "Polled attribute name = ";
        //  Split in device and attribute name.
        String  deviceName = attributeName.substring(0, attributeName.lastIndexOf('/'));
        attributeName = attributeName.substring(attributeName.lastIndexOf('/')+1);
        // get the polling period for attributes
        String[] globalPollingStatus = new DeviceProxy(fullName(tangoHost, deviceName)).polling_status();

        //  And search for specified one
        for (String attPollingStatus : globalPollingStatus) {
            String name = attPollingStatus.substring(header.length(),
                    attPollingStatus.indexOf('\n'));
            if (name.toLowerCase().equals(attributeName.toLowerCase())) {
                //return attPollingStatus;
                //  Get Only polling period
                int start = attPollingStatus.indexOf("Polling period");
                if (start>0) {
                    int end = attPollingStatus.indexOf('\n', start);
                    if (end>start)
                        return attPollingStatus.substring(start, end).trim();
                }
            }
        }
        return "Attribute Not Polled";
    }
    //======================================================================
    /**
     *
     * @return archiver label list (deviceName, Label).
     * @throws DevFailed if database connection failed
     */
    //======================================================================
    public static List<String[]> getSubscriberLabels() throws DevFailed {
        DbDatum datum = ApiUtil.get_db_obj().get_property("HdbConfigurator", "ArchiverLabels");
        List<String[]> labels = new ArrayList<>();
        if (datum.is_empty())
            return labels;

        //  Get property contents and split
        String[]    lines = datum.extractStringArray();
        for (String line : lines) {
            StringTokenizer stk = new StringTokenizer(line, ":");
            if (stk.countTokens()>1) {
                labels.add(new String[]{
                        stk.nextToken().trim(),    //  device Name
                        stk.nextToken().trim(),    //  label
                });
            }
        }
        return labels;
    }
    //======================================================================
    /**
     * Write archiver label list (deviceName, Label) in data base.
     *
     * @throws DevFailed if database connection failed
     */
    //======================================================================
    public static void setSubscriberLabels(List<String[]> labels) throws DevFailed {
        String[] lines = new String[labels.size()];
        int i=0;
        for (String[] label : labels) {
            lines[i++] = label[0] + ":  " + label[1];
        }
        DbDatum datum = new DbDatum("ArchiverLabels");
        datum.insert(lines);
        ApiUtil.get_db_obj().put_property("HdbConfigurator", new DbDatum[] {datum});
    }
    //======================================================================
    //======================================================================



    //======================================================================
    //======================================================================
    public static String[] getFilteredDeviceField(int fieldIndex, String header, String[] deviceNames) {
        if (header==null) header = "";
        if (!header.isEmpty() && !header.endsWith("/"))
            header += "/";
        //  For each device name
        List<String>   fields = new ArrayList<>();
        for (String deviceName : deviceNames) {
            //  Start with header ?
            if (deviceName.startsWith(header)) {
                StringTokenizer stk = new StringTokenizer(deviceName, "/");
                //  get specified field and check if not already added.
                String  field = getToken(stk, fieldIndex);
                if (mustBeAdded(field, fields))
                    fields.add(field);
            }
        }
        String[] array = new String[fields.size()];
        for (int i=0 ; i<fields.size() ; i++)
            array[i] = fields.get(i);
        return array;
    }
    //======================================================================
    //======================================================================
    private static boolean mustBeAdded(String str, List<String> list) {
        if (str==null)
            return false;
        for (String s : list) {
            if (s.toLowerCase().equals(str.toLowerCase()))
                return false;
        }
        //  Not found
        return true;
    }
    //======================================================================
    //======================================================================
    private static String getToken(StringTokenizer stk, int index) {
        String token = null;
        for (int i=0 ; stk.hasMoreTokens() && i<=index ; i++) {
            String str = stk.nextToken();
            if (i==index)
                token = str;
        }
        return token;
    }
    //======================================================================
    //======================================================================
    public static String[] getFilteredDeviceNames(String tangoHost, String wildcard) throws DevFailed {
        return ApiUtil.get_db_obj(tangoHost).get_device_list(wildcard);
    }
    //======================================================================
    //======================================================================
    public static void testDevice(Component parent, String deviceName) throws DevFailed {
        JDialog dialog;
        if (parent instanceof JFrame)
            dialog = new JDialog((JFrame)parent, false);
        else
            dialog = new JDialog((JDialog)parent, false);
        dialog.setTitle(deviceName + " Device Panel");
        dialog.setContentPane(new jive.ExecDev(deviceName));
        dialog.pack();
        ATKGraphicsUtils.centerDialog(dialog);
        dialog.setVisible(true);
    }
    //======================================================================
    //======================================================================
    public static String getArchiveName(DeviceProxy deviceProxy) {
        try {
            DbDatum datum = deviceProxy.get_property("ArchiveName");
            if (datum.is_empty())
                return "";
            else
                return datum.extractString();
        }
        catch (DevFailed e) {
            System.err.println(e.errors[0].desc);
            return "";
        }
    }
    //======================================================================
    //======================================================================
    public static List<String> getDefaultTangoHostList() throws DevFailed {
        List<String>   list = new ArrayList<>();
        Database database = ApiUtil.get_db_obj();
        String[]    tangoHosts = database.getPossibleTangoHosts();
        Collections.addAll(list, tangoHosts);
        return list;
    }
    //======================================================================
    //======================================================================
    public static String[] getServersForClass(String className) throws DevFailed {
        String mysqlCommand = "server from device where class=\"" + className + "\"";
        DeviceData argIn = new DeviceData();
        argIn.insert(mysqlCommand);
        System.out.println(mysqlCommand);
        DeviceData argOut = ApiUtil.get_db_obj().command_inout("DbMySqlSelect", argIn);

        DevVarLongStringArray lsa = argOut.extractLongStringArray();
        if (lsa.svalue.length>0)
            return lsa.svalue;
        else
            return new String[0];
    }
    //======================================================================
    //======================================================================
    public static String getServerNameForClass(String className) throws DevFailed {
        String[] servers = getServersForClass(className);
        if (servers.length==0)
            return null;
        return servers[0].substring(0, servers[0].indexOf('/'));
    }
    //===============================================================
    //===============================================================
    public static long getDefaultTTL() {
        if(!ttlRead) {
            try  {
                DbDatum datum = ApiUtil.get_db_obj().get_property("HdbConfigurator", "DefaultTTL");
                if (!datum.is_empty())
                    defaultTTL = datum.extractLong();
                ttlRead = true;
            }
            catch (DevFailed e) {
                System.err.println(e.errors[0].desc);
            }
        }
        return defaultTTL;
    }
    //======================================================================
    //======================================================================



    //======================================================================
    //======================================================================
    public static void main(String[] args) {
        try {
            List<String>   list = TangoUtils.getDefaultTangoHostList();
            for (String s : list)
                System.out.println(s);
            String tangoHost = TangoUtils.getOnlyTangoHost("tango://hal:2001/a/b/c/d");
            System.out.println(tangoHost);
         }
        catch (DevFailed e) {
            Except.print_exception(e);
        }
    }
    //======================================================================
    //======================================================================
}
