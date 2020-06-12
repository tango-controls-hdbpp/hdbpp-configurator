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

import fr.esrf.tangoatk.widget.util.chart.*;

import java.awt.*;
import java.util.List;

import static org.tango.hdb_configurator.statistics.StatisticsTable.COLUMN_NAMES;
import static org.tango.hdb_configurator.statistics.StatisticsTable.EVENTS_NUMBER;

/**
 * This class is able display statics on a chart
 *
 * @author verdier
 */
class StatisticsChart extends JLChart implements IJLChartListener {
    private JLDataView statDataView = new JLDataView();
    private List<StatAttribute> filteredStatAttributes;
    //=====================================================================
    //=====================================================================
    StatisticsChart(List<StatAttribute> filteredStatAttributes) {
        setJLChartListener(this);
        //  Create X axis.
        getXAxis().setName("Attributes");
        getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
        getXAxis().setAutoScale(false);
        getXAxis().setMinimum(-1.0);

        //  Create Y1
        getY1Axis().setName(COLUMN_NAMES[EVENTS_NUMBER]);
        getY1Axis().setAutoScale(true);
        getY1Axis().setScale(JLAxis.LINEAR_SCALE);
        getY1Axis().setGridVisible(true);
        getY1Axis().setSubGridVisible(true);

        statDataView.setColor(Color.red);
        statDataView.setFillColor(Color.red);
        statDataView.setName("Statistics");
        statDataView.setFill(true);
        statDataView.setLabelVisible(false);
        statDataView.setViewType(JLDataView.TYPE_BAR);
        statDataView.setBarWidth(1);
        statDataView.setFillStyle(JLDataView.FILL_STYLE_SOLID);
        getY1Axis().addDataView(statDataView);
        updateValues(filteredStatAttributes);
    }
    //=====================================================================
    //=====================================================================
    void updateValues(List<StatAttribute> filteredStatAttributes) {
        this.filteredStatAttributes = filteredStatAttributes;
        statDataView.reset();
        double x = 1.0;
        for (StatAttribute attribute : filteredStatAttributes) {
            statDataView.add(x, attribute.nbEvents);
            x++;
        }
        getXAxis().setMaximum(filteredStatAttributes.size() + 2);
        repaint();
    }
    //=====================================================================
    //=====================================================================
    @Override
    public String[] clickOnChart(JLChartEvent event) {
        int index = event.getDataViewIndex();
        if (filteredStatAttributes!=null) {
            //  Build message to be displayed
            StatAttribute attribute = filteredStatAttributes.get(index);
            return new String[]{
                    attribute.deviceName,
                    "Receive:   " + attribute.nbEvents + " events",
                    "Period av.:" + attribute.averageString,
            };
        }
        else
            return new String[0];
    }
    //=====================================================================
    //=====================================================================
}
