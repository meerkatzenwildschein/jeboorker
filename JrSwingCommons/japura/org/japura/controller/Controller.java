package org.japura.controller;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.japura.modal.Modal;
import org.japura.modal.ModalEvent;
import org.japura.modal.ModalListener;
import org.japura.task.Task;
import org.japura.task.TaskManager;
import org.japura.task.TaskManagerListener;

/**
 * The Controller isolates business logic from presentation.
 * <P>
 * Must implement the following methods:
 * <UL>
 * <LI> <code>getComponent</code> to get the controlled component</LI>
 * <LI> <code>isComponentInstancied</code> to indicates whether the controller is
 * instantiated</LI>
 * </UL>
 * <P>
 * Every instantiated controller is added to a pool of controllers. Through the
 * pool, its possible reach any controller.
 * <P>
 * The state of permanent indicates that the controller can't be removed from
 * the pool unless the parent has been removed.
 * <P>
 * Annotations:
 * <P>
 * <UL>
 * <LI><code>DisposeAction</code> - marks a method to run when the controller is
 * removed from memory with the method <code>free</code>.</LI>
 * <LI><code>ChildController</code> - defines that a controller can't be
 * instantiated by constructor, only through method <code>createChild</code>.</LI>
 * <LI><code>Singleton</code> - defines a controller as singleton.</LI>
 * </UL>
 * <P>
 * Copyright (C) 2009-2011 Carlos Eduardo Leite de Andrade
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
 * E
 * 
 * @author Carlos Eduardo Leite de Andrade
 * 
 * @param V
 *          controlled component class
 */
public abstract class Controller<V extends Component> {

  static {
	TaskManager.addListener(getControllerTaskListener());
  }

  private static ControllerTaskListener controllerTaskListener;

  private static Random idRandom = new Random();

  private static ModalPanelFactory modalPanelFactory =
	  new DefaultModalPanelFactory();

  private static ArrayList<Controller<?>> controllers =
	  new ArrayList<Controller<?>>();

  private static Object parentControllerLock = new Object();
  private static Integer newParentControllerId;

  private static DebugWindow debugWindow;

  private String controllerName;
  private Integer parentControllerId;
  private List<Controller<?>> childrenControllers;
  private List<MessageFilter> filters;
  private LinkedHashMap<Class<? extends Message>, List<Subscriber>> subscribers;
  private int id;
  private Group group;
  private boolean permanent;
  private String modalGroupName;

  /**
   * Constructor
   */
  public Controller() {
	synchronized (parentControllerLock) {
	  if (getClass().isAnnotationPresent(ChildController.class)
		  && newParentControllerId == null) {
		throw new ControllerException(
			"["
				+ getClass().getName()
				+ "] Child controller must be instantiated through method createChild");
	  }

	  this.parentControllerId = newParentControllerId;
	  newParentControllerId = null;
	  childrenControllers = new ArrayList<Controller<?>>();
	  filters = new ArrayList<MessageFilter>();
	  subscribers =
		  new LinkedHashMap<Class<? extends Message>, List<Subscriber>>();
	  join(this);
	}
  }

  public String getControllerName() {
	return controllerName;
  }

  public void setControllerName(String controllerName) {
	this.controllerName = controllerName;
  }

  private static void removeFromDebugWindow(Controller<?> controller) {
	if (debugWindow != null) {
	  debugWindow.controllerRemoved(controller);
	}
  }

  private static void updateDebugWindow() {
	if (debugWindow != null) {
	  debugWindow.update();
	}
  }

  public static void closeDebugWindow() {
	debugWindow.dispose();
	debugWindow = null;
  }

  public static void showDebugWindow() {
	if (debugWindow == null) {
	  debugWindow = new DebugWindow();
	  debugWindow.setLocationRelativeTo(null);
	}
	debugWindow.update();
	debugWindow.setVisible(true);
  }

  private static synchronized ControllerTaskListener getControllerTaskListener() {
	if (controllerTaskListener == null) {
	  controllerTaskListener = new ControllerTaskListener();
	}
	return controllerTaskListener;
  }

  /**
   * Adds a controller in the pool of controllers.
   * 
   * @param controller
   *          the controller
   */
  private static synchronized void join(Controller<?> controller) {
	if (controller != null) {
	  Class<?> cl = controller.getClass();
	  if (cl.isAnnotationPresent(Singleton.class) && count(cl) > 0) {
		throw new ControllerException("[" + cl.getName()
			+ "] Singleton Controller");
	  }
	  int nextId;
	  do {
		nextId = idRandom.nextInt(Integer.MAX_VALUE);
	  } while (existsId(nextId));
	  controller.setId(nextId);

	  Integer parentId = controller.getParentId();
	  if (parentId != null) {
		Controller<?> parentController = get(parentId);
		Group group = parentController.getGroup();
		controller.setGroup(group);
		parentController.addChild(controller);
	  } else {
		do {
		  nextId = idRandom.nextInt(Integer.MAX_VALUE);
		} while (existsId(nextId));
		controller.setGroup(new Group(nextId));
	  }

	  controllers.add(controller);
	  updateDebugWindow();
	}
  }

