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

import fr.esrf.Tango.DevFailed;

import java.util.*;

/**
 * This class defines an attribute to be added to a subscriber
 * It extends a Strategy to be stored or not in HDB
 * @author verdier
 */

public class HdbAttribute extends Strategy {
    private String  name;
    private boolean pushedByCode;
    private boolean isError = false;
    private long ttl = 0;
    //===============================================================
    /**
     * Create a HdbAttribute object.
     *
     * @param name  specified attribute name.
     */
    //===============================================================
    public HdbAttribute(String name, Strategy strategy) {
        this.name = name;
        try {
            //  Get attribute strategy as String
            String attributeStrategy = ArchiverUtils.getAttributeStrategy(name);
            setStrategy(strategy, attributeStrategy);
        }
        catch (DevFailed e) {
            //  Set default ones
            setStrategy(strategy);
            System.err.println(e.errors[0].desc);
        }
    }
    //===============================================================
    /**
     * Create a HdbAttribute object representing an error
     * @param name  specified attribute name.
     */
    //===============================================================
    public HdbAttribute(String name) {
        this.name = name;
        isError = true;
    }
    //===============================================================
    /**
     * Create a HdbAttribute object
     * @param name            specified attribute name.
     * @param pushedByCode    true if event will be pushed by device code.
     */
    //===============================================================
    public HdbAttribute(String name, Strategy strategy, boolean pushedByCode) {
        this.name = name;
        this.setStrategy(strategy);
        this.pushedByCode = pushedByCode;
    }
    //===============================================================
    /**
     * Returns the attribute name
     * @return the attribute name
     */
    //===============================================================
    public String getName() {
        return name;
    }
    //===============================================================
    /**
     *
     * @param pushedByCode true if event pushed by code
     */
    //===============================================================
    public void setPushedByCode(boolean pushedByCode) {
        this.pushedByCode = pushedByCode;
    }
    //===============================================================
    /**
     * Returns true if the event is pushed by the device class code.
     * @return true if the event is pushed by the device class code.
     */
    //===============================================================
    public boolean isPushedByCode() {
        return pushedByCode;
    }
    //===============================================================
    //===============================================================
    public void setStrategy(Strategy strategy) {
        this.addAll(strategy);
    }
    //===============================================================
    //===============================================================
    public void updateUsedContexts(Strategy strategy) {
        if (isEmpty()) {
            //  If empty add context
            this.addAll(strategy);
        }
        else
        for (int i=0 ; i<size() && i<strategy.size() ; i++) {
            //  Else just update
            Context context = get(i);
            context.setUsed(strategy.get(i).isUsed());
        }
    }
    //===============================================================
    //===============================================================
    public void setStrategy(Strategy strategy, String contextNames) {
        StringTokenizer stk = new StringTokenizer(contextNames, "|");
        List<String> contextList = new ArrayList<>();
        while (stk.hasMoreTokens())
            contextList.add(stk.nextToken());

        //  Add a copy of input strategy
        for (Context context : strategy) {
            //  Check if used
            boolean used = false;
            for (String contextName : contextList) {
                if (context.getName().equalsIgnoreCase(contextName)) {
                    used = true;
                    break;
                }
            }
            // build new context and add to this
            Context newContext = new Context(context.getName(), used, context.getDescription());
            this.add(newContext);
        }
    }
    //===============================================================
    //===============================================================
    public Strategy getStrategyCopy() {
        Strategy strategy = new Strategy();
        for (Context context : this) {
            Context newContext = new Context(
                    context.getName(),context.getDescription(), context.isDefault());
            newContext.setUsed(context.isUsed());
            strategy.add(newContext);
        }
        return strategy;
    }
    //===============================================================
    //===============================================================
    public void setTTL(long ttl) {
        this.ttl = ttl;
    }
    //===============================================================
    //===============================================================
    public long getTTL() {
        return ttl;
    }
    //===============================================================
    //===============================================================
    public String getTtlString() {
        if (ttl==0)
            return "- - -";
        return (ttl/24) + " day" + (ttl>24? "s":"");
    }
    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder( name+"     " + strategyToString());
        if (ttl>0)
            sb.append("  (ttl=").append(getTtlString()).append(")");
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public String strategyToString() {
        StringBuilder sb = new StringBuilder();
        for (Context context : this)
            if (context.isUsed())
                sb.append(context.getName()).append("|");
        //  Check to remove last '|'
        String str = sb.toString();
        if (str.endsWith("|")) {
            str = str.substring(0, str.length()-1);
        }
        return  str;
    }
    //===============================================================
    //===============================================================
    public boolean isError() {
        return isError;
    }
    //===============================================================
    //===============================================================
    private AttributeNameComponents nameComponents = null;
    //===============================================================
    //===============================================================
    public List<String> getNameComponents() {
        if (nameComponents==null) {
            nameComponents = new AttributeNameComponents(name);
        }
        return nameComponents;
    }
    //===============================================================
    //===============================================================




    //===============================================================
    /* A list of attribute name components ( domain, family,...)*/
    //===============================================================
    private static class AttributeNameComponents extends ArrayList<String> {
        //===========================================================
        private AttributeNameComponents(String name) {
            int start = name.lastIndexOf('/');
            String attributeName = name.substring(start+1);

            int end = start;
            start = name.lastIndexOf('/', end-1);
            String member = name.substring(start+1, end);

            end = start;
            start = name.lastIndexOf('/', end-1);
            String family = name.substring(start+1, end);

            end = start;
            start = name.lastIndexOf('/', end-1);
            String domain = name.substring(start+1, end);

            end = start;
            start = name.lastIndexOf('/', end-1);
            String tangoHost = name.substring(start+1, end);

            add(tangoHost);
            add(domain);
            add(family);
            add(member);
            add(attributeName);
        }
        //===========================================================
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String s : this) sb.append(" - ").append(s).append("\n");
            return sb.toString();
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================
}
