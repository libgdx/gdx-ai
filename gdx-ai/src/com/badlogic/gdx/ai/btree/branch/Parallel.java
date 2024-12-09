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
import com.badlogic.gdx.ai.btree.Task.Status;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.utils.Array;

/** A {@code Parallel} is a special branch task that runs all children when stepped. 
 * Its actual behavior depends on its {@link orchestrator} and {@link policy}.<br>
 * <br>
 * The execution of the parallel task's children depends on its {@link #orchestrator}:
 * <ul>
 * <li>{@link Orchestrator#Resume}: the parallel task restarts or runs each child every step</li>
 * <li>{@link Orchestrator#Join}: child tasks will run until success or failure but will not re-run until the parallel task has succeeded or failed</li>
 * </ul>
 * 
 * The actual result of the parallel task depends on its {@link #policy}:
 * <ul>
 * <li>{@link Policy#Sequence}: the parallel task fails as soon as one child fails; if all children succeed, then the parallel
 * task succeeds. This is the default policy.</li>
 * <li>{@link Policy#Selector}: the parallel task succeeds as soon as one child succeeds; if all children fail, then the parallel
 * task fails.</li>
 * </ul>
 * 
 * The typical use case: make the game entity react on event while sleeping or wandering.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public class Parallel<E> extends BranchTask<E> {

	/** Optional task attribute specifying the parallel policy (defaults to {@link Policy#Sequence}) */
	@TaskAttribute public Policy policy;
	/** Optional task attribute specifying the execution policy (defaults to {@link Orchestrator#Resume}) */
	@TaskAttribute public Orchestrator orchestrator;

	private boolean noRunningTasks;
	private Boolean lastResult;
	private int currentChildIndex;

	/** Creates a parallel task with sequence policy, resume orchestrator and no children */
	public Parallel () {
		this(new Array<Task<E>>());
	}

	/** Creates a parallel task with sequence policy, resume orchestrator and the given children
	 * @param tasks the children */
	public Parallel (Task<E>... tasks) {
		this(new Array<Task<E>>(tasks));
	}

	/** Creates a parallel task with sequence policy, resume orchestrator and the given children
	 * @param tasks the children */
	public Parallel (Array<Task<E>> tasks) {
		this(Policy.Sequence, tasks);
	}

	/** Creates a parallel task with the given policy, resume orchestrator and no children
	 * @param policy the policy */
	public Parallel (Policy policy) {
		this(policy, new Array<Task<E>>());
	}

	/** Creates a parallel task with the given policy, resume orchestrator and the given children
	 * @param policy the policy
	 * @param tasks the children */
	public Parallel (Policy policy, Task<E>... tasks) {
		this(policy, new Array<Task<E>>(tasks));
	}

	/** Creates a parallel task with the given policy, resume orchestrator and the given children
	 * @param policy the policy
	 * @param tasks the children */
	public Parallel (Policy policy, Array<Task<E>> tasks) {
		this(policy, Orchestrator.Resume, tasks);
	}

	/** Creates a parallel task with the given orchestrator, sequence policy and the given children
	 * @param orchestrator the orchestrator
	 * @param tasks the children */
	public Parallel (Orchestrator orchestrator, Array<Task<E>> tasks) {
		this(Policy.Sequence, orchestrator, tasks);
	}
	
	/** Creates a parallel task with the given orchestrator, sequence policy and the given children
	 * @param orchestrator the orchestrator
	 * @param tasks the children */
	public Parallel (Orchestrator orchestrator, Task<E>... tasks) {
		this(Policy.Sequence, orchestrator, new Array<Task<E>>(tasks));
	}
	
	/** Creates a parallel task with the given orchestrator, policy and children
	 * @param policy the policy
	 * @param orchestrator the orchestrator
	 * @param tasks the children */
	public Parallel (Policy policy, Orchestrator orchestrator, Array<Task<E>> tasks) {
		super(tasks);
		this.policy = policy;
		this.orchestrator = orchestrator;
		noRunningTasks = true;
	}

	@Override
	public void run () {
		orchestrator.execute(this);
	}

	@Override
	public void childRunning (Task<E> task, Task<E> reporter) {
		noRunningTasks = false;
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		lastResult = policy.onChildSuccess(this);
	}

	@Override
	public void childFail (Task<E> runningTask) {
		lastResult = policy.onChildFail(this);
	}

	@Override
	public void resetTask () {
		super.resetTask();
		noRunningTasks = true;
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		Parallel<E> parallel = (Parallel<E>)task;
		parallel.policy = policy; // no need to clone since it is immutable
		parallel.orchestrator = orchestrator; // no need to clone since it is immutable
		return super.copyTo(task);
	}
	
	public void resetAllChildren() {
		for (int i = 0, n = getChildCount(); i < n; i++) {
			Task<E> child = getChild(i);
			child.reset();
		}
	}
	
	/** The enumeration of the child orchestrators supported by the {@link Parallel} task */
	public enum Orchestrator {
		/** The default orchestrator - starts or resumes all children every single step */
		Resume() {
			@Override
			public void execute(Parallel<?> parallel) {
				parallel.noRunningTasks = true;
				parallel.lastResult = null;
				for (parallel.currentChildIndex = 0; parallel.currentChildIndex < parallel.children.size; parallel.currentChildIndex++) {
					Task child = parallel.children.get(parallel.currentChildIndex);
					if (child.getStatus() == Status.RUNNING) {
						child.run();
					} else {
						child.setControl(parallel);
						child.start();
						if (child.checkGuard(parallel))
							child.run();
						else
							child.fail();
					}

					if (parallel.lastResult != null) { // Current child has finished either with success or fail
						parallel.cancelRunningChildren(parallel.noRunningTasks ? parallel.currentChildIndex + 1 : 0);
						if (parallel.lastResult)
							parallel.success();
						else
							parallel.fail();
						return;
					}
				}
				parallel.running();
			}
		},
		/** Children execute until they succeed or fail but will not re-run until the parallel task has succeeded or failed */
		Join() {
			@Override
			public void execute(Parallel<?> parallel) {
				parallel.noRunningTasks = true;
				parallel.lastResult = null;
				for (parallel.currentChildIndex = 0; parallel.currentChildIndex < parallel.children.size; parallel.currentChildIndex++) {
					Task child = parallel.children.get(parallel.currentChildIndex);
					
					switch(child.getStatus()) {
					case RUNNING:
						child.run();
						break;
					case SUCCEEDED:
					case FAILED:
						break;
					default:
						child.setControl(parallel);
						child.start();
						if (child.checkGuard(parallel))
							child.run();
						else
							child.fail();
						break;
					}
					
					if (parallel.lastResult != null) { // Current child has finished either with success or fail
						parallel.cancelRunningChildren(parallel.noRunningTasks ? parallel.currentChildIndex + 1 : 0);
						parallel.resetAllChildren();
						if (parallel.lastResult)
							parallel.success();
						else
							parallel.fail();
						return;
					}
				}
				parallel.running();
			}
		};
		
		/**
		 * Called by parallel task each run
		 * @param parallel The {@link Parallel} task
		 */
		public abstract void execute(Parallel<?> parallel);
	}
	
	@Override
	public void reset() {
		// first call super reset to free child
		super.reset();
		policy = Policy.Sequence;
		orchestrator = Orchestrator.Resume;
		noRunningTasks = true;
		lastResult = null;
		currentChildIndex = 0;
	}

	/** The enumeration of the policies supported by the {@link Parallel} task. */
	public enum Policy {
		/** The sequence policy makes the {@link Parallel} task fail as soon as one child fails; if all children succeed, then the
		 * parallel task succeeds. This is the default policy. */
		Sequence() {
			@Override
			public Boolean onChildSuccess (Parallel<?> parallel) {
				switch(parallel.orchestrator) {
				case Join:
					return parallel.noRunningTasks && parallel.children.get(parallel.children.size - 1).getStatus() == Status.SUCCEEDED ? Boolean.TRUE : null;
				case Resume:
				default:
					return parallel.noRunningTasks && parallel.currentChildIndex == parallel.children.size - 1 ? Boolean.TRUE : null;
				}
			}

			@Override
			public Boolean onChildFail (Parallel<?> parallel) {
				return Boolean.FALSE;
			}
		},
		/** The selector policy makes the {@link Parallel} task succeed as soon as one child succeeds; if all children fail, then the
		 * parallel task fails. */
		Selector() {
			@Override
			public Boolean onChildSuccess (Parallel<?> parallel) {
				return Boolean.TRUE;
			}

			@Override
			public Boolean onChildFail (Parallel<?> parallel) {
				return parallel.noRunningTasks && parallel.currentChildIndex == parallel.children.size - 1 ? Boolean.FALSE : null;
			}
		};

		/** Called by parallel task each time one of its children succeeds.
		 * @param parallel the parallel task
		 * @return {@code Boolean.TRUE} if parallel must succeed, {@code Boolean.FALSE} if parallel must fail and {@code null} if
		 *         parallel must keep on running. */
		public abstract Boolean onChildSuccess (Parallel<?> parallel);

		/** Called by parallel task each time one of its children fails.
		 * @param parallel the parallel task
		 * @return {@code Boolean.TRUE} if parallel must succeed, {@code Boolean.FALSE} if parallel must fail and {@code null} if
		 *         parallel must keep on running. */
		public abstract Boolean onChildFail (Parallel<?> parallel);

	}
}
