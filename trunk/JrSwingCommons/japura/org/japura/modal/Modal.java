package org.japura.modal;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameListener;

/**
 * Modal for frames and dialogs.
 * <P>
 * Adds a component as modal to the JFrame.
 * <P>
 * Each JFrame may have more than one component as modal, but only one component
 * can be visible at once.
 * <P>
 * Copyright (C) 2009-2010 Carlos Eduardo Leite de Andrade
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
 */
public class Modal{

  private static Integer defaultModalDepth = Integer
	  .valueOf(JLayeredPane.DEFAULT_LAYER + 1);

  private static Hashtable<RootPaneContainer, Modal> modals =
	  new Hashtable<RootPaneContainer, Modal>();

  private RootPaneContainer rootPane;

  private ModalPanel modalPanel;

  private int oldDefaultCloseOperation;

  private WindowListener[] oldWindowListeners;
  private InternalFrameListener[] oldInternalFrameListeners;

  private ComponentListener resizeListener;

  private List<Component> components;

  private HashMap<Component, List<ModalListener>> listeners;
  private HashMap<Component, Integer> depths;

  /**
   * Get the current modal component of a specific frame.
   * 
   * @param frame
   *          the frame with a modal
   * @return Component
   */
  public static Component getCurrentModal(JFrame frame) {
	return getCurrentModal_(frame);
  }

  /**
   * Get the current modal component of a specific dialog.
   * 
   * @param dialog
   *          the dialog with a modal
   * @return Component
   */
  public static Component getCurrentModal(JDialog dialog) {
	return getCurrentModal_(dialog);
  }

  /**
   * Get the current modal component of a specific internal frame.
   * 
   * @param frame
   *          the internal frame with a modal
   * @return Component
   */
  public static Component getCurrentModal(JInternalFrame frame) {
	return getCurrentModal_(frame);
  }

  /**
   * Get the current modal component of a specific RootPaneContainer.
   * 
   * @param rootPane
   *          the RootPaneContainer with a modal
   * @return Component or <code>NULL</code>
   */
  private static Component getCurrentModal_(RootPaneContainer rootPane) {
	synchronized (modals) {
	  Modal modal = modals.get(rootPane);
	  if (modal != null) {
		if (modal.getSize() > 0) {
		  return modal.components.get(modal.components.size() - 1);
		}
	  }
	  return null;
	}
  }

  /**
   * Close the current modal component of a specific frame.
   * 
   * @param frame
   *          the frame with a modal
   */
  public static void closeCurrentModal(JFrame frame) {
	closeCurrentModal_(frame);
  }

  /**
   * Close the current modal component of a specific dialog.
   * 
   * @param dialog
   *          the dialog with a modal
   */
  public static void closeCurrentModal(JDialog dialog) {
	closeCurrentModal_(dialog);
  }

  /**
   * Close the current modal component of a specific internal frame.
   * 
   * @param frame
   *          the internal frame with a modal
   */
  public static void closeCurrentModal(JInternalFrame frame) {
	closeCurrentModal_(frame);
  }

  /**
   * Close the current modal component of a specific RootPaneContainer.
   * 
   * @param rootPane
   *          the RootPaneContainer with a modal
   */
  private static void closeCurrentModal_(RootPaneContainer rootPane) {
	synchronized (modals) {
	  Modal modal = modals.get(rootPane);
	  if (modal != null) {
		modal.closeCurrent();
		if (modal.getSize() == 0) {
		  modals.remove(rootPane);
		}
	  }
	}
  }

  /**
   * Close a specific modal component.
   * 
   * @param component
   *          the modal's component
   */
  public static void closeModal(Component component) {
	synchronized (modals) {
	  RootPaneContainer frame = null;
	  Modal modal = null;

	  Iterator<Entry<RootPaneContainer, Modal>> it =
		  modals.entrySet().iterator();
	  while (it.hasNext()) {
		Entry<RootPaneContainer, Modal> entry = it.next();
		modal = entry.getValue();
		frame = entry.getKey();
		if (frame != null && modal != null
			&& modal.components.contains(component)) {
		  break;
		} else {
		  frame = null;
		  modal = null;
		}
	  }
	  if (frame != null && modal != null) {
		modal.close(component);
		if (modal.getSize() == 0) {
		  modals.remove(frame);
		}
	  }
	}
  }

