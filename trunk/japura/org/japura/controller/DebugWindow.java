package org.japura.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * 
 * Copyright (C) 2011 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 * 
 */
class DebugWindow extends JDialog{

  private static final long serialVersionUID = 1966734031063184008L;
  private JList list;
  private JTree tree;
  private JScrollPane sp;

  private JRadioButton treeButton;
  private JRadioButton listButton;

  private JCheckBox fullNameButton;

  private List<TreePath> pathsToExpand;
  private List<Integer> expandedNodes;

  public DebugWindow() {
	pathsToExpand = new ArrayList<TreePath>();
	expandedNodes = new ArrayList<Integer>();
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	addWindowListener(new WindowAdapter() {
	  @Override
	  public void windowClosing(WindowEvent e) {
		Controller.closeDebugWindow();
	  }
	});
	setModal(false);
	setTitle("Controllers - Pool (0)");

	setSize(500, 500);

	ButtonGroup bg = new ButtonGroup();
	bg.add(getListButton());
	bg.add(getTreeButton());

	JPanel buttonsPanel = new JPanel();
	buttonsPanel.setLayout(new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 0;
	buttonsPanel.add(getTreeButton(), gbc);
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.weightx = 1;
	gbc.anchor = GridBagConstraints.WEST;
	buttonsPanel.add(getListButton(), gbc);
	gbc.gridx = 2;
	gbc.gridy = 0;
	gbc.weightx = 1;
	gbc.anchor = GridBagConstraints.EAST;
	buttonsPanel.add(getFullNameButton(), gbc);

	setLayout(new BorderLayout());
	add(buttonsPanel, BorderLayout.NORTH);
	sp = new JScrollPane(getTree());
	add(sp, BorderLayout.CENTER);
  }

  public void controllerRemoved(Controller<?> controller) {
	Integer id = Integer.valueOf(controller.getId());
	expandedNodes.remove(id);
  }

  public JCheckBox getFullNameButton() {
	if (fullNameButton == null) {
	  fullNameButton = new JCheckBox("Full name");
	  fullNameButton.setSelected(true);
	  fullNameButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		  tree.updateUI();
		}
	  });
	}
	return fullNameButton;
  }

  public JRadioButton getListButton() {
	if (listButton == null) {
	  listButton = new JRadioButton("List");
	  listButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		  sp.setViewportView(getList());
		}
	  });
	}
	return listButton;
  }

  public JRadioButton getTreeButton() {
	if (treeButton == null) {
	  treeButton = new JRadioButton("Tree");
	  treeButton.setSelected(true);
	  treeButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		  sp.setViewportView(getTree());
		}
	  });
	}
	return treeButton;
  }

  public JTree getTree() {
	if (tree == null) {
	  tree = new JTree();
	  tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
	  tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	  tree.setCellRenderer(new TRenderer());
	  tree.setRootVisible(false);
	  tree.setShowsRootHandles(true);
	  tree.addTreeExpansionListener(new TreeExpansionListener() {
		@Override
		public void treeExpanded(TreeExpansionEvent event) {
		  DefaultMutableTreeNode node =
			  (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		  Controller<?> controller = (Controller<?>) node.getUserObject();
		  Integer id = Integer.valueOf(controller.getId());
		  expandedNodes.add(id);
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
		  DefaultMutableTreeNode node =
			  (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		  Controller<?> controller = (Controller<?>) node.getUserObject();
		  Integer id = Integer.valueOf(controller.getId());
		  expandedNodes.remove(id);
		}
	  });
	}
	return tree;
  }

  public JList getList() {
	if (list == null) {
	  list = new JList();
	  list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	  list.setCellRenderer(new LRenderer());
	}
	return list;
  }

  public void update() {
	pathsToExpand.clear();
	List<Controller<?>> list = Controller.getControllers();
	setTitle("Controllers - Pool (" + list.size() + ")");

	DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	DefaultTreeModel treeModel = new DefaultTreeModel(root);
	DefaultListModel listModel = new DefaultListModel();

	for (Controller<?> c : list) {
	  if (c.getParentId() == null) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(c);
		root.add(node);
		addToPathList(node);
		listModel.addElement(c);
		addChild(c, listModel);
		addChild(c, node);
	  }
	}

	getTree().setModel(treeModel);
	getList().setModel(listModel);

	for (TreePath tp : pathsToExpand) {
	  tree.expandPath(tp);
	}
	pathsToExpand.clear();
  }

  private void addToPathList(DefaultMutableTreeNode node) {
	Controller<?> controller = (Controller<?>) node.getUserObject();
	Integer id = Integer.valueOf(controller.getId());
	if (expandedNodes.contains(id)) {
	  pathsToExpand.add(new TreePath(node.getPath()));
	}
  }

  private void addChild(Controller<?> parent, DefaultMutableTreeNode parentNode) {
	List<Controller<?>> list = parent.getChildren();
	if (list.size() > 0) {
	  for (Controller<?> child : list) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
		parentNode.add(childNode);
		addToPathList(childNode);
		addChild(child, childNode);
	  }
	}
  }

  private void addChild(Controller<?> parent, DefaultListModel listModel) {
	List<Controller<?>> list = parent.getChildren();
	if (list.size() > 0) {
	  for (Controller<?> child : list) {
		listModel.addElement(child);
		addChild(child, listModel);
	  }
	}
  }

  private class LRenderer extends DefaultListCellRenderer{
	private static final long serialVersionUID = 197732829250426432L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus) {

	  JLabel comp =
		  (JLabel) super.getListCellRendererComponent(list, value, index,
			  isSelected, cellHasFocus);
	  Controller<?> controller = (Controller<?>) value;
	  boolean fullName = getFullNameButton().isSelected();
	  comp.setText(controllerToString(controller, fullName));
	  return comp;
	}
  }

  private class TRenderer extends DefaultTreeCellRenderer{
	private static final long serialVersionUID = 8655744295863210194L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
												  boolean sel,
												  boolean expanded,
												  boolean leaf, int row,
												  boolean hasFocus) {
	  JLabel comp =
		  (JLabel) super.getTreeCellRendererComponent(tree, value, sel,
			  expanded, leaf, row, hasFocus);
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	  Controller<?> controller = (Controller<?>) node.getUserObject();
	  if (controller != null) {
		boolean fullName = getFullNameButton().isSelected();
		comp.setText(controllerToString(controller, fullName));
	  }
	  return comp;
	}

	@Override
	public void setIcon(Icon icon) {}
  }

  private static String controllerToString(Controller<?> controller,
										   boolean fullName) {
	String name = null;
	if (fullName) {
	  name = controller.getClass().getName();
	} else {
	  name = controller.getClass().getSimpleName();
	}
	if (controller.getControllerName() != null) {
	  return name + " [ Id:" + controller.getId() + " - Name: "
		  + controller.getControllerName() + " ]";
	}
	return name + " [ Id:" + controller.getId() + " ]";
  }
}
