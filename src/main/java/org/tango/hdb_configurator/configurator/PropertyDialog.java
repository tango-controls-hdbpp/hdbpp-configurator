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


package org.tango.hdb_configurator.configurator;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;
import org.tango.hdb_configurator.configurator.strategy.SelectionContextPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


//===============================================================
/**
 * Class Description: Basic dialog archive event properties.
 *
 * @author verdier pascal
 */
//===============================================================


@SuppressWarnings({"MagicConstant"})
public class PropertyDialog extends JDialog implements TangoConst {
    private JFrame parent;
    private SubscriberMap subscriberMap;
    private List<HdbAttribute> attributeList = new ArrayList<>();
    private AttributeProxy  attributeProxy = null;
    private AttributeInfoEx attributeInfoEx;
    private boolean manageProperties;
    private SelectionContextPanel strategyPanel;
    private String nbDayStr;

    private boolean canceled = false;
    private static final int MaxRows = 30;
    //===============================================================
    /**
     * Creates new form PropertyDialog for one attribute
     */
    //===============================================================
    public PropertyDialog(JFrame parent,
                          HdbAttribute attribute,
                          SubscriberMap subscriberMap,
                          String defaultItem) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        this.subscriberMap = subscriberMap;
        this.attributeList.add(attribute);
        initComponents();
        initOwnComponents(defaultItem);

        manageProperties = true;
        displayProperty();

