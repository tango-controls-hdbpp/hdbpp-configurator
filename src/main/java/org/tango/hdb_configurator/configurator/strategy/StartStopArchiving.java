//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015
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

package org.tango.hdb_configurator.configurator.strategy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.SplashUtils;
import org.tango.hdb_configurator.common.Subscriber;
import org.tango.hdb_configurator.common.SubscriberMap;
import org.tango.hdb_configurator.common.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//=======================================================
/**
 *	JFrame Class to display a listof subscribers to
 *      Start/Stop all attributes
 *
 * @author  Pascal Verdier
 */
//=======================================================
@SuppressWarnings("MagicConstant")
public class StartStopArchiving extends JFrame {

	private SubscriberMap subscriberMap;
    private boolean modified = false;
    private List<JRadioButton> radioButtonList = new ArrayList<>();

    private static final int Start = 0;
    private static final int Stop  = 1;
    private static final String[] actionNames = { "Start", "Stop"};
	//=======================================================
    /**
	 *	Creates new form StartStopArchiving
	 */
	//=======================================================
    private StartStopArchiving() throws DevFailed {
        SplashUtils.getInstance().startSplash();
        SplashUtils.getInstance().setSplashProgress(20, "Building GUI");
        initComponents();
        //  Build subscriber map
        subscriberMap = new SubscriberMap(Utils.getConfiguratorProxy());

        // Get subscriber list to be managed
        SplashUtils.getInstance().setSplashProgress(30, "Building GUI");
        DbDatum datum = ApiUtil.get_db_obj().get_property("HdbConfigurator", "ShutdownArchivers");
        if (datum.is_empty())
            Except.throw_exception("NoProperty",
                    "Free property \'HdbConfigurator/ShutdownArchivers\' is not set");
        String[] defaultSubscriberNames = datum.extractStringArray();
        List<String> labelList =  subscriberMap.getLabelList();

        //  And build buttons in panel
        buildRadioButtons(labelList, defaultSubscriberNames);

        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
        SplashUtils.getInstance().stopSplash();
	}
	//=======================================================
	//=======================================================
    private void buildRadioButtons(List<String> labelList, String[] defaultSubscriberNames) {
        GridBagConstraints gbc = new GridBagConstraints();
        //  add separator
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 0, 10);
        centerPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        //  Display default ones on first column
        SplashUtils.getInstance().setSplashProgress(50, "Building GUI");
        int x1 = 0;
        int x2 = 1;
        int y1 = 1;
        int y2 = 2;
        gbc.gridy++;
        for (String label : labelList) {
            JRadioButton radioButton = new JRadioButton(label);
            radioButton.setFont(new Font("Dialog", Font.BOLD, 12));
            boolean inList = labelInList(label, defaultSubscriberNames);
            radioButton.setSelected(inList);
            radioButtonList.add(radioButton);
            radioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    radioButtonActionPerformed();
                }
            });

            if (inList) {
                gbc.gridx = x1;
                gbc.gridy = y1++;
            }
            else {
                gbc.gridx = x2;
                gbc.gridy = y2++;
            }

            centerPanel.add(radioButton, gbc);
        }
        //  Add separator
        gbc.gridx = x1;
        gbc.gridy = (y1>y2)? y1 : y2;
        gbc.gridwidth = 2;
        centerPanel.add(new JSeparator(), gbc);
    }
	//=======================================================
	//=======================================================
    private boolean labelInList(String label, String[] stringList) {
        for (String string : stringList) {
            if (string.equals(label))
                return true;
        }
        return false;
    }
	//=======================================================
	//=======================================================
    private void radioButtonActionPerformed() {
        modified = true;
    }
	//=======================================================
	//=======================================================

	//=======================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        centerPanel = new javax.swing.JPanel();
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton startButton = new javax.swing.JButton();
        javax.swing.JLabel dummyLabel = new javax.swing.JLabel();
        javax.swing.JButton stopButton = new javax.swing.JButton();
        javax.swing.JLabel dummyLabel1 = new javax.swing.JLabel();
        javax.swing.JButton cancelButton = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        centerPanel.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("Select Subscribers");
        topPanel.add(jLabel1);

        getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

        startButton.setText("Start All");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        bottomPanel.add(startButton);

        dummyLabel.setText("   ");
        dummyLabel.setToolTipText("");
        bottomPanel.add(dummyLabel);

        stopButton.setText("Stop All");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        bottomPanel.add(stopButton);

        dummyLabel1.setText("                             ");
        dummyLabel1.setToolTipText("");
        bottomPanel.add(dummyLabel1);

        cancelButton.setText("Dismiss");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelButton);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // TODO add your handling code here:
        List<String> subscriberNames = getSelectedSubscribers(Start);
        if (subscriberNames==null || subscriberNames.isEmpty())
            return;

        try {
            SplashUtils.getInstance().startSplash();
            int ratio = 90/(subscriberNames.size()+1);
            for (String subscriberName : subscriberNames) {
                SplashUtils.getInstance().setSplashProgress(ratio, "Start " + subscriberName + " attributes");
                doAction(subscriberName, Start);
            }
            SplashUtils.getInstance().stopSplash();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_startButtonActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        List<String> subscriberNames = getSelectedSubscribers(Stop);
        if (subscriberNames==null || subscriberNames.isEmpty())
            return;

        try {
            SplashUtils.getInstance().startSplash();
            int ratio = 90 / (subscriberNames.size() + 1);
            for (String subscriberName : subscriberNames) {
                SplashUtils.getInstance().setSplashProgress(ratio, "Stop " + subscriberName + " attributes");
                doAction(subscriberName, Stop);
            }
            SplashUtils.getInstance().stopSplash();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_stopButtonActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm
	//=======================================================
	//=======================================================
    private void doClose() {
        if (modified) {
            //  If modified -> save as default ?
            if (JOptionPane.showConfirmDialog(this,
                    "Save default selected archivers in database?",
                    "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                //  Build list of selected archivers
                List<String> subscribers = new ArrayList<>();
                for (JRadioButton button : radioButtonList) {
                    if (button.isSelected()) {
                        subscribers.add(button.getText());
                    }
                }
                for (String subscriber : subscribers) {
                    System.out.println(subscriber);
                }
                try {
                    DbDatum datum = new DbDatum("ShutdownArchivers");
                    datum.insert(subscribers.toArray(new String[subscribers.size()]));
                    ApiUtil.get_db_obj().put_property(
                            "HdbConfigurator", new DbDatum[] { datum } );
                }
                catch (DevFailed e) {
                    ErrorPane.showErrorMessage(this, e.getMessage(), e);
                }
            }
        }
        System.exit(0);
    }
	//=======================================================
	//=======================================================
    private List<String> getSelectedSubscribers(int action) {
        List<String> subscriberNames = new ArrayList<>();
        String message = actionNames[action] + " archiving for all attributes for:";
        for (JRadioButton radioButton : radioButtonList) {
            if (radioButton.isSelected()) {
                String name = radioButton.getText();
                subscriberNames.add(name);
                message += "\n - " + name;
            }
        }
        if (JOptionPane.showConfirmDialog(this, message,
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.NO_OPTION)
            return null;
        return subscriberNames;
    }
	//=======================================================
	//=======================================================
    private void doAction(String subscriberLabel, int action) throws DevFailed {
        Subscriber subscriber = subscriberMap.getSubscriberByLabel(subscriberLabel);
        String[] attributeNames;
        if (action==Start)
            attributeNames = getAttributes(subscriber, "StoppedList");
        else
            attributeNames = getAttributes(subscriber, "StartedList");
        for (String attributeName :attributeNames) {
            DeviceData argIn = new DeviceData();
            argIn.insert(attributeName);
            subscriber.command_inout("Attribute" + actionNames[action], argIn);
        }
    }
	//=======================================================
	//=======================================================
    private String[] getAttributes(Subscriber subscriber, String actionName) throws DevFailed {
        String attributeName = "Attribute" + actionName;
        DeviceAttribute attribute = subscriber.read_attribute(attributeName);
        if (attribute.hasFailed()) {
            throw new DevFailed(attribute.getErrStack());
        }
        return attribute.extractStringArray();
    }
	//=======================================================
	//=======================================================
    public static void main(String args[]) {
		try {
      		new StartStopArchiving().setVisible(true);
		}
		catch(DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    // End of variables declaration//GEN-END:variables
	//=======================================================

}
