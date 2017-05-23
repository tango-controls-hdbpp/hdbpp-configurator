//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,
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
// $Revision: 25293 $
//
//-======================================================================


package org.tango.hdb_configurator.common;

import admin.astor.tools.EventsTable;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import java.util.List;


//===============================================================
/**
 * Class Description: a singleton to call Astor class to test events
 *
 * @author Pascal Verdier
 */
//===============================================================


public class TestEvents {

    private EventsTable eventsTable;
    private static final int ARCHIVE = 2;
    private static TestEvents instance = null;
    //===============================================================
    /*
     * Creates new form Selector
     */
    //===============================================================
    private TestEvents(JFrame parent) throws DevFailed {
        eventsTable = new EventsTable(parent, true);
    }
    //===============================================================
    //===============================================================
    public static TestEvents getInstance(JFrame parent) throws DevFailed {
        if (instance==null) {
            instance = new TestEvents(parent);
        }
        return instance;
    }
    //===============================================================
    //===============================================================
    public void add(String attributeName) {
        //  Check if already subscribed
        List<String>    subscribedList = eventsTable.getSubscribedNames();
        boolean alreadySubscribed = false;
        for (String subscribed : subscribedList)
            if (subscribed.equalsIgnoreCase(attributeName))
                alreadySubscribed = true;

        //  If done display else subscribe
        if (alreadySubscribed)
            eventsTable.setVisible(true);
        else
            eventsTable.add(attributeName, ARCHIVE);
    }
    //===============================================================
    //===============================================================
}