        titleLabel.setText(attribute.getName());
        attributeListScrollPane.setVisible(false);
        pack();
        ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    /**
     * Creates new form PropertyDialog for a list of attributes
     */
    //===============================================================
    public PropertyDialog(JFrame parent, List<HdbAttribute> attributeList,
                          SubscriberMap subscriberMap, String defaultItem) {
        super(parent, true);
        this.parent = parent;
        this.subscriberMap = subscriberMap;
        this.attributeList = attributeList;
        initComponents();
        initOwnComponents(defaultItem);

        manageProperties = false;
        propertyPanel.setVisible(false);

        titleLabel.setVisible(false);
        StringBuilder   sb = new StringBuilder();
        int length = 0;
        for (HdbAttribute attribute : attributeList) {
            sb.append(attribute.getName()).append("\n");
            if (attribute.getName().length()>length)
                length = attribute.getName().length();
        }
        int nbRows = attributeList.size();
        if (attributeList.size()>MaxRows)
            nbRows = MaxRows;
        attributeListArea.setRows(nbRows);
        attributeListArea.setColumns(length + 1);
        attributeListArea.setText(sb.toString().trim());
        attributeListArea.setEditable(false);
        pack();
        ATKGraphicsUtils.centerDialog(this);
    }
    //===============================================================
    //===============================================================
    private void initOwnComponents(String defaultItem) {
        ttlTextField.setEnabled(false);
        nbDayStr = Long.toString(TangoUtils.getDefaultTTL());

        //  Change panel look for property and subscription panels
        propertyPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Event Properties"));
        subscriptionPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Events Subscription"));

        // Add tooltips
        pushedByCodeButton.setToolTipText(Utils.buildTooltip(
                "The TANGO class manages events by push_archive_event method.\n"+
                "The polling period and the event criteria will not be checked when adding to HDB++."));
        startArchivingButton.setToolTipText(Utils.buildTooltip("Select to start archiving at subscription"));
        archiverLabel.setToolTipText(Utils.buildTooltip("Select archiver to manage storage"));
        subscriberComboBox.setToolTipText(Utils.buildTooltip("Select archiver to manage storage"));

        //  Init combo box for subscribers
        subscriberComboBox.removeAllItems();
        List<String> subscriberNames = subscriberMap.getLabelList();
        for (String subscriberName : subscriberNames)
            subscriberComboBox.addItem(subscriberName);
        subscriberComboBox.setSelectedItem(defaultItem);

        //  Add strategy panel
        try {
            Subscriber subscriber =
                    subscriberMap.getSubscriberByLabel((String) subscriberComboBox.getSelectedItem());
            addStrategyPanel(subscriber);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }
    //===============================================================
    //===============================================================
    private void addStrategyPanel(Subscriber subscriber) throws DevFailed {
        Strategy strategy = Strategy.getContextsFromDB(subscriber);
        strategyPanel = new SelectionContextPanel(strategy);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.insets = new java.awt.Insets(0, 30, 0, 0);
        subscriptionPanel.add(strategyPanel, gbc);
    }
    //===============================================================
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        javax.swing.JLabel titleLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        attributeListScrollPane = new javax.swing.JScrollPane();
        attributeListArea = new javax.swing.JTextArea();
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        subscriptionPanel = new javax.swing.JPanel();
        startArchivingButton = new javax.swing.JRadioButton();
        pushedByCodeButton = new javax.swing.JRadioButton();
        subscriberComboBox = new javax.swing.JComboBox<>();
        archiverLabel = new javax.swing.JLabel();
        ttlButton = new javax.swing.JRadioButton();
        ttlTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        propertyPanel = new javax.swing.JPanel();
        javax.swing.JLabel absLbl = new javax.swing.JLabel();
        javax.swing.JLabel relLbl = new javax.swing.JLabel();
        javax.swing.JLabel periodLbl = new javax.swing.JLabel();
        absTxt = new javax.swing.JTextField();
        relTxt = new javax.swing.JTextField();
        eventPeriodTxt = new javax.swing.JTextField();
        javax.swing.JButton resetAbsBtn = new javax.swing.JButton();
        javax.swing.JButton resetRelBtn = new javax.swing.JButton();
        javax.swing.JButton resetPerBtn = new javax.swing.JButton();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        pollingPeriodTxt = new javax.swing.JTextField();
        javax.swing.JLabel dummyLabel = new javax.swing.JLabel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        javax.swing.JButton subscribeBtn = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel1.setText("Archive  Events  for");
        jPanel5.add(titleLabel1);

        topPanel.add(jPanel5, java.awt.BorderLayout.NORTH);

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Attribute Name");
        jPanel2.add(titleLabel);

        attributeListArea.setBackground(new java.awt.Color(240, 240, 240));
        attributeListArea.setColumns(20);
        attributeListArea.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        attributeListArea.setRows(5);
        attributeListScrollPane.setViewportView(attributeListArea);

        jPanel2.add(attributeListScrollPane);

        topPanel.add(jPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        subscriptionPanel.setLayout(new java.awt.GridBagLayout());

        startArchivingButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        startArchivingButton.setSelected(true);
        startArchivingButton.setText("Start Archiving");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        subscriptionPanel.add(startArchivingButton, gridBagConstraints);

        pushedByCodeButton.setText("Event pushed by code");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        subscriptionPanel.add(pushedByCodeButton, gridBagConstraints);

        subscriberComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subscriberComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        subscriptionPanel.add(subscriberComboBox, gridBagConstraints);

        archiverLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        archiverLabel.setText("Archiver: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 22, 0, 0);
        subscriptionPanel.add(archiverLabel, gridBagConstraints);

        ttlButton.setText("Set TTL  ");
        ttlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ttlButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        subscriptionPanel.add(ttlButton, gridBagConstraints);

        ttlTextField.setColumns(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        subscriptionPanel.add(ttlTextField, gridBagConstraints);

        jLabel2.setText(" days");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        subscriptionPanel.add(jLabel2, gridBagConstraints);

        centerPanel.add(subscriptionPanel, java.awt.BorderLayout.NORTH);

        propertyPanel.setLayout(new java.awt.GridBagLayout());

        absLbl.setText("absolute change:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        propertyPanel.add(absLbl, gridBagConstraints);

        relLbl.setText("relative change:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        propertyPanel.add(relLbl, gridBagConstraints);

        periodLbl.setText("event period (ms):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 20, 10);
        propertyPanel.add(periodLbl, gridBagConstraints);

        absTxt.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        propertyPanel.add(absTxt, gridBagConstraints);

        relTxt.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        propertyPanel.add(relTxt, gridBagConstraints);

        eventPeriodTxt.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 20, 10);
        propertyPanel.add(eventPeriodTxt, gridBagConstraints);

        resetAbsBtn.setText("Reset");
        resetAbsBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetAbsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAbsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        propertyPanel.add(resetAbsBtn, gridBagConstraints);

        resetRelBtn.setText("Reset");
        resetRelBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetRelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetRelBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        propertyPanel.add(resetRelBtn, gridBagConstraints);

        resetPerBtn.setText("Reset");
        resetPerBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetPerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetPerBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 20, 10);
        propertyPanel.add(resetPerBtn, gridBagConstraints);

        jLabel4.setText("Attribute polling period (ms):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 15, 10);
        propertyPanel.add(jLabel4, gridBagConstraints);

        pollingPeriodTxt.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 15, 10);
        propertyPanel.add(pollingPeriodTxt, gridBagConstraints);

        centerPanel.add(propertyPanel, java.awt.BorderLayout.SOUTH);

        dummyLabel.setText("     ");
        centerPanel.add(dummyLabel, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        subscribeBtn.setText("Subscribe");
        subscribeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subscribeBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(subscribeBtn);

        jLabel1.setText("        ");
        bottomPanel.add(jLabel1);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void resetPerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetPerBtnActionPerformed
        eventPeriodTxt.setText(Tango_AlrmValueNotSpec);
    }//GEN-LAST:event_resetPerBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void resetRelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetRelBtnActionPerformed
        relTxt.setText(Tango_AlrmValueNotSpec);
    }//GEN-LAST:event_resetRelBtnActionPerformed

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void resetAbsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAbsBtnActionPerformed
        absTxt.setText(Tango_AlrmValueNotSpec);
    }//GEN-LAST:event_resetAbsBtnActionPerformed

    //===============================================================
    /**
     * Verify if value set are coherent and if at least one is set.
     */
    //===============================================================
    private boolean checkValues() {
        if (getNbDays()<0) {
            ErrorPane.showErrorMessage(this, null, new Exception("TTL input syntax error"));
            return false;
        }
        if (manageProperties) {
            try {
                String strValue;
                strValue = absTxt.getText().trim();
                if (!strValue.equals(Tango_AlrmValueNotSpec)) {
                    Double.parseDouble(strValue);
                }
                strValue = relTxt.getText().trim();
                if (!strValue.equals(Tango_AlrmValueNotSpec)) {
                    Double.parseDouble(strValue);
                }
                strValue = eventPeriodTxt.getText().trim();
                if (!strValue.equals(Tango_AlrmValueNotSpec)) {
                    Integer.parseInt(strValue);
                }
                strValue = pollingPeriodTxt.getText().trim();
                if (!strValue.equals("Not Polled")) {
                    Integer.parseInt(strValue);
                }
            } catch (Exception e) {
                ErrorPane.showErrorMessage(this, null, e);
                return false;
            }
        }
        return true;
    }
    //===============================================================
    //===============================================================
    private boolean writeValues() {
        if (manageProperties) {
            try {
                //	Get property values.
                boolean changed = false;
                if (attributeInfoEx.events.arch_event.abs_change!=null &&
                   !attributeInfoEx.events.arch_event.abs_change.equals(absTxt.getText().trim())) {
                    attributeInfoEx.events.arch_event.abs_change = absTxt.getText().trim();
                    changed = true;
                }
                if (attributeInfoEx.events.arch_event.rel_change!=null &&
                   !attributeInfoEx.events.arch_event.rel_change.equals(relTxt.getText().trim())) {
                    attributeInfoEx.events.arch_event.rel_change = relTxt.getText().trim();
                     changed = true;
                }
                if (attributeInfoEx.events.arch_event.period!=null &&
                   !attributeInfoEx.events.arch_event.period.equals(eventPeriodTxt.getText().trim())) {
                    attributeInfoEx.events.arch_event.period = eventPeriodTxt.getText().trim();
                    changed = true;
                }

                //	And set them if have changed
                if (changed)
                    attributeProxy.set_info(new AttributeInfoEx[]{ attributeInfoEx });

                //  Check for polling period
                String s = pollingPeriodTxt.getText().trim();
                if (!s.equals("Not Polled")) {
                    try {
                        int value = Integer.parseInt(s);
                        if (value!=pollingPeriod) {
                            attributeProxy.poll(value);
                        }
                    }
                    catch (NumberFormatException e) {
                        Except.throw_exception("SyntaxError", e.getMessage());
                    }
                }
                return true;
            } catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
                return false;
            }
        }
        else
            return true;
    }

    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void subscribeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subscribeBtnActionPerformed
        if (checkValues()) {
            if (writeValues()) // write properties if needed
                canceled = false;
                doClose();
        }
    }//GEN-LAST:event_subscribeBtnActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        canceled = true;
        doClose();
    }//GEN-LAST:event_cancelBtnActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        canceled = true;
        doClose();
    }//GEN-LAST:event_closeDialog
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void subscriberComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subscriberComboBoxActionPerformed
        if (isVisible()) {
            try {
                //  When subscriber change, the strategies can have changed.
                System.out.println(subscriberComboBox.getSelectedItem());
                Subscriber subscriber = subscriberMap.getSubscriberByLabel(
                        (String) subscriberComboBox.getSelectedItem());
                Strategy strategy = strategyPanel.getStrategy();
                Strategy deviceContext =
                        Strategy.getContextsFromDB(subscriber);
                if (listHasChanged(strategy, deviceContext)) {
                    System.out.println("Change it");
                    subscriptionPanel.remove(strategyPanel);
                    addStrategyPanel(subscriber);
                    pack();
                }
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, e.getMessage(), e);
            }
        }
    }//GEN-LAST:event_subscriberComboBoxActionPerformed
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedParameters")
    private void ttlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ttlButtonActionPerformed
        //  ToDo
        JRadioButton button = (JRadioButton) evt.getSource();
        if (button.isSelected())
            ttlTextField.setText(nbDayStr);
        else {
            //  Get value (as str) for next time
            nbDayStr = ttlTextField.getText();
            ttlTextField.setText("");
        }
        ttlTextField.setEnabled(button.isSelected());
    }//GEN-LAST:event_ttlButtonActionPerformed
    //===============================================================
    //===============================================================
    private long getNbDays() {
        // check if TTL is set
        if (ttlButton.isSelected()) {
            try {
                return Long.parseLong(ttlTextField.getText());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        else
            return 0;
    }
    //===============================================================
    //===============================================================
    private boolean listHasChanged(Strategy strategy1, Strategy strategy2) {
        if (strategy1.size()!=strategy2.size())
            return true;
        //  List of contexts has changed (ES device property != class property)
        for (Context context1 : strategy1) {
            boolean found = false;
            for (Context context2 : strategy2) {
                if (context1.getName().equalsIgnoreCase(context2.getName()))
                    found = true;
            }
            if (!found)
                return true;
        }
        return false;
    }
    //===============================================================
    //===============================================================
    private void doClose() {
        if (parent==null)
            System.exit(0);

        setVisible(false);
        dispose();
    }
    //===============================================================
    //===============================================================
    private int pollingPeriod = 0;
    private void displayProperty() throws DevFailed {
        if (!manageProperties)
            return;
        // get attributeList info
        if (attributeProxy== null)
            attributeProxy = new AttributeProxy(attributeList.get(0).getName());
        attributeInfoEx = attributeProxy.get_info_ex();

        String abs_change;
        String rel_change;
        String period;
        if (attributeInfoEx.events != null && attributeInfoEx.events.arch_event != null) {
            abs_change = attributeInfoEx.events.arch_event.abs_change;
            rel_change = attributeInfoEx.events.arch_event.rel_change;
            period = attributeInfoEx.events.arch_event.period;
        } else {
            abs_change = Tango_AlrmValueNotSpec;
            rel_change = Tango_AlrmValueNotSpec;
            period = Tango_AlrmValueNotSpec;
        }
        absTxt.setText(abs_change);
        relTxt.setText(rel_change);
        eventPeriodTxt.setText(period);

        //  Add polling period
        try {
            pollingPeriod = attributeProxy.get_polling_period();
            pollingPeriodTxt.setText(Integer.toString(pollingPeriod));
        }
        catch (DevFailed e) {
            if (e.errors[0].desc.contains("not polled"))
                pollingPeriodTxt.setText("Not Polled");
            else
                throw e;
        }
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("WeakerAccess")
    public boolean isCanceled() {
        return canceled;
    }
    //===============================================================
    //===============================================================
    public List<HdbAttribute> getHdbAttributes() {
        for (HdbAttribute attribute : attributeList) {
            attribute.setPushedByCode(pushedByCodeButton.isSelected());
            Strategy strategy = strategyPanel.getStrategy();
            attribute.updateUsedContexts(strategy);
            attribute.setTTL(getNbDays()*24); //  in hours
        }
        return attributeList;
    }
    //===============================================================
    //===============================================================
    public String getSubscriber() {
        return (String) subscriberComboBox.getSelectedItem();
    }
    //===============================================================
    //===============================================================


    //===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField absTxt;
    private javax.swing.JLabel archiverLabel;
    private javax.swing.JTextArea attributeListArea;
    private javax.swing.JScrollPane attributeListScrollPane;
    private javax.swing.JTextField eventPeriodTxt;
    private javax.swing.JTextField pollingPeriodTxt;
    private javax.swing.JPanel propertyPanel;
    private javax.swing.JRadioButton pushedByCodeButton;
    private javax.swing.JTextField relTxt;
    private javax.swing.JRadioButton startArchivingButton;
    private javax.swing.JComboBox<String> subscriberComboBox;
    private javax.swing.JPanel subscriptionPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JRadioButton ttlButton;
    private javax.swing.JTextField ttlTextField;
    // End of variables declaration//GEN-END:variables
    //===============================================================



    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
            HdbAttribute attribute =
                    new HdbAttribute("tango://orion.esrf.fr:10000/sys/hqps-accumulator/5a/speed");
            //  Build subscriber map
            SubscriberMap subscriberMap = new SubscriberMap(Utils.getConfiguratorProxy());
            Subscriber subscriber = subscriberMap.getSubscriberList().get(2);
            new PropertyDialog(null, attribute, subscriberMap, subscriber.getLabel()).setVisible(true);
        } catch (Exception e) {
            ErrorPane.showErrorMessage(null, null, e);
        }
    }

}
