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

/** A {@code Sequence} is a branch task that runs every children until one of them fails. If a child task succeeds, the selector will
 * start and run the next child task.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation */
public class Sequence<E> extends BranchTask<E> {

	public Sequence () {
		super(new Array<Task<E>>());
	}

	public Sequence (Array<Task<E>> tasks) {
		super(tasks);
	}

	public Sequence (Task<E>... tasks) {
		super(new Array<Task<E>>(tasks));
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		super.childSuccess(runningTask);
		if (++actualTask < children.size) {
			run(this.object);
		} else {
			success();
		}
	}

	@Override
	public void childFail (Task<E> runningTask) {
		fail();
	}

}
