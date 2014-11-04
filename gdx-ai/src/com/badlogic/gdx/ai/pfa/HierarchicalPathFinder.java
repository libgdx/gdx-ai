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
	HierarchicalGraph<N> graph;
	PathFinder<N> levelPathFinder;

	public HierarchicalPathFinder (HierarchicalGraph<N> graph, PathFinder<N> levelPathFinder) {
		this.graph = graph;
		this.levelPathFinder = levelPathFinder;
	}

	@Override
	public boolean searchNodePath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> outPath) {
		// Check if we have no path to find
		if (startNode == endNode) return true;

		int levelCount = graph.getLevelCount();
		if (levelCount > 1) {
			// If start and end nodes have the same parent at level 1
			// we can perform non-hierarchical pathfinding at level 0 directly
			if (graph.convertNodeBetweenLevels(0, startNode, 1) == graph.convertNodeBetweenLevels(0, endNode, 1))
				return levelPathFinder.searchNodePath(startNode, endNode, heuristic, outPath);
		}
			
		// Set up our initial pair of nodes
		N currentStartNode = startNode;
		N currentEndNode = endNode;
		int levelOfNodes = 0;

		// Descend through levels of the graph
		int currentLevel = levelCount - 1;
		while (currentLevel >= 0) {
			// Find the start and end nodes at this level
			currentStartNode = graph.convertNodeBetweenLevels(0, startNode, currentLevel);
			currentEndNode = graph.convertNodeBetweenLevels(levelOfNodes, currentEndNode, currentLevel);
			levelOfNodes = currentLevel;
			currentLevel--;

			// Skip this level if start and end node are the same
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

		int levelCount = graph.getLevelCount();
		if (levelCount > 1) {
			// If start and end nodes have the same parent at level 1
			// we can perform non-hierarchical pathfinding at level 0 directly
			if (graph.convertNodeBetweenLevels(0, startNode, 1) == graph.convertNodeBetweenLevels(0, endNode, 1))
				return levelPathFinder.searchConnectionPath(startNode, endNode, heuristic, outPath);
		}

		// Set up our initial pair of nodes
		N currentStartNode = startNode;
		N currentEndNode = endNode;
		int levelOfNodes = 0;

		// Descend through levels of the graph
		int currentLevel = levelCount - 1;
		while (currentLevel >= 0) {
			// Find the start and end nodes at this level
			currentStartNode = graph.convertNodeBetweenLevels(0, startNode, currentLevel);
			currentEndNode = graph.convertNodeBetweenLevels(levelOfNodes, currentEndNode, currentLevel);
			levelOfNodes = currentLevel;
			currentLevel--;

			// Skip this level if start and end node are the same
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

}
