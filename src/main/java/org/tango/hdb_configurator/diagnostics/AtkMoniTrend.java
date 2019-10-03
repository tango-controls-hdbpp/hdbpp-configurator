//+======================================================================
// $Source: /segfs/tango/cvsroot/jclient/jblvac/src/jblvac/vacuum_panel/AtkMoniTrend.java,v $
//
// Project:   Tango
//
// Description:  Manage a ATK trend
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
// $Revision: 1.1.1.1 $
//
//-======================================================================

package org.tango.hdb_configurator.diagnostics;

import fr.esrf.tangoatk.widget.attribute.Trend;
import fr.esrf.tangoatk.widget.util.Gradient;
import org.tango.hdb_configurator.common.SplashUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


//===============================================================
/**
 * Manage a ATK trend
 *
 * @author Pascal Verdier
 */
//===============================================================


public class AtkMoniTrend extends Trend {

    private String  title;
    private static final Dimension trendSize = new Dimension(1024, 640);
    //===============================================================
    /**
     * Creates new form AtkMoniTrend trend
     *
     * @param title trend title
     * @param attributeNames list of devices to be monitored
     */
    //===============================================================
    public AtkMoniTrend(String title, List<String> attributeNames) {
        super();
        this.title = title;
        String config = buildAtkMoniConfig(attributeNames);
        setSetting(config);
        setPreferredSize(trendSize);
        fr.esrf.tangoatk.core.DeviceFactory.getInstance().startRefresher();
    }
    //===============================================================
    //===============================================================
    private String buildAtkMoniConfig(String attributeName, int curveNumber) {
        String code = atkMoniLinearAttributeConfig;
        code = replace(code, "dv0", "dv" + curveNumber);
        code = replace(code, "ATTRIBUTE", "\'" + attributeName + "\'");
        String newColor = getNewColorString();
        code = replace(code, "COLOR", newColor);
        if (curveNumber==0) //  Put first (manager) on Y1 axis
            code = replace(code, "dv0_selected:3\n", "dv0_selected:2\n");
        return code;
    }
    //===============================================================
    //===============================================================
    private String buildAtkMoniConfig(List<String> attributeNames) {
        StringBuilder code = new StringBuilder(replace(atkMoniConfigHeader, "TITLE", title));

        //  Add each attribute
        int curveNumber = 0;
        for (String attributeName : attributeNames) {
            SplashUtils.getInstance().increaseSplashProgress(2, "Create configuration");
            code.append(buildAtkMoniConfig(attributeName, curveNumber++));
        }
        return replace(code.toString(), "NB_CURVES", Integer.toString(curveNumber));
    }
    //===============================================================
    //===============================================================
    public static String replace(String code, String oldSrc, String newSrc) {
        int start;
        int end = 0;
        while ((start=code.indexOf(oldSrc, end))>=0) {
            end = start+oldSrc.length();
            code = code.substring(0, start) + newSrc + code.substring(end);
        }
        return code;
    }
    //===============================================================
    //===============================================================






    //===============================================================
    /**
     *  Color management
     */
    //===============================================================
    private MyGradient gradient = null;

    //===============================================================
    //===============================================================
    private String getNewColorString() {
        Color   color = getNewColor();
        return "" + color.getRed() + ',' + color.getGreen() + ',' + color.getBlue();
    }
    //===============================================================
    //===============================================================
    private Color getNewColor() {
        if (gradient == null)
            gradient = new MyGradient();
        return gradient.getNextColor();
    }

    //===============================================================
    //===============================================================
    private static class MyGradient extends Gradient {
        private final int nbColors = 256;
        private int step = nbColors / 2;
        private int colorIdx = -step;
        private int[] colorMap;
        private List<Integer> colors = new ArrayList<>();

        //===========================================================
        private MyGradient() {
            buildRainbowGradient();
            colorMap = buildColorMap(nbColors);
        }

        //===========================================================
        private boolean alreadyUsed(int idx) {
            for (int i : colors)
                if (i == idx)
                    return true;
            return false;
        }

        //===========================================================
        private Color getNextColor() {
            if (colors.size() == nbColors) {
                colors.clear();
                step = nbColors / 2;
                colorIdx = -step;
            }
            do {
                colorIdx += step;
                if (colorIdx >= colorMap.length) {
                    colorIdx = step;
                    step /= 2;
                }
            }
            while (alreadyUsed(colorIdx));

            colors.add(colorIdx);
            return new Color(colorMap[colorIdx]);
        }
    }
    //===============================================================
    //===============================================================

    private static final String atkMoniConfigHeader =
            "graph_title:'TITLE'\n" +
                    "label_visible:false\n" +
                    "graph_background:204,204,204\n" +
                    "title_font:dialog,1,24\n" +
                    "display_duration:14400000\n" +
                    "refresh_time:1000\n" +
                    "xsubgrid:false\n" +
                    "xgrid:true\n" +
                    "xgrid_style:1\n" +
                    "xmin:0.0\n" +
                    "xmax:100.0\n" +
                    "xautoscale:true\n" +
                    "xfit_display_duration:false\n" +
                    "xscale:0\n" +
                    "xformat:0\n" +
                    "xtitle:null\n" +
                    "xcolor:0,0,0\n" +
                    "xlabel_font:dialog,1,11\n" +
                    "xpercentscrollback:10.0\n" +

                    "y1grid:true\n" +
                    "y1subgrid:true\n" +
                    "y1grid_style:1\n" +
                    "y1min:0.0\n" +
                    "y1max:1.0\n" +
                    "y1autoscale:true\n" +
                    "y1scale:0\n" +
                    "y1title:'Events / second'\n" +
                    "y1color:0,0,0\n" +
                    "y1label_font:dialog,1,11\n" +

                    "dv_number:NB_CURVES\n";

    private static final String atkMoniLinearAttributeConfig =
            "dv0_name:ATTRIBUTE\n" +
                    "dv0_selected:3\n" +
                    "dv0_linecolor:COLOR\n" +
                    "dv0_linewidth:1\n" +
                    "dv0_linestyle:0\n" +
                    "dv0_markercolor:255,0,0\n" +
                    "dv0_markersize:6\n" +
                    "dv0_markerstyle:0\n" +
                    "dv0_A0:0.0\n" +
                    "dv0_A1:1.0\n" +
                    "dv0_A2:0.0\n";
}
