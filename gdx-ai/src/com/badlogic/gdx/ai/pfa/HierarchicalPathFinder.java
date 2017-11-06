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

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.utils.TimeUtils;

/** A {@code HierarchicalPathFinder} can find a path in an arbitrary {@link HierarchicalGraph} using the given {@link PathFinder},
 * known as level path finder, on each level of the hierarchy.
 * <p>
 * Pathfinding on a hierarchical graph applies the level path finder algorithm several times, starting at a high level of the
 * hierarchy and working down. The results at high levels are used to limit the work it needs to do at lower levels.
 * <p>
 * Note that the hierarchical path finder calls the {@link HierarchicalGraph#setLevel(int)} method to switch the graph into a
 * particular level. All future calls to the {@link HierarchicalGraph#getConnections(Object) getConnections} method of the
 * hierarchical graph then act as if the graph was just a simple, non-hierarchical graph at that level. This way, the level path
 * finder has no way of telling that it is working with a hierarchical graph and it doesn't need to, meaning that you can use any
 * path finder implementation for the level path finder.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class HierarchicalPathFinder<N> implements PathFinder<N> {
	private static final String TAG = "HierarchicalPathFinder";

	public static boolean DEBUG = false;

	HierarchicalGraph<N> graph;
	PathFinder<N> levelPathFinder;
	LevelPathFinderRequest<N> levelRequest;
	PathFinderRequestControl<N> levelRequestControl;

	public HierarchicalPathFinder (HierarchicalGraph<N> graph, PathFinder<N> levelPathFinder) {
		this.graph = graph;
		this.levelPathFinder = levelPathFinder;
		this.levelRequest = null;
		this.levelRequestControl = null;
	}

	@Override
	public boolean searchNodePath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> outPath) {
		// Check if we have no path to find
		if (startNode == endNode) return true;

		// Set up our initial pair of nodes
		N currentStartNode = startNode;
		N currentEndNode = endNode;
		int levelOfNodes = 0;

		// Descend through levels of the graph
		int currentLevel = graph.getLevelCount() - 1;
		while (currentLevel >= 0) {
			// Find the start node at current level
			currentStartNode = graph.convertNodeBetweenLevels(0, startNode, currentLevel);

			// Find the end node at current level
			// Note that if we're examining level 0 and the current end node, the end node and the
			// start node have the same parent at level 1 then we can use the end node directly.
			currentEndNode = graph.convertNodeBetweenLevels(levelOfNodes, currentEndNode, currentLevel);
			if (currentLevel == 0) {
				N currentEndNodeParent = graph.convertNodeBetweenLevels(0, currentEndNode, 1);
				if (currentEndNodeParent == graph.convertNodeBetweenLevels(0, endNode, 1)
					&& currentEndNodeParent == graph.convertNodeBetweenLevels(0, startNode, 1)) {
					currentEndNode = endNode;
				}
			}

			// Decrease current level and skip it if start and end node are the same
			levelOfNodes = currentLevel;
			currentLevel--;
			if (currentStartNode == currentEndNode) continue;

			// Otherwise we can perform the plan
			graph.setLevel(levelOfNodes);
			outPath.clear();
			boolean pathFound = levelPathFinder.searchNodePath(currentStartNode, currentEndNode, heuristic, outPath);

			if (!pathFound) return false;

			// Now take the first move of this plan and use it for the next run through
			currentEndNode = outPath.get(1);
		}

		// Return success.
		// Note that outPath contains the last path we considered which is at level zero
		return true;
	}

	@Override
	public boolean searchConnectionPath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<Connection<N>> outPath) {
		// Check if we have no path to find
		if (startNode == endNode) return true;

		// Set up our initial pair of nodes
		N currentStartNode = startNode;
		N currentEndNode = endNode;
		int levelOfNodes = 0;

		// Descend through levels of the graph
		int currentLevel = graph.getLevelCount() - 1;
		while (currentLevel >= 0) {
			// Find the start node at current level
			currentStartNode = graph.convertNodeBetweenLevels(0, startNode, currentLevel);

			// Find the end node at current level
			// Note that if we're examining level 0 and the current end node, the end node and the
			// start node have the same parent at level 1 then we can use the end node directly.
			currentEndNode = graph.convertNodeBetweenLevels(levelOfNodes, currentEndNode, currentLevel);
			if (currentLevel == 0) {
				N currentEndNodeParent = graph.convertNodeBetweenLevels(0, currentEndNode, 1);
				if (currentEndNodeParent == graph.convertNodeBetweenLevels(0, endNode, 1)
					&& currentEndNodeParent == graph.convertNodeBetweenLevels(0, startNode, 1)) {
					currentEndNode = endNode;
				}
			}

			// Decrease current level and skip it if start and end node are the same
			levelOfNodes = currentLevel;
			currentLevel--;
			if (currentStartNode == currentEndNode) continue;

			// Otherwise we can perform the plan
			graph.setLevel(levelOfNodes);
			outPath.clear();
			boolean pathFound = levelPathFinder.searchConnectionPath(currentStartNode, currentEndNode, heuristic, outPath);

			if (!pathFound) return false;

			// Now take the first move of this plan and use it for the next run through
			currentEndNode = outPath.get(0).getToNode();
		}

		// Return success.
		// Note that outPath contains the last path we considered which is at level zero
		return true;
	}

	@Override
	public boolean search (PathFinderRequest<N> request, long timeToRun) {
		if (DEBUG) GdxAI.getLogger().debug(TAG, "Enter interruptible HPF; request.status = " + request.status);

		// Make sure the level request and its control are instantiated
		if (levelRequest == null) {
			levelRequest = new LevelPathFinderRequest<N>();
			levelRequestControl = new PathFinderRequestControl<N>();
		}

		// We have to initialize the search if the status has just changed
		if (request.statusChanged) {
			if (DEBUG) GdxAI.getLogger().debug(TAG, "-- statusChanged");

			// Check if we have no path to find
			if (request.startNode == request.endNode) return true;

			// Prepare the the level request control
			levelRequestControl.lastTime = TimeUtils.nanoTime(); // Keep track of the current time
			levelRequestControl.timeToRun = timeToRun;
			levelRequestControl.timeTolerance = PathFinderQueue.TIME_TOLERANCE;
			levelRequestControl.server = null;
			levelRequestControl.pathFinder = levelPathFinder;

			// Prepare the level request
			levelRequest.hpf = this;
			levelRequest.hpfRequest = request;
			levelRequest.status = PathFinderRequest.SEARCH_NEW;
			levelRequest.statusChanged = true;
			levelRequest.heuristic = request.heuristic;
			levelRequest.resultPath = request.resultPath;
			levelRequest.startNode = request.startNode;
			levelRequest.endNode = request.endNode;
			levelRequest.levelOfNodes = 0;
			levelRequest.currentLevel = graph.getLevelCount() - 1;
		}

		while (levelRequest.currentLevel >= 0) {
//			if (DEBUG) GdxAI.getLogger().debug(TAG, "currentLevel = "+levelRequest.currentLevel);

			boolean finished = levelRequestControl.execute(levelRequest);
//			if (DEBUG) GdxAI.getLogger().debug(TAG, "finished = "+finished);
//			if (DEBUG) GdxAI.getLogger().debug(TAG, "pathFound = "+levelRequest.pathFound);

//			if (finished && !levelRequest.pathFound) return true;
			if (!finished) {
				return false;
			}
			else {
				levelRequest.executionFrames = 0;
//				levelRequest.pathFound = false;
				levelRequest.status = PathFinderRequest.SEARCH_NEW;
				levelRequest.statusChanged = true;

				if (!levelRequest.pathFound) return true;
			}
		}

		if (DEBUG) GdxAI.getLogger().debug(TAG, "-- before exit");
		// If we're here we have finished
		return true;
	}

	static class LevelPathFinderRequest<N> extends PathFinderRequest<N> {
		HierarchicalPathFinder<N> hpf;
		PathFinderRequest<N> hpfRequest;

		int levelOfNodes;
		int currentLevel;

		@Override
		public boolean initializeSearch (long timeToRun) {

			// Reset the status
			// We can do it here because we know this method completes during this frame,
			// meaning that it is executed once per request
			this.executionFrames = 0;
			this.pathFound = false;
			this.status = SEARCH_NEW;
			this.statusChanged = false;

			do {
				// Find the start node at current level
				startNode = hpf.graph.convertNodeBetweenLevels(0, hpfRequest.startNode, currentLevel);

				// Find the end node at current level
				// Note that if we're examining level 0 and the current end node, the end node and the
				// start node have the same parent at level 1 then we can use the end node directly.
				endNode = hpf.graph.convertNodeBetweenLevels(levelOfNodes, endNode, currentLevel);
				if (currentLevel == 0) {
					N currentEndNodeParent = hpf.graph.convertNodeBetweenLevels(0, endNode, 1);
					if (currentEndNodeParent == hpf.graph.convertNodeBetweenLevels(0, hpfRequest.endNode, 1)
						&& currentEndNodeParent == hpf.graph.convertNodeBetweenLevels(0, hpfRequest.startNode, 1)) {
						endNode = hpfRequest.endNode;
					}
				}

				// Decrease current level and skip it if start and end node are the same
				// FIXME the break below is wrong
				if (DEBUG) GdxAI.getLogger().debug(TAG, "LevelPathFinder initializeSearch");
				levelOfNodes = currentLevel;
				currentLevel--;
				if (startNode != endNode) break;

			} while (currentLevel >= 0);

			// Otherwise we can perform the plan
			hpf.graph.setLevel(levelOfNodes);
			resultPath.clear();
			return true;
		}

		@Override
		public boolean search (PathFinder<N> pathFinder, long timeToRun) {
			if (DEBUG) GdxAI.getLogger().debug(TAG, "LevelPathFinder search; status: " + status);
			return super.search(pathFinder, timeToRun);
		}

		@Override
		public boolean finalizeSearch (long timeToRun) {
			hpfRequest.pathFound = pathFound;
			if (pathFound) {
				// Take the first move of this plan and use it for the next run through
				endNode = resultPath.get(1);
			}
			if (DEBUG) GdxAI.getLogger().debug(TAG, "LevelPathFinder finalizeSearch; status: " + status);
			return true;
		}

	}
}
