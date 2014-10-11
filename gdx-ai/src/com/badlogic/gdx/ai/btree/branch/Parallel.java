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

package com.badlogic.gdx.ai.btree.branch;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;

/** A {@code Parallel} is a special branch task that starts or resumes all children every single time, parallel task will succeed if
 * all the children succeed, fail if one of the children fail. The typical use case: make the game entity react on event while
 * sleeping or wandering.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation */
public class Parallel<E> extends BranchTask<E> {

	private final Array<Task<E>> runningTasks;
	private boolean success;
	private int notDone;

	public Parallel () {
		this(new Array<Task<E>>());
	}

	public Parallel (Task<E>... tasks) {
		this(new Array<Task<E>>(tasks));
	}

	public Parallel (Array<Task<E>> tasks) {
		super(tasks);
		this.success = true;
		// Create an unordered array to make item removal faster
		this.runningTasks = new Array<Task<E>>(false, tasks.size > 0 ? tasks.size : 8);
	}

	@Override
	public void start (E object) {
		this.object = object;
		runningTasks.clear();
		success = true;
	}

	@Override
	public void childRunning (Task<E> task, Task<E> reporter) {
		if (!runningTasks.contains(reporter, true)) {
			runningTasks.add(reporter);
		}
		notDone--;
		control.childRunning(this, this);
	}

	@Override
	public void run (E object) {
		notDone = children.size;
		this.object = object;
		for (Task<E> child : children) {
			if (runningTasks.contains(child, true)) {
				child.run(object);
			} else {
				child.setControl(this);
				child.start(object);
				child.run(object);
			}
		}
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		runningTasks.removeValue(runningTask, true);
		success = success && true;
		notDone--;
		if (runningTasks.size == 0 && notDone == 0) {
			if (success) {
				success();
			} else {
				fail();
			}
		}
	}

	@Override
	public void childFail (Task<E> runningTask) {
		runningTasks.removeValue(runningTask, true);
		success = false;
		notDone--;
		if (runningTasks.size == 0 && notDone == 0) {
			if (success) {
				success();
			} else {
				fail();
			}
		}
	}

}
