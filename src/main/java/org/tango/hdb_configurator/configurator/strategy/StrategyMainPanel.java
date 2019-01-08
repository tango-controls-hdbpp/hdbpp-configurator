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
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

//=======================================================
/**
 *	JFrame Class to display hdb attributes sorted by strategies
 *
 * @author  Pascal Verdier
 */
//=======================================================
public class StrategyMainPanel extends JDialog {
    private Component parent;
    private SubscriberMap subscriberMap;
    /** Hashtable<strategyStr, list<HdbAttribute> > */
    private Hashtable<String, List<HdbAttribute>> strategyMap;
    private List<StrategyAttributeTree> attributeTreeList;

    private static final String[] expandLevels = {
            "Tango database", "Domain", "Family", "Member", "Attribute", };
    private static final Dimension treeDimension = new Dimension(500, 600);
	//=======================================================
    /**
	 *	Creates new form StrategyMainPanel
	 */
	//=======================================================
    public StrategyMainPanel(JFrame parent, String configuratorDeviceName) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        buildForm(configuratorDeviceName);
    }
    //=======================================================
    //=======================================================
    public StrategyMainPanel(JDialog parent) throws DevFailed {
        super(parent, true);
        this.parent = parent;
        buildForm(null);
    }
    //=======================================================
    //=======================================================
    private void buildForm(String configuratorDeviceName) throws DevFailed {
        SplashUtils.getInstance().startSplash();
        SplashUtils.getInstance().increaseSplashProgress(10, "Building GUI");
        setTitle(Utils.getInstance().getApplicationName());

        initComponents();
        setIconImage(Utils.getInstance().getIcon("hdb++.gif").getImage());
        //  Get subscriber proxies
        if (configuratorDeviceName==null)
            configuratorDeviceName = TangoUtils.getConfiguratorDeviceName();
        subscriberMap = Utils.getSubscriberMap(configuratorDeviceName);
        buildStrategyLists();
        buildTabbedPanes();

        pack();
        ATKGraphicsUtils.centerDialog(this);
        SplashUtils.getInstance().stopSplash();
	}
    //=======================================================
    //=======================================================
    void setPane(String strategyStr) {
        for (int i=0 ; i<tabbedPane.getTabCount() ; i++) {
            String title = tabbedPane.getTitleAt(i);
            if (title.startsWith(strategyStr))
                tabbedPane.setSelectedIndex(i);
        }
    }
    //=======================================================
    //=======================================================
    String  hasAttributeUsingContext(Context context) {
        Collection<String> strategies = strategyMap.keySet();
        for (String strategyStr : strategies) {
            StringTokenizer stk = new StringTokenizer(strategyStr, "|");
            while (stk.hasMoreTokens()) {
                String contextName = stk.nextToken();
                if (contextName.equalsIgnoreCase(context.getName())) {
                    return strategyStr;
                }
            }
        }
        return null;
    }
    //=======================================================
    //=======================================================
    private void buildTabbedPanes() {
        //  remove panes if any before re create
        tabbedPane.removeAll();
        attributeTreeList = new ArrayList<>();

        Collection<String> strategies = strategyMap.keySet();
        int progress = 25;
        int i = 0;
        //  Add a pane for each strategy
        for (String strategy : strategies) {
            progress += 25;
            SplashUtils.getInstance().increaseSplashProgress(progress, "Building tree for " + strategy);
            List<HdbAttribute> attributeList = strategyMap.get(strategy);
            JPanel panel = new JPanel(new BorderLayout());

            //  Add a combo box on top to select expand level
            JComboBox<String> comboBox = new JComboBox<>(expandLevels);
            comboBox.setSelectedIndex(1);   //  domain
            comboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    comboBoxActionPerformed(evt);
                }
            });
            JPanel topPanel = new JPanel();
            topPanel.add(new JLabel("Expand Level:  "));
            topPanel.add(comboBox);
            panel.add(topPanel, BorderLayout.NORTH);

            //  Add a button to change strategy for selection
            JButton btn = new JButton("Change selection strategy");
            btn.setToolTipText("Select attributes, members or families to change strategy");
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    changeStrategyActionPerformed();
                }
            });
            topPanel.add(new JLabel("       "));
            topPanel.add(btn);

            //  Then add a JTree to display attributes for this strategy
            StrategyAttributeTree tree = new StrategyAttributeTree(this, strategy, attributeList);
            attributeTreeList.add(tree);
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView(tree);
            scrollPane.setPreferredSize(treeDimension);
            panel.add(scrollPane, BorderLayout.CENTER);
            tabbedPane.add(panel);
            tabbedPane.setTitleAt(i++, strategy + " (" +attributeList.size() + ")");
        }
    }
	//=======================================================
	//=======================================================
    private void buildStrategyLists() throws DevFailed {
        SplashUtils.getInstance().increaseSplashProgress(25, "Reading devices");
        strategyMap = new Hashtable<>();
        List<Subscriber> subscriberList = subscriberMap.getSubscriberList();
        //  For each subscriber
        for (Subscriber subscriber : subscriberList) {
            try {
                //  Get subscriber attribute and strategy list
                String[] attributeNames = {"AttributeList", "AttributeStrategyList",};
                DeviceAttribute[] attributes = subscriber.read_attribute(attributeNames);
                buildStrategyLists(subscriber.getStrategy(),
                        attributes[0].extractStringArray(),
                        attributes[1].extractStringArray());
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
        if (strategyMap.size()==0)
            Except.throw_exception("EmptyStrategies", "No strategy defined.");
    }
    //=======================================================
    //=======================================================
    private void buildStrategyLists(Strategy strategy,
                                    String[] attributeNames, String[] strategyNames) {
        //  For strategy name
        for (int i=0 ; i<strategyNames.length ; i++) {
            List<HdbAttribute> list = strategyMap.get(strategyNames[i]);
            //  If not already defined
            if (list==null) {
                //  Create it with an empty attribute list
                list = new ArrayList<>();
                strategyMap.put(strategyNames[i], list);
            }
            //  Create a HdbAttribute object with this strategy and fill list
            HdbAttribute attribute = new HdbAttribute(attributeNames[i]);
            attribute.clear();
            attribute.setStrategy(strategy, strategyNames[i]);
            list.add(attribute);
        }
    }
	//=======================================================
	//=======================================================
    private void changeStrategyActionPerformed() {
        final int selection = tabbedPane.getSelectedIndex();
        List<HdbAttribute> attributeList =
                attributeTreeList.get(selection).getSelectedAttributes();
        if (attributeList.isEmpty()) {
            ErrorPane.showErrorMessage(this, "No selection",
                    new Exception("Select attributes, members or families to change strategy"));
            return;
        }

        try {
            //  Get a copy of strategies in case of cancel
            List<Strategy> strategyCopies = new ArrayList<>();
            for (HdbAttribute attribute : attributeList)
                strategyCopies.add(attribute.getStrategyCopy());

            //  Check if strategies are not different and open strategy panel
            Strategy strategy = checkSubscriberStrategies(attributeList);
            if (strategy==null)
                    return;
            SelectionStrategiesDialog dialog =
                    new SelectionStrategiesDialog(this,
                            attributeList, strategy, Utils.getConfiguratorProxy());
            if (dialog.showDialog()==JOptionPane.OK_OPTION) {
                SplashUtils.getInstance().startSplash();
                SplashUtils.getInstance().setSplashProgress(5, "Setting attribute strategy");
                SplashUtils.getInstance().startAutoUpdate();
                //  IF OK -> apply new strategies on subscriber
                dialog.setAttributeStrategy();
                try { Thread.sleep(3000); } catch (InterruptedException e) { /*  */ }
            }
            else {
                //  CANCEL -> apply original strategies
                for (int i=0 ; i<attributeList.size() ; i++)
                    attributeList.get(i).updateUsedContexts(strategyCopies.get(i));
                return;
            }
        }
        catch(Exception e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
        //  re initialize panes a bit later
        //  to have receive strategy events
        try {
            buildStrategyLists();
            new ReInitializePanesThread(selection).start();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }
	//=======================================================
	//=======================================================
    private Strategy checkSubscriberStrategies(List<HdbAttribute> attributeList) {
        //  Get subscribers and check if strategies are not different
        List<Subscriber> subscriberList = subscriberMap.getSubscriberList();
        List<Subscriber> subscribers = new ArrayList<>();
        for (HdbAttribute attribute : attributeList) {
            for (Subscriber subscriber : subscriberList) {
                if (!subscribers.contains(subscriber) && subscriber.manageAttribute(attribute.getName())) {
                    subscribers.add(subscriber);
                }
            }
        }
        Strategy strategy = subscribers.get(0).getStrategy();
        for (Subscriber subscriber : subscribers) {
            Strategy strategy2 = subscriber.getStrategy();
            boolean ok = strategy.size()==strategy2.size();
            for (int i=0 ; ok && i<strategy.size() ; i++) {
                ok = (strategy.get(i).getName().equalsIgnoreCase(strategy2.get(i).getName()));
            }
            if (!ok) {
                ErrorPane.showErrorMessage(this, null, new Exception("Subscribers have different strategies"));
                return null;
            }
        }
        return strategy;
    }
	//=======================================================
	//=======================================================
    private void comboBoxActionPerformed(ActionEvent event) {
        //noinspection unchecked
        JComboBox<String> comboBox = (JComboBox<String>) event.getSource();
        int index = comboBox.getSelectedIndex();
        attributeTreeList.get(tabbedPane.getSelectedIndex()).expandChildren(index);
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

        tabbedPane = new javax.swing.JTabbedPane();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JButton dismissButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        dismissButton.setText("Dismiss");
        dismissButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dismissButtonActionPerformed(evt);
            }
        });
        jPanel1.add(dismissButton);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("Strategies");
        jPanel2.add(jLabel1);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void dismissButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissButtonActionPerformed
        doClose();
    }//GEN-LAST:event_dismissButtonActionPerformed
	//=======================================================
	//=======================================================
    private void doClose() {
        if (parent==null)
            System.exit(0);
        else {
            setVisible(false);
            dispose();
        }
    }
	//=======================================================
    /**
     * @param args the command line arguments
     */
	//=======================================================
    public static void main(String[] args) {
		try {
      		new StrategyMainPanel(null, null).setVisible(true);
		}
		catch(DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
	//=======================================================

    //=======================================================
    /*
     *  re initialize panes a bit later
     *  to have receive strategy events
     */
    //=======================================================
    private class ReInitializePanesThread extends Thread {
        private int selection;
        private ReInitializePanesThread(int selection) { this.selection = selection; }
        public void run() {
            buildTabbedPanes();
            while (selection>=tabbedPane.getTabCount() && selection>0)
                selection--;
            tabbedPane.setSelectedIndex(selection);
            SplashUtils.getInstance().stopSplash();
        }
    }
    //=======================================================
    //=======================================================
}
