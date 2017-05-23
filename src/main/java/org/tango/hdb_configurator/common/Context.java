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



/**
 * Created by verdier on 14/04/2016.
 * Define strategy object
 */
public class Context {
    private String name;
    private Boolean used = false ;
    private String description = null;
    private boolean isDefault = false;
    //===========================================================
    //===========================================================
    public Context(String name, String description, boolean isDefault) {
        this(name, isDefault, description);
        this.isDefault = isDefault;
    }
    //===========================================================
    //===========================================================
    public Context(String name, boolean used, String description) {
        this.name = name;
        this.used = used;
        this.description = description;
    }
    //===========================================================
    //===========================================================
    public String getName() {
        return name;
    }
    //===========================================================
    //===========================================================
    public void setName(String name) {
        this.name = name;
    }
    //===========================================================
    //===========================================================
    public void setDescription(String description) {
        this.description = description;
    }
    //===========================================================
    //===========================================================
    public boolean isDefault() {
        return isDefault;
    }
    //===========================================================
    //===========================================================
    public void setDefault(boolean b) {
        isDefault = b;
    }
    //===========================================================
    //===========================================================
    public boolean isUsed() {
        return used;
    }
    //===========================================================
    //===========================================================
    public void setUsed(Boolean used) {
        this.used = used;
    }
    //===========================================================
    //===========================================================
    public void toggleUsed() {
        used = !used;
    }
    //===========================================================
    //===========================================================
    public String getHtmlDescription() {
        StringBuilder sb = new StringBuilder("<b><u>" + this.toString() + ":</u></b><br>\n");
        if (description==null)
            return sb.toString() + "....";
        //  Convert '\n' in "<br>"
        int start = 0;
        int end;
        String target = "\\n";
        while ((end=description.indexOf(target, start+1))>0) {
            sb.append(description.substring(start, end)).append("<br>");
            start = end+target.length();
        }
        sb.append(description.substring(start));
        return sb.toString();
    }
    //===========================================================
    //===========================================================
    public boolean different(Context context) {
        return !(context.getName().equals(name)  &&
                context.getDescription().equals(description) &&
                context.isUsed()==used && context.isDefault()==isDefault);
    }
    //===========================================================
    //===========================================================
    public String getDescription() {
        return description;
    }
    //===========================================================
    //===========================================================
    public String toString() {
        return name;
    }
    //===========================================================
    //===========================================================
}
