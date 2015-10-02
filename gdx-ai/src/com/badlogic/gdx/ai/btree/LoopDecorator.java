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


/** A {@code Loop} is an abstract decorator that provides basic functionalities for concrete looping decorators.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author davebaol */
public abstract class LoopDecorator<E> extends Decorator<E> {

	protected boolean loop;

	/** Creates a loop decorator with no child task. */
	public LoopDecorator () {
	}

	/** Creates a loop decorator that wraps the given task.
	 * 
	 * @param child the task that will be wrapped */
	public LoopDecorator (Task<E> child) {
		super(child);
	}

	/** Whether the {@link #run()} method must keep looping or not.
	 * @return {@code true} if it must keep looping; {@code false} otherwise. */
	public boolean condition () {
		return loop;
	}

	@Override
	public void run () {
		loop = true;
		while (condition()) {
			if (child.status != Status.RUNNING) {
				child.setControl(this);
				child.start();
			}
			child.run();
		}
	}

	@Override
	public void childRunning (Task<E> runningTask, Task<E> reporter) {
		super.childRunning(runningTask, reporter);
		loop = false;
	}

}
