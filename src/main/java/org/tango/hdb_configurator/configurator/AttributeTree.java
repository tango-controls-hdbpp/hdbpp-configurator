//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:	java source code for display JTree
//
// $Author: pascal_verdier $
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
// $Revision: 1.2 $
//
// $Log:  $
//
//-======================================================================

package org.tango.hdb_configurator.configurator;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.hdb_configurator.common.*;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * This class is able to display in a tree device and attributes.
 * It is used to display and to add or edit property of attribute.
 *
 * @author verdier
 */

public class AttributeTree extends JTree {
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private DefaultMutableTreeNode selectedNode = null;
    private AttributeTreePopupMenu menu;
    private HdbConfigurator parent;
    private String tangoHost;
    private String[] deviceNames;
    private boolean useDefaultTangoHost;
    private boolean multiTangoHosts;

    private static final int DOMAIN = 0;
    private static final int FAMILY = 1;
    private static final int MEMBER = 2;
    //===============================================================
    //===============================================================
    public AttributeTree(HdbConfigurator parent, String tangoHost) throws DevFailed {
        this(parent, tangoHost, null);
    }
    //===============================================================
    //===============================================================
    public AttributeTree(HdbConfigurator parent, String tangoHost, String wildcard) throws DevFailed {
        super();
        this.parent = parent;
        this.tangoHost = tangoHost;

        if (wildcard!=null) {
            deviceNames = TangoUtils.getFilteredDeviceNames(tangoHost, wildcard);
        }
        buildTree();

        List<String> defaultTangoHosts = TangoUtils.getDefaultTangoHostList();
        for (String defaultTangoHost : defaultTangoHosts) {
            if (tangoHost.equals(defaultTangoHost)) {
                useDefaultTangoHost = true;
                break;
            }
        }

        //  Check if change TANGO_HOST available
        String onlyOneCS = System.getenv("SingleControlSystem");
        multiTangoHosts = ! (onlyOneCS!=null && onlyOneCS.equals("true"));
        menu = new AttributeTreePopupMenu(this);
        setSelectionPath(null);
    }
    //======================================================
    //======================================================
    public String getTangoHost() {
        return tangoHost;
    }

    //===============================================================
    //===============================================================
    private void buildTree() throws DevFailed {
        //  Create the nodes.
        root = new DefaultMutableTreeNode(tangoHost);
        createDomainNodes();

        //	Create the tree that allows several selection at a time.
        getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
                //(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //	Create Tree and Tree model
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);

        //  Set the icon for leaf nodes.
        TangoRenderer renderer = new TangoRenderer();
        setCellRenderer(renderer);

