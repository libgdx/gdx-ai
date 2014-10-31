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

/** A {@code PathFinder} that can find a {@link GraphPath path} from one node in an arbitrary {@link Graph graph} to a goal node
 * based on information provided by that graph.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public interface PathFinder<N> {

	/** Tries to find a path made up of connections from the start node to the goal node attempting to honor costs provided by the
	 * graph.
	 * 
	 * @param startNode the start node
	 * @param endNode the end node
	 * @param heuristic the heuristic function
	 * @param outPath the output path that will only be filled if a path is found, otherwise it won't get touched.
	 * @return {@code true} if a path was found; {@code false} otherwise. */
	public boolean searchConnectionPath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<Connection<N>> outPath);

	/** Tries to find a path made up of nodes from the start node to the goal node attempting to honor costs provided by the graph.
	 * 
	 * @param startNode the start node
	 * @param endNode the end node
	 * @param heuristic the heuristic function
	 * @param outPath the output path that will only be filled if a path is found, otherwise it won't get touched.
	 * @return {@code true} if a path was found; {@code false} otherwise. */
	public boolean searchNodePath (N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> outPath);
}
