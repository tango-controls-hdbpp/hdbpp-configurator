//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014,2015,2016
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
import fr.esrf.TangoApi.DbClass;
import fr.esrf.TangoApi.DbDatum;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by verdier on 14/09/2016.
 * Define strategy object
 */
public class Strategy extends ArrayList<Context> {
    private boolean classProperty;
    //===========================================================
    //===========================================================
    public Strategy() {
    }
    //===========================================================
    //===========================================================
    public String[] getNames() {
        String[] names = new String[size()];
        int i=0;
        for (Context context : this)
            names[i++] = context.getName();
        return names;
    }
    //===========================================================
    //===========================================================
    public boolean isClassProperty() {
        return classProperty;
    }
    //===========================================================
    //===========================================================
    public boolean different(Strategy strategy) {
        if (strategy.size()!=size())
            return true;
        if (strategy.isClassProperty()!=classProperty)
            return true;
        for (int i=0 ; i<size() && i<strategy.size() ; i++) {
            Context context = strategy.get(i);
            if (context.different(get(i)))
                return true;
        }
        return false;
    }
    //===========================================================
    //===========================================================
    public void setClassProperty(boolean classProperty) {
        this.classProperty = classProperty;
    }
    //===========================================================
    //===========================================================
    public String toString() {
        if (isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        int i=0;
        for (Context context : this) {
            if (context.isUsed()) sb.append("[");
            sb.append(context);
            if (context.isUsed()) sb.append("]");
            if (i++<size()-1) sb.append(",");
        }
        return sb.toString();
    }
    //===========================================================
    //===========================================================





    /*
     *  Static methods part
     */
    //===========================================================
    //===========================================================
    public static final int ALWAYS_INDEX = 0;
    private static final String ALWAYS_CONTEXT = "ALWAYS";
    private static final String ContextsPropertyName = "ContextsList";
    private static final String DefaultPropertyName = "DefaultStrategy";
    private static final String SubscriberClassName = "HdbEventSubscriber";
    private static final String AlwaysDescription = "Store in HDB++ under any circumstances";
    private static final String SEPARATOR = ":";
    //===============================================================
    //===============================================================
    public static Strategy getContextsFromDB(Subscriber subscriber) throws DevFailed {
        //  If no subscriber get it from Class
        if (subscriber==null)
            return getContextsFromDB();
        //  Read device property
        DbDatum dbDatum = subscriber.get_property(ContextsPropertyName);
        // If empty,  get it from Class
        if (dbDatum.is_empty())
            return getContextsFromDB();
        else {
            //  Manage datum read from DB
            return buildStrategy(dbDatum, getDefaultContexts(subscriber), false);
        }
    }
    //===============================================================
    //===============================================================
    public static Strategy getContextsFromDB() throws DevFailed {
        DbDatum dbDatum = new DbClass(SubscriberClassName).get_property(ContextsPropertyName);
        return buildStrategy(dbDatum, getDefaultContexts(), true);
    }
    //===============================================================
    //===============================================================
    private static Strategy manageDefaultContexts(Strategy strategy) {
        //  If empty add it
        if (strategy.isEmpty()) {
            strategy.add(new Context(ALWAYS_CONTEXT, AlwaysDescription, true));
            return strategy;
        }

        //  Check if Always is at first
        boolean found = false;
        for (int i=0 ; i<strategy.size() ; i++) {
            String context = strategy.get(i).getName();
            if (context.equalsIgnoreCase(ALWAYS_CONTEXT)) {
                found = true;
                if (i>0) {
                    strategy.add(ALWAYS_INDEX, strategy.remove(i));
                    break;
                }
            }
        }
        if (!found)
            strategy.add(ALWAYS_INDEX, new Context(ALWAYS_CONTEXT, AlwaysDescription, true));
        return strategy;
    }
    //===============================================================
    //===============================================================
    private static Strategy buildStrategy(DbDatum dbDatum, String[] defaultContexts, boolean classProperty) throws DevFailed {
        Strategy strategy = new Strategy();
        if (!dbDatum.is_empty()) {
            String[] lines = dbDatum.extractStringArray();
            for (String line : lines) {
                //  Split strategy name and description
                StringTokenizer stk = new StringTokenizer(line, SEPARATOR);
                if (stk.countTokens() > 0) {
                    String name = stk.nextToken().trim();
                    String desc = "";
                    if (stk.hasMoreTokens())
                        desc = stk.nextToken().trim();

                    //  Check if is default
                    boolean isDefault = false;
                    for (String str : defaultContexts) {
                        if (name.equalsIgnoreCase(str))
                            isDefault = true;
                    }
                    strategy.add(new Context(name, desc, isDefault));
                }
            }
        }
        //  Check default case
        strategy = manageDefaultContexts(strategy);
        strategy.setClassProperty(classProperty);
        return strategy;
    }
    //===============================================================
    //===============================================================
    public static String[] getDefaultContexts() throws DevFailed {
        return getDefaultContexts(null);
    }
    //===============================================================
    //===============================================================
    public static String[] getDefaultContexts(Subscriber subscriber) throws DevFailed {
        DbDatum dbDatum = null;
        if (subscriber!=null) {
            //  Device property
            dbDatum = subscriber.get_property(DefaultPropertyName);
        }
        if (subscriber==null || dbDatum==null || dbDatum.is_empty()) {
            //  Class property
            dbDatum = new DbClass(SubscriberClassName).get_property(DefaultPropertyName);
        }

        //  Extract if not empty
        if (dbDatum.is_empty())
            return new String[] { ALWAYS_CONTEXT };
        else
            return dbDatum.extractStringArray();
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    public static void putStrategiesToDB(Strategy strategy, Subscriber subscriber) throws DevFailed {

        //  Convert in string array (name: desc)
        String[] array = new String[strategy.size()];
        int i = 0;
        for (Context context : strategy) {
            array[i++] = context.getName() + SEPARATOR + " " + context.getDescription();
        }

        //  Search default strategy
        List<String> defaultNames = new ArrayList<>();
        for (Context context : strategy) {
            if (context.isDefault())
                defaultNames.add(context.getName());
        }

        //  Put it in database for subscriber device or class
        DbDatum[] dbData = new DbDatum[]{
                new DbDatum(ContextsPropertyName, array),
                new DbDatum(DefaultPropertyName, defaultNames.toArray(new String[defaultNames.size()])),
        };
        if (subscriber==null)
            new DbClass(SubscriberClassName).put_property(dbData);
        else
            subscriber.put_property(dbData);
    }
    //===========================================================
    //===========================================================
}
