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

import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.TimeUtils;

/** A {@code PathSmoother} takes a {@link SmoothableGraphPath} and transforms it by linking directly the nodes that are in line of
 * sight. The smoothed path contains at most as many nodes as the original path. Also, the nodes in the smoothed path are unlikely
 * to have any connections between them (if they were connected in the graph, the pathfinder would have found the smoothed route
 * directly, unless their connections had dramatically large costs).
 * <p>
 * Some world representations are more prone to rough paths than others. Fore example, tile-based graphs tend to be highly
 * erratic. The final appearance also depends on how characters act on the path. If they are using some kind of path following
 * steering behavior, then the path will be gently smoothed by the steering. It is worth testing your game before assuming the
 * path will need smoothing.
 * <p>
 * For some games, path smoothing is essential to get the AI looking smart. The path smoothing algorithm is rather simple but
 * involves raycast and can be somewhat time-consuming.
 * <p>
 * The algorithm assumes that there is a clear route between any two adjacent nodes in the given path. Although this algorithm
 * produces a smooth path, it doesn't search all possible smoothed paths to find the best one, but the final result is usually
 * much more satisfactory than the original path.
 * 
 * @param <N> Type of node
 * @param <V> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class PathSmoother<N, V extends Vector<V>> {

	RaycastCollisionDetector<V> raycastCollisionDetector;
	Ray<V> ray;

	/** Creates a {@code PathSmoother} using the given {@link RaycastCollisionDetector}
	 * @param raycastCollisionDetector the raycast collision detector */
	public PathSmoother (RaycastCollisionDetector<V> raycastCollisionDetector) {
		this.raycastCollisionDetector = raycastCollisionDetector;
	}

	/** Smoothes the given path in place.
	 * @param path the path to smooth
	 * @return the number of nodes removed from the path. */
	public int smoothPath (SmoothableGraphPath<N, V> path) {
		int inputPathLength = path.getCount();

		// If the path is two nodes long or less, then we can't smooth it
		if (inputPathLength <= 2) return 0;

		// Make sure the ray is instantiated
		if (this.ray == null) {
			V vec = path.getNodePosition(0);
			this.ray = new Ray<V>(vec.cpy(), vec.cpy());
		}

		// Keep track of where we are in the smoothed path.
		// We start at 1, because we must always include the start node in the smoothed path.
		int outputIndex = 1;

		// Keep track of where we are in the input path
		// We start at 2, because we assume two adjacent
		// nodes will pass the ray cast
		int inputIndex = 2;

		// Loop until we find the last item in the input
		while (inputIndex < inputPathLength) {
			// Set the ray
			ray.start.set(path.getNodePosition(outputIndex - 1));
			ray.end.set(path.getNodePosition(inputIndex));

			// Do the ray cast
			boolean collides = raycastCollisionDetector.collides(ray);

			if (collides) {
				// The ray test failed, swap nodes and consider the next output node
				path.swapNodes(outputIndex, inputIndex - 1);
				outputIndex++;
			}

			// Consider the next input node
			inputIndex++;
		}

		// Reached the last input node, always add it to the smoothed path.
		path.swapNodes(outputIndex, inputIndex - 1);
		path.truncatePath(outputIndex + 1);

		// Return the number of removed nodes
		return inputIndex - outputIndex - 1;
	}

	/** Smoothes in place the path specified by the given request, possibly over multiple consecutive frames.
	 * @param request the path smoothing request
	 * @param timeToRun the time in nanoseconds that this call can use on the current frame
	 * @return {@code true} if this operation has completed; {@code false} if more time is needed to complete. */
	public boolean smoothPath (PathSmootherRequest<N, V> request, long timeToRun) {

		long lastTime = TimeUtils.nanoTime();

		SmoothableGraphPath<N, V> path = request.path;
		int inputPathLength = path.getCount();

		// If the path is two nodes long or less, then we can't smooth it
		if (inputPathLength <= 2) return true;

		if (request.isNew) {
			request.isNew = false;

			// Make sure the ray is instantiated
			if (this.ray == null) {
				V vec = request.path.getNodePosition(0);
				this.ray = new Ray<V>(vec.cpy(), vec.cpy());
			}

			// Keep track of where we are in the smoothed path.
			// We start at 1, because we must always include the start node in the smoothed path.
			request.outputIndex = 1;

			// Keep track of where we are in the input path
			// We start at 2, because we assume two adjacent
			// nodes will pass the ray cast
			request.inputIndex = 2;

		}

		// Loop until we find the last item in the input
		while (request.inputIndex < inputPathLength) {

			// Check the available time
			long currentTime = TimeUtils.nanoTime();
			timeToRun -= currentTime - lastTime;
			if (timeToRun <= PathFinderQueue.TIME_TOLERANCE) return false;

			// Set the ray
			ray.start.set(path.getNodePosition(request.outputIndex - 1));
			ray.end.set(path.getNodePosition(request.inputIndex));

			// Do the ray cast
			boolean collided = raycastCollisionDetector.collides(ray);

			if (collided) {
				// The ray test failed, swap nodes and consider the next output node
				path.swapNodes(request.outputIndex, request.inputIndex - 1);
				request.outputIndex++;
			}

			// Consider the next input node
			request.inputIndex++;

			// Store the current time
			lastTime = currentTime;
		}

		// Reached the last input node, always add it to the smoothed path
		path.swapNodes(request.outputIndex, request.inputIndex - 1);
		path.truncatePath(request.outputIndex + 1);

		// Smooth completed
		return true;
	}

}
