package org.japura.controller;

import java.util.List;

/**
 * Copyright (C) 2010-2011 Carlos Eduardo Leite de Andrade
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
public class Group{

  private final int id;

  public Group(int id) {
	this.id = id;
  }

  public int getId() {
	return id;
  }

  /**
   * Get the all controllers of a specific class from this group.
   * 
   * @param cls
   *          the controller's class
   * @return List with the controllers.
   * 
   */
  public <E> List<E> getAll(Class<E> cls) {
	return Controller.getAllFromGroup(getId(), cls);
  }

  /**
   * Get the all controllers from this group.
   * 
   * @return List with the controllers.
   * 
   */
  public List<Controller<?>> getAll() {
	return Controller.getAllFromGroup(getId());
  }

  /**
   * Get the controller of a specific class from this group.
   * 
   * @param cls
   *          the controller's class
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   */
  public <E> E get(Class<E> cls) {
	return Controller.getFromGroup(getId(), cls);
  }

  /**
   * Get the controller of a specific class and identifier from this group.
   * 
   * @param id
   *          the controller's identifier
   * @param cls
   *          the controller's class
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   */
  public <E> E get(int id, Class<E> cls) {
	return Controller.getFromGroup(getId(), id, cls);
  }

  /**
   * Get the controller of a specific identifier from this group.
   * 
   * @param id
   *          the controller's identifier
   * 
   * @return the controller or <code>NULL</code> if its not exist.
   * 
   */
  public Controller<?> get(int id) {
	return Controller.getFromGroup(getId(), id);
  }

  public void freeAll(Class<?> cls) {
	Controller.freeAllFromGroup(getId(), cls);
  }

  public void free() {
	Controller.freeGroup(getId());
  }

  /**
   * Indicates whether the controller is instantiated in this group.
   * 
   * @param cls
   *          controller's class
   * @return boolean
   */
  public boolean isInstancied(Class<?> cls) {
	return Controller.isInstanciedInGroup(getId(), cls);
  }

}
