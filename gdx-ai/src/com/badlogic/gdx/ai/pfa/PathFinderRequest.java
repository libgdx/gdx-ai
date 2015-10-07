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

package com.badlogic.gdx.ai.pfa;

import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;

/** A request for interruptible pathfinding that should be sent to a {@link PathFinderQueue} through a {@link Telegram}.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class PathFinderRequest<N> {

	public static final int SEARCH_NEW = 0;
	public static final int SEARCH_INITIALIZED = 1;
	public static final int SEARCH_DONE = 2;
	public static final int SEARCH_FINALIZED = 3;

	public N startNode;
	public N endNode;
	public Heuristic<N> heuristic;
	public GraphPath<N> resultPath;
	public int executionFrames;
	public boolean pathFound;
	public int status;
	public boolean statusChanged;
	public Telegraph client;
	public int responseMessageCode;
	public MessageDispatcher dispatcher;

	/** Creates an empty {@code PathFinderRequest} */
	public PathFinderRequest () {
	}

	/** Creates a {@code PathFinderRequest} with the given arguments that uses the singleton message dispatcher provided by
	 * {@link MessageManager}. */
	public PathFinderRequest (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> resultPath) {
		this(startNode, endNode, heuristic, resultPath, MessageManager.getInstance());
	}

	/** Creates a {@code PathFinderRequest} with the given arguments. */
	public PathFinderRequest (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> resultPath, MessageDispatcher dispatcher) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.heuristic = heuristic;
		this.resultPath = resultPath;
		this.dispatcher = dispatcher;

		this.executionFrames = 0;
		this.pathFound = false;
		this.status = SEARCH_NEW;
		this.statusChanged = false;
	}

	public void changeStatus (int newStatus) {
		this.status = newStatus;
		this.statusChanged = true;
	}

	/** Interruptible method called by the {@link PathFinderRequestControl} as soon as this request starts to be served.
	 * @param timeToRun the time in nanoseconds that this call can use on the current frame
	 * @return {@code true} if initialization has completed; {@code false} if more time is needed to complete. */
	public boolean initializeSearch (long timeToRun) {
		return true;
	}

	/** @param pathFinder the path finder
	 * @param timeToRun the time in nanoseconds that this call can use on the current frame
	 * @return {@code true} if the search has completed; {@code false} if more time is needed to complete. */
	public boolean search (PathFinder<N> pathFinder, long timeToRun) {
		return pathFinder.search(this, timeToRun);
	}

	/** Interruptible method called by {@link PathFinderQueue} when the path finder has completed the search. You have to check the
	 * {@link #pathFound} field of this request to know if a path has been found.
	 * @param timeToRun the time in nanoseconds that this call can use on the current frame
	 * @return {@code true} if finalization has completed; {@code false} if more time is needed to complete. */
	public boolean finalizeSearch (long timeToRun) {
		return true;
	}
}
