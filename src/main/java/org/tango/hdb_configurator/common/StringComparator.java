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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//======================================================
/**
 * Comparators class to sort Strings
 */
//======================================================
public class StringComparator implements Comparator<String> {

    //======================================================
    //======================================================
    public int compare(String s1, String s2) {

        if (s1==null)
            return 1;
        else if (s2==null)
            return -1;
        else
            return s1.compareTo(s2);
    }
    //======================================================
    //======================================================
    public static List<String> sort(List<String> list) {
        Collections.sort(list, new StringComparator());
        return list;
    }
    //======================================================
    //======================================================
    public static String[] sortArray(String[] inArray) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, inArray);
        Collections.sort(list, new StringComparator());
        String[]    outArray = new String[list.size()];
        for (int i=0 ; i<list.size() ; i++)
            outArray[i] = list.get(i);
        return outArray;
    }
    //======================================================
    //======================================================
}
