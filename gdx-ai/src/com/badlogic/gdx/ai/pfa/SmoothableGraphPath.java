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

import com.badlogic.gdx.math.Vector;

/** A path that can be smoothed by a {@link PathSmoother}.
 * 
 * @param <N> Type of node
 * @param <V> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public interface SmoothableGraphPath<N, V extends Vector<V>> extends GraphPath<N> {

	/** Returns the position of the node at the given index.
	 * @param index the index of the node you want to know the position */
	public V getNodePosition (int index);

	/** Swaps the specified nodes of this path.
	 * @param index1 index of the first node to swap
	 * @param index2 index of the second node to swap */
	public void swapNodes (int index1, int index2);

	/** Reduces the size of this path to the specified length (number of nodes). If the path is already smaller than the specified
	 * length, no action is taken.
	 * @param newLength the new length */
	public void truncatePath (int newLength);

}
