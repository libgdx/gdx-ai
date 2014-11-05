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

package com.badlogic.gdx.ai.tests.pfa.tests.tiled.hrchy;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledNode;
import com.badlogic.gdx.utils.Array;

/** A node for a {@link HierarchicalTiledGraph}.
 * 
 * @author davebaol */
public class HierarchicalTiledNode extends TiledNode<HierarchicalTiledNode> {
	/** The index of this tile. */
	public final int index;

	public HierarchicalTiledNode (int x, int y, int type, int index, int connectionCapacity) {
		super(x, y, type, new Array<Connection<HierarchicalTiledNode>>(connectionCapacity));
		this.index = index;
	}

	@Override
	public int getIndex () {
		return index;
	}

	public HierarchicalTiledNode getLowerLevelNode() {
		return null;
	}

}
