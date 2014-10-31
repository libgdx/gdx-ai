/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.ai.tests.pfa.tests.tiled;

import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** A smoothable path for a {@link TiledGraph}. It holds a list of {@link TiledNode tiled nodes} that are part of the path.
 * 
 * @author davebaol */
public class TiledSmoothableGraphPath implements SmoothableGraphPath<TiledNode, Vector2> {
	public final Array<TiledNode> nodes = new Array<TiledNode>();

	@Override
	public void clear () {
		nodes.clear();
	}

	@Override
	public int getCount () {
		return nodes.size;
	}

	private Vector2 tmpPosition = new Vector2();

	/** Returns the position of the node at the given index.
	 * <p>
	 * <b>Note that the same Vector2 instance is returned each time this method is called.</b>
	 * @param index the index of the node you want to know the position */
	@Override
	public Vector2 getNodePosition (int index) {
		TiledNode node = nodes.get(index);
		return tmpPosition.set(node.x, node.y);
	}

	@Override
	public void swapNodes (int index1, int index2) {
// x.swap(index1, index2);
// y.swap(index1, index2);
		nodes.set(index1, nodes.get(index2));
	}

	@Override
	public void truncatePath (int newLength) {
		nodes.truncate(newLength);
	}

	@Override
	public void add (TiledNode node) {
		nodes.add(node);
	}

	@Override
	public void reverse () {
		nodes.reverse();
	}

	@Override
	public TiledNode get (int index) {
		return nodes.get(index);
	}
}
