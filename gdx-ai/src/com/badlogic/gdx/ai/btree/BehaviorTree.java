/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.btree;

import com.badlogic.gdx.utils.Array;

/** The behavior tree itself.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public class BehaviorTree<E> extends Task<E> {

	private Task<E> rootTask;
	private E object;

	/** Creates a {@code BehaviorTree} with no root task and no blackboard object. Both the root task and the blackboard object must
	 * be set before running this behavior tree, see {@link #addChild(Task) addChild()} and {@link #setObject(Object) setObject()}
	 * respectively. */
	public BehaviorTree () {
		this(null, null);
	}

	/** Creates a behavior tree with a root task and no blackboard object. Both the root task and the blackboard object must be set
	 * before running this behavior tree, see {@link #addChild(Task) addChild()} and {@link #setObject(Object) setObject()}
	 * respectively.
	 * 
	 * @param rootTask the root task of this tree. It can be {@code null}. */
	public BehaviorTree (Task<E> rootTask) {
		this(rootTask, null);
	}

	/** Creates a behavior tree with a root task and a blackboard object. Both the root task and the blackboard object must be set
	 * before running this behavior tree, see {@link #addChild(Task) addChild()} and {@link #setObject(Object) setObject()}
	 * respectively.
	 * 
	 * @param rootTask the root task of this tree. It can be {@code null}.
	 * @param object the blackboard. It can be {@code null}. */
	public BehaviorTree (Task<E> rootTask, E object) {
		this.rootTask = rootTask;
		this.object = object;
		this.tree = this;
	}

	/** Returns the blackboard object of this behavior tree. */
	@Override
	public E getObject () {
		return object;
	}

	/** Sets the blackboard object of this behavior tree.
	 * 
	 * @param object the new blackboard */
	public void setObject (E object) {
		this.object = object;
	}

	/** This method will add a child, namely the root, to this behavior tree.
	 * 
	 * @param child the root task to add
	 * @return the index where the root task has been added (always 0).
	 * @throws IllegalStateException if the root task is already set. */
	@Override
	protected int addChildToTask (Task<E> child) {
		if (this.rootTask != null) throw new IllegalStateException("A behavior tree cannot have more than one root task");
		this.rootTask = child;
		return 0;
	}

	@Override
	public int getChildCount () {
		return rootTask == null ? 0 : 1;
	}

	@Override
	public Task<E> getChild (int i) {
		if (i == 0 && rootTask != null) return rootTask;
		throw new IndexOutOfBoundsException("index can't be >= size: " + i + " >= " + getChildCount());
	}

	@Override
	public final void childRunning (Task<E> runningTask, Task<E> reporter) {
	}

	@Override
	public final void childFail (Task<E> runningTask) {
	}

	@Override
	public final void childSuccess (Task<E> runningTask) {
	}

	/** This method should be called when game entity needs to make decisions: call this in game loop or after a fixed time slice if
	 * the game is real-time, or on entity's turn if the game is turn-based */
	public void step () {
		if (rootTask.status != Status.RUNNING) {
			rootTask.setControl(this);
			rootTask.start();
		}
		rootTask.run();
	}

	@Override
	public void run () {
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		BehaviorTree<E> tree = (BehaviorTree<E>)task;
		tree.rootTask = rootTask.cloneTask();

		return task;
	}

	public Array<Listener<E>> listeners;

	public void addListener (Listener<E> listener) {
		if (listeners == null) listeners = new Array<Listener<E>>();
		listeners.add(listener);
	}

	public void removeListener (Listener<E> listener) {
		if (listeners != null) listeners.removeIndex(listeners.indexOf(listener, true));
	}

	public void removeListeners () {
		if (listeners != null) listeners.clear();
	}

	public void notifyStatusUpdated (Task<E> task, Status previousStatus) {
		for (Listener<E> listener : listeners) {
			listener.statusUpdated(task, previousStatus);
		}
	}

	public void notifyChildAdded (Task<E> task, int index) {
		for (Listener<E> listener : listeners) {
			listener.childAdded(task, index);
		}
	}

	/** The listener interface for receiving task events. The class that is interested in processing a task event implements this
	 * interface, and the object created with that class is registered with a behavior tree, using the
	 * {@link BehaviorTree#addListener(Listener)} method. When a task event occurs, the corresponding method is invoked.
	 *
	 * @param <E> type of the blackboard object that tasks use to read or modify game state
	 * 
	 * @author davebaol */
	public interface Listener<E> {

		/** This method is invoked when the task status is set. This does not necessarily mean that the status has changed.
		 * @param task the task whose status has been set
		 * @param previousStatus the task's status before the update */
		public void statusUpdated (Task<E> task, Status previousStatus);

		/** This method is invoked when a child task is added to the children of a parent task.
		 * @param task the parent task of the newly added child
		 * @param index the index where the child has been added */
		public void childAdded (Task<E> task, int index);
	}
}
