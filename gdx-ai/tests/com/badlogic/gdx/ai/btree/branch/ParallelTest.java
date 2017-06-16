/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.Task.Status;
import com.badlogic.gdx.ai.btree.branch.Parallel.Policy;
import com.badlogic.gdx.ai.btree.branch.Parallel.Strategy;
import com.badlogic.gdx.utils.Array;

public class ParallelTest {
	private final BehaviorTree<String> behaviorTree = new BehaviorTree<String>();
	private final TestTask task1 = new TestTask();
	private final TestTask task2 = new TestTask();
	private final Array<Task<String>> tasks = new Array<Task<String>>();

	@Before
	public void setUp() {
		tasks.add(task1);
		tasks.add(task2);
	}

	/**
	 * Resume stategy - all tasks start or run on each step<br>
	 * Sequence policy - all tasks have to succeed for the parallel task to succeed
	 */
	@Test
	public void testResumeStrategySequencePolicy() {
		Parallel<String> parallel = new Parallel<String>(Policy.Sequence, Strategy.Resume, tasks);
		behaviorTree.addChild(parallel);
		behaviorTree.step();

		Assert.assertEquals(1, task1.executions);
		Assert.assertEquals(1, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		task1.status = Status.SUCCEEDED;
		behaviorTree.step();

		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(2, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		behaviorTree.step();

		Assert.assertEquals(3, task1.executions);
		Assert.assertEquals(3, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		task2.status = Status.SUCCEEDED;
		behaviorTree.step();

		Assert.assertEquals(4, task1.executions);
		Assert.assertEquals(4, task2.executions);
		Assert.assertEquals(Status.SUCCEEDED, parallel.getStatus());
	}

	/**
	 * Resume stategy - all tasks start or run on each step<br>
	 * Selector policy - only one task has to succeed for the parallel task to succeed
	 */
	@Test
	public void testResumeStrategySelectorPolicy() {
		Parallel<String> parallel = new Parallel<String>(Policy.Selector, Strategy.Resume, tasks);
		behaviorTree.addChild(parallel);
		behaviorTree.step();

		Assert.assertEquals(1, task1.executions);
		Assert.assertEquals(1, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		behaviorTree.step();

		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(2, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		task1.status = Status.SUCCEEDED;
		behaviorTree.step();

		Assert.assertEquals(3, task1.executions);
		Assert.assertEquals(2, task2.executions);
		Assert.assertEquals(Status.SUCCEEDED, parallel.getStatus());

		behaviorTree.step();

		// Resume strategy - all tasks start/run
		Assert.assertEquals(4, task1.executions);
		Assert.assertEquals(2, task2.executions);
		Assert.assertEquals(Status.SUCCEEDED, parallel.getStatus());
	}

	/**
	 * Join stategy - all tasks run until success/failure then don't run again
	 * until the parallel task has succeeded or failed<br>
	 * Sequence policy - all tasks have to succeed for the parallel task to succeed
	 */
	@Test
	public void testJoinStrategySequencePolicy() {
		Parallel<String> parallel = new Parallel<String>(Policy.Sequence, Strategy.Join, tasks);
		behaviorTree.addChild(parallel);
		behaviorTree.step();

		Assert.assertEquals(1, task1.executions);
		Assert.assertEquals(1, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		task1.status = Status.SUCCEEDED;
		behaviorTree.step();

		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(2, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		behaviorTree.step();

		// Join strategy - task 1 will not execute again until the parallel task
		// succeeds or fails
		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(3, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		task2.status = Status.SUCCEEDED;
		behaviorTree.step();

		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(4, task2.executions);
		Assert.assertEquals(Status.SUCCEEDED, parallel.getStatus());

		task1.status = Status.RUNNING;
		task2.status = Status.RUNNING;

		behaviorTree.step();
		Assert.assertEquals(3, task1.executions);
		Assert.assertEquals(5, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());
	}

	/**
	 * Join stategy - all tasks run until success/failure then don't run again
	 * until the parallel task has succeeded or failed<br>
	 * Selector policy - only one task has to succeed for the parallel task to succeed
	 */
	@Test
	public void testJoinStrategySelectorPolicy() {
		Parallel<String> parallel = new Parallel<String>(Policy.Selector, Strategy.Join, tasks);
		behaviorTree.addChild(parallel);
		behaviorTree.step();

		Assert.assertEquals(1, task1.executions);
		Assert.assertEquals(1, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		task1.status = Status.FAILED;
		behaviorTree.step();

		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(2, task2.executions);
		Assert.assertEquals(Status.RUNNING, parallel.getStatus());

		// Join strategy - task 1 will not execute again until the parallel task
		// succeeds or fails
		task2.status = Status.SUCCEEDED;
		behaviorTree.step();

		Assert.assertEquals(2, task1.executions);
		Assert.assertEquals(3, task2.executions);
		Assert.assertEquals(Status.SUCCEEDED, parallel.getStatus());

		behaviorTree.step();

		Assert.assertEquals(3, task1.executions);
		Assert.assertEquals(4, task2.executions);
		Assert.assertEquals(Status.SUCCEEDED, parallel.getStatus());
	}

	private class TestTask extends LeafTask<String> {
		Task.Status status = Status.RUNNING;
		int executions = 0;

		@Override
		public com.badlogic.gdx.ai.btree.Task.Status execute() {
			executions++;
			return status;
		}

		@Override
		protected Task<String> copyTo(Task<String> task) {
			return task;
		}
	}
}