  /**
   * Close all modals of a specific frame.
   * 
   * @param frame
   *          the frame with a modal
   */
  public static void closeAllModals(JFrame frame) {
	closeAllModals_(frame);
  }

  /**
   * Close all modals of a specific dialog.
   * 
   * @param dialog
   *          the dialog with a modal
   */
  public static void closeAllModals(JDialog dialog) {
	closeAllModals_(dialog);
  }

  /**
   * Close all modals of a specific internal frame.
   * 
   * @param frame
   *          the internal frame with a modal
   */
  public static void closeAllModals(JInternalFrame frame) {
	closeAllModals_(frame);
  }

  /**
   * Close all modals of a specific RootPaneContainer.
   * 
   * @param rootPane
   *          the RootPaneContainer with a modal
   */
  private static void closeAllModals_(RootPaneContainer rootPane) {
	synchronized (modals) {
	  Modal modal = modals.get(rootPane);
	  if (modal != null) {
		modal.closeAll();
		modals.remove(rootPane);
	  }
	}
  }

  /**
   * Indicates whether the frame has a modal.
   * 
   * @param frame
   *          the frame with a modal
   * @return boolean
   */
  public static boolean hasModal(JFrame frame) {
	return hasModal_(frame);
  }

  /**
   * Indicates whether the dialog has a modal.
   * 
   * @param dialog
   *          the dialog with a modal
   * @return boolean
   */
  public static boolean hasModal(JDialog dialog) {
	return hasModal_(dialog);
  }

  /**
   * Indicates whether the internal frame has a modal.
   * 
   * @param frame
   *          the internal frame with a modal
   * @return boolean
   */
  public static boolean hasModal(JInternalFrame frame) {
	return hasModal_(frame);
  }

  /**
   * Indicates whether the RootPaneContainer has a modal.
   * 
   * @param rootPane
   *          the RootPaneContainer with a modal
   * @return boolean
   */
  private static boolean hasModal_(RootPaneContainer rootPane) {
	synchronized (modals) {
	  Modal modal = modals.get(rootPane);
	  if (modal != null && modal.getSize() > 0) {
		return true;
	  }
	  return false;
	}
  }

  /**
   * Get the modal of a specific frame
   * 
   * @param rootPane
   *          the RootPaneContainer with a modal
   * @return {@link Modal}
   */
  private static Modal getModal(RootPaneContainer rootPane) {
	synchronized (modals) {
	  Modal modal = modals.get(rootPane);
	  if (modal == null) {
		modal = new Modal(rootPane);
		modals.put(rootPane, modal);
	  }
	  return modal;
	}
  }

  /**
   * Indicates whether the component is a modal component.
   * 
   * @param component
   *          the component
   * @return boolean
   */
  public static boolean isModal(Component component) {
	synchronized (modals) {
	  Iterator<Entry<RootPaneContainer, Modal>> it =
		  modals.entrySet().iterator();
	  while (it.hasNext()) {
		Entry<RootPaneContainer, Modal> entry = it.next();
		if (entry.getValue().components.contains(component)) {
		  return true;
		}
	  }
	  return false;
	}
  }

  /**
   * Add a modal component in the internal frame.
   * 
   * @param frame
   *          the internal frame
   * @param component
   *          the component for modal
   */
  public static void addModal(JInternalFrame frame, Component component) {
	addModal(frame, component, null);
  }

  /**
   * Add a modal component in the internal frame.
   * 
   * @param frame
   *          the internal frame
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   */
  public static void addModal(JInternalFrame frame, Component component,
							  ModalListener listener) {
	addModal_(frame, component, listener, defaultModalDepth);
  }

