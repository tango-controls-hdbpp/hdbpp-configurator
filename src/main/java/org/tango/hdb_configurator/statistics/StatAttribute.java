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

package org.tango.hdb_configurator.statistics;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import org.tango.hdb_configurator.common.HtmlUtils;
import org.tango.hdb_configurator.common.Subscriber;
import org.tango.hdb_configurator.common.TangoUtils;
import org.tango.hdb_configurator.common.Utils;

import java.util.List;

/**
 * This class models an attribute used to compute statistics
 *
 * @author verdier
 */

public class StatAttribute {
     String name;
     String shortName;
     int nbStatistics;
     long nbEvents;
     Subscriber subscriber;
     String deviceName;
     long resetTime;
     double average = 0;
     String averageString;
     String tangoHost;
     String nameTooltip;
     String resetTooltip;
     private long readTime;

    /** if use another TANGO_HOST, cannot configure with Jive */
     boolean useDefaultTangoHost = false;
    //===========================================================
    //===========================================================
     StatAttribute(String name,
                   long readTime,
                   List<String> defaultTangoHosts,
                   double frequency,
                   long nbEvents,
                   Subscriber subscriber) {
        this.name = name;
        this.shortName = TangoUtils.getOnlyDeviceName(name);
        this.nbStatistics = (int)frequency;
        this.nbEvents  = nbEvents;
        this.subscriber = subscriber;
        this.readTime = readTime;

        //  Get tango host without fqdn
        String str = TangoUtils.getOnlyTangoHost(name);
        int idx = str.indexOf('.');
        if (idx>0)
            tangoHost = str.substring(0, idx);
        idx = str.indexOf(':');
        if (idx>0)
            tangoHost += str.substring(idx);

        //  Average period since reset
        resetTime = subscriber.getStatisticsResetTime();
        long sinceReset = readTime - resetTime;
        if (resetTime>0 && sinceReset>0 && nbEvents>0) {
            long nbSeconds = sinceReset/1000;
            average = (double) nbEvents/nbSeconds;
            averageString = Utils.formatEventFrequency(average);
        }
        else {
            averageString = "---";
        }

        deviceName = shortName.substring(0, shortName.lastIndexOf('/'));
        String tangoHost = TangoUtils.getOnlyTangoHost(name);
        for (String defaultTangoHost : defaultTangoHosts) {
            if (tangoHost.equals(defaultTangoHost)) {
                useDefaultTangoHost = true;
                break;
            }
        }
        nameTooltip  = name;//Utils.buildTooltip(name);
        resetTooltip = "During: " +
                Utils.strPeriod((double) sinceReset/1000) +
                "   (since " + Utils.formatDateTime(resetTime) + ")";
    }
    //===========================================================
    //===========================================================
     String getInfo() {
        String host = null;
        String server = null;
        String attributeStatus = null;
        try {
            String deviceName = name.substring(0, name.lastIndexOf('/'));
            DeviceProxy deviceProxy = new DeviceProxy(deviceName);
            DeviceInfo info = deviceProxy.get_info();
            host = info.hostname;
            server = info.server;
            attributeStatus = subscriber.getAttributeStatus(name);
        }
        catch (DevFailed e) {
            Except.print_exception(e);
        }
        catch (Exception e) {
            // cannot find host
        }
        return HtmlUtils.attributeInfoToHtml(
                subscriber, server, host, nbEvents, resetTime, readTime) +
                "<br><br><br>" +
                HtmlUtils.attributeStatusToHtml(attributeStatus);
    }
    //===========================================================
    //===========================================================
     void configureEvent() {
        Utils.startJiveForDevice(deviceName);
    }
    //===========================================================
    //===========================================================
    public String toString() {
        return shortName + ":\t" + nbEvents;
    }
    //===========================================================
    //===========================================================
}
