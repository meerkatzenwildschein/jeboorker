package org.japura.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * Pool of task.
 * <P>
 * Execute a task for a specific group.
 * <P>
 * The tasks for the same group, are queued and executed in order.
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
 * @see Task
 */
public class TaskManager{

  private static List<TaskManagerListener> listeners =
	  new ArrayList<TaskManagerListener>();

  private static HashMap<Integer, WorkerList> pool =
	  new HashMap<Integer, WorkerList>();

  public static List<TaskManagerListener> getListeners() {
	synchronized (listeners) {
	  return new ArrayList<TaskManagerListener>(listeners);
	}
  }

  public static void addListener(TaskManagerListener listener) {
	synchronized (listeners) {
	  if (listeners.contains(listener) == false) {
		listeners.add(listener);
	  }
	}
  }

  public static void removeListener(TaskManagerListener listener) {
	synchronized (listeners) {
	  listeners.remove(listener);
	}
  }

  private static void fireTaskExecuted(Integer taskGroupId, Task<?> task) {
	synchronized (listeners) {
	  for (int i = listeners.size() - 1; i >= 0; i--) {
		TaskManagerListener listener = listeners.get(i);
		listener.taskExecuted(taskGroupId, task);
	  }
	}
  }

  private static void fireExecutionsFinished(Integer taskGroupId) {
	synchronized (listeners) {
	  for (int i = listeners.size() - 1; i >= 0; i--) {
		TaskManagerListener listener = listeners.get(i);
		listener.executionsFinished(taskGroupId);
	  }
	}
  }

  private static void fireTaskWillExecute(Integer taskGroupId, Task<?> task,
										  String taskMessage) {
	synchronized (listeners) {
	  for (int i = listeners.size() - 1; i >= 0; i--) {
		TaskManagerListener listener = listeners.get(i);
		listener.taskWillExecute(taskGroupId, task, taskMessage);
	  }
	}
  }

  /**
   * Execute a task.
   * <P>
   * The task is queued if another is already running.
   * <P>
   * The execution returns immediately.
   * 
   * @param taskGroupId
   *          {@link Integer}
   * @param task
   *          {@link Task}
   */
  public static void execute(Integer taskGroupId, Task<?> task) {
	execute(taskGroupId, "", task);
  }

  /**
   * Execute a task.
   * <P>
   * The task is queued if another is already running.
   * <P>
   * The execution returns immediately.
   * 
   * @param taskGroupId
   *          {@link Integer}
   * @param message
   *          the message
   * @param task
   *          {@link Task}
   */
  public static void execute(Integer taskGroupId, String message, Task<?> task) {
	WorkerList workerList = pool.get(taskGroupId);
	if (workerList == null) {
	  workerList = new WorkerList(taskGroupId);
	  pool.put(taskGroupId, workerList);
	}
	workerList.add(new Worker(taskGroupId, message, task));
  }

  static void executeNext(Integer taskGroupId) {
	WorkerList workerList = pool.get(taskGroupId);
	if (workerList != null) {
	  if (workerList.executeNext() == false) {
		pool.remove(taskGroupId);
		workerList.finishExecutions();
	  }
	}
  }

  /**
   * Cancels the running task and all other queue.
   * 
   * @param taskGroupId
   *          {@link Integer}
   */
  public static void cancel(Integer taskGroupId) {
	WorkerList workerList = pool.get(taskGroupId);
	if (workerList != null) {
	  workerList.cancel();
	}
  }

  /**
   * Thread para executar a tarefa.
   * 
   */
  private static class Worker extends SwingWorker<Void, Void>{
	private Task<?> task;
	private Integer taskGroupId;
	private String message;

	/**
	 * Construtor
	 * 
	 * @param taskGroupId
	 *          {@link Integer}
	 * @param message
	 *          mensagem
	 * @param task
	 *          {@link Task}
	 */
	protected Worker(Integer taskGroupId, String message, Task<?> task) {
	  this.task = task;
	  this.taskGroupId = taskGroupId;
	  this.message = message;
	}

	/**
	 * Obtém a mensagem
	 * 
	 * @return {@link String}
	 */
	public String getMessage() {
	  return message;
	}

	@Override
	protected Void doInBackground() throws Exception {
	  try {
		task.doInBackground();
	  } catch (Exception e) {
		task.setException(e);
		throw e;
	  }
	  return null;
	}

	@Override
	protected void done() {
	  if (task.getException() != null) {
		exception();
		return;
	  }

	  try {
		if (isCancelled()) {
		  task.canceled();
		} else
		  task.done();
	  } catch (Exception e) {
		task.setException(e);
		exception();
		return;
	  }
	  fireTaskExecuted(taskGroupId, task);
	  TaskManager.executeNext(taskGroupId);
	}

	/**
	 * Ação quando ocorre uma exceção.
	 * 
	 */
	private void exception() {
	  TaskManager.cancel(taskGroupId);
	  task.handleException(task.getException(), taskGroupId);
	  TaskManager.executeNext(taskGroupId);
	}
  }

  /**
   * Fila para tarefas
   * 
   */
  private static class WorkerList{
	private Integer taskGroupId;
	private ArrayList<Worker> workers;
	private Worker currentWorker;

	/**
	 * Constructor
	 * 
	 * @param taskGroupId
	 */
	private WorkerList(Integer taskGroupId) {
	  this.taskGroupId = taskGroupId;
	  workers = new ArrayList<Worker>();
	}

	/**
	 * Remove todos as tarefas da fila de execução.
	 * <P>
	 * A tarefa em execução será cancelada.
	 */
	private synchronized void cancel() {
	  workers.clear();
	  if (currentWorker != null && currentWorker.isDone() == false
		  && currentWorker.isCancelled() == false) {
		currentWorker.cancel(true);
	  }
	}

	private synchronized void finishExecutions() {
	  fireExecutionsFinished(taskGroupId);
	}

	/**
	 * Adiciona uma tarefa na fila de execução.
	 * <P>
	 * Casos:
	 * <UL>
	 * <LI>nenhuma tarefa sendo executada: tarefa é executada imediatamente</LI>
	 * <LI>outra tarefa sendo executada: tarefa entra na fila de execução</LI>
	 * </UL>
	 * 
	 * @param worker
	 *          {@link Worker}
	 */
	private synchronized void add(Worker worker) {
	  if (currentWorker == null) {
		currentWorker = worker;
		executeCurrentWorker();
	  } else {
		workers.add(worker);
	  }
	}

	/**
	 * Executa a tarefa setada como corrente
	 * 
	 */
	private void executeCurrentWorker() {
	  fireTaskWillExecute(taskGroupId, currentWorker.task,
		  currentWorker.getMessage());
	  currentWorker.execute();
	}

	/**
	 * Executa a próxima tarefa caso exista
	 * 
	 * @return true se existir mais tarefa a ser executada
	 */
	private synchronized boolean executeNext() {
	  boolean hasNext = false;
	  if (workers.size() > 0) {
		hasNext = true;
		currentWorker = workers.remove(0);
		executeCurrentWorker();
	  } else {
		currentWorker = null;
	  }
	  return hasNext;
	}
  }

}