  /**
   * Add a modal component in the internal frame.
   * 
   * @param frame
   *          the internal frame
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   * @param modalDepth
   *          the depth for modal
   */
  public static void addModal(JInternalFrame frame, Component component,
							  ModalListener listener, Integer modalDepth) {
	addModal_(frame, component, listener, modalDepth);
  }

  /**
   * Add a modal component in the dialog.
   * 
   * @param dialog
   *          the dialog
   * @param component
   *          the component for modal
   */
  public static void addModal(JDialog dialog, Component component) {
	addModal(dialog, component, null);
  }

  /**
   * Add a modal component in the dialog.
   * 
   * @param dialog
   *          the dialog
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   */
  public static void addModal(JDialog dialog, Component component,
							  ModalListener listener) {
	addModal_(dialog, component, listener, defaultModalDepth);
  }

  /**
   * Add a modal component in the dialog.
   * 
   * @param dialog
   *          the dialog
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   * @param modalDepth
   *          the depth for modal
   */
  public static void addModal(JDialog dialog, Component component,
							  ModalListener listener, Integer modalDepth) {
	addModal_(dialog, component, listener, modalDepth);
  }

  /**
   * Add a modal component in the frame.
   * 
   * @param frame
   *          the frame
   * @param component
   *          the component for modal
   */
  public static void addModal(JFrame frame, Component component) {
	addModal(frame, component, null);
  }

  /**
   * Add a modal component in the frame.
   * 
   * @param frame
   *          the frame
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   */
  public static void addModal(JFrame frame, Component component,
							  ModalListener listener) {
	addModal_(frame, component, listener, defaultModalDepth);
  }

  /**
   * Add a modal component in the frame.
   * 
   * @param frame
   *          the frame
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   * @param modalDepth
   *          the depth for modal
   */
  public static void addModal(JFrame frame, Component component,
							  ModalListener listener, Integer modalDepth) {
	addModal_(frame, component, listener, modalDepth);
  }

  /**
   * Add a modal component in the RootPaneContainer.
   * 
   * @param rootPane
   *          the RootPaneContainer
   * @param component
   *          the component for modal
   * @param listener
   *          the listener for modal
   * @param modalDepth
   *          the depth for modal
   */
  private static void addModal_(RootPaneContainer rootPane,
								Component component, ModalListener listener,
								Integer modalDepth) {
	synchronized (modals) {
	  if (isModal(component) == false) {
		getModal(rootPane).addModal(component, listener, modalDepth);
	  }
	}
  }

  public static void addListener(JFrame frame, Component component,
								 ModalListener listener) {
	addListener_(frame, component, listener);
  }

  public static void addListener(JDialog dialog, Component component,
								 ModalListener listener) {
	addListener_(dialog, component, listener);
  }

  public static void addListener(JInternalFrame frame, Component component,
								 ModalListener listener) {
	addListener_(frame, component, listener);
  }

  private static void addListener_(RootPaneContainer rootPane,
								   Component component, ModalListener listener) {
	synchronized (modals) {
	  if (isModal(component)) {
		getModal(rootPane).addListener(component, listener);
	  }
	}
  }

  /**
   * Get the modal count of a specific frame.
   * 
   * @param frame
   *          the frame with a modal
   * @return int
   */
  public static int getModalCount(JFrame frame) {
	return getModalCount_(frame);
  }

  /**
   * Get the modal count of a specific dialog.
   * 
   * @param dialog
   *          the dialog with a modal
   * @return int
   */
  public static int getModalCount(JDialog dialog) {
	return getModalCount_(dialog);
  }

  /**
   * Get the modal count of a specific internal frame.
   * 
   * @param frame
   *          the internal frame with a modal
   * @return int
   */
  public static int getModalCount(JInternalFrame frame) {
	return getModalCount_(frame);
  }

