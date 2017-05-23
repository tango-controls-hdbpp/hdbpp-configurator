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
import fr.esrf.tangoatk.widget.util.JSmoothProgressBar;
import fr.esrf.tangoatk.widget.util.Splash;

import javax.swing.*;
import java.awt.*;


//===============================================================
/**
 * This class is a singleton and manage some utilities.
 *
 * @author Pascal Verdier
 */
//===============================================================


public class SplashUtils {
    private static Splash splash = null;
    private static int splashProgress = 0;
    private static boolean splashActive = false;
    private static final String packageName = "org.tango.hdb_configurator";
    public static final String revNumber =
            "2.2  -  23-05-2017  13:18:05";

    private static SplashUtils instance = new SplashUtils();
    private static final String imageFile = "FullTangoLogo.gif";
    //=======================================================
    //=======================================================
    public static SplashUtils getInstance() {
        return instance;
    }
    //=======================================================
    //=======================================================
    public void startSplash() {
        if (splash!=null) {
            splash.setVisible(false);
        }
        //  Create a new one
        String title = packageName;
        int end = revNumber.indexOf("-");
        if (end > 0)
            title += " - " + revNumber.substring(0, end).trim();

        //	Create a splash window.
        JSmoothProgressBar myBar = new JSmoothProgressBar();
        myBar.setStringPainted(true);
        myBar.setBackground(Color.lightGray);
        myBar.setProgressBarColors(Color.gray, Color.lightGray, Color.darkGray);

        try {
            ImageIcon icon = Utils.getInstance().getIcon(imageFile);
            splash = new Splash(icon, Color.black, myBar);
            splash.setTitle(title);
            splash.setMessage("Starting....");
            splash.setVisible(true);
            splashProgress = 0;
        } catch (DevFailed e) {
            System.err.println(e.errors[0].desc);
        }
    }

    //=======================================================
    //=======================================================
    public void increaseSplashProgress(int i, String message) {
        if (splash == null)
            return;
        splashProgress += i;
        if (splashProgress> 98)
            splashProgress = 1;
        splash.progress(splashProgress);
        if (message!=null)
            splash.setMessage(message);

        //System.out.println(splashProgress);
    }
    //=======================================================
    //=======================================================
    public void increaseSplashProgressForLoop(int size, String message) {
        if (splash == null)
            return;
        splashProgress += getStep(size);
        if (splashProgress> 98)
            splashProgress = 1;
        splash.progress(splashProgress);
        if (message!=null)
            splash.setMessage(message);

        //System.out.println(splashProgress);
    }
    //=======================================================
    //=======================================================
    public void reset() {
        splashProgress = 1;
    }
    //=======================================================
    //=======================================================
    private int getStep(int size) {
        int step = 98/size;
        if (step<1) step = 1;
        return step;
    }
    //=======================================================
    //=======================================================
    public void setSplashProgress(int i, String message) {
        if (splash == null)
            return;
        splashProgress = i;
        if (splashProgress> 98)
            splashProgress = 98;
        splash.progress(splashProgress);
        if (message!=null)
            splash.setMessage(message);

        //System.out.println(splashProgress + " - " + message);
    }

    //=======================================================
    //=======================================================
    public void showSplash(boolean b) {
        if (splash != null)
            splash.setVisible(b);
    }

    //=======================================================
    //=======================================================
    public void stopSplash() {
        if (splash != null) {
            splashProgress = 100;
            splash.progress(splashProgress);
            splash.setVisible(false);
        }
    }

    //=======================================================
    //=======================================================
    @SuppressWarnings("unused")
    public boolean getSplashActive() {
        return splashActive;
    }

    //=======================================================
    //=======================================================
    @SuppressWarnings("unused")
    public void setSplashActive(boolean b) {
        splashActive = b;
    }
    //=======================================================
    //=======================================================
    @SuppressWarnings("unused")
    public void startAutoUpdate() {
        new UpdateThread().start();
    }
    //=======================================================
    //=======================================================


    //=======================================================
    //=======================================================
    private class UpdateThread extends Thread {
        public void run() {
            while (splash.isVisible()) {
                increaseSplashProgress(1, null);
                try { Thread.sleep(500); } catch (InterruptedException e) { /* */ }
            }
         }
    }
    //=======================================================
    //=======================================================
}
