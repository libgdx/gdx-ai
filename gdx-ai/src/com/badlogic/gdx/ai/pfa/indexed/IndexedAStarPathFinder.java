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

package com.badlogic.gdx.ai.pfa.indexed;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.PathFinderQueue;
import com.badlogic.gdx.ai.pfa.PathFinderRequest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.TimeUtils;

/** A fully implemented {@link PathFinder} that can perform both interruptible and non-interruptible pathfinding.
 * <p>
 * This implementation is a common variation of the A* algorithm that is faster than the general A*.
 * <p>
 * In the general A* implementation, data are held for each node in the open or closed lists, and these data are held as a
 * NodeRecord instance. Records are created when a node is first considered and then moved between the open and closed lists, as
 * required. There is a key step in the algorithm where the lists are searched for a node record corresponding to a particular
 * node. This operation is something time-consuming.
 * <p>
 * The indexed A* algorithm improves execution speed by using an array of all the node records for every node in the graph. Nodes
 * must be numbered using sequential integers (see {@link IndexedGraph#getIndex(Object)}), so we don't need to search for a node in the
 * two lists at all. We can simply use the node index to look up its record in the array (creating it if it is missing). This
 * means that the close list is no longer needed. To know whether a node is open or closed, we use the {@link NodeRecord#category
 * category} of the node record. This makes the search step very fast indeed (in fact, there is no search, and we can go straight
 * to the information we need). Unfortunately, we can't get rid of the open list because we still need to be able to retrieve the
 * element with the lowest cost. However, we use a {@link BinaryHeap} for the open list in order to keep performance as high as
 * possible.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class IndexedAStarPathFinder<N> implements PathFinder<N> {
	IndexedGraph<N> graph;
	NodeRecord<N>[] nodeRecords;
	BinaryHeap<NodeRecord<N>> openList;
	NodeRecord<N> current;
	public Metrics metrics;

	/** The unique ID for each search run. Used to mark nodes. */
	private int searchId;

	private static final int UNVISITED = 0;
	private static final int OPEN = 1;
	private static final int CLOSED = 2;

	public IndexedAStarPathFinder (IndexedGraph<N> graph) {
		this(graph, false);
	}

	@SuppressWarnings("unchecked")
	public IndexedAStarPathFinder (IndexedGraph<N> graph, boolean calculateMetrics) {
		this.graph = graph;
		this.nodeRecords = (NodeRecord<N>[])new NodeRecord[graph.getNodeCount()];
		this.openList = new BinaryHeap<NodeRecord<N>>();
		if (calculateMetrics) this.metrics = new Metrics();
	}

	@Override
	public boolean searchConnectionPath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<Connection<N>> outPath) {

		// Perform AStar
		boolean found = search(startNode, endNode, heuristic);

		if (found) {
			// Create a path made of connections
			generateConnectionPath(startNode, outPath);
		}

		return found;
	}

	@Override
	public boolean searchNodePath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> outPath) {

		// Perform AStar
		boolean found = search(startNode, endNode, heuristic);

		if (found) {
			// Create a path made of nodes
			generateNodePath(startNode, outPath);
		}

		return found;
	}

	protected boolean search (N startNode, N endNode, Heuristic<N> heuristic) {

		initSearch(startNode, endNode, heuristic);

		// Iterate through processing each node
		do {
			// Retrieve the node with smallest estimated total cost from the open list
			current = openList.pop();
			current.category = CLOSED;

			// Terminate if we reached the goal node
			if (current.node == endNode) return true;

			visitChildren(endNode, heuristic);

		} while (openList.size > 0);

		// We've run out of nodes without finding the goal, so there's no solution
		return false;
	}

	@Override
	public boolean search (PathFinderRequest<N> request, long timeToRun) {

		long lastTime = TimeUtils.nanoTime();

		// We have to initialize the search if the status has just changed
		if (request.statusChanged) {
			initSearch(request.startNode, request.endNode, request.heuristic);
			request.statusChanged = false;
		}

		// Iterate through processing each node
		do {

			// Check the available time
			long currentTime = TimeUtils.nanoTime();
			timeToRun -= currentTime - lastTime;
			if (timeToRun <= PathFinderQueue.TIME_TOLERANCE) return false;

			// Retrieve the node with smallest estimated total cost from the open list
			current = openList.pop();
			current.category = CLOSED;

			// Terminate if we reached the goal node; we've found a path.
			if (current.node == request.endNode) {
				request.pathFound = true;

				generateNodePath(request.startNode, request.resultPath);

				return true;
			}

			// Visit current node's children
			visitChildren(request.endNode, request.heuristic);

			// Store the current time
			lastTime = currentTime;

		} while (openList.size > 0);

		// The open list is empty and we've not found a path.
		request.pathFound = false;
		return true;
	}

	protected void initSearch (N startNode, N endNode, Heuristic<N> heuristic) {
		if (metrics != null) metrics.reset();

		// Increment the search id
		if (++searchId < 0) searchId = 1;

		// Initialize the open list
		openList.clear();

		// Initialize the record for the start node and add it to the open list
		NodeRecord<N> startRecord = getNodeRecord(startNode);
		startRecord.node = startNode;
		startRecord.connection = null;
		startRecord.costSoFar = 0;
		addToOpenList(startRecord, heuristic.estimate(startNode, endNode));

		current = null;
	}

	protected void visitChildren (N endNode, Heuristic<N> heuristic) {
		// Get current node's outgoing connections
		Array<Connection<N>> connections = graph.getConnections(current.node);

		// Loop through each connection in turn
		for (int i = 0; i < connections.size; i++) {
			if (metrics != null) metrics.visitedNodes++;

			Connection<N> connection = connections.get(i);

			// Get the cost estimate for the node
			N node = connection.getToNode();
			float nodeCost = current.costSoFar + connection.getCost();

			float nodeHeuristic;
			NodeRecord<N> nodeRecord = getNodeRecord(node);
			if (nodeRecord.category == CLOSED) { // The node is closed

				// If we didn't find a shorter route, skip
				if (nodeRecord.costSoFar <= nodeCost) continue;

				// We can use the node's old cost values to calculate its heuristic
				// without calling the possibly expensive heuristic function
				nodeHeuristic = nodeRecord.getEstimatedTotalCost() - nodeRecord.costSoFar;
			} else if (nodeRecord.category == OPEN) { // The node is open

				// If our route is no better, then skip
				if (nodeRecord.costSoFar <= nodeCost) continue;

				// Remove it from the open list (it will be re-added with the new cost)
				openList.remove(nodeRecord);

				// We can use the node's old cost values to calculate its heuristic
				// without calling the possibly expensive heuristic function
				nodeHeuristic = nodeRecord.getEstimatedTotalCost() - nodeRecord.costSoFar;
			} else { // the node is unvisited

				// We'll need to calculate the heuristic value using the function,
				// since we don't have a node record with a previously calculated value
				nodeHeuristic = heuristic.estimate(node, endNode);
			}

			// Update node record's cost and connection
			nodeRecord.costSoFar = nodeCost;
			nodeRecord.connection = connection;

			// Add it to the open list with the estimated total cost
			addToOpenList(nodeRecord, nodeCost + nodeHeuristic);
		}

	}

	protected void generateConnectionPath (N startNode, GraphPath<Connection<N>> outPath) {

		// Work back along the path, accumulating connections
		// outPath.clear();
		while (current.node != startNode) {
			outPath.add(current.connection);
			current = nodeRecords[graph.getIndex(current.connection.getFromNode())];
		}

		// Reverse the path
		outPath.reverse();
	}

	protected void generateNodePath (N startNode, GraphPath<N> outPath) {

		// Work back along the path, accumulating nodes
		// outPath.clear();
		while (current.connection != null) {
			outPath.add(current.node);
			current = nodeRecords[graph.getIndex(current.connection.getFromNode())];
		}
		outPath.add(startNode);

		// Reverse the path
		outPath.reverse();
	}

	protected void addToOpenList (NodeRecord<N> nodeRecord, float estimatedTotalCost) {
		openList.add(nodeRecord, estimatedTotalCost);
		nodeRecord.category = OPEN;
		if (metrics != null) {
			metrics.openListAdditions++;
			metrics.openListPeak = Math.max(metrics.openListPeak, openList.size);
		}
	}

	protected NodeRecord<N> getNodeRecord (N node) {
		int index = graph.getIndex(node);
		NodeRecord<N> nr = nodeRecords[index];
		if (nr != null) {
			if (nr.searchId != searchId) {
				nr.category = UNVISITED;
				nr.searchId = searchId;
			}
			return nr;
		}
		nr = nodeRecords[index] = new NodeRecord<N>();
		nr.node = node;
		nr.searchId = searchId;
		return nr;
	}

	/** This nested class is used to keep track of the information we need for each node during the search.
	 * 
	 * @param <N> Type of node
	 * 
	 * @author davebaol */
	static class NodeRecord<N> extends BinaryHeap.Node {
		/** The reference to the node. */
		N node;

		/** The incoming connection to the node */
		Connection<N> connection;

		/** The actual cost from the start node. */
		float costSoFar;

		/** The node category: {@link #UNVISITED}, {@link #OPEN} or {@link #CLOSED}. */
		int category;

		/** ID of the current search. */
		int searchId;

		/** Creates a {@code NodeRecord}. */
		public NodeRecord () {
			super(0);
		}

		/** Returns the estimated total cost. */
		public float getEstimatedTotalCost () {
			return getValue();
		}
	}

	/** A class used by {@link IndexedAStarPathFinder} to collect search metrics.
	 * 
	 * @author davebaol */
	public static class Metrics {
		public int visitedNodes;
		public int openListAdditions;
		public int openListPeak;

		public Metrics () {
		}

		public void reset () {
			visitedNodes = 0;
			openListAdditions = 0;
			openListPeak = 0;
		}
	}
}
