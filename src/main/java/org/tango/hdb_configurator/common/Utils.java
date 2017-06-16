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
import jive3.MainPanel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class Utils {
    //private static SubscriberMap subscriberMap = null;
    private static Hashtable<String,SubscriberMap> subscriberTable = new Hashtable<>();
    //private static DeviceProxy  configuratorProxy = null;
    private static Hashtable<String, DeviceProxy>  configuratorProxyTable = new Hashtable<>();
    private static Utils instance = new Utils();
    private static final String DefaultImagePath = "/org/tango/hdb_configurator/img/";
    private static MainPanel jive = null;
    private static ImageIcon orangeBall = null;
    private static ImageIcon greenBall = null;
    private static final boolean trace =
            (System.getenv("trace")!=null && System.getenv("trace").equals("true"));
    public static final Color selectionBackground   = new Color(0xe0e0ff);
    public static final Color firstColumnBackground = new Color(0xe0e0e0);
    //======================================================================
    //======================================================================
    private Utils() {
    }
    //======================================================================
    //======================================================================
    public static Utils getInstance() {
        return instance;
    }
    //===============================================================
    //===============================================================
    public ImageIcon getIcon(String filename) throws DevFailed {
        java.net.URL url =
                getClass().getResource(DefaultImagePath + filename);
        if (url == null) {
            Except.throw_exception("FILE_NOT_FOUND",
                    "Icon file  " + filename + "  not found");
        }

        //noinspection ConstantConditions
        return new ImageIcon(url);
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(String filename, double ratio) throws DevFailed {
        ImageIcon icon = getIcon(filename);
        return getIcon(icon, ratio);
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(ImageIcon icon, double ratio) {
        if (icon != null) {
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            width = (int) (ratio * width);
            height = (int) (ratio * height);

            icon = new ImageIcon(
                    icon.getImage().getScaledInstance(
                            width, height, Image.SCALE_SMOOTH));
        }
        return icon;
    }
    //======================================================================
    //======================================================================
    public static ImageIcon getOrangeBall() throws DevFailed {
        if (orangeBall==null)
            orangeBall = getInstance().getIcon("orangeBall.gif");
        return orangeBall;
    }
    //======================================================================
    //======================================================================
    public static ImageIcon getGreenBall() throws DevFailed {
        if (greenBall==null)
            greenBall = getInstance().getIcon("greenBall.gif");
        return greenBall;
    }
    //======================================================================
    //======================================================================
    public static DeviceProxy getConfiguratorProxy() throws DevFailed {
        String configuratorDeviceName = TangoUtils.getConfiguratorDeviceName();
        DeviceProxy configuratorProxy = configuratorProxyTable.get(configuratorDeviceName);
        if (configuratorProxy==null) {
            configuratorProxy = new DeviceProxy(configuratorDeviceName);
            configuratorProxyTable.put(configuratorDeviceName, configuratorProxy);
        }
        return configuratorProxy;
    }
    //======================================================================
    //======================================================================
    public static SubscriberMap getSubscriberMap(String configuratorDeviceName) throws DevFailed {
        return getSubscriberMap(configuratorDeviceName, false);
    }
    //======================================================================
    //======================================================================
    public static SubscriberMap getSubscriberMap(String configuratorDeviceName, boolean reInit) throws DevFailed {
        SubscriberMap subscriberMap = subscriberTable.get(configuratorDeviceName);
        if (subscriberMap==null) {
            subscriberMap = new SubscriberMap(configuratorDeviceName);
            subscriberTable.put(configuratorDeviceName, subscriberMap);
        }
        if (reInit) {
            subscriberMap = new SubscriberMap(configuratorDeviceName);
        }
        return subscriberMap;
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    public static boolean isTraceMode() {
        return trace;
    }
    //===============================================================
    //===============================================================
    public static void startJiveForDevice(String deviceName) {
        //  Start jive and go to the device node
        if (jive==null) {
            jive = new MainPanel(false, false);
        }
        jive.setVisible(true);
        jive.goToDeviceNode(deviceName);
        System.out.println("Go to device node "+deviceName);
    }
    //===============================================================
    //===============================================================
    public Component startExternalApplication(JFrame parent, String className) throws DevFailed {
        return startExternalApplication(parent, className, null);
    }
    //===============================================================
    //===============================================================
    public Component startExternalApplication(JFrame parent, String className, Object parameter) throws DevFailed {

        try {
            //	Retrieve class object
            Class	_class = Class.forName(className);

            //	And build object
            Constructor[] constructors = _class.getDeclaredConstructors();
            for (Constructor constructor : constructors) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameter==null) {
                    if (parameterTypes.length==1 && parameterTypes[0]==JFrame.class){
                        Component component = (Component) constructor.newInstance(parent);
                        component.setVisible(true);
                        return component;
                    }
                }
                else {
                    if (parameterTypes.length==2 && parameterTypes[0]==JFrame.class){
                        if (parameterTypes[1]==String.class && parameter instanceof String) {
                            Component component = (Component) constructor.newInstance(parent, parameter);
                            component.setVisible(true);
                            return component;
                        }
                        else
                        if (parameterTypes[1]==String[].class && parameter instanceof String[]) {
                            Component component = (Component) constructor.newInstance(parent, parameter);
                            component.setVisible(true);
                            return component;
                        }
                    }
                }
            }
            throw new Exception("Cannot find constructor for " + className);
        }
        catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException   ite = (InvocationTargetException) e;
                Throwable   throwable = ite.getTargetException();
                System.err.println(throwable.getMessage());
                if (throwable instanceof DevFailed)
                    throw (DevFailed) throwable;
                else
                    Except.throw_exception(throwable.toString(), throwable.getMessage());
            }
            Except.throw_exception(e.toString(), e.toString());
        }
        return null;
    }
    //===============================================================
    /**
     * Open a file and return lines read.
     *
     * @param fileName file to be read.
     * @return the file content read as lines.
     * @throws DevFailed in case of failure during read file.
     */
    //===============================================================
    public static List<String> readFileLines(String fileName) throws DevFailed {
        List<String>   lines = new ArrayList<>();
        String code = readFile(fileName);
        StringTokenizer stringTokenizer = new StringTokenizer(code, "\n");
        while (stringTokenizer.hasMoreTokens())
            lines.add(stringTokenizer.nextToken());
        return lines;
    }
    //===============================================================
    /**
     * Open a file and return text read.
     *
     * @param fileName file to be read.
     * @return the file content read.
     * @throws DevFailed in case of failure during read file.
     */
    //===============================================================
    public static String readFile(String fileName) throws DevFailed {
        String str = "";
        try {
            FileInputStream fid = new FileInputStream(fileName);
            int nb = fid.available();
            byte[] inStr = new byte[nb];
            nb = fid.read(inStr);
            fid.close();

            if (nb > 0)
                str = new String(inStr);
        } catch (Exception e) {
            Except.throw_exception(e.getMessage(), e.toString());
        }
        return str;
    }
    //===============================================================
    //===============================================================
    public static void writeFile(String fileName, String code) throws DevFailed {
        try {
            FileOutputStream fid = new FileOutputStream(fileName);
            fid.write(code.getBytes());
            fid.close();
        } catch (Exception e) {
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //======================================================================
    //======================================================================
    public static List<HdbAttribute> matchFilter(List<HdbAttribute> attributes, String pattern) {
        List<HdbAttribute> hdbAttributes = new ArrayList<>();
        for (HdbAttribute attribute : attributes) {
            if (matches(attribute.getName().substring("tango://".length()), pattern)) {
                hdbAttributes.add(attribute);
            }
        }
        return hdbAttributes;
    }
    //======================================================================
    //======================================================================
    public static boolean matches(String attributeName, String pattern) {
        StringTokenizer stk = new StringTokenizer(attributeName, "/");
        List<String>   attributeTokens = new ArrayList<>();
        while (stk.hasMoreTokens())  attributeTokens.add(stk.nextToken());

        stk = new StringTokenizer(pattern, "/");
        List<String>   patternTokens = new ArrayList<>();
        while (stk.hasMoreTokens())  patternTokens.add(stk.nextToken());

        int index = 0;
        for (String attributeToken : attributeTokens) {
            if (index>patternTokens.size()-1)
                return true;
            String  patternToken = patternTokens.get(index++);
            if (!matchField(attributeToken, patternToken))
                return false;
        }

        return true;
    }
    //======================================================================
    //======================================================================
    private static boolean matchField(String field, String pattern) {
        if (pattern.equals("*"))
            return true;
        if (pattern.contains("*")) {
            if (pattern.startsWith("*")) {
                //  Start with *
                pattern = pattern.substring(1);
            }
            int pos = pattern.indexOf('*');
            if (pos>0) {
                String patternStart = pattern.substring(0, pos);
                if (field.contains(patternStart)) {
                    pos++;  //  after *
                    String  patternEnd = pattern.substring(pos);
                    pos = pattern.indexOf('*', pos);
                    if (pos>0) {
                        patternEnd = pattern.substring(pos);
                        return field.endsWith(patternEnd);
                    }
                    else {
                        //  No * any more return endsWith
                        return field.endsWith(patternEnd);
                    }
                }
                else  {
                    //  No pattern return equals
                    return false;
                }
            }
            else  {
                //  No * any more return endsWith
                return field.endsWith(pattern);
            }
        }
        else {
            //  No * return equals
            return pattern.equals(field);
        }
    }
    //===============================================================
    //===============================================================
    public static String strPeriod(double period) {
        if (period<2e-3) {
            return String.format("%.2f us", 1e6*period);
        }
        else
        if (period<1) {
            return String.format("%.2f ms.", 1e3*period);
        }
        else
        if (period < 60.0) {
            return String.format("%.2f sec.", period);
        } else {
            int intPeriod = (int) period;
            if (intPeriod < 3600) {
                int mn  = intPeriod / 60;
                int sec = intPeriod - 60 * mn;
                return "" + mn + " mn " + ((sec < 10) ? "0" : "") + sec + " sec.";
            } else if (intPeriod < 24 * 3600) {
                int h = intPeriod / 3600;
                intPeriod -= h * 3600;
                int mn = intPeriod / 60;
                int sec = intPeriod - 60 * mn;
                return "" + h + " h " + ((mn < 10) ? "0" : "") + mn + " mn " +
                        ((sec < 10) ? "0" : "") + sec + " sec.";
            } else {
                int days = intPeriod / (24 * 3600);
                return "" + days + " day" + ((days > 1) ? "s " : " ") +
                        strPeriod((double) intPeriod - (days * 24 * 3600));
            }
        }
    }
    //======================================================================
    //======================================================================
    public static String buildTooltip(String text) {
        StringBuilder sb = new StringBuilder( "<html><BODY TEXT=\"#000000\" BGCOLOR=\"#FFFFD0\">\n");
        if (text.contains("\n"))
            sb.append(splitTextLines(text));
        else {
            //  Put it in a table for margin
            sb.append("<table border=0 cellSpacing=2>\n");
            sb.append("<tr>").append(text).append("</tr>\n");
            sb.append("</table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }
    //======================================================================
    //======================================================================
    private static String splitTextLines(String text) {
        StringTokenizer stk = new StringTokenizer(text, "\n");
        StringBuilder sb = new StringBuilder();
        while (stk.hasMoreTokens()) {
            sb.append(stk.nextToken()).append("<br>\n");
        }
        return sb.toString().trim();
    }
    //======================================================================
    //======================================================================
    public static void popupError(Component component, String message) {
        ErrorPane.showErrorMessage(component, "Error", new Exception(message));
    }
    //======================================================================
    //======================================================================
    private static TestEvents testEvents;
    //=======================================================
    //=======================================================
    public static TestEvents getTestEvents() {
        //  Check if event tester is available
        if (testEvents==null) {
            try {
                testEvents = TestEvents.getInstance(new JFrame());
            }
            catch (NoClassDefFoundError e) {
                System.err.println(e.getMessage());
            }
            catch (DevFailed e) {
                System.err.println(e.errors[0].desc);
            }
        }
        return testEvents;
    }
    //======================================================================
    //======================================================================
    public static void startHdbViewer(String fullAttributeName) throws DevFailed {
        //  Split tango host and attribute name
        String tangoHost = TangoUtils.getOnlyTangoHost(fullAttributeName);
        String attributeName = TangoUtils.getOnlyDeviceName(fullAttributeName);
        System.out.println(attributeName);
        System.out.println(tangoHost);

        //  Start hdb viewer
        /*
        try {
            HDBViewer.MainPanel hdbViewer = new HDBViewer.MainPanel(false, true);
            hdbViewer.setTimeInterval(3); // Last day
            hdbViewer.addAttribute(tangoHost, attributeName);
            hdbViewer.setVisible(true);
            hdbViewer.performSearch();
        }
        catch (Exception | Error e) {
            Except.throw_exception("HdbFailed", e.toString());
        }
        */
    }
 //===============================================================
    //===============================================================
    public static String getLongestLine(List<String> lines) {
        return getLongestLine(lines.toArray(new String[lines.size()]));
    }
    //===============================================================
    //===============================================================
    public static String getLongestLine(String[] lines) {
        String  longest = "";
        for (String line : lines)
            if (line.length()>longest.length())
                longest = line;
        return longest;
    }
    //===============================================================
    private static final int hPadding = 20;
    /**
     * @param lines rows text list
     * @return the specified text width to be displayed.
     */
    //===============================================================
    public static int getTableColumnWidth(List<String> lines) {
        return getLongestLine(lines).length() * 7 + hPadding;
    }
    //===============================================================
    //===============================================================
    private static final JTextField textField = new JTextField();
    public static void copyToClipboard(final String message) {
        textField.setText(message);
        textField.setSelectionStart(0);
        textField.setSelectionEnd(message.length());
        textField.copy();
    }
    //===============================================================
    //===============================================================
}
