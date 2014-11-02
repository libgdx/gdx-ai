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

/** A {@code GraphPath} represents a path in a {@link Graph}. Note that a path can be defined in terms of nodes or
 * {@link Connection connections} so that multiple edges between the same pair of nodes can be discriminated.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public interface GraphPath<N> extends Iterable<N> {

	/** Returns the number of items of this path. */
	public int getCount ();

	/** Returns the item of this path at the given index. */
	public N get (int index);

	/** Adds an item at the end of this path. */
	public void add (N node);

	/** Clears this path. */
	public void clear ();

	/** Reverses this path. */
	public void reverse ();

}