  /**
   * Indicates whether a controller or group identifier exists.
   * 
   * @param id
   *          the controller's identifier or group's identifier
   * @return boolean
   */
  private static synchronized boolean existsId(int id) {
	for (Controller<?> controller : controllers) {
	  if (controller.getId() == id) {
		return true;
	  }
	  if (controller.getGroupId() == id) {
		return true;
	  }
	}
	return false;
  }

  static synchronized List<Controller<?>> getControllers() {
	return Collections.unmodifiableList(controllers);
  }

  /**
   * Remove a controller from the memory.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * <P>
   * All executions of the tasks will be canceled.
   * 
   * @param id
   *          the controller's identifier
   * 
   * @see #setPermanent(boolean)
   * @see #isPermanent()
   * @see #freeAll()
   * @see #freeAll(Class)
   */
  public static synchronized void free(int id) {
	free(id, true);
  }

  /**
   * Remove a controller from the memory.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * 
   * @param id
   *          the controller's identifier
   * @param cancelTaskExecution
   * 
   * @see #setPermanent(boolean)
   * @see #isPermanent()
   * @see #freeAll()
   * @see #freeAll(Class)
   */
  public static synchronized void free(int id, boolean cancelTaskExecution) {
	for (Controller<?> controller : controllers) {
	  if (controller.getId() == id) {
		free(controller, cancelTaskExecution);
		break;
	  }
	}
  }