  /**
   * Get the modal count of a specific RootPaneContainer.
   * 
   * @param frame
   *          the RootPaneContainer with a modal
   * @return int
   */
  private static int getModalCount_(RootPaneContainer rootPane) {
	synchronized (modals) {
	  Modal modal = modals.get(rootPane);
	  if (modal != null) {
		return modal.getSize();
	  }
	  return 0;
	}
  }

  /**
   * Constructor
   * 
   * @param rootPane
   */
  private Modal(RootPaneContainer rootPane) {
	this.rootPane = rootPane;
	resizeListener = new ComponentAdapter() {
	  @Override
	  public void componentResized(ComponentEvent e) {
		resizeModalPanel();
	  }
	};

	components = new ArrayList<Component>();
	depths = new HashMap<Component, Integer>();
	listeners = new HashMap<Component, List<ModalListener>>();

	this.rootPane.getRootPane().addComponentListener(resizeListener);

	resizeModalPanel();

	if (rootPane instanceof JFrame) {
	  backupProperties((JFrame) rootPane);
	} else if (rootPane instanceof JDialog) {
	  backupProperties((JDialog) rootPane);
	} else if (rootPane instanceof JInternalFrame) {
	  backupProperties((JInternalFrame) rootPane);
	}

	getModalPanel().setVisible(true);
  }

  private void backupProperties(JFrame frame) {
	oldDefaultCloseOperation = frame.getDefaultCloseOperation();
	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	oldWindowListeners = frame.getWindowListeners();
	for (WindowListener listener : oldWindowListeners) {
	  frame.removeWindowListener(listener);
	}
  }

  private void backupProperties(JDialog dialog) {
	oldDefaultCloseOperation = dialog.getDefaultCloseOperation();
	dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	oldWindowListeners = dialog.getWindowListeners();
	for (WindowListener listener : oldWindowListeners) {
	  dialog.removeWindowListener(listener);
	}
  }

  private void backupProperties(JInternalFrame frame) {
	oldDefaultCloseOperation = frame.getDefaultCloseOperation();
	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	oldInternalFrameListeners = frame.getInternalFrameListeners();
	for (InternalFrameListener listener : oldInternalFrameListeners) {
	  frame.removeInternalFrameListener(listener);
	}
  }

  /**
   * Close the current modal.
   */
  private void closeCurrent() {
	if (components.size() > 0) {
	  Component component = components.remove(components.size() - 1);
	  getModalPanel().removeAll();
	  fireCloseActionListener(listeners.get(component));
	  listeners.remove(component);

	  if (components.size() > 0) {
		showNextComponent();
	  } else {
		restoreRootPane();
	  }
	}
  }

  private void resizeModalPanel() {
	Dimension dim = Modal.this.rootPane.getRootPane().getSize();
	getModalPanel().setBounds(0, 0, dim.width, dim.height);
	getModalPanel().revalidate();
  }

  private void restoreRootPane() {
	rootPane.getRootPane().removeComponentListener(resizeListener);
	rootPane.getLayeredPane().remove(getModalPanel());
	((Component) rootPane).repaint();
	if (rootPane instanceof JFrame) {
	  JFrame frame = (JFrame) rootPane;
	  frame.setDefaultCloseOperation(oldDefaultCloseOperation);
	  for (WindowListener listener : oldWindowListeners) {
		frame.addWindowListener(listener);
	  }
	} else if (rootPane instanceof JDialog) {
	  JDialog dialog = (JDialog) rootPane;
	  dialog.setDefaultCloseOperation(oldDefaultCloseOperation);
	  for (WindowListener listener : oldWindowListeners) {
		dialog.addWindowListener(listener);
	  }
	} else if (rootPane instanceof JInternalFrame) {
	  JInternalFrame frame = (JInternalFrame) rootPane;
	  frame.setDefaultCloseOperation(oldDefaultCloseOperation);
	  for (InternalFrameListener listener : oldInternalFrameListeners) {
		frame.addInternalFrameListener(listener);
	  }
	}
  }

