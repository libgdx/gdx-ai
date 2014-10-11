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

/** A {@code Decorator} is a wrapper that provides custom behavior for its child. The child can be of any kind (branch task, leaf
 * task, or another decorator).
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public abstract class Decorator<E> extends Task<E> {

	/** The task metadata specifying static information used by parsers and tools. */
	public static final Metadata METADATA = new Metadata(1);

	protected Task<E> child;

	/** Creates a decorator with no child task. */
	public Decorator () {
	}

	/** Creates a decorator that wraps a tree task
	 * 
	 * @param child the task that will be wrapped */
	public Decorator (Task<E> child) {
		this.child = child;
	}

	@Override
	public void addChild (Task<E> child) {
		this.child = child;
	}

	@Override
	public int getChildCount () {
		return child == null ? 0 : 1;
	}

	@Override
	public Task<E> getChild (int i) {
		return child;
	}

	@Override
	public void run (E object) {
		this.object = object;
		child.run(object);
	}

	@Override
	public void end (E object) {
		child.end(object);
	}

	@Override
	public void start (E object) {
		child.setControl(this);
		child.start(object);
	}

	@Override
	public void childRunning (Task<E> runningTask, Task<E> reporter) {
		control.childRunning(runningTask, this);
	}

	@Override
	public void childFail (Task<E> runningTask) {
		control.childFail(this);
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		control.childSuccess(this);
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		Decorator<E> decorator = (Decorator<E>)task;
		decorator.child = this.child.cloneTask();

		return task;
	}

}