        //	Listen for collapse tree
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                //collapsedPerformed(e);
            }

            public void treeExpanded(TreeExpansionEvent e) {
                expandedPerformed(e);
            }
        });
        //	Add Action listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);    //	for tree clicked, menu,...
            }
        });
        /*
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                updateSelectedPath();
            }
        });
        */
    }
    //======================================================
    /**
     * Manage event on clicked mouse on JTree object.
     */
    //======================================================
    private void treeMouseClicked(java.awt.event.MouseEvent evt) {
        //	Set selection at mouse position
        TreePath selectedPath = getPathForLocation(evt.getX(), evt.getY());
        if (selectedPath==null)
            return;
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) selectedPath.getPathComponent(selectedPath.getPathCount() - 1);
        Object userObject = node.getUserObject();

        //  Check button clicked
        int mask = evt.getModifiers();
        if (evt.getClickCount()==2 && (mask & MouseEvent.BUTTON1_MASK)!=0) {
            addAttribute();
        }
        else if ((mask & MouseEvent.BUTTON3_MASK)!=0) {
            if (node==root && multiTangoHosts)
                menu.showMenu(evt, (String) node.getUserObject());
            else if (userObject instanceof Member)
                menu.showMenu(evt, (Member) userObject);
            else if (userObject instanceof Attribute)
                menu.showMenu(evt, (Attribute) userObject);
        }
    }

    //===============================================================
    //===============================================================
    private void expandedPerformed(TreeExpansionEvent evt) {
        //  ToDo
        TreePath treePath = evt.getPath();
        DefaultMutableTreeNode  parentNode =
                (DefaultMutableTreeNode) treePath.getPathComponent(treePath.getPathCount() - 1);
        Object userObject = parentNode.getUserObject();
        try {
            int  childCount = parentNode.getChildCount();
            //  Create child nodes (depending on parent instance)
            if (userObject instanceof Domain) {
                String[] families;
                if (deviceNames!=null)  //  filtered
                    families = TangoUtils.getFilteredDeviceField(
                            FAMILY, userObject.toString(), deviceNames);
                else
                    families = TangoUtils.getFamilies(tangoHost, userObject.toString());
                createFamilyNodes(parentNode, families);
            }
            else
            if (userObject instanceof Family) {
                Family  family =  (Family) userObject;
                String[]  member;
                if (deviceNames!=null)  //  filtered
                    member = TangoUtils.getFilteredDeviceField(
                            MEMBER, family.path, deviceNames);
                else
                    member = TangoUtils.getMembers(tangoHost, family.path);
                createMemberNodes(parentNode, member);
            }
            else
            if (userObject instanceof Member) {
                Member member = (Member) userObject;
                createAttributeNodes(parentNode,
                        TangoUtils.getDeviceAttributes(tangoHost, member.path));
            }
            else // Instance unknown
                 return;

            //  Then remove old nodes (Dummy or could have changed)
            for (int i=0 ; i< childCount ; i++)
                treeModel.removeNodeFromParent(
                        (MutableTreeNode) parentNode.getChildAt(0));
        }
        catch (DevFailed e) {
            this.collapsePath(treePath);
            ErrorPane.showErrorMessage(this, null, e);
        }
    }

    //===============================================================
    //===============================================================
    private void createFamilyNodes(DefaultMutableTreeNode domainNode, String[] families) {
        for (String family : families) {
            DefaultMutableTreeNode familyNode =
                    new DefaultMutableTreeNode(new Family(
                            family, (Domain) domainNode.getUserObject()));
            treeModel.insertNodeInto(familyNode, domainNode, domainNode.getChildCount());
            treeModel.insertNodeInto(new DummyNode(), familyNode, 0);
        }
    }
    //===============================================================
    //===============================================================
    private void createMemberNodes(DefaultMutableTreeNode familyNode, String[] members) {
        for (String member : members) {
            DefaultMutableTreeNode memberNode =
                    new DefaultMutableTreeNode(new Member(
                            member, (Family) familyNode.getUserObject()));
            treeModel.insertNodeInto(memberNode, familyNode, familyNode.getChildCount());
            treeModel.insertNodeInto(new DummyNode(), memberNode, 0);
        }
    }
    //===============================================================
    //===============================================================
    private void createAttributeNodes(DefaultMutableTreeNode memberNode, String[] attributes) {
        for (String attribute : attributes) {
            DefaultMutableTreeNode attributeNode =
                    new DefaultMutableTreeNode(new Attribute(
                            attribute, (Member) memberNode.getUserObject()));
            treeModel.insertNodeInto(attributeNode, memberNode, memberNode.getChildCount());
        }
    }
    //===============================================================
    //===============================================================
    private void createDomainNodes() throws DevFailed {
        String[] domains;
        if (deviceNames!=null)  //  filtered
            domains = TangoUtils.getFilteredDeviceField(DOMAIN, null, deviceNames);
        else
            domains = TangoUtils.getDomains(tangoHost);
        for (String domain : domains) {
            DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode(new Domain(domain));
            root.add(node);
            node.add(new DummyNode());
        }
    }

    //======================================================
    //======================================================
    private DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) getLastSelectedPathComponent();
    }
    //======================================================
    //======================================================
    private Object getSelectedObject() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node==null)
            return null;
        return node.getUserObject();
    }

    //===============================================================
    //===============================================================
    public List<String> getSelectedAttributes() {
        List<String>   attributes = new ArrayList<>();
        TreePath[]  paths = getSelectionPaths();
        if (paths!=null) {
            for (TreePath path : paths) {
                //  Check if attribute
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof Attribute) {
                    Attribute   attribute = (Attribute) node.getUserObject();
                    attributes.add(TangoUtils.fullName(tangoHost,attribute.path));
                }
            }
        }
        return attributes;
    }
    //===============================================================
    //===============================================================
    public void goToNode(String path) {

        //  Split path
        StringTokenizer stk = new StringTokenizer(path, "/");
        List<String> fields = new ArrayList<>(stk.countTokens());
        while (stk.hasMoreTokens())
            fields.add(stk.nextToken().toLowerCase());
        if (fields.isEmpty())
            return;

        //  And search
        DefaultMutableTreeNode domainNode = gotoNode(root, fields.get(0));
        if (domainNode!=null && fields.size()>1) {
            DefaultMutableTreeNode familyNode = gotoNode(domainNode, fields.get(1));
            if (familyNode!=null && fields.size()>2) {
                DefaultMutableTreeNode memberNode = gotoNode(familyNode, fields.get(2));
                if (memberNode!=null && fields.size()>3) {
                    gotoNode(memberNode, fields.get(3));
                }
            }
        }
    }
    //===============================================================
    //===============================================================
    private DefaultMutableTreeNode gotoNode(DefaultMutableTreeNode parentNode, String name) {
        if (parentNode.getChildCount()>0)
            expandNode((DefaultMutableTreeNode) parentNode.getChildAt(0));
        for (int i=0 ; i<parentNode.getChildCount() ; i++) {
            DefaultMutableTreeNode  node = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            if (node.toString().toLowerCase().equals(name)) {
                TreePath treePath = new TreePath(node.getPath());
                setSelectionPath(treePath);
                scrollPathToVisible(new TreePath(node.getPath()));
                return node;
            }
        }
        return null;
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("UnusedDeclaration")
    private void expandChildren(DefaultMutableTreeNode node) {
        boolean levelDone = false;
        for (int i=0 ; i<node.getChildCount() ; i++) {
            DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) node.getChildAt(i);
            if (child.isLeaf()) {
                if (!levelDone) {
                    expandNode(child);
                    levelDone = true;
                }
            } else
                expandChildren(child);
        }
    }

    //===============================================================
    //===============================================================
    private void expandNode(DefaultMutableTreeNode node) {
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        nodes.add(node);
        while (node!=root) {
            node = (DefaultMutableTreeNode) node.getParent();
            nodes.add(0, node);
        }
        TreeNode[] treeNodes = new DefaultMutableTreeNode[nodes.size()];
        for (int i=0 ; i<nodes.size() ; i++)
            treeNodes[i] =  nodes.get(i);
        TreePath treePath = new TreePath(treeNodes);
        setSelectionPath(treePath);
        scrollPathToVisible(treePath);
    }

    //===============================================================
    //===============================================================
    private void testEvent() {
        Object  userObject = getSelectedObject();
        if (userObject instanceof Attribute) {
            Attribute   attribute = ((Attribute)userObject);
            Utils.getTestEvents().add("tango://"+tangoHost+'/'+attribute.path);
        }
    }
    //===============================================================
    //===============================================================
    private void addAttribute() {
        Object  userObject = getSelectedObject();
        if (userObject instanceof Attribute) {
            Attribute   attribute = ((Attribute)userObject);
            parent.addSpecifiedAttribute(TangoUtils.fullName(tangoHost, attribute.path));
            attribute.checkIfSubscribedLater();
        }
    }
    //===============================================================
    //===============================================================
    private void selectArchiver() {
        Object  userObject = getSelectedObject();
        if (userObject instanceof Attribute) {
            final Attribute   attribute = ((Attribute)userObject);
            parent.selectArchiver(attribute.archiver);
            parent.selectAttributeInList(TangoUtils.fullName(tangoHost,attribute.path));
        }
    }
    //===============================================================
    //===============================================================
    private void configureAttribute() {
        Object  userObject = getSelectedObject();
        if (userObject instanceof Attribute) {
            String deviceName = ((Attribute)userObject).member.path;
            Utils.startJiveForDevice(deviceName);
        }
        else
        if (userObject instanceof Member) {
            String deviceName = ((Member)userObject).path;
            Utils.startJiveForDevice(deviceName);
        }
    }
    //======================================================
    //======================================================
    private void updateSelectedPathInfo() {

        //  Do it later after GUI refreshing.
        Runnable doItLater = () -> {
            Object userObject = selectedNode.getUserObject();
            //  If parent is HdbConfigurator display path
            if (userObject instanceof PathComponent)
                parent.displayPathInfo(tangoHost, ((PathComponent) userObject).path);
            else
                parent.displayPathInfo(tangoHost, "");
        };
        SwingUtilities.invokeLater(doItLater);
    }
    //===============================================================
    //===============================================================
    public void updateAttributeInfo(final List<HdbAttribute> attributes) {
        for (HdbAttribute attribute : attributes) {
            updateAttributeInfo(attribute.getName());
        }
    }
    //===============================================================
    //===============================================================
    public void updateAttributeInfo(final String attributeName) {
        DefaultMutableTreeNode  attributeNode = getAttributeNode(attributeName);
        if (attributeNode!=null) {
            Attribute attribute = (Attribute) attributeNode.getUserObject();
            attribute.checkIfSubscribedLater();
        }
    }
    //===============================================================
    //===============================================================
    private DefaultMutableTreeNode getAttributeNode(String attributeName) {
        //  Split attribute name
        String[] fields = TangoUtils.getAttributeFields(attributeName);

        //  Search domain/family/member/attribute node
        for (int i=0 ; i<root.getChildCount() ; i++) {
            DefaultMutableTreeNode  domainNode = (DefaultMutableTreeNode) root.getChildAt(i);
            if (domainNode.toString().equalsIgnoreCase(fields[0])) {
                for (int j=0 ; j<domainNode.getChildCount() ; j++) {
                    DefaultMutableTreeNode  familyNode =
                            (DefaultMutableTreeNode) domainNode.getChildAt(j);
                    if (familyNode.toString().equalsIgnoreCase(fields[1])) {
                        for (int k=0 ; k<familyNode.getChildCount() ; k++) {
                            DefaultMutableTreeNode  memberNode = (DefaultMutableTreeNode) familyNode.getChildAt(k);
                            if (memberNode.toString().equalsIgnoreCase(fields[2])) {
                                for (int n=0 ; n<memberNode.getChildCount() ; n++) {
                                    DefaultMutableTreeNode  attributeNode =
                                            (DefaultMutableTreeNode) memberNode.getChildAt(n);
                                    if (attributeNode.toString().equalsIgnoreCase(fields[3])) {
                                        return attributeNode;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    //===============================================================
    //===============================================================













    //===============================================================
    /**
     *  Define a generic Tango path component (domain, family, ....)
     */
    //===============================================================
    private static class PathComponent {
        String name;
        String path;
    }
    //===============================================================
	/*
	 *	Domain object definition
	 */
    //===============================================================
    private static class Domain extends PathComponent {
        //===========================================================
        private Domain(String name) {
            this.name = name;
            this.path = name;
        }
        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
	/*
	 *	Family object definition
	 */
    //===============================================================
    private static class Family extends PathComponent {
        Domain domain;
        //===========================================================
        private Family(String name, Domain domain) {
            this.name = name;
            this.domain = domain;
            this.path = domain.name + "/" + name;
        }
        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
	/*
	 *	Member object definition
	 */
    //===============================================================
    private static class Member extends PathComponent {
        Family family;
        //===========================================================
        private Member(String name, Family family) {
            this.name = name;
            this.family = family;
            this.path = family.path + "/" + name;
        }
        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
	/*
	 *	Attribute object definition
	 */
    //===============================================================
    private class Attribute extends PathComponent {
        Member member;
        String archiver = null;
        boolean checked = false;
        //===========================================================
        private Attribute(String name, Member member) {
            this.name = name;
            this.member = member;
            this.path = member.path + "/" + name;
            //  Start a runnable to check if attribute is subscribed.
            checkIfSubscribedLater();
        }
        //===========================================================
        private void checkIfSubscribedLater() {
            Runnable doItLater = () -> {
                parent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                checked = false;
                checkIfSubscribed();
                checked = true;
                repaint();
                parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            };
            //  Do it later to display tree without waiting
            SwingUtilities.invokeLater(doItLater);
        }
        //===========================================================
        private void checkIfSubscribed() {
            try {
                archiver = ArchiverUtils.getArchiver(
                        parent.getConfiguratorProxy(),
                        TangoUtils.fullName(tangoHost, path));
                if (archiver.isEmpty())
                    archiver = null;
                else
                    archiver = parent.getArchiverLabel(archiver);
            }
            catch (Exception e) {
                //  Do not know
                archiver = null;
            }
            //System.out.println(path +":  "+archiver);
        }
        //===========================================================
        ImageIcon getIcon() {
            if (archiver==null)
                return unselectedIcon;
            else
                return selectedIcon;
        }
        //===========================================================
        String getToolTip() {
            if (archiver==null)
                return "Not Subscribed";
            else
                return "Subscribed on " + archiver;
        }
        //===========================================================
        public String toString() {
            return name;
        }
        //===========================================================
    }
    //===============================================================
	/*
	 *	A Dummy object definition
	 */
    //===============================================================
    private static class DummyNode extends DefaultMutableTreeNode {
        public String toString() {
            return "? ?";
        }
    }







    //===============================================================
    /**
     * Renderer Class
     */
    //===============================================================
    private static ImageIcon tangoIcon;
    private static ImageIcon deviceIcon;
    private static ImageIcon selectedIcon;
    private static ImageIcon unselectedIcon;

    private static final Font titleFont  = new Font("Dialog", Font.BOLD, 18);
    private static final Font deviceFont = new Font("Dialog", Font.BOLD, 12);
    private static final Font attributeFont = new Font("Dialog", Font.PLAIN, 12);
    private class TangoRenderer extends DefaultTreeCellRenderer {

        //===============================================================
        //===============================================================
        private TangoRenderer() {
            try {
    			Utils	utils = Utils.getInstance();
	    		tangoIcon     = utils.getIcon("TangoLogo.gif", 0.25);
		    	deviceIcon    = utils.getIcon("device.gif");
			    selectedIcon = utils.getIcon("selected.gif", 0.75);
			    unselectedIcon = utils.getIcon("unselected.gif", 0.75);
            }
            catch (DevFailed e) {
                SplashUtils.getInstance().stopSplash();
                ErrorPane.showErrorMessage(parent, null, e);
            }
        }

        //===============================================================
        //===============================================================
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object object,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, object, selected,
                    expanded, leaf, row,
                    hasFocus);

            //  Set default properties
            setBackgroundNonSelectionColor(Color.white);
            setForeground(Color.black);
            setBackgroundSelectionColor(Color.lightGray);
            setToolTipText(null);

            if (object instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
                if (selected) {
                    if (node!=selectedNode) {
                        selectedNode = node;
                        updateSelectedPathInfo();
                    }
                }
                if (row==0) {
                    //	ROOT
                    setFont(titleFont);
                    setIcon(tangoIcon);
                }
                else
                if (node.getUserObject() instanceof Attribute) {
                    Attribute attribute = (Attribute) node.getUserObject();
                    setIcon(attribute.getIcon());
                    setToolTipText(attribute.getToolTip());
                    if (!attribute.checked)
                        setFont(deviceFont);    //  BOLD
                    else
                    if (attribute.archiver!=null) {
                        setFont(deviceFont);    //  BOLD
                    }
                    else {
                        setFont(attributeFont);
                    }
                }
                else {
                    setFont(deviceFont);
                    setIcon(deviceIcon);
                }
            }
            return this;
        }
    }//	End of Renderer Class
    //==============================================================================
    //==============================================================================









    //==============================================================================
    //==============================================================================
    private static final int CHANGE_TANGO_HOST = 0;
    private static final int ADD_ATTRIBUTE     = 1;
    private static final int CONFIGURE         = 2;
    private static final int TEST_EVENT        = 3;
    private static final int SELECT_ARCHIVER   = 4;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Change Tango Host",
            "Add Attribute to Subscriber",
            "Configure Polling/Events",
            "Test Event",
            "Select Archiver",
    };

    private class AttributeTreePopupMenu extends JPopupMenu {
        private JTree tree;
        private JLabel title;

        //======================================================
        /**
         * Popup menu constructor
         * @param tree parent component for menu
         */
        //======================================================
        private AttributeTreePopupMenu(JTree tree) {
            this.tree = tree;
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                JMenuItem btn = new JMenuItem(menuLabel);
                btn.addActionListener(this::hostActionPerformed);
                add(btn);
            }
        }
        //======================================================
        /**
         * Show menu on root
         */
        //======================================================
        private void showMenu(MouseEvent evt, String name) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath==null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(name);

            //	Reset all items
            for (int i=0 ; i<menuLabels.length ; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET /*+ CHANGE_TANGO_HOST*/).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        /**
         * Show menu on Attribute
         */
        //======================================================
        private void showMenu(MouseEvent evt, Attribute attribute) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath==null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(attribute.toString());

            //	Reset all items
            for (int i = 0 ; i<menuLabels.length ; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET + ADD_ATTRIBUTE).setVisible(true);
            getComponent(OFFSET + CONFIGURE).setVisible(true);
            getComponent(OFFSET + CONFIGURE).setEnabled(useDefaultTangoHost);
            getComponent(OFFSET + TEST_EVENT).setVisible(Utils.getTestEvents()!=null);
            getComponent(OFFSET + ADD_ATTRIBUTE).setEnabled(attribute.archiver==null);
            if (attribute.archiver!=null) {
                getComponent(OFFSET + SELECT_ARCHIVER).setVisible(true);
                ((JMenuItem)getComponent(OFFSET + SELECT_ARCHIVER)).setText(
                        menuLabels[SELECT_ARCHIVER] + ":   " + attribute.archiver);
            }
            show(tree, evt.getX(), evt.getY());
        }
        //======================================================
        /**
         * Show menu on Member (device)
         */
        //======================================================
        private void showMenu(MouseEvent evt, Member member) {
            //	Set selection at mouse position
            TreePath selectedPath =
                    tree.getPathForLocation(evt.getX(), evt.getY());
            if (selectedPath==null)
                return;
            tree.setSelectionPath(selectedPath);

            title.setText(member.path);

            //	Reset all items
            for (int i = 0 ; i<menuLabels.length ; i++)
                getComponent(OFFSET + i).setVisible(false);

            getComponent(OFFSET + CONFIGURE).setVisible(true);
            show(tree, evt.getX(), evt.getY());
        }

        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = 0;
            for (int i = 0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i)==obj)
                    itemIndex = i;

            switch (itemIndex) {
                case CHANGE_TANGO_HOST:
                    parent.changeTangoHost(tangoHost);
                    break;
                case ADD_ATTRIBUTE:
                    addAttribute();
                    break;
                case SELECT_ARCHIVER:
                    selectArchiver();
                    break;
                case TEST_EVENT:
                    testEvent();
                    break;
                case CONFIGURE:
                    configureAttribute();
                    break;
            }
        }
    }
}
