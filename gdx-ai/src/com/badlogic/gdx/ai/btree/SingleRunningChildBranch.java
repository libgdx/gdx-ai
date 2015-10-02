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

import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** A branch task defines a behavior tree branch, contains logic of starting or running sub-branches and leaves
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public abstract class SingleRunningChildBranch<E> extends BranchTask<E> {

	@TaskAttribute public boolean deterministic = true;

	protected Task<E> runningChild;
	protected int currentChildIndex;

	/** Create a branch task with no children */
	public SingleRunningChildBranch () {
		super();
	}

	/** Create a branch task with a list of children
	 * 
	 * @param tasks list of this task's children, can be empty */
	public SingleRunningChildBranch (Array<Task<E>> tasks) {
		super(tasks);
	}

	@Override
	public void childRunning (Task<E> task, Task<E> reporter) {
		runningChild = task;
		running();
	}

	@Override
	public void childSuccess (Task<E> task) {
		this.runningChild = null;
	}

	@Override
	public void childFail (Task<E> task) {
		this.runningChild = null;
	}

	@Override
	public void run () {
		if (runningChild != null) {
			runningChild.run();
		} else {
			if (currentChildIndex < children.size) {
				if (!deterministic) {
					int last = children.size - 1;
					if (currentChildIndex < last) children.swap(currentChildIndex, MathUtils.random(currentChildIndex, last));
				}
				runningChild = children.get(currentChildIndex);
				runningChild.setControl(this);
				runningChild.start();
				run();
			} else {
				end();
			}
		}
	}

	@Override
	public void start () {
		this.currentChildIndex = 0;
		runningChild = null;
	}

	@Override
	protected void cancelRunningChildren (int fromChildIndex) {
		super.cancelRunningChildren(fromChildIndex);
		runningChild = null;
	}

	@Override
	public void reset () {
		super.reset();
		this.currentChildIndex = 0;
		this.runningChild = null;
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		SingleRunningChildBranch<E> branch = (SingleRunningChildBranch<E>)task;
		branch.deterministic = deterministic;

		return super.copyTo(task);
	}

}
