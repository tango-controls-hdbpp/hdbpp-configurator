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


package org.tango.hdb_configurator.configurator;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;
import org.tango.hdb_configurator.configurator.strategy.EditStrategiesDialog;
import org.tango.hdb_configurator.configurator.strategy.SelectionStrategiesDialog;
import org.tango.hdb_configurator.configurator.strategy.StrategyMainPanel;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//=======================================================
/**
 *	JFrame Class to display about HDB++ configuration
 *  and a tree to browse a control system to add attribute
 *  to a selected archive event subscriber to be written in HDB++.
 *
 * @author  Pascal Verdier
 */
//=======================================================
@SuppressWarnings({"MagicConstant", "Convert2Diamond", "WeakerAccess"})
public class HdbConfigurator extends JFrame {
    private JFrame parent;
    private DeviceProxy configuratorProxy;
    private ListPopupMenu  menu = new ListPopupMenu();
    private JScrollPane    treeScrollPane;
    private AttributeTree  attributeTree;
    private List<AttributeTable> attributeTableList = new ArrayList<>();
    private SubscriberMap  subscriberMap;
    private JFileChooser   fileChooser = null;
    private JFrame         diagnosticsPanel = null;
    private List<String>   tangoHostList;
    private UpdateListThread updateListThread;

    private static final String[]  strAttributeState = {
            "Started Attributes","Paused Attributes", "Stopped Attributes"
    };
    private static final Dimension treeDimension = new Dimension(350, 400);
    private static final Dimension tableDimension = new Dimension(600, 400);
    //=======================================================
    /**
	 *	Creates new form HdbConfigurator
	 */
	//=======================================================
    public HdbConfigurator(JFrame parent) throws DevFailed {
        this(parent, false);
    }
    //=======================================================
    /**
	 *	Creates new form HdbConfigurator
	 */
	//=======================================================
    public HdbConfigurator(JFrame parent, boolean parentIsDiagnostic) throws DevFailed {
        this.parent = parent;
        SplashUtils.getInstance().startSplash();
        SplashUtils.getInstance().increaseSplashProgress(10, "Building GUI");
        setTitle(Utils.getInstance().getApplicationName());

        initComponents();
        initOwnComponents();
        if (parentIsDiagnostic)
            diagnosticsPanel = parent;
        ManageAttributes.setDisplay(true);
        updateListThread = new UpdateListThread();
        updateListThread.start();

        //  Check expert mode
        String expert = System.getenv("EXPERT_MODE");
        if (expert!=null && expert.equals("false")) {
            addSubscriberItem.setVisible(false);
            removeSubscriberItem.setVisible(false);
            manageAliasesItem.setVisible(false);
            contextsItem.setVisible(false);
        }

        //  Check if change TANGO_HOST available
        String onlyOnCS = System.getenv("SingleControlSystem");
        if (onlyOnCS!=null && onlyOnCS.equals("true"))
            changeCsItem.setVisible(false);
        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
        SplashUtils.getInstance().stopSplash();
	}
    //=======================================================
    //=======================================================
    private void initOwnComponents() throws DevFailed {
        buildTable();
        initSubscribers();
        buildAttributeTree();

        //  Build Title
        String title = titleLabel.getText();
        String archiveName = TangoUtils.getArchiveName(configuratorProxy);
        if (!archiveName.isEmpty()) {
            title += "  ("+archiveName+")";
            titleLabel.setText(title);

            //  Check if extraction available
            String s = System.getenv("HdbExtraction");
            if (s!=null && s.equals("true"))
                System.setProperty("HDB_TYPE", archiveName);
        }
        ImageIcon icon = Utils.getInstance().getIcon("hdb++.gif", 0.75);
        titleLabel.setIcon(icon);
        setIconImage(icon.getImage());

        //  Set device filter
        String filter = System.getenv("DeviceFilter");
        if (filter!=null && !filter.isEmpty()) {
            deviceFilterText.setText(filter);
            deviceFilterTextActionPerformed(null);
        }
        else
            deviceFilterText.setText("*/*/*");
    }
	//=======================================================
	//=======================================================
    private void buildTable() {
        SplashUtils.getInstance().increaseSplashProgress(15, "Reading devices");

        JScrollPane[] scrollPanes = new JScrollPane[] {
                startedScrollPane, pausedScrollPane, stoppedScrollPane
        };
        //  Build attribute tables.
        for (int i=Subscriber.ATTRIBUTE_STARTED ; i<=Subscriber.ATTRIBUTE_STOPPED ; i++) {
            AttributeTable table = new AttributeTable();
            scrollPanes[i].setViewportView(table);
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    tableActionPerformed(evt);
                }
            });
            attributeTableList.add(table);
        }

        //  Set size
        tableDimension.width = attributeTableList.get(0).getTableWidth();
        startedScrollPane.setPreferredSize(tableDimension);
    }
    //=======================================================
    //=======================================================
    private void initSubscribers() throws DevFailed {
        SplashUtils.getInstance().increaseSplashProgress(25, "Reading devices");
        //  get configurator and subscriber proxies
        configuratorProxy = Utils.getConfiguratorProxy();
        subscriberMap = Utils.getSubscriberMap(configuratorProxy.name(), true);

        //  Check if at least one subscriber is defined
        if (subscriberMap.size()==0) {
            addSubscriber();
        }

        //  Get subscriber labels if any
        archiverComboBox.removeAllItems();
        for (String subscriberName : subscriberMap.getLabelList())
            archiverComboBox.addItem(subscriberName);

        //  Get used tango host list (used later to change tango host)
        tangoHostList = subscriberMap.getTangoHostList();

        if (subscriberMap.size()==0)
            archiverLabel.setText("No subscriber defined ");
        else
        if (subscriberMap.size()==1)
            archiverLabel.setText("1 subscriber: ");
        else
            archiverLabel.setText(subscriberMap.size() + " subscribers: ");
    }
    //=======================================================
    //=======================================================
    private void buildAttributeTree() throws DevFailed {
        //  Add a tree to select attribute
        SplashUtils.getInstance().increaseSplashProgress(50, "Building Tree");
        attributeTree = new AttributeTree(this, TangoUtils.getEventTangoHost());
        treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(attributeTree);
        treeScrollPane.setPreferredSize(treeDimension);
        attrTreePanel.add(treeScrollPane, BorderLayout.CENTER);

        searchButton.setText("");
        searchButton.setIcon(Utils.getInstance().getIcon("search.gif"));
        addAttributeButton.setEnabled(false);
    }
    //=======================================================
    //=======================================================
    public DeviceProxy getConfiguratorProxy() {
        return configuratorProxy;
    }
	//=======================================================
	//=======================================================
    public void changeTangoHost(String tangoHost) {
        try {
            Selector    selector = new Selector(this,
                    "Change Control System", "TANGO_HOST ?", tangoHostList, tangoHost);
            String  newTangoHost = selector.showDialog();
            if (newTangoHost!=null && !newTangoHost.isEmpty() && !newTangoHost.equals(tangoHost)) {
                tangoHost = TangoUtils.getTangoHost(newTangoHost);

                //  Check if it is a new one
                if (!tangoHostList.contains(tangoHost))
                    tangoHostList.add(tangoHost);

                //  Remove old tree
                if (attributeTree!=null) {
                    attributeTree.removeAll();
                    treeScrollPane.remove(attributeTree);
                }
                //  And finally, create the new one
                attributeTree = new AttributeTree(this, tangoHost);
                treeScrollPane.setViewportView(attributeTree);
                deviceFilterText.setText("*/*/*");
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
	//=======================================================
	//=======================================================
    private void tableActionPerformed(MouseEvent event) {

        try {
            //  Check if subscriber is faulty
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(
                    (String) archiverComboBox.getSelectedItem());
            if (subscriber.isFaulty())
                return;
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
        AttributeTable table = (AttributeTable) event.getSource();
        Point clickedPoint = new Point(event.getX(), event.getY());
        int selectedRow = table.rowAtPoint(clickedPoint);
        int selectedCol = table.columnAtPoint(clickedPoint);
        int mask = event.getModifiers();
        //  Check button clicked
        if ((mask & MouseEvent.BUTTON1_MASK)!=0 && event.getClickCount()==2) {
            try {
                //  Double click --> change strategy
                if (selectedCol==AttributeTable.ATTRIBUTE_TTL) {
                    changeAttributeTTL(table, selectedRow);
                }
                else {
                    changeArchivingStrategy(table, selectedRow);
                }
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, e.getMessage(), e);
            }
        }
        else
        if ((event.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            //  Display menu for selected attributes
            int[] rows = table.getSelectedRows();
            if (rows.length>0) {
                menu.showMenu(event, table.getSelectedAttributes());
            }
        }
    }
	//=======================================================
	//=======================================================
    private void changeAttributeTTL(AttributeTable table, int row) throws DevFailed {
        List<HdbAttribute> attributes = new ArrayList<>(1);
        attributes.add(table.getAttribute(row));
        changeAttributeTTL(attributes);
    }
	//=======================================================
    //=======================================================
    private void changeAttributeTTL(List<HdbAttribute> hdbAttributeList) throws DevFailed {
        String subscriberName = (String) archiverComboBox.getSelectedItem();
        Subscriber subscriber = subscriberMap.getSubscriberByLabel(subscriberName);
        new TTLDialog(this, hdbAttributeList, subscriber).setVisible(true);
    }
	//=======================================================
	//=======================================================
    private void changeArchivingStrategy(AttributeTable table, int row) {
        List<HdbAttribute> attributes = new ArrayList<>(1);
        attributes.add(table.getAttribute(row));
        changeArchivingStrategy(attributes);
    }
	//=======================================================
	//=======================================================
    private void changeArchivingStrategy(List<HdbAttribute> attributeList) {
        try {
            //  Get a copy of strategies in case of cancel
            List<Strategy> strategyCopies = new ArrayList<>();
            for (HdbAttribute attribute : attributeList)
                strategyCopies.add(attribute.getStrategyCopy());

            //  Get subscriber and display attributes and strategies
            String subscriberName = (String) archiverComboBox.getSelectedItem();
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(subscriberName);
            SelectionStrategiesDialog dialog =
                new SelectionStrategiesDialog(this,
                        attributeList, subscriber.getStrategy(), configuratorProxy);
            if (dialog.showDialog()==JOptionPane.OK_OPTION) {
                //  IF OK -> apply new strategies on subscriber
                dialog.setAttributeStrategy();
                //  And fire specified table
                attributeTableList.get(tabbedPane.getSelectedIndex()).fireTableDataChanged();
            }
            else {
                //  CANCEL -> apply original strategies
                for (int i=0 ; i<attributeList.size() ; i++)
                    attributeList.get(i).updateUsedContexts(strategyCopies.get(i));
            }
        }
        catch(Exception e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }
	//=======================================================
	//=======================================================
    private void updateAttributeList(Subscriber subscriber) {
        JLabel[] labels = new JLabel[] {
                startedAttrLabel, pausedAttrLabel, stoppedAttrLabel
        };
        //  Get displayed list
        int selection =  tabbedPane.getSelectedIndex();
        List<HdbAttribute> attributeList = subscriber.getAttributeList(selection, true);
        AttributeTable table = attributeTableList.get(selection);
        updateAttributeList(table,
                        attributeList, labels[selection], startedScrollPane);
        //  Update pane label
        for (int i=Subscriber.ATTRIBUTE_STARTED ; i<=Subscriber.ATTRIBUTE_STOPPED ; i++)
           updatePaneTitle(subscriber, i);
    }

	//=======================================================
	//=======================================================
    private void updatePaneTitle(Subscriber subscriber, int index) {
        int nb = subscriber.getAttributeList(index, false).size();
        tabbedPane.setTitleAt(index, nb + " " + strAttributeState[index]);
    }
	//=======================================================
	//=======================================================
    private void updateAttributeList(AttributeTable attributeTable,
                                     List<HdbAttribute> attributes,
                                     JLabel jLabel,
                                     JScrollPane scrollPane) {

        //  Display attributes in attributeTable
        HdbAttributeComparator.sort(attributes);
        attributeTable.updateAttributeList(attributes);

        String s = (attributes.size() > 1) ? "s" : "";
        jLabel.setText(Integer.toString(attributes.size()) + " attribute" + s);

        //  move horizontal scroll bar to see end of attribute name
        JScrollBar horizontal = scrollPane.getVerticalScrollBar();
        horizontal.setValue(horizontal.getMaximum());
    }
	//=======================================================
	//=======================================================
    public void displayPathInfo(String tangoHost, String attributeName) {

        //  Check if attribute name (or just domain, family,...)
        StringTokenizer stk = new StringTokenizer(attributeName, "/");
        addAttributeButton.setEnabled(stk.countTokens()==4);
        if(stk.countTokens()<4) {
            attributeField.setText(attributeName);
            propertiesArea.setText("");
            return;
        }

        String  s = attributeField.getText();
        if (!s.equals(attributeName)) {
            //  Update attribute field
            attributeField.setText(attributeName);

            //  Display properties and polling status
            if (!attributeName.isEmpty()) {
                try {
                    propertiesArea.setText(TangoUtils.getAttPollingInfo(tangoHost, attributeName) +"\n" +
                            TangoUtils.getEventProperties(tangoHost, attributeName));
                }
                catch (DevFailed e) {
                    propertiesArea.setText(Except.str_exception(e));
                }
            }
        }
    }
	//=======================================================
	//=======================================================
    private void moveAttributeToSubscriber(String targetSubscriberLabel,
                                           List<HdbAttribute> attributeList) {
        try {
            System.out.println("Move to " + targetSubscriberLabel);
            Subscriber targetSubscriber = subscriberMap.getSubscriberByLabel(targetSubscriberLabel);

            //  Before everything, check if target is alive
            targetSubscriber.ping();

            //  Check attribute strategies
            if (checkStrategyCompatibility(attributeList, targetSubscriber)) {
                //  Then remove attributes to subscribe and add to another
                SplashUtils.getInstance().startSplash();
                SplashUtils.getInstance().setSplashProgress(10, "Removing/Adding attributes");
                for (HdbAttribute attribute : attributeList) {
                    SplashUtils.getInstance().increaseSplashProgressForLoop(
                            attributeList.size(), "Removing/adding " + attribute.getName());
                    //  Not bug less !
                    ArchiverUtils.moveAttribute(configuratorProxy, attribute, targetSubscriber.name);
                }
                //  Wait a bit. Add is done by a thread --> DevFailed(Not subscribed)
                SplashUtils.getInstance().setSplashProgress(30, "Wait a while");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { /* */ }

                //  Set combo box selection to src subscriber
                archiverComboBox.setSelectedItem(targetSubscriberLabel);

                //  And update lists
                manageSubscriberChanged(targetSubscriberLabel);
                SplashUtils.getInstance().stopSplash();
            }
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //=======================================================
    //=======================================================
    private boolean checkStrategyCompatibility(
            List<HdbAttribute> attributeList, Subscriber targetSubscriber) throws DevFailed  {
        //  Get un compatible attribute list
        List<HdbAttribute> unCompatibleList = targetSubscriber.checkAttributeStrategies(attributeList);

        if (unCompatibleList.isEmpty())
            return true;

        //  Else attribute(s) not compatible found
        //  Open a selection panel
        SelectionStrategiesDialog dialog =
                new SelectionStrategiesDialog(this, unCompatibleList, targetSubscriber.getStrategy());
        if (dialog.showDialog()==JOptionPane.CANCEL_OPTION)
            return false;

        //  Else OK, get new strategies
        unCompatibleList = dialog.getHdbAttributeList();

        //  Update attribute strategies
        for (HdbAttribute attribute : attributeList) {
            for (HdbAttribute unCompatible : unCompatibleList) {
                if (attribute.getName().equals(unCompatible.getName())) {
                    attribute.clear();
                    attribute.addAll(unCompatible);
                }
            }
        }
        return true;
    }
	//=======================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
        attrTreePanel = new javax.swing.JPanel();
        javax.swing.JPanel addingPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        attributeField = new javax.swing.JTextField();
        addAttributeButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        javax.swing.JPanel statusPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        deviceFilterText = new javax.swing.JTextField();
        javax.swing.JScrollPane propertiesScrollPane = new javax.swing.JScrollPane();
        propertiesArea = new javax.swing.JTextArea();
        javax.swing.JPanel rightPanel = new javax.swing.JPanel();
        javax.swing.JPanel archiverPanel = new javax.swing.JPanel();
        archiverLabel = new javax.swing.JLabel();
        archiverComboBox = new javax.swing.JComboBox<>();
        tabbedPane = new javax.swing.JTabbedPane();
        javax.swing.JPanel startedPanel = new javax.swing.JPanel();
        javax.swing.JPanel startedTopPanel = new javax.swing.JPanel();
        javax.swing.JLabel filterLabel = new javax.swing.JLabel();
        startedFilterText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        startedAttrLabel = new javax.swing.JLabel();
        startedScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel pausedPanel = new javax.swing.JPanel();
        javax.swing.JPanel pausedTopPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        pausedFilterText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        pausedAttrLabel = new javax.swing.JLabel();
        pausedScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel stoppedPanel = new javax.swing.JPanel();
        javax.swing.JPanel stoppedTopPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        stoppedFilterText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        stoppedAttrLabel = new javax.swing.JLabel();
        stoppedScrollPane = new javax.swing.JScrollPane();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem openItem = new javax.swing.JMenuItem();
        changeCsItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem diagnosticsItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenu toolMenu = new javax.swing.JMenu();
        addSubscriberItem = new javax.swing.JMenuItem();
        removeSubscriberItem = new javax.swing.JMenuItem();
        manageAliasesItem = new javax.swing.JMenuItem();
        contextsItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem byStrategiesItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem principleItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem releaseNoteItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        attrTreePanel.setMinimumSize(new java.awt.Dimension(400, 167));
        attrTreePanel.setLayout(new java.awt.BorderLayout());

        addingPanel.setLayout(new java.awt.GridBagLayout());

        titleLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        titleLabel.setText("HDB++ Configurator");
        jPanel4.add(titleLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        addingPanel.add(jPanel4, gridBagConstraints);

        attributeField.setColumns(26);
        attributeField.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        attributeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        addingPanel.add(attributeField, gridBagConstraints);

        addAttributeButton.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        addAttributeButton.setText(" + ");
        addAttributeButton.setToolTipText("Add selection to HDB");
        addAttributeButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addAttributeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAttributeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        addingPanel.add(addAttributeButton, gridBagConstraints);

        searchButton.setText("Search");
        searchButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 0);
        addingPanel.add(searchButton, gridBagConstraints);

        attrTreePanel.add(addingPanel, java.awt.BorderLayout.NORTH);

        statusPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Device Filter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        statusPanel.add(jLabel1, gridBagConstraints);

        deviceFilterText.setColumns(20);
        deviceFilterText.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        deviceFilterText.setText("*/*/*");
        deviceFilterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deviceFilterTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        statusPanel.add(deviceFilterText, gridBagConstraints);

        propertiesArea.setEditable(false);
        propertiesArea.setColumns(35);
        propertiesArea.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        propertiesArea.setRows(6);
        propertiesScrollPane.setViewportView(propertiesArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        statusPanel.add(propertiesScrollPane, gridBagConstraints);

        attrTreePanel.add(statusPanel, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(attrTreePanel);

        rightPanel.setLayout(new java.awt.BorderLayout());

        archiverLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        archiverLabel.setText("Archivers:");
        archiverPanel.add(archiverLabel);

        archiverComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                archiverComboBoxActionPerformed(evt);
            }
        });
        archiverPanel.add(archiverComboBox);

        rightPanel.add(archiverPanel, java.awt.BorderLayout.NORTH);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        startedPanel.setLayout(new java.awt.BorderLayout());

        filterLabel.setText("Filter    tango://");
        startedTopPanel.add(filterLabel);

        startedFilterText.setColumns(25);
        startedFilterText.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        startedFilterText.setText("*/*/*/*/*");
        startedFilterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startedFilterTextActionPerformed(evt);
            }
        });
        startedTopPanel.add(startedFilterText);

        jLabel2.setText("   ");
        startedTopPanel.add(jLabel2);

        startedAttrLabel.setText("Attributes");
        startedTopPanel.add(startedAttrLabel);

        startedPanel.add(startedTopPanel, java.awt.BorderLayout.NORTH);
        startedPanel.add(startedScrollPane, java.awt.BorderLayout.CENTER);

        tabbedPane.addTab("Started Attributes", startedPanel);

        pausedPanel.setLayout(new java.awt.BorderLayout());

        jLabel4.setText("Filter    tango://");
        pausedTopPanel.add(jLabel4);

        pausedFilterText.setColumns(25);
        pausedFilterText.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        pausedFilterText.setText("*/*/*/*/*");
        pausedFilterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pausedFilterTextActionPerformed(evt);
            }
        });
        pausedTopPanel.add(pausedFilterText);

        jLabel6.setText("   ");
        pausedTopPanel.add(jLabel6);

        pausedAttrLabel.setText("Attributes");
        pausedTopPanel.add(pausedAttrLabel);

        pausedPanel.add(pausedTopPanel, java.awt.BorderLayout.NORTH);
        pausedPanel.add(pausedScrollPane, java.awt.BorderLayout.CENTER);

        tabbedPane.addTab("Paused Attributes", pausedPanel);

        stoppedPanel.setLayout(new java.awt.BorderLayout());

        jLabel5.setText("Filter    tango://");
        stoppedTopPanel.add(jLabel5);

        stoppedFilterText.setColumns(25);
        stoppedFilterText.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        stoppedFilterText.setText("*/*/*/*/*");
        stoppedFilterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stoppedFilterTextActionPerformed(evt);
            }
        });
        stoppedTopPanel.add(stoppedFilterText);

        jLabel7.setText("   ");
        stoppedTopPanel.add(jLabel7);

        stoppedAttrLabel.setText("Attributes");
        stoppedTopPanel.add(stoppedAttrLabel);

        stoppedPanel.add(stoppedTopPanel, java.awt.BorderLayout.NORTH);
        stoppedPanel.add(stoppedScrollPane, java.awt.BorderLayout.CENTER);

        tabbedPane.addTab("Stopped Attributes", stoppedPanel);

        rightPanel.add(tabbedPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(rightPanel);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openItem.setMnemonic('O');
        openItem.setText("Open Attribute List");
        openItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openItemActionPerformed(evt);
            }
        });
        fileMenu.add(openItem);

        changeCsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        changeCsItem.setMnemonic('T');
        changeCsItem.setText("Change TANGO_HOST");
        changeCsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeCsItemActionPerformed(evt);
            }
        });
        fileMenu.add(changeCsItem);

        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exitItem.setMnemonic('E');
        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");

        diagnosticsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        diagnosticsItem.setMnemonic('D');
        diagnosticsItem.setText("HDB++ Diagnostics");
        diagnosticsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diagnosticsItemActionPerformed(evt);
            }
        });
        viewMenu.add(diagnosticsItem);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setMnemonic('T');
        jMenuItem1.setText("TTL attribute list");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        viewMenu.add(jMenuItem1);

        menuBar.add(viewMenu);

        toolMenu.setText("Tools");

        addSubscriberItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, java.awt.event.InputEvent.CTRL_MASK));
        addSubscriberItem.setMnemonic('A');
        addSubscriberItem.setText("Add Subscriber");
        addSubscriberItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSubscriberItemActionPerformed(evt);
            }
        });
        toolMenu.add(addSubscriberItem);

        removeSubscriberItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, java.awt.event.InputEvent.CTRL_MASK));
        removeSubscriberItem.setMnemonic('R');
        removeSubscriberItem.setText("Remove Subscriber");
        removeSubscriberItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSubscriberItemActionPerformed(evt);
            }
        });
        toolMenu.add(removeSubscriberItem);

        manageAliasesItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        manageAliasesItem.setMnemonic('M');
        manageAliasesItem.setText("Manage Subscriber Aliases");
        manageAliasesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageAliasesItemActionPerformed(evt);
            }
        });
        toolMenu.add(manageAliasesItem);

        contextsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        contextsItem.setMnemonic('x');
        contextsItem.setText("Manage Strategies & Contexts");
        contextsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextsItemActionPerformed(evt);
            }
        });
        toolMenu.add(contextsItem);

        byStrategiesItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        byStrategiesItem.setMnemonic('x');
        byStrategiesItem.setText("Manage Attributes by Strategies");
        byStrategiesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                byStrategiesItemActionPerformed(evt);
            }
        });
        toolMenu.add(byStrategiesItem);

        menuBar.add(toolMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("help");

        principleItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        principleItem.setMnemonic('P');
        principleItem.setText("HDB++ Principle");
        principleItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                principleItemActionPerformed(evt);
            }
        });
        helpMenu.add(principleItem);

        releaseNoteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        releaseNoteItem.setMnemonic('A');
        releaseNoteItem.setText("Release Notes");
        releaseNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseNoteItemActionPerformed(evt);
            }
        });
        helpMenu.add(releaseNoteItem);

        aboutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutItem.setMnemonic('A');
        aboutItem.setText("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        doClose();
    }//GEN-LAST:event_exitItemActionPerformed

	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        String  message = "This application is able to configure HDB++\n" +
                "It is used to Add attributes to subscriber and\n" +
                "Start and Stop HDB filling for selected attributes\n" +
                "\nIt manages " + subscriberMap.size() + " event subscriber devices\n" +
                "\nPascal Verdier - ESRF - Software Group";
        JOptionPane.showMessageDialog(this, message, "Help Window", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void archiverComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_archiverComboBoxActionPerformed
        String  archiverLabel = (String) archiverComboBox.getSelectedItem();
        if (archiverLabel==null)
            return;
        manageSubscriberChanged(archiverLabel);
    }
    //=======================================================
    //=======================================================
    private void manageSubscriberChanged(String archiverLabel) {
        try {
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(archiverLabel);
            startedFilterText.setText(subscriber.getStartedFilter());
            stoppedFilterText.setText(subscriber.getStoppedFilter());
            pausedFilterText.setText(subscriber.getPausedFilter());

            updateAttributeList(subscriberMap.getSubscriberByLabel(archiverLabel));
        }
        catch (DevFailed e) {
            List<HdbAttribute> attributes = new ArrayList<>();
            for (AttributeTable table : attributeTableList)
                table.updateAttributeList(attributes);
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_archiverComboBoxActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void attributeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeFieldActionPerformed
        //  If it is an attribute name, add it, otherwise search it
        String attributeName = attributeField.getText();
        StringTokenizer stk = new StringTokenizer(attributeName, "/");
        if (stk.countTokens()==4)
            addSpecifiedAttribute();
        else
            searchPath();
    }//GEN-LAST:event_attributeFieldActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void addAttributeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAttributeButtonActionPerformed
        List<String>   attributeNames = attributeTree.getSelectedAttributes();
        if (attributeNames.size()==0)   // nothing to do
            return;
        if (attributeNames.size()==1)   {
            //  Add one attribute
            addSpecifiedAttribute();
        }
        else {
            //  Add all selected attributes
            addAttributeList(attributeNames);
        }

    }//GEN-LAST:event_addAttributeButtonActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchPath();
    }//GEN-LAST:event_searchButtonActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void releaseNoteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseNoteItemActionPerformed
        new PopupHtml(this).show(ReleaseNotes.htmlString);
    }//GEN-LAST:event_releaseNoteItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void openItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openItemActionPerformed
        if (subscriberMap.size()==0) {
            Utils.popupError(this, "No Subscriber defined");
            return;
        }
        if (fileChooser==null) {
            //  Build file chooser
            fileChooser=new JFileChooser(new File("").getAbsolutePath());
        }
        if (fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file!=null) {
                if (file.isFile()) {
                    try {
                        String	filename = file.getAbsolutePath();
                        List<String>   attributeNames = Utils.readFileLines(filename);
                        addAttributeList(attributeNames);
                    }
                    catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, null, e);
                    }
                }
            }
        }
    }//GEN-LAST:event_openItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void stoppedFilterTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stoppedFilterTextActionPerformed
        try {
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(
                    (String) archiverComboBox.getSelectedItem());
            subscriber.setStoppedFilter(stoppedFilterText.getText());
            updateAttributeList(subscriber);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_stoppedFilterTextActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void pausedFilterTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pausedFilterTextActionPerformed
        try {
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(
                    (String) archiverComboBox.getSelectedItem());
            subscriber.setPausedFilter(pausedFilterText.getText());
            updateAttributeList(subscriber);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_pausedFilterTextActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void startedFilterTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startedFilterTextActionPerformed
        try {
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(
                    (String) archiverComboBox.getSelectedItem());
            subscriber.setStartedFilter(startedFilterText.getText());
            updateAttributeList(subscriber);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_startedFilterTextActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void deviceFilterTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceFilterTextActionPerformed
        try {
            if (attributeTree==null)
                return;
            String tangoHost = attributeTree.getTangoHost();
            attributeTree.removeAll();
            treeScrollPane.remove(attributeTree);

            String filter = deviceFilterText.getText();
            StringTokenizer stk = new StringTokenizer(filter, "/");
            if (stk.countTokens()<3)
                Except.throw_exception("SyntaxError",
                        "Syntax error in device filter: " + filter + "\n" +
                                "Device name needs 3 fields : <domain>/<family>/<member>");

            attributeTree = new AttributeTree(this, tangoHost, filter);
            treeScrollPane.setViewportView(attributeTree);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_deviceFilterTextActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void diagnosticsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diagnosticsItemActionPerformed
        try {
            if (diagnosticsPanel==null) {
                //  Start as external application to avoid cross compilation
                diagnosticsPanel =
                        (JFrame) Utils.getInstance().startExternalApplication(
                                this, "org.tango.hdb_configurator.diagnostics.HdbDiagnostics");
            }
            diagnosticsPanel.setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.toString(), e);
        }
    }//GEN-LAST:event_diagnosticsItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void addSubscriberItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSubscriberItemActionPerformed
        addSubscriber();
    }//GEN-LAST:event_addSubscriberItemActionPerformed

    //=======================================================
    //=======================================================
    private void addSubscriber() {
        try {
            if (new CreateSubscriberPanel(this, configuratorProxy,
                    CreateSubscriberPanel.CREATE).showDialog()==JOptionPane.OK_OPTION) {
                restartApplication();
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void removeSubscriberItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSubscriberItemActionPerformed
        try {
            if (subscriberMap.size()==0) {
                Utils.popupError(this, "No Subscriber to remove");
                return;
            }

            if (new CreateSubscriberPanel(this,  configuratorProxy,
                    CreateSubscriberPanel.REMOVE).showDialog()==JOptionPane.OK_OPTION) {
                restartApplication();
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_removeSubscriberItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void changeCsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeCsItemActionPerformed
        changeTangoHost(attributeTree.getTangoHost());
    }//GEN-LAST:event_changeCsItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        // Check if already initialized
        if (subscriberMap==null)
            return;
        try {
            Subscriber subscriber = subscriberMap.getSubscriberByLabel(
                    (String) archiverComboBox.getSelectedItem());
            updateAttributeList(subscriber);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_tabbedPaneStateChanged
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void manageAliasesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageAliasesItemActionPerformed
        try {
            ArchiverAliasesDialog aliasesDialog = new ArchiverAliasesDialog(this, subscriberMap);
            if (aliasesDialog.showDialog()==JOptionPane.OK_OPTION) {
                restartApplication();
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_manageAliasesItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void contextsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contextsItemActionPerformed
        try {
            new EditStrategiesDialog(this,
                    subscriberMap.getSubscriberList()).setVisible(true);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }//GEN-LAST:event_contextsItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void byStrategiesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_byStrategiesItemActionPerformed
        try {
            new StrategyMainPanel(this, configuratorProxy.name()).setVisible(true);
        } catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_byStrategiesItemActionPerformed

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void principleItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_principleItemActionPerformed
        // TODO add your handling code here:
        try {
            JOptionPane.showMessageDialog(this, null, "HDB++ Principle",
                    JOptionPane.INFORMATION_MESSAGE, Utils.getInstance().getIcon("Principle.png"));
        }catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_principleItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        try {
            SplashUtils.getInstance().startSplash();
            SplashUtils.getInstance().increaseSplashProgress(10, "Building GUI");
            new TtlTableDialog(this, subscriberMap).setVisible(true);
        }
        catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

	//=======================================================
	//=======================================================
    private void restartApplication() {
        try {
            new HdbConfigurator(parent).setVisible(true);
            if (diagnosticsPanel!=null) {
                diagnosticsPanel.setVisible(false);
                diagnosticsPanel.dispose();
            }
            setVisible(false);
            dispose();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
	//=======================================================
	//=======================================================
    private void searchPath() {
        String  path = attributeField.getText();
        attributeTree.goToNode(path);
    }
    //=======================================================
    //=======================================================
    private void addAttributeList(List<String> attributeNames) {
        try {
            Strategy strategy = Strategy.getContextsFromDB(null);
            List<HdbAttribute> attributeList = new ArrayList<>();
            for (String attributeName : attributeNames) {
                attributeList.add(new HdbAttribute(attributeName, strategy));
            }

             //  Get info for selected attributes
            PropertyDialog  propertyDialog = new PropertyDialog(
                    this, attributeList, subscriberMap,
                    (String) archiverComboBox.getSelectedItem());
            propertyDialog.setVisible(true);
            if (propertyDialog.isCanceled())
                return;

            //  If OK, add them
            String archiverName = propertyDialog.getSubscriber();

            Subscriber  subscriber = subscriberMap.getSubscriberByLabel(archiverName);
            List<HdbAttribute> attributes = propertyDialog.getHdbAttributes();

            //  Then add them
            ManageAttributes.addAttributes(configuratorProxy, subscriber.getName(), attributes);
            //  And select archiver to display results
            selectArchiver(archiverName);
            new UpdateSubscribedThread(attributes).start();
        }
        catch (DevFailed e) {
            if (e.errors[0].desc.contains("\n"))
                new PopupHtml(this).show(
                        PopupHtml.toHtml(e.errors[0].desc), "Cannot Add Attributes");
            else
                ErrorPane.showErrorMessage(this, null, e);
        }
    }
	//=======================================================
	//=======================================================
    public void selectArchiver(String archiverName) {
        archiverComboBox.setSelectedItem(archiverName);
        manageSubscriberChanged(archiverName);
    }
    //=======================================================
    //=======================================================
    private int attributeRow(AttributeTable table, String attributeName) {
        List<HdbAttribute> attributeList = table.getAttributeList();
        int row = 0;
        for (HdbAttribute attribute : attributeList) {
            if (attribute.getName().equalsIgnoreCase(attributeName)) {
                return row;
            }
            row++;
        }
        return -1;
    }
    //=======================================================
    //=======================================================
    public void selectAttributeInList(String attributeName) {
        //  Search in which table.
        AttributeTable  table = attributeTableList.get(Subscriber.ATTRIBUTE_STARTED);
        int row = attributeRow(table, attributeName);
        if (row<0) {
            table = attributeTableList.get(Subscriber.ATTRIBUTE_STOPPED);
            row = attributeRow(table, attributeName);
            if(row<0) {
                table = attributeTableList.get(Subscriber.ATTRIBUTE_PAUSED);
                row = attributeRow(table, attributeName);
                if (row<0)
                    return;
            }
        }
        //  Do selection
        table.setSelectedRow(row);
    }
	//=======================================================
	//=======================================================

	//=======================================================
	//=======================================================
    private void addSpecifiedAttribute() {
        String  attributeName = attributeField.getText();
        String  fullName = TangoUtils.fullName(attributeTree.getTangoHost(), attributeName);
        addSpecifiedAttribute(fullName);
    }
	//=======================================================
	//=======================================================
    public void addSpecifiedAttribute(String attributeName) {
        try {
            HdbAttribute hdbAttribute = new HdbAttribute(attributeName);
            PropertyDialog propertyDialog = new PropertyDialog(this,
                    hdbAttribute, subscriberMap, (String) archiverComboBox.getSelectedItem());
            propertyDialog.setVisible(true);
            if (propertyDialog.isCanceled())
                return;

            hdbAttribute = propertyDialog.getHdbAttributes().get(0);
            Subscriber subscriber =
                    subscriberMap.getSubscriberByLabel(propertyDialog.getSubscriber());

            //  If OK add the attribute
            final boolean lock = true;
            ArchiverUtils.addAttribute(configuratorProxy, subscriber.getName(), hdbAttribute, lock);
            if (hdbAttribute.needsStart())
                subscriber.startAttribute(hdbAttribute);

            updateAttributeList(subscriber);
            new UpdateSubscribedThread(hdbAttribute).start();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }
	//=======================================================
	//=======================================================
    public String getArchiverLabel(String deviceName) {
        return subscriberMap.getLabel(deviceName);
    }
	//=======================================================
	//=======================================================
    private void doClose() {
        updateListThread.stopIt = true;
        if ((parent!=null && parent.isVisible()) || (diagnosticsPanel!=null && diagnosticsPanel.isVisible())) {
            setVisible(false);
            dispose();
        }
        else {
            try { updateListThread.join(); }
            catch (InterruptedException e) { System.err.println(e.getMessage()); }
            System.exit(0);
        }
    }
	//=======================================================
    /**
     * @param args the command line arguments
     */
	//=======================================================
    public static void main(String args[]) {
		try {
            UIManager.put("ToolTip.foreground", new ColorUIResource(Color.black));
            UIManager.put("ToolTip.background", new ColorUIResource(Utils.toolTipBackground));
      		new HdbConfigurator(null).setVisible(true);
		}
		catch(DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            ErrorPane.showErrorMessage(new Frame(), null, e);
            System.exit(0);
		}
		catch(Exception e) {
            SplashUtils.getInstance().stopSplash();
            e.printStackTrace();
            ErrorPane.showErrorMessage(new Frame(), null, e);
            System.exit(0);
		}
		catch(Error e) {
            SplashUtils.getInstance().stopSplash();
            e.printStackTrace();
            ErrorPane.showErrorMessage(new Frame(), null, new Exception(e));
            System.exit(0);
		}
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAttributeButton;
    private javax.swing.JMenuItem addSubscriberItem;
    private javax.swing.JComboBox<String> archiverComboBox;
    private javax.swing.JLabel archiverLabel;
    private javax.swing.JPanel attrTreePanel;
    private javax.swing.JTextField attributeField;
    private javax.swing.JMenuItem changeCsItem;
    private javax.swing.JMenuItem contextsItem;
    private javax.swing.JTextField deviceFilterText;
    private javax.swing.JMenuItem manageAliasesItem;
    private javax.swing.JLabel pausedAttrLabel;
    private javax.swing.JTextField pausedFilterText;
    private javax.swing.JScrollPane pausedScrollPane;
    private javax.swing.JTextArea propertiesArea;
    private javax.swing.JMenuItem removeSubscriberItem;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel startedAttrLabel;
    private javax.swing.JTextField startedFilterText;
    private javax.swing.JScrollPane startedScrollPane;
    private javax.swing.JLabel stoppedAttrLabel;
    private javax.swing.JTextField stoppedFilterText;
    private javax.swing.JScrollPane stoppedScrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//=======================================================




    //======================================================
    /**
     * Popup menu class
     */
    //======================================================
    private static final int ARCHIVING_STRATEGY  = 0;
    private static final int ARCHIVING_TTL    = 1;
    private static final int START_ARCHIVING  = 2;
    private static final int STOP_ARCHIVING   = 3;
    private static final int PAUSE_ARCHIVING  = 4;
    private static final int REMOVE_ATTRIBUTE = 5;
    private static final int MOVE_TO = 6;
    private static final int COPY_AS_TEXT = 7;

    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Change Archiving Strategy",
            "Change Archiving TTL",
            "Start Archiving",
            "Stop Archiving",
            "Pause Archiving",
            "Remove Attribute",
            "Move Attribute To ",
            "Copy as Text",
    };
    //=======================================================
    //=======================================================
    private class ListPopupMenu extends JPopupMenu {
        private JLabel title;
        private AttributeTable  table;
        private JMenu  subscriberMenu = new JMenu(menuLabels[MOVE_TO]);
        //======================================================
        private ListPopupMenu() {
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
            add(title);
            add(new JPopupMenu.Separator());

            int i=0;
            for (String menuLabel : menuLabels) {
                if (i++==MOVE_TO){
                    add(subscriberMenu);
                }
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            menuActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }
        //======================================================
        private void setSubscriberMenu() {

            List<String> subscriberList = subscriberMap.getLabelList();
            String  selected = (String) archiverComboBox.getSelectedItem();
            subscriberMenu.removeAll();
            //subscriberList.remove(selected);

            for (String subscriber : subscriberList) {
                if (!subscriber.equals(selected)) {
                    JMenuItem item = new JMenuItem(subscriber);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            menuActionPerformed(evt);
                        }
                    });
                    subscriberMenu.add(item);
                }
            }
            subscriberMenu.setVisible(subscriberList.size()>1);
        }
        //======================================================
        //noinspection PointlessArithmeticExpression
        private void showMenu(MouseEvent event, List<HdbAttribute> attributes) {
            String str;
            if (attributes.size()==1)
                str = attributes.get(0).getName();
            else
                str = attributes.size()+" attributes";
            title.setText(str);
            setSubscriberMenu();

            table = (AttributeTable) event.getSource();
            if (table==attributeTableList.get(Subscriber.ATTRIBUTE_STARTED)) {
                getComponent(OFFSET + START_ARCHIVING).setVisible(false);
                getComponent(OFFSET + STOP_ARCHIVING).setVisible(true);
                getComponent(OFFSET + PAUSE_ARCHIVING).setVisible(true);
            }
            else
            if (table==attributeTableList.get(Subscriber.ATTRIBUTE_STOPPED)) {
                getComponent(OFFSET + START_ARCHIVING).setVisible(true);
                getComponent(OFFSET + STOP_ARCHIVING).setVisible(false);
                getComponent(OFFSET + PAUSE_ARCHIVING).setVisible(false);
            }
            else
            if (table==attributeTableList.get(Subscriber.ATTRIBUTE_PAUSED)) {
                getComponent(OFFSET + START_ARCHIVING).setVisible(true);
                getComponent(OFFSET + STOP_ARCHIVING).setVisible(true);
                getComponent(OFFSET + PAUSE_ARCHIVING).setVisible(false);
            }
            show(table, event.getX(), event.getY());
        }
        //======================================================
        private void menuActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = -1;
            String  targetSubscriber = null;
            for (int i=0; i<menuLabels.length; i++)
                if (getComponent(OFFSET + i) == obj)
                    itemIndex = i;
            if (itemIndex<0) {
                //  Check subscriberMenu items
                JMenuItem item = (JMenuItem) evt.getSource();
                targetSubscriber = item.getText();
                itemIndex = MOVE_TO;
            }

            try {
                Subscriber  archiver =
                        subscriberMap.getSubscriberByLabel((String) archiverComboBox.getSelectedItem());
                List<HdbAttribute> attributeList = table.getSelectedAttributes();
                switch (itemIndex) {
                    case ARCHIVING_STRATEGY:
                        changeArchivingStrategy(attributeList);
                        break;
                    case ARCHIVING_TTL:
                        changeAttributeTTL(attributeList);
                        break;
                    case START_ARCHIVING:
                        ManageAttributes.startAttributes(configuratorProxy, attributeList);
                        updateAttributeList(archiver);
                        break;
                    case STOP_ARCHIVING:
                        ManageAttributes.stopAttributes(configuratorProxy, attributeList);
                        updateAttributeList(archiver);
                        break;
                    case PAUSE_ARCHIVING:
                        ManageAttributes.pauseAttributes(configuratorProxy, attributeList);
                        updateAttributeList(archiver);
                        break;
                    case REMOVE_ATTRIBUTE:
                        if (JOptionPane.showConfirmDialog(this,
                                "Remove selected attributes ?", "Confirm",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION) {
                            ManageAttributes.removeAttributes(configuratorProxy,
                                    attributeList.toArray(new HdbAttribute[0]));
                            updateAttributeList(archiver);
                            new UpdateSubscribedThread(attributeList).start();
                            attributeTree.updateAttributeInfo(attributeList);
                        }
                        break;
                    case MOVE_TO:
                        moveAttributeToSubscriber(targetSubscriber, attributeList);
                        new UpdateSubscribedThread(attributeList).start();
                        attributeTree.updateAttributeInfo(attributeList);
                        break;
                    case COPY_AS_TEXT:
                        copyAttributeAsText();
                        break;
                }
            }
            catch (DevFailed e) {
                ErrorPane.showErrorMessage(this, null, e);
            }
        }
        //======================================================
        private void copyAttributeAsText() {
            //  Put selection in a text area, select and copy to clipboard
            List<HdbAttribute> attributes = table.getSelectedAttributes();
            StringBuilder   sb = new StringBuilder();
            for (HdbAttribute attribute : attributes) {
                sb.append(attribute.getName()).append('\n');
            }
            JTextArea   textArea = new JTextArea(sb.toString());
            textArea.setSelectionStart(0);
            textArea.setSelectionEnd(sb.length());
            textArea.copy();
        }
        //======================================================
    }
    //======================================================
    //======================================================



    //=======================================================
    /**
     * A thread to update Started/Stopped/Paused attribute lists
     * on selected subscriber if they have changed
     */
    //=======================================================
    private class UpdateListThread extends Thread {
        private List<HdbAttribute> displayedList = null;
        private boolean stopIt = false;
        //===================================================
        public void run() {
            while (!stopIt) {
                try { sleep(500); } catch (InterruptedException e) { /* */ }

                //  Get selected subscriber
                String  archiverLabel = (String) archiverComboBox.getSelectedItem();
                if (archiverLabel!=null) {
                    try {
                        Subscriber subscriber =
                                subscriberMap.getSubscriberByLabel(archiverLabel);

                        //  Get displayed list
                        int selection = tabbedPane.getSelectedIndex();
                        //  Get attribute list (read from events) and check if has changed
                        List<HdbAttribute> hdbAttributes =
                                subscriber.getAttributeList(selection, false);
                        if (hdbAttributes!=displayedList) {
                            displayedList = hdbAttributes;
                            updateAttributeList(subscriber);
                        }
                        if (subscriber.needsRepaint())
                            attributeTableList.get(selection).fireTableDataChanged();
                    }
                    catch (DevFailed e) {
                        System.err.println(e.errors[0].desc);
                    }
                }
            }
        }
        //===================================================
    }
    //=======================================================
    //=======================================================





    //=======================================================
    /**
     * A thread to update Subscribe/Remove attribute on JTree
     */
    //=======================================================
    private static final int executionTime = 15000;
    private class UpdateSubscribedThread extends Thread {
        private boolean stopIt = false;
        private long t0 = System.currentTimeMillis();
        private List<HdbAttribute> attributes;
        //===================================================
        private UpdateSubscribedThread(List<HdbAttribute> attributes) {
            this.attributes = attributes;
        }
        //===================================================
        private UpdateSubscribedThread(HdbAttribute attribute) {
            this.attributes = new ArrayList<>();
            attributes.add(attribute);
        }
        //===================================================
        public void run() {
            while (!stopIt) {
                try { sleep(500); } catch (InterruptedException e) { /* */ }
                if (attributes.size()==1)
                    attributeTree.updateAttributeInfo(attributes.get(0).getName());
                else {
                    attributeTree.updateAttributeInfo(attributes);
                }

                //  After a while forget it
                if (System.currentTimeMillis()-t0 > executionTime)
                    stopIt = true;
            }
        }
    }
    //=======================================================
    //=======================================================

}
