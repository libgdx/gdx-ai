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
import com.badlogic.gdx.ai.btree.annotation.TaskConstraint;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** A branch task defines a behavior tree branch, contains logic of starting or running sub-branches and leaves
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
@TaskConstraint(minChildren=1)
public abstract class BranchTask<E> extends Task<E> {

	@TaskAttribute
	public boolean deterministic = true;

	protected int actualTask;

	/** Create a branch task with a list of children
	 * 
	 * @param tasks list of this task's children, can be empty */
	public BranchTask (Array<Task<E>> tasks) {
		this.children = tasks;
	}

	@Override
	public void childRunning (Task<E> task, Task<E> reporter) {
		runningTask = task;
		control.childRunning(task, this);
	}

	@Override
	public void run () {
		if (runningTask != null) {
			runningTask.run();
		} else {
			if (actualTask < children.size) {
				if (!deterministic) {
					int lastTask = children.size - 1;
					if (actualTask < lastTask) children.swap(actualTask, MathUtils.random(actualTask, lastTask));
				}
				runningTask = children.get(actualTask);
				runningTask.setControl(this);
				runningTask.start();
				run();
			} else {
				end();
			}
		}
	}

	@Override
	public void start () {
		this.actualTask = 0;
		runningTask = null;
	}

    @Override
    public void end(){
        // end all running children
        for (int i = 0; i < children.size; i++) {
            Task<E> child = children.get(i);
            child.end();
        }
    }

	@Override
	public void reset () {
		super.reset();
		this.actualTask = 0;
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		BranchTask<E> branch = (BranchTask<E>)task;
		branch.deterministic = deterministic;
		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				branch.children.add(children.get(i).cloneTask());
			}
		}

		return task;
	}

}
