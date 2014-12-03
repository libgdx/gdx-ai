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

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.sched.Schedulable;
import com.badlogic.gdx.ai.utils.CircularBuffer;
import com.badlogic.gdx.utils.TimeUtils;

/** @param <N> Type of node
 * 
 * @author davebaol */
public class PathFinderQueue<N> implements Schedulable, Telegraph {

	public static final long TIME_TOLERANCE = 100L;

	CircularBuffer<PathFinderRequest<N>> requestQueue;

	PathFinder<N> pathFinder;

	PathFinderRequest<N> currentRequest;

	PathFinderRequestControl<N> requestControl;

	public PathFinderQueue (PathFinder<N> pathFinder) {
		this.pathFinder = pathFinder;
		this.requestQueue = new CircularBuffer<PathFinderRequest<N>>(16);
		this.currentRequest = null;
		this.requestControl = new PathFinderRequestControl<N>();
	}

	@Override
	public void run (long timeToRun) {
		// Keep track of the current time
		requestControl.lastTime = TimeUtils.nanoTime();
		requestControl.timeToRun = timeToRun;

		requestControl.timeTolerance = TIME_TOLERANCE;
		requestControl.pathFinder = pathFinder;
		requestControl.server = this;

		// If no search in progress, take the next from the queue
		if (currentRequest == null) currentRequest = requestQueue.read();

		while (currentRequest != null) {

			boolean finished = requestControl.execute(currentRequest);

			if (!finished) return;

			// Read next request from the queue
			currentRequest = requestQueue.read();
		}
	}

	@Override
	public boolean handleMessage (Telegram telegram) {
		@SuppressWarnings("unchecked")
		PathFinderRequest<N> pfr = (PathFinderRequest<N>)telegram.extraInfo;
		pfr.client = telegram.sender; // set the client to be notified once the request has completed
		pfr.status = PathFinderRequest.SEARCH_NEW; // Reset status
		pfr.statusChanged = true; // Status has just changed
		pfr.executionFrames = 0; // Reset execution frames counter
		requestQueue.store(pfr);
		return true;
	}

	public int size () {
		return requestQueue.size();
	}
}
