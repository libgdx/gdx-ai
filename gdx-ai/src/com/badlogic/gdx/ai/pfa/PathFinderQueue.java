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
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.sched.Schedulable;
import com.badlogic.gdx.ai.utils.CircularBuffer;
import com.badlogic.gdx.utils.TimeUtils;

/** @author davebaol */
public class PathFinderQueue<N> implements Schedulable, Telegraph {

	public static final boolean DEBUG = false;

	public static final long TIME_TOLERANCE = 100L;

	CircularBuffer<PathFinderRequest<N>> requestQueue;

	PathFinder<N> pathFinder;

	PathFinderRequest<N> currentRequest;

	public PathFinderQueue (PathFinder<N> pathFinder) {
		this.pathFinder = pathFinder;
		this.requestQueue = new CircularBuffer<PathFinderRequest<N>>(16);
		this.currentRequest = null;
	}

	@Override
	public void run (long timeToRun) {
		// Keep track of the current time
		long lastTime = TimeUtils.nanoTime();

		// If no search in progress, take the next from the queue
		if (currentRequest == null) currentRequest = requestQueue.read();

		while (currentRequest != null) {

			currentRequest.executionFrames++;

			// Should perform search begin?
			if (currentRequest.status < PathFinderRequest.SEARCH_BEGIN_COMPLETE) {
				long currentTime = TimeUtils.nanoTime();
				timeToRun -= currentTime - lastTime;
				if (timeToRun <= TIME_TOLERANCE) return;
				if (DEBUG) System.out.println("search begin");
				currentRequest.statusChanged = currentRequest.onSearchBegin(timeToRun);
				if (!currentRequest.statusChanged) return; 
				currentRequest.status = PathFinderRequest.SEARCH_BEGIN_COMPLETE;
				lastTime = currentTime;
			}

			// Should perform search path?
			if (currentRequest.status < PathFinderRequest.SEARCH_PATH_COMPLETE) {
				long currentTime = TimeUtils.nanoTime();
				timeToRun -= currentTime - lastTime;
				if (timeToRun <= TIME_TOLERANCE) return;
				if (DEBUG) System.out.println("search path");
				currentRequest.statusChanged = pathFinder.search(currentRequest, timeToRun);
				if (!currentRequest.statusChanged) return;
				currentRequest.status = PathFinderRequest.SEARCH_PATH_COMPLETE;
				lastTime = currentTime;
			}

			// Should perform search end?
			if (currentRequest.status < PathFinderRequest.SEARCH_END_COMPLETE) {
				long currentTime = TimeUtils.nanoTime();
				timeToRun -= currentTime - lastTime;
				if (timeToRun <= TIME_TOLERANCE) return;
				if (DEBUG) System.out.println("search end");
				currentRequest.statusChanged = currentRequest.onSearchEnd(timeToRun);
				if (!currentRequest.statusChanged) return; 
				currentRequest.status = PathFinderRequest.SEARCH_END_COMPLETE;

				// Search finished, send result to the client
				MessageDispatcher.getInstance().dispatchMessage(this, currentRequest.client, currentRequest.responseMessageCode,
						currentRequest);
				lastTime = currentTime;
			}

			// Read next request from the queue
			currentRequest = requestQueue.read();

			// Store the current time
//			lastTime = currentTime;
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
