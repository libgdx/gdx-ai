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

/** A {@code Heuristic} generates estimates of the cost to move from a given node to the goal.
 * <p>
 * With a heuristic function pathfinding algorithms can choose the node that is most likely to lead to the optimal path. The
 * notion of "most likely" is controlled by a heuristic. If the heuristic is accurate, then the algorithm will be efficient. If
 * the heuristic is terrible, then it can perform even worse than other algorithms that don't use any heuristic function such as
 * Dijkstra.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public interface Heuristic<N> {

	/** Calculates an estimated cost to reach the goal node from the given node.
	 * @param node the start node
	 * @param endNode the end node
	 * @return the estimated cost */
	public float estimate (N node, N endNode);
}
