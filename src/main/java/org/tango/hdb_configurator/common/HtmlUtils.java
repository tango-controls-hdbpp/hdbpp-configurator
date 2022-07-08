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

package org.tango.hdb_configurator.common;

import javax.swing.*;
import java.util.StringTokenizer;


/**
 * This class is a set of static HTML utilities
 *
 * @author verdier
 */

@SuppressWarnings({"SameParameterValue", "StringBufferReplaceableByString"})
public class HtmlUtils {
    //===============================================================
    //===============================================================
    public static String attributeInfoToHtml(Subscriber subscriber,
                                             String server, String host,
                                             long nbEvents, long resetTime, long readTime) {
        StringBuilder sb = new StringBuilder();
        if (server!=null)
            sb.append("<li> Server: ").append(server).append("\n");
        if (host!=null)
            sb.append("<li>Registered on: ").append(host).append("\n");

        sb.append("<br>");
        sb.append(subscriberInfoToHtml(subscriber, resetTime));
        sb.append(eventsInfoToHtml(nbEvents, resetTime, readTime));

        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public static String subscriberInfoToHtml(Subscriber subscriber, long resetTime) {
        StringBuilder sb = new StringBuilder();

        sb.append("<br><li>Archived by <b> ").append(subscriber.getLabel())
                .append("</b><br>");
        sb.append(" (").append(subscriber.getName()).append(")<br>\n");
        sb.append("Last statistics reset: ").append(Utils.formatDate(resetTime));

        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public static String eventsInfoToHtml(long nbEvents, long resetTime, long readTime) {
        // Compute event frequency
        long sinceReset = readTime-resetTime;
        long nbSeconds = sinceReset/1000;
        double frequency = (double) nbEvents/nbSeconds;

        // And build info html string
        StringBuilder sb = new StringBuilder();

        sb.append("<ul>");
        sb.append("<li>").append(nbEvents).append(" during ").
                append(Utils.strPeriod((double) sinceReset/1000)).append("\n");

        sb.append("<li>Average: ").append(Utils.formatEventFrequency(frequency))
                .append("").append("\n");
        sb.append("</ul>");

        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public static String attributeStatusToHtml(String attributeStatus) {
        StringBuilder sb = new StringBuilder("<table border=\"0\">");
        sb.append("<tr><font size=+1><b> Attribute Status </b></font></tr>");

        StringTokenizer stk = new StringTokenizer(attributeStatus, "\n");
        while (stk.hasMoreTokens()) {
            sb.append("\t").append(lineToTableRow(stk.nextToken(), ":")).append("\n");
        }
        return sb.toString() + "</table>";
    }
    //===============================================================
    //===============================================================
    private static String lineToTableRow(String line, String separator) {
        StringBuilder sb = new StringBuilder("<tr>");
        int idx = line.indexOf(separator);
        if (idx<0)
            return "";

        String first = line.substring(0, idx++).trim();
        String second = line.substring(idx).trim();
        String third = null;
        if (second.equals("0 - YYYY-MM-DD HH:MM:SS.UUUUUU"))
            second = "0";
        else {
            idx = second.indexOf("-");
            if (idx>0) {
                third = second.substring(idx+1);
                second = second.substring(0, idx);
            }
        }

        sb.append("<td>").append(first).append("</td>");
        sb.append("<td>").append(second).append("</td>");
        if (third!=null)
            sb.append("<td>").append(third).append("</td>");
        return sb.toString() + "</tr>";
    }
    //===============================================================
    //===============================================================

    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        String attributeStatus =
                "Event status       : Event received\n" +
                "Events engine      : ZMQ\n" +
                "Archiving          : Started\n" +
                "Event OK counter   : 71162 - 2020-05-27 11:11:19.161937\n" +
                "Event NOK counter  : 0 - YYYY-MM-DD HH:MM:SS.UUUUUU\n" +
                "DB ERRORS counter  : 3 - 2020-05-27 07:36:00.161627\n" +
                "Storing time AVG   : 0.013196s\n" +
                "Processing time AVG: 0.049529s\n";

        new PopupHtml((JFrame) null, true).show(attributeStatusToHtml(attributeStatus));
    }
}
