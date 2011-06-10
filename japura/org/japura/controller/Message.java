package org.japura.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <P>
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
 */
public class Message{

  private List<SubscriberFilter> filters;
  private int type;
  private Controller<?> publisher;
  private boolean consumed;
  private boolean controllerConsumed;
  private boolean ignorePublisher;

  public Message() {
	this(-1);
  }

  public Message(int type) {
	this.type = type;
	filters = new ArrayList<SubscriberFilter>();
  }

  public List<SubscriberFilter> getSubscriberFilters() {
	return Collections.unmodifiableList(filters);
  }

  public void removeSubscriberFilters() {
	filters.clear();
  }

  public void removeSubscriberFilter(SubscriberFilter filter) {
	if (filter != null) {
	  filters.remove(filter);
	}
  }

  public boolean containsSubscriberFilter(SubscriberFilter filter) {
	if (filter != null) {
	  return filters.contains(filter);
	}
	return false;
  }

  boolean acceptsController(Controller<?> controller) {
	for (SubscriberFilter filter : filters) {
	  if (filter.accepts(controller) == false) {
		return false;
	  }
	}
	return true;
  }

  boolean acceptsSubscriber(Subscriber subscriber) {
	for (SubscriberFilter filter : filters) {
	  if (filter.accepts(subscriber) == false) {
		return false;
	  }
	}
	return true;
  }

  public void addSubscriberFilter(SubscriberFilter filter) {
	if (filter != null && filters.contains(filter) == false) {
	  filters.add(filter);
	}
  }

  void setPublisher(Controller<?> publisher) {
	this.publisher = publisher;
  }

  public int getType() {
	return type;
  }

  public void setType(int type) {
	this.type = type;
  }

  public Controller<?> getPublisher() {
	return publisher;
  }

  public boolean isConsumed() {
	return consumed;
  }

  public void consume() {
	this.consumed = true;
  }

  public boolean isControllerConsumed() {
	return controllerConsumed;
  }

  public void consumeController() {
	this.controllerConsumed = true;
  }

  public void setIgnorePublisher(boolean ignorePublisher) {
	this.ignorePublisher = ignorePublisher;
  }

  public boolean isIgnorePublisher() {
	return ignorePublisher;
  }

  public boolean isPublisherAssignableFrom(Class<?> cls) {
	if (getPublisher() != null) {
	  return cls.isAssignableFrom(getPublisher().getClass());
	}
	return false;
  }
}