  /**
   * Remove from the memory all controllers of a specific group.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * <P>
   * All executions of the tasks will be canceled.
   * 
   * @param groupdId
   * 
   * @see #setPermanent(boolean)
   * @see #isPermanent()
   * @see #free(int)
   */
  public static synchronized void freeGroup(int groupdId) {
	List<Integer> ids = new ArrayList<Integer>();
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupdId) {
		ids.add(controller.getId());
	  }
	}
	for (int id : ids) {
	  free(id);
	}
  }

  /**
   * Remove from the memory all controllers of a specific class.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * <P>
   * All executions of the tasks will be canceled.
   * 
   * @param cls
   *          controller's class
   * 
   * @see #setPermanent(boolean)
   * @see #isPermanent()
   * @see #free(int)
   */
  public static synchronized void freeAll(Class<?> cls) {
	List<Integer> ids = new ArrayList<Integer>();
	for (Controller<?> controller : controllers) {
	  if (cls.isAssignableFrom(controller.getClass())) {
		ids.add(controller.getId());
	  }
	}
	for (int id : ids) {
	  free(id);
	}
  }

  /**
   * Remove from the memory all controllers of a specific class and group.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * <P>
   * All executions of the tasks will be canceled.
   * 
   * @param groupId
   * @param cls
   *          controller's class
   * 
   * @see #setPermanent(boolean)
   * @see #isPermanent()
   * @see #free(int)
   */
  public static synchronized void freeAllFromGroup(int groupId, Class<?> cls) {
	List<Integer> ids = new ArrayList<Integer>();
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId
		  && cls.isAssignableFrom(controller.getClass())) {
		ids.add(controller.getId());
	  }
	}
	for (int id : ids) {
	  free(id);
	}
  }

  /**
   * Indicates whether the controller is instantiated.
   * 
   * @param cls
   *          controller's class
   * @return boolean
   */
  public static synchronized boolean isInstancied(Class<?> cls) {
	for (Controller<?> controller : controllers) {
	  if (cls.isAssignableFrom(controller.getClass())) {
		return true;
	  }
	}
	return false;
  }

  /**
   * Indicates whether the controller is instantiated in a group.
   * 
   * @param groupId
   * @param cls
   *          controller's class
   * @return boolean
   */
  public static synchronized boolean isInstanciedInGroup(int groupId,
														 Class<?> cls) {
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId
		  && cls.isAssignableFrom(controller.getClass())) {
		return true;
	  }
	}
	return false;
  }

  /**
   * Remove from the memory all controllers.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * <P>
   * All executions of the tasks will be canceled.
   * 
   * @see #setPermanent(boolean)
   * @see #isPermanent()
   * @see #free(int)
   * @see #freeAll(Class)
   */
  public static synchronized void freeAll() {
	List<Integer> ids = new ArrayList<Integer>();
	for (Controller<?> controller : controllers) {
	  ids.add(controller.getId());
	}
	for (int id : ids) {
	  free(id);
	}
  }

  /**
   * Remove a specific controller from the memory.
   * <P>
   * Controllers defined as permanent, can not be removed unless the parent has
   * been removed.
   * 
   * @param controller
   *          the controller
   * @param cancelTaskExecution
   *          <CODE>TRUE</CODE> to cancel all executions of the tasks
   */
  private static synchronized void free(Controller<?> controller,
										boolean cancelTaskExecution) {
	if (controller != null && controller.isPermanent() == false) {
	  if (cancelTaskExecution) {
		TaskManager.cancel(controller.getGroupId());
	  }

	  List<Controller<?>> children = new ArrayList<Controller<?>>();
	  fetchHierarchy(controller, children);

	  closeModal(controller);
	  for (Controller<?> child : children) {
		closeModal(child);
	  }

	  runDisposeActions(controller);
	  for (Controller<?> child : children) {
		runDisposeActions(child);
	  }

	  Integer parentId = controller.getParentId();
	  if (parentId != null) {
		Controller<?> parentController = get(parentId);
		parentController.removeChild(controller);
	  }

	  controllers.remove(controller);
	  removeFromDebugWindow(controller);
	  for (Controller<?> child : children) {
		controllers.remove(child);
		removeFromDebugWindow(child);
	  }
	  updateDebugWindow();
	}
  }

  private static void closeModal(Controller<?> controller) {
	if (controller.isComponentInstancied()) {
	  Component component = controller.getComponent();
	  if (component != null) {
		if (component instanceof JFrame) {
		  JFrame frame = (JFrame) component;
		  Modal.closeAllModals(frame);
		} else if (component instanceof JDialog) {
		  JDialog dialog = (JDialog) component;
		  Modal.closeAllModals(dialog);
		} else if (component instanceof JInternalFrame) {
		  JInternalFrame frame = (JInternalFrame) component;
		  Modal.closeAllModals(frame);
		}
	  }
	}
  }

  private static synchronized void runDisposeActions(Controller<?> controller) {
	Class<?> cls = controller.getClass();
	while (cls != null) {
	  Method[] methods = cls.getDeclaredMethods();
	  for (Method method : methods) {
		method.setAccessible(true);
		if (method.isAnnotationPresent(DisposeAction.class)) {
		  try {
			method.invoke(controller);
		  } catch (IllegalArgumentException e) {
			throw new ControllerException(
				"Method ["
					+ cls.getName()
					+ "."
					+ method.getName()
					+ "]. DisposeAction can not be used in method with parameters.",
				e);
		  } catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		  } catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		  }
		}
	  }
	  cls = cls.getSuperclass();
	}
  }

  private static synchronized void fetchHierarchy(Controller<?> parentController,
												  List<Controller<?>> list) {
	List<Controller<?>> children = parentController.getChildren();
	for (Controller<?> child : children) {
	  list.add(child);
	  fetchHierarchy(child, list);
	}
  }

  /**
   * Get the total of controllers of a specific class.
   * 
   * @param cl
   *          the controller's class
   * @return the total of controllers
   */
  public static synchronized int count(Class<?> cl) {
	int count = 0;
	for (Controller<?> controller : controllers) {
	  if (cl.isAssignableFrom(controller.getClass())) {
		count++;
	  }
	}
	return count;
  }

  /**
   * Get the first controller of a specific class.
   * <P>
   * If not exists, a controller will be instantiated.
   * 
   * @param <E>
   *          .
   * @param cl
   *          the controller's class
   * @return the controller
   * 
   * @see #get(int, Class)
   */
  public static synchronized <E> E get(Class<E> cl) {
	if (Controller.class.isAssignableFrom(cl) == false) {
	  throw new InvalidControllerClassException(cl);
	}
	for (Controller<?> controller : controllers) {
	  if (cl.isAssignableFrom(controller.getClass())) {
		return cl.cast(controller);
	  }
	}
	return newInstance(cl);
  }

  /**
   * Get the controller of a specific class and identifier.
   * 
   * @param id
   *          the controller's identifier
   * @param cl
   *          the controller's class
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   * @see #get(Class)
   */
  public static synchronized <E> E get(int id, Class<E> cl) {
	for (Controller<?> controller : controllers) {
	  if (controller.getId() == id
		  && cl.isAssignableFrom(controller.getClass())) {
		return cl.cast(controller);
	  }
	}
	return null;
  }

  /**
   * Get the controller of a specific group and class.
   * 
   * @param groupId
   *          the controller's group identifier
   * @param cl
   *          the controller's class
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   */
  public static synchronized <E> E getFromGroup(int groupId, Class<E> cl) {
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId
		  && cl.isAssignableFrom(controller.getClass())) {
		return cl.cast(controller);
	  }
	}
	return null;
  }

  /**
   * Get the controller of a specific group, class and identifier.
   * 
   * @param groupId
   *          the controller's group identifier
   * @param id
   *          the controller's identifier
   * @param cl
   *          the controller's class
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   */
  public static synchronized <E> E getFromGroup(int groupId, int id, Class<E> cl) {
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId && controller.getId() == id
		  && cl.isAssignableFrom(controller.getClass())) {
		return cl.cast(controller);
	  }
	}
	return null;
  }

  /**
   * Get the controller of a specific group and identifier.
   * 
   * @param groupId
   *          the controller's group identifier
   * @param id
   *          the controller's identifier
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   */
  public static synchronized Controller<?> getFromGroup(int groupId, int id) {
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId && controller.getId() == id) {
		return controller;
	  }
	}
	return null;
  }

  /**
   * Get the all controllers of a specific group and class.
   * 
   * @param groupId
   *          the controller's group identifier
   * @param cl
   *          the controller's class
   * @return List with the controllers.
   * 
   * @see #get(Class)
   */
  public static synchronized <E> List<E> getAllFromGroup(int groupId,
														 Class<E> cl) {
	List<E> list = new ArrayList<E>();
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId
		  && cl.isAssignableFrom(controller.getClass())) {
		list.add(cl.cast(controller));
	  }
	}
	return list;
  }

  /**
   * Get the all controllers of a specific group.
   * 
   * @param groupId
   *          the controller's group identifier
   * @return List with the controllers.
   * 
   * @see #get(Class)
   */
  public static synchronized List<Controller<?>> getAllFromGroup(int groupId) {
	List<Controller<?>> list = new ArrayList<Controller<?>>();
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId) {
		list.add(controller);
	  }
	}
	return list;
  }

  /**
   * Get the controller with a specific identifier.
   * 
   * @param id
   *          the controller's identifier
   * @return the controller or <code>NULL</code> if its not exist.
   */
  public static synchronized Controller<?> get(int id) {
	for (Controller<?> controller : controllers) {
	  if (controller.getId() == id) {
		return controller;
	  }
	}
	return null;
  }

  /**
   * Get the controller with a specific identifier.
   * 
   * @param id
   *          the controller's identifier
   * @return the controller or <code>NULL</code> if its not exist.
   */
  private Controller<?> localGet(int id) {
	for (Controller<?> controller : controllers) {
	  if (controller.getId() == id) {
		return controller;
	  }
	}
	return null;
  }

  /**
   * Creates a new controller
   * 
   * @param <E>
   * @param cl
   *          controller's class
   * @return the controller
   */
  private static <E> E newInstance(Class<E> cl) {
	E controller = null;
	if (Controller.class.isAssignableFrom(cl)) {
	  try {
		controller = cl.newInstance();
	  } catch (InstantiationException e) {
		e.printStackTrace();
	  } catch (IllegalAccessException e) {
		e.printStackTrace();
	  } catch (SecurityException e) {
		e.printStackTrace();
	  } catch (IllegalArgumentException e) {
		e.printStackTrace();
	  }
	}
	return controller;
  }

  private static void setNewUpControllerId(int id) {
	newParentControllerId = id;
  }

  /**
   * Creates a child controller.
   * <P>
   * The new controller contains the parent controller's identifier.
   * 
   * @param <E>
   * @param cl
   *          the controller's class
   * @return the new controller
   * @see #getRoot()
   * @see #getParent()
   */
  public final <E> E createChild(Class<E> cl) {
	synchronized (parentControllerLock) {
	  if (Controller.class.isAssignableFrom(cl) == false) {
		throw new InvalidControllerClassException(cl);
	  }
	  if (controllers.contains(this) == false) {
		throw new ControllerException("The controller " + getClass() + " id ("
			+ getId() + ") already had been removed from the pool. ");
	  }
	  setNewUpControllerId(getId());
	  return newInstance(cl);
	}
  }

  /**
   * Get all controllers of a specific class.
   * 
   * @param cl
   *          the controller's class
   * @return List with the controllers
   */
  public static synchronized <E> List<E> getAll(Class<E> cl) {
	List<E> list = new ArrayList<E>();
	for (Controller<?> controller : controllers) {
	  if (cl.isAssignableFrom(controller.getClass())) {
		list.add(cl.cast(controller));
	  }
	}
	return list;
  }

  /**
   * Get all controllers
   * 
   * @return List with all controllers
   */
  public static synchronized List<Controller<?>> getAll() {
	return new ArrayList<Controller<?>>(controllers);
  }

  /**
   * Get the first controller of a specific class and controlled component.
   * 
   * @param <E>
   *          .
   * @param cl
   *          the controller's class
   * @param comp
   *          the controlled component
   * @return the controller or <code>NULL</code> if its not exist.
   * @see #get(Class)
   * @see #get(int)
   */
  public static synchronized <E> E get(Class<E> cl, Component comp) {
	if (Controller.class.isAssignableFrom(cl) == false) {
	  return null;
	}
	for (Controller<?> controller : controllers) {
	  if (cl.isAssignableFrom(controller.getClass())
		  && controller.isComponentInstancied()
		  && controller.getComponent().equals(comp)) {
		return cl.cast(controller);
	  }
	}
	return null;
  }

  /**
   * Get the first controller of a specific controlled component.
   * 
   * @param comp
   *          the controlled component
   * @return the controller or <code>NULL</code> if its not exist.
   * @see #get(Class)
   * @see #get(int)
   */
  public static synchronized Controller<?> get(Component comp) {
	for (Controller<?> controller : controllers) {
	  if (controller.isComponentInstancied()
		  && controller.getComponent().equals(comp)) {
		return controller;
	  }
	}
	return null;
  }

  public static void setModalPanelFactory(ModalPanelFactory modalPanelFactory) {
	if (modalPanelFactory != null) {
	  Controller.modalPanelFactory = modalPanelFactory;
	}
  }

  public static ModalPanelFactory getModalPanelFactory() {
	return modalPanelFactory;
  }

  /**
   * Indicates whether the child controller is instantiated.
   * 
   * @param cls
   *          controller's class
   * @return boolean
   */
  public synchronized boolean isChildInstancied(Class<?> cls) {
	for (Controller<?> controller : childrenControllers) {
	  if (cls.isAssignableFrom(controller.getClass())) {
		return true;
	  }
	}
	return false;
  }

  public synchronized List<Controller<?>> getChildren() {
	return Collections.unmodifiableList(childrenControllers);
  }

  public synchronized <E> E getChild(Class<E> clss) {
	for (Controller<?> controller : childrenControllers) {
	  if (clss.isAssignableFrom(controller.getClass())) {
		return clss.cast(controller);
	  }
	}
	return null;
  }

  public synchronized Controller<?> getChild(int id) {
	for (Controller<?> controller : childrenControllers) {
	  if (controller.getId() == id) {
		return controller;
	  }
	}
	return null;
  }

  public synchronized <E> List<E> getChildren(Class<E> clss) {
	List<E> list = new ArrayList<E>();
	for (Controller<?> controller : childrenControllers) {
	  if (clss.isAssignableFrom(controller.getClass())) {
		list.add(clss.cast(controller));
	  }
	}
	return list;
  }

  private void addChild(Controller<?> controller) {
	this.childrenControllers.add(controller);
  }

  private void removeChild(Controller<?> controller) {
	this.childrenControllers.remove(controller);
  }

  public Window getWindowAncestor() {
	if (isComponentInstancied()) {
	  if (getComponent() instanceof Window) {
		return (Window) getComponent();
	  }
	  Window window = SwingUtilities.getWindowAncestor(getComponent());
	  if (window != null) {
		return window;
	  }
	}
	return null;
  }

  public void requestWindowFocus() {
	Window window = getWindowAncestor();
	if (window != null) {
	  window.requestFocus();
	}
  }

  public boolean isWindowFocused() {
	Window window = getWindowAncestor();
	if (window != null) {
	  return window.isFocused();
	}
	return false;
  }

  public void requestFocus() {
	if (isComponentInstancied()) {
	  getComponent().requestFocus();
	}
  }

  public boolean isFocused() {
	if (isComponentInstancied()) {
	  return getComponent().isFocusOwner();
	}
	return false;
  }

  /**
   * Set the controller's identifier
   * 
   * @param id
   *          the controller's identifier
   */
  private void setId(int id) {
	this.id = id;
  }

  /**
   * Get the identifier.
   * 
   * @return the identifier
   */
  public final int getId() {
	return id;
  }

  private void setGroup(Group group) {
	this.group = group;
  }

  public final Group getGroup() {
	return group;
  }

  public final int getGroupId() {
	return getGroup().getId();
  }

  /**
   * Indicates whether a controller is defined as permanent.
   * <P>
   * The state of permanent indicates that the controller can't be removed from
   * the memory, even if it used the removal methods.
   * 
   * @return boolean
   * @see #setPermanent(boolean)
   */
  public final boolean isPermanent() {
	return permanent;
  }

  /**
   * Defines whether the controller is permanent.
   * <P>
   * The state of permanent indicates that the controller can't be removed from
   * the memory, even if it used the removal methods.
   * 
   * @param permanent
   * @see #isPermanent()
   */
  protected final void setPermanent(boolean permanent) {
	this.permanent = permanent;
  }

  /**
   * Get the controlled component.
   * 
   * @return V the component
   */
  protected abstract V getComponent();

  /**
   * Indicates whether the controller is instantiated.
   * 
   * @return boolean
   */
  public abstract boolean isComponentInstancied();

  @Override
  public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	return result;
  }

  @Override
  public boolean equals(Object obj) {
	if (this == obj)
	  return true;
	if (obj == null)
	  return false;
	if (getClass() != obj.getClass())
	  return false;
	final Controller<?> other = (Controller<?>) obj;
	if (id != other.id)
	  return false;
	return true;
  }

  /**
   * Removes the controller from the memory.
   * <P>
   * All task executions of the group will be canceled.
   * 
   * @see DisposeAction
   * @see #free(int)
   * @see #freeAll()
   * @see #freeAll(Class)
   */
  public void free() {
	free(getId(), true);
  }

  /**
   * Removes the controller from the memory.
   * 
   * @param cancelTaskExecution
   * 
   * @see DisposeAction
   * @see #free(int)
   * @see #freeAll()
   * @see #freeAll(Class)
   */
  public void free(boolean cancelTaskExecution) {
	free(getId(), cancelTaskExecution);
  }

  public void freeGroup() {
	freeGroup(getGroupId());
  }

  /**
   * Removes the controller from the memory after the current task execution.
   * <P>
   * All other tasks will be canceled.
   * 
   * @see DisposeAction
   * @see #free(int)
   * @see #freeAll()
   * @see #freeAll(Class)
   */
  public final void freeAfterCurrentExecution() {
	getControllerTaskListener().freeAfterCurrentExecution(this);
  }

  /**
   * Removes the controller from the memory after the all task executions.
   * 
   * @see DisposeAction
   * @see #free(int)
   * @see #freeAll()
   * @see #freeAll(Class)
   */
  public final void freeAfterExecutions() {
	getControllerTaskListener().freeAfterExecutions(this);
  }

  /**
   * Shows a question message modal.
   * 
   * @param title
   *          title for the modal
   * @param question
   *          the question
   * @param yesAction
   *          action for the confirmation button
   * @param noAction
   *          action for the cancel button
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showQuestionModal(String title, String question,
								ModalAction yesAction, ModalAction noAction) {
	addModal(modalPanelFactory.buildQuestionPanel(this, title, question,
		yesAction, noAction));
  }

  /**
   * Shows a question message modal.
   * <P>
   * The cancel button just close the modal.
   * 
   * @param title
   *          title for the modal
   * @param question
   *          the question
   * @param yesAction
   *          action for the confirmation button
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showQuestionModal(String title, String question,
								ModalAction yesAction) {
	addModal(modalPanelFactory.buildQuestionPanel(this, title, question,
		yesAction, null));
  }

  /**
   * Shows a confirmation message modal.
   * 
   * @param title
   *          title for the modal
   * @param question
   *          the question
   * @param confirmAction
   *          action for the confirmation button
   * @param cancelAction
   *          action for the cancel button
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showConfirmationModal(String title, String question,
									ModalAction confirmAction,
									ModalAction cancelAction) {
	addModal(modalPanelFactory.buildConfirmationPanel(this, title, question,
		confirmAction, cancelAction));
  }

  /**
   * Shows a confirmation message modal.
   * <P>
   * The cancel button just close the modal.
   * 
   * @param title
   *          title for the modal
   * @param question
   *          the question
   * @param confirmAction
   *          action for the confirmation button
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showConfirmationModal(String title, String question,
									ModalAction confirmAction) {
	addModal(modalPanelFactory.buildConfirmationPanel(this, title, question,
		confirmAction, null));
  }

  /**
   * Shows a information message modal.
   * 
   * @param title
   *          title for the modal
   * @param info
   *          the information
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showInformationModal(String title, String info) {
	addModal(modalPanelFactory.buildInformationPanel(this, title, info));
  }

  /**
   * Shows a warning message modal.
   * 
   * @param title
   *          title for the modal
   * @param warning
   *          the warning
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showWarningModal(String title, String warning) {
	addModal(modalPanelFactory.buildWarningPanel(this, title, warning));
  }

  /**
   * Shows a error message modal.
   * 
   * @param title
   *          title for the modal
   * @param error
   *          the error
   * @see #setModalPanelFactory(ModalPanelFactory)
   */
  public void showErrorModal(String title, String error) {
	addModal(modalPanelFactory.buildErrorPanel(this, title, error));
  }

  /**
   * Shows a modal
   * 
   * @param component
   *          {@link Component}
   */
  public void addModal(Component component) {
	addModal(component, null);
  }

  public void addModal(Component component, ModalListener listener) {
	addModal(component, listener, null);
  }

  public void addModal(Component component, ModalListener listener,
					   Integer modalDepth) {
	RootPaneContainer rootPane = getRootPaneContainer();
	if (rootPane == null) {
	  return;
	}

	if (rootPane instanceof JFrame) {
	  Modal.addModal((JFrame) rootPane, component, listener, modalDepth);
	} else if (rootPane instanceof JDialog) {
	  Modal.addModal((JDialog) rootPane, component, listener, modalDepth);
	} else if (rootPane instanceof JInternalFrame) {
	  Modal
		  .addModal((JInternalFrame) rootPane, component, listener, modalDepth);
	} else {
	  return;
	}

	List<RootPaneContainer> rootPanes = getRootPaneContainers(getGroupId());

	if (getModalGroupName() != null) {
	  String groupName = getModalGroupName().toLowerCase();
	  for (Controller<?> controller : controllers) {
		if (controller.getModalGroupName() != null) {
		  String groupNameTemp = controller.getModalGroupName().toLowerCase();
		  if (groupName.equals(groupNameTemp)) {
			RootPaneContainer rootPaneTemp = controller.getRootPaneContainer();
			if (rootPaneTemp != null
				&& rootPanes.contains(rootPaneTemp) == false) {
			  rootPanes.add(rootPaneTemp);
			}
		  }
		}
	  }
	}

	rootPanes.remove(rootPane);
	List<Component> list = new ArrayList<Component>();
	for (RootPaneContainer otherRootPane : rootPanes) {
	  JPanel panel = new JPanel();
	  panel.setOpaque(false);
	  list.add(panel);
	  if (otherRootPane instanceof JFrame) {
		Modal.addModal((JFrame) otherRootPane, panel, null, modalDepth);
	  } else if (otherRootPane instanceof JDialog) {
		Modal.addModal((JDialog) otherRootPane, panel, null, modalDepth);
	  }
	}

	if (rootPane instanceof JFrame) {
	  Modal.addListener((JFrame) rootPane, component,
		  new ModalAllFramesListener(list));
	} else if (rootPane instanceof JDialog) {
	  Modal.addListener((JDialog) rootPane, component,
		  new ModalAllFramesListener(list));
	}

  }

  /**
   * Get the current modal component
   * 
   * @return Component
   */
  public Component getCurrentModal() {
	RootPaneContainer rootPane = getRootPaneContainer();
	if (rootPane != null) {
	  if (rootPane instanceof JFrame) {
		return Modal.getCurrentModal((JFrame) rootPane);
	  } else if (rootPane instanceof JDialog) {
		return Modal.getCurrentModal((JDialog) rootPane);
	  }
	}
	return null;
  }

  /**
   * Close the current modal.
   */
  public void closeCurrentModal() {
	RootPaneContainer rootPane = getRootPaneContainer();
	if (rootPane != null) {
	  if (rootPane instanceof JFrame) {
		Modal.closeCurrentModal((JFrame) rootPane);
	  } else if (rootPane instanceof JDialog) {
		Modal.closeCurrentModal((JDialog) rootPane);
	  }
	}
  }

  /**
   * Close a modal
   * 
   * @param component
   *          the component's modal
   */
  public void closeModal(Component component) {
	Modal.closeModal(component);
  }

  /**
   * Close all modals.
   */
  public void closeAllModals() {
	RootPaneContainer rootPane = getRootPaneContainer();
	if (rootPane != null) {
	  if (rootPane instanceof JFrame) {
		Modal.closeAllModals((JFrame) rootPane);
	  } else if (rootPane instanceof JDialog) {
		Modal.closeAllModals((JDialog) rootPane);
	  }
	}
  }

  /**
   * Indicates whether exists a modal.
   * 
   * @return boolean
   */
  protected boolean hasModal() {
	RootPaneContainer rootPane = getRootPaneContainer();
	if (rootPane == null) {
	  return false;
	}

	if (rootPane instanceof JFrame) {
	  return Modal.hasModal((JFrame) rootPane);
	} else if (rootPane instanceof JDialog) {
	  return Modal.hasModal((JDialog) rootPane);
	}
	return false;
  }

  /**
   * Get the RootPaneContainer of the controlled component.
   * 
   * @return the RootPaneContainer or <CODE>NULL</CODE> if its not exist.
   */
  private RootPaneContainer getRootPaneContainer() {
	return getRootPaneContainer(this);
  }

  /**
   * Get the RootPaneContainer of the specific controller's component.
   * 
   * @param controller
   * @return the RootPaneContainer or <CODE>NULL</CODE> if its not exist.
   */
  private static RootPaneContainer getRootPaneContainer(Controller<?> controller) {
	Component comp = controller.getComponent();
	if (comp != null) {
	  if (comp instanceof RootPaneContainer) {
		return (RootPaneContainer) comp;
	  }

	  for (Container p = comp.getParent(); p != null; p = p.getParent()) {
		if (p instanceof RootPaneContainer) {
		  return (RootPaneContainer) p;
		}
	  }
	}
	return null;
  }

  private static List<RootPaneContainer> getRootPaneContainers(int groupId) {
	List<RootPaneContainer> list = new ArrayList<RootPaneContainer>();
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId) {
		RootPaneContainer rootPane = controller.getRootPaneContainer();
		if (rootPane != null && list.contains(rootPane) == false) {
		  list.add(rootPane);
		}
	  }
	}
	return list;
  }

  public String getModalGroupName() {
	return modalGroupName;
  }

  public void setModalGroupName(String modalGroupName) {
	this.modalGroupName = modalGroupName;
  }

  /**
   * Show all the instantiated controllers in the pool.
   */
  public static void printAllControllers() {
	for (Controller<?> controller : controllers) {
	  System.out.println("Id: " + controller.getId() + " GroupId: "
		  + controller.getGroupId() + " Class: "
		  + controller.getClass().getName());
	}
  }

  /**
   * Get the parent controller's identifier.
   * 
   * @return Integer the identifier or <CODE>NULL</CODE> if its not exist.
   */
  public final Integer getParentId() {
	return parentControllerId;
  }

  /**
   * Get the parent controller
   * 
   * @return the parent Controller or <CODE>NULL</CODE> if its not exist.
   */
  public final Controller<?> getParent() {
	if (parentControllerId != null) {
	  return get(parentControllerId);
	}
	return null;
  }

  public boolean isRoot() {
	if (parentControllerId != null) {
	  return true;
	}
	return false;
  }

  /**
   * Get the root controller in the hierarchy group.
   * 
   * @return the root controller in the hierarchy or the self controller if not
   *         exists a owner controller.
   */
  public final Controller<?> getRoot() {
	Controller<?> superController = this;
	while (superController.getParentId() != null) {
	  superController = localGet(superController.getParentId());
	}
	return superController;
  }

  public static Controller<?> getRoot(int groupId) {
	for (Controller<?> controller : controllers) {
	  if (controller.getGroupId() == groupId) {
		return controller.getRoot();
	  }
	}
	return null;
  }

  /**
   * Executes a task
   * 
   * @param message
   *          message for the task
   * @param task
   *          the task
   * @see org.japura.task.TaskManagerListener
   */
  protected void execute(String message, Task<?> task) {
	TaskManager.execute(getGroupId(), message, task);
  }

  protected List<MessageFilter> getMessageFilters() {
	return Collections.unmodifiableList(filters);
  }

  protected void removeMessageFilters() {
	filters.clear();
  }

  protected void registerSubscriber(Class<? extends Message> clss,
									Subscriber subscriber) {
	if (clss != null && subscriber != null) {
	  synchronized (subscribers) {
		List<Subscriber> list = subscribers.get(clss);
		if (list == null) {
		  list = new ArrayList<Subscriber>();
		  subscribers.put(clss, list);
		}
		if (list.contains(subscriber) == false) {
		  list.add(subscriber);
		}
	  }
	}
  }

  protected void removeSubscriber(Class<? extends Message> clss,
								  Subscriber subscriber) {
	if (clss != null && subscriber != null) {
	  synchronized (subscribers) {
		List<Subscriber> list = subscribers.get(clss);
		if (list != null) {
		  list.remove(subscriber);
		  if (list.size() == 0) {
			subscribers.remove(clss);
		  }
		}
	  }
	}
  }

  protected void removeSubscriber(Subscriber subscriber) {
	if (subscriber != null && subscribers.size() > 0) {
	  synchronized (subscribers) {
		List<Class<? extends Message>> keys =
			new ArrayList<Class<? extends Message>>();
		for (Class<? extends Message> clss : subscribers.keySet()) {
		  keys.add(clss);
		}

		for (Class<? extends Message> clss : keys) {
		  List<Subscriber> list = subscribers.get(clss);
		  if (list != null) {
			list.remove(subscriber);
			if (list.size() == 0) {
			  subscribers.remove(clss);
			}
		  }
		}
	  }
	}
  }

  protected void removeSubscribers(Class<? extends Message> clss) {
	if (clss != null) {
	  synchronized (subscribers) {
		subscribers.remove(clss);
	  }
	}
  }

  protected List<Subscriber> getSubscribers(Class<? extends Message> clss) {
	List<Subscriber> list = null;
	if (clss != null) {
	  list = subscribers.get(clss);
	}
	if (list == null) {
	  return new ArrayList<Subscriber>();
	}
	return Collections.unmodifiableList(list);
  }

  protected int getSubscribersCount(Class<? extends Message> clss) {
	List<Subscriber> list = null;
	if (clss != null) {
	  list = subscribers.get(clss);
	}
	if (list != null) {
	  return list.size();
	}
	return 0;
  }

  protected void removeMessageFilter(MessageFilter filter) {
	if (filter != null) {
	  filters.remove(filter);
	}
  }

  protected boolean containsMessageFilter(MessageFilter filter) {
	if (filter != null) {
	  return filters.contains(filter);
	}
	return false;
  }

  protected void addMessageFilter(MessageFilter filter) {
	if (filter != null && filters.contains(filter) == false) {
	  filters.add(filter);
	}
  }

  private boolean accepts(Message message) {
	for (MessageFilter filter : filters) {
	  if (filter.accepts(message) == false) {
		return false;
	  }
	}
	return true;
  }

  private static void publishToAll(Message message) {
	for (Controller<?> controller : getAll()) {
	  if (message.isConsumed()) {
		break;
	  }
	  if (message.getPublisher() != null && message.isIgnorePublisher()
		  && message.getPublisher().getId() == controller.getId()) {
		continue;
	  }
	  if (controller.accepts(message) && message.acceptsController(controller)) {
		controller.subscribe(message);
	  }
	}
  }

  protected final void publish(Message message) {
	message.setPublisher(this);
	publishToAll(message);
  }

  protected void subscribe(Message message) {
	if (message == null) {
	  return;
	}
	synchronized (subscribers) {
	  for (Entry<Class<? extends Message>, List<Subscriber>> entry : subscribers
		  .entrySet()) {
		if (message.isConsumed() || message.isControllerConsumed()) {
		  break;
		}
		Class<? extends Message> clss = entry.getKey();
		if (clss.isAssignableFrom(message.getClass())) {
		  List<Subscriber> list = entry.getValue();
		  if (list != null) {
			for (Subscriber subscriber : list) {
			  if (message.acceptsSubscriber(subscriber)) {
				subscriber.performMessage(message);
			  }
			}
		  }
		}
	  }
	}
  }

  private static class ModalAllFramesListener implements ModalListener{

	private List<Component> components;

	public ModalAllFramesListener(List<Component> components) {
	  this.components = components;
	}

	@Override
	public void modalClosed(ModalEvent event) {
	  for (Component component : components)
		Modal.closeModal(component);
	}

  }

  private static class ControllerTaskListener implements TaskManagerListener{

	private List<Integer> afterExecutions;
	private HashMap<Integer, Integer> afterCurrentExecution;

	public ControllerTaskListener() {
	  afterExecutions = new ArrayList<Integer>();
	  afterCurrentExecution = new HashMap<Integer, Integer>();
	}

	public synchronized void freeAfterExecutions(Controller<?> controller) {
	  Integer id = controller.getId();
	  if (afterExecutions.contains(id) == false) {
		afterExecutions.add(id);
	  }
	}

	public synchronized void freeAfterCurrentExecution(Controller<?> controller) {
	  Integer id = controller.getId();
	  Integer groupId = controller.getGroupId();
	  if (afterCurrentExecution.containsKey(groupId) == false) {
		afterCurrentExecution.put(groupId, id);
	  }
	}

	@Override
	public synchronized void executionsFinished(Integer taskGroupId) {
	  if (afterExecutions.contains(taskGroupId)) {
		afterExecutions.remove(taskGroupId);
		afterCurrentExecution.remove(taskGroupId);
		Controller.free(taskGroupId);
	  }
	}

	@Override
	public synchronized void taskExecuted(Integer taskGroupId, Task<?> task) {
	  if (afterCurrentExecution.containsKey(taskGroupId)) {
		int id = afterCurrentExecution.get(taskGroupId);
		afterExecutions.remove(taskGroupId);
		afterCurrentExecution.remove(taskGroupId);
		Controller.free(id);
	  }
	}

	@Override
	public void taskWillExecute(Integer taskGroupId, Task<?> task,
								String taskMessage) {

	}

  }

}
