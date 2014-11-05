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

/** A {@code HierarchicalGraph} is a multilevel graph that can be traversed by a {@link HierarchicalPathFinder} at any level of its
 * hierarchy.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public interface HierarchicalGraph<N> extends Graph<N> {

	/** Returns the number of levels in this hierarchical graph. */
	public int getLevelCount ();

	/** Switches the graph into the given level so all future calls to the {@link #getConnections(Object) getConnections} methods
	 * act as if the graph was just a simple, non-hierarchical graph at that level.
	 * @param level the level to set */
	public void setLevel (int level);

	/** Converts the node at the input level into a node at the output level.
	 * @param inputLevel the input level
	 * @param node the node at the input level
	 * @param outputLevel the output level
	 * @return the node at the output level. */
	public N convertNodeBetweenLevels (int inputLevel, N node, int outputLevel);
}
