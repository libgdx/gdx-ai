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

import com.badlogic.gdx.ai.btree.annotation.TaskConstraint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** The {@code Task} of a behavior tree has one control and a list of children.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
@TaskConstraint
public abstract class Task<E> {

	protected Task<E> control;
	protected Task<E> runningTask;
	protected Array<Task<E>> children;
	protected BehaviorTree<E> tree;

	/** This method will add a child to the list of this task's children
	 * 
	 * @param child the child task which will be added */
	public void addChild (Task<E> child) {
		children.add(child);
	}

	/** Returns the number of children of this task.
	 * 
	 * @return an int giving the number of children of this task */
	public int getChildCount () {
		return children.size;
	}

	/** Returns the child at the given index. */
	public Task<E> getChild (int i) {
		return children.get(i);
	}

	/** Resets this task to make it restart from scratch on next run. */
	public void reset () {
		runningTask = null;
		int n = getChildCount();
		for (int i = 0; i < n; i++) {
			getChild(i).reset();
		}
	}

	/** Returns the blackboard object of the behavior tree this task belongs to.
	 * @throws IllegalStateException if this task has never run */
	public E getObject () {
		if (tree == null) throw new IllegalStateException("This task has never run");
		return tree.getObject();
	}

	/** This method will set a task as this task's control (parent)
	 * 
	 * @param control the parent task */
	public void setControl (Task<E> control) {
		this.control = control;
		this.tree = control.tree;
	}

	/** This method will be called once before this task's first run. */
	public void start () {

	}

	/** This method will be called when this task succeeds or fails. */
	public void end () {

	}

	/** This method contains update logic of this task. */
	public abstract void run ();

	/** This method will be called in {@link #run()} to inform control that this task needs to run again */
	public final void running () {
		control.childRunning(this, this);
	}

	/** This method will be called in {@link #run()} to inform control that this task has finished running with a success result */
	public void success () {
		end();
		control.childSuccess(this);
	}

	/** This method will be called in {@link #run()} to inform control that this task has finished running with a failure result */
	public void fail () {
		end();
		control.childFail(this);
	}

	/** This method will be called when one of the children of this task succeeds
	 * 
	 * @param task the task that succeeded */
	public void childSuccess (Task<E> task) {
		this.runningTask = null;
	}

	/** This method will be called when one of the children of this task fails
	 * 
	 * @param task the task that failed */
	public void childFail (Task<E> task) {
		this.runningTask = null;
	}

	/** This method will be called when one of the ancestors of this task needs to run again
	 * 
	 * @param runningTask the task that needs to run again
	 * @param reporter the task that reports, usually one of this task's children */
	public void childRunning (Task<E> runningTask, Task<E> reporter) {
		this.runningTask = runningTask;
	}

	/** Clones this task to a new one.
	 * @return the cloned task
	 * @throws TaskCloneException if the task cannot be successfully cloned. */
	@SuppressWarnings("unchecked")
	public Task<E> cloneTask () {
		try {
			return copyTo(ClassReflection.newInstance(this.getClass()));
		} catch (ReflectionException e) {
			throw new TaskCloneException(e);
		}
	}

	/** Copies this task to the given task.
	 * @param task the task to be filled
	 * @return the given task for chaining
	 * @throws TaskCloneException if the task cannot be successfully copied. */
	protected abstract Task<E> copyTo (Task<E> task);

}
