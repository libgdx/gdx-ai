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

/** The behavior tree itself.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public class BehaviorTree<E> extends Task<E> {

	private Task<E> rootTask;

	/** Creates a {@code BehaviorTree} with no root task and no blackboard object. Both the root task and the blackboard object must
	 * be set before running this behavior tree. */
	public BehaviorTree () {
		this(null, null);
	}

	/** Creates a behavior tree with a root task and no blackboard object. The blackboard object must be set before running this
	 * behavior tree.
	 * 
	 * @param rootTask the root task of this tree */
	public BehaviorTree (Task<E> rootTask) {
		this(rootTask, null);
	}

	/** Creates a behavior tree with a root task and a blackboard object
	 * 
	 * @param rootTask the root task of this tree
	 * @param object the blackboard */
	public BehaviorTree (Task<E> rootTask, E object) {
		this.rootTask = rootTask;
		this.object = object;
	}

	/** Sets the blackboard object.
	 * 
	 * @param object the new blackboard */
	public void setObject (E object) {
		this.object = object;
	}

	/** This method should be called when game entity needs to make decisions: call this in game loop or after a fixed time slice if
	 * the game is realtime, or on entity's turn if the game is turn-based */
	public void step () {
		if (runningTask != null) {
			runningTask.run(object);
		} else {
			rootTask.setControl(this);
			rootTask.object = object;
			rootTask.start(object);
			rootTask.run(object);
		}
	}

	@Override
	public int getChildCount () {
		return rootTask == null ? 0 : 1;
	}

	@Override
	public Task<E> getChild (int i) {
		return rootTask;
	}

	@Override
	public void run (E object) {
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		BehaviorTree<E> tree = (BehaviorTree<E>)task;
		tree.rootTask = rootTask.cloneTask();

		return task;
	}

}