  /**
   * Close a modal
   * 
   * @param component
   *          - the modal's component
   * 
   */
  private void close(Component component) {
	if (components.size() > 0) {
	  if (components.contains(component) == false) {
		return;
	  }

	  components.remove(component);
	  depths.remove(component);
	  getModalPanel().removeAll();
	  fireCloseActionListener(listeners.get(component));
	  listeners.remove(component);

	  if (components.size() > 0) {
		showNextComponent();
	  } else {
		restoreRootPane();
	  }
	}
  }

  private void showNextComponent() {
	Component component = components.get(components.size() - 1);
	Integer modalDepth = depths.get(component);
	rootPane.getLayeredPane().remove(getModalPanel());
	rootPane.getLayeredPane().add(getModalPanel(), modalDepth);
	getModalPanel().add("", component);
	getModalPanel().revalidate();
	getModalPanel().repaint();
  }

  private void fireCloseActionListener(List<ModalListener> list) {
	if (list != null) {
	  for (ModalListener listener : list) {
		listener.modalClosed(new ModalEvent(rootPane));
	  }
	}
  }

  /**
   * Close all modals.
   */
  private void closeAll() {
	getModalPanel().removeAll();
	components.clear();
	depths.clear();
	listeners.clear();
	restoreRootPane();
  }

  /**
   * Add a modal component
   * 
   * @param component
   *          {@link Component}
   * @param listener
   *          {@link ModalListener}
   * @param modalDepth
   */
  private void addModal(Component component, ModalListener listener,
						Integer modalDepth) {
	if (modalDepth == null) {
	  modalDepth = defaultModalDepth;
	}
	if (components.contains(component) == false) {

	  rootPane.getLayeredPane().remove(getModalPanel());
	  rootPane.getLayeredPane().add(getModalPanel(), modalDepth);

	  KeyboardFocusManager focusManager =
		  KeyboardFocusManager.getCurrentKeyboardFocusManager();
	  focusManager.clearGlobalFocusOwner();
	  getModalPanel().removeAll();
	  getModalPanel().add("", component);
	  resizeModalPanel();
	  getModalPanel().repaint();
	  components.add(component);
	  depths.put(component, modalDepth);
	  addListener(component, listener);
	}
  }

  private void addListener(Component component, ModalListener listener) {
	if (components.contains(component)) {
	  List<ModalListener> list = listeners.get(component);
	  if (list == null) {
		list = new ArrayList<ModalListener>();
		listeners.put(component, list);
	  }
	  if (listener != null) {
		list.add(listener);
	  }
	}
  }

  /**
   * Get the modal count
   * 
   * @return int
   */
  private int getSize() {
	return components.size();
  }

  /**
   * Get the transparent modal panel
   * 
   * @return {@link ModalPanel}
   */
  private ModalPanel getModalPanel() {
	if (modalPanel == null) {
	  modalPanel = new ModalPanel();
	  modalPanel.setLayout(new ModalLayout());
	}
	return modalPanel;
  }

  /**
   * Layout manager for modal panel
   * 
   */
  private static class ModalLayout implements LayoutManager{

	private Component comp;

	@Override
	public void addLayoutComponent(String name, Component comp) {
	  this.comp = comp;
	}

	@Override
	public void layoutContainer(Container parent) {
	  if (comp != null) {
		Dimension cdim = comp.getPreferredSize();
		Dimension pdim = parent.getSize();
		int x = (pdim.width / 2) - (cdim.width / 2);
		int y = (pdim.height / 2) - (cdim.height / 2);
		comp.setBounds(x, y, cdim.width, cdim.height);
	  }
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
	  return preferredLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
	  Dimension dim = new Dimension();
	  if (comp != null) {
		return comp.getPreferredSize();
	  }
	  return dim;
	}

	@Override
	public void removeLayoutComponent(Component comp) {

	}
  }

}
