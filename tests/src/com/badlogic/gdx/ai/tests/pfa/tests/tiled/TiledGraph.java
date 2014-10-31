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

import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** A random generated graph representing a tiled map.
 * 
 * @author davebaol */
public class TiledGraph extends IndexedGraph<TiledNode> {
	public static final int sizeX = 125; // 200; //100;
	public static final int sizeY = 75; // 120; //60;

	public boolean diagonal;
	public TiledNode startNode;

	public TiledGraph () {
		diagonal = false;
		nodes = new Array<TiledNode>(sizeX * sizeY);
		int roomCount = MathUtils.random(80, 150);// 100, 260);//70, 120);
		int roomMinSize = 3;
		int roomMaxSize = 15;
		int map[][] = DungeonUtils.generate(sizeX, sizeY, roomCount, roomMinSize, roomMaxSize);
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				nodes.add(new TiledNode(x, y, map[x][y]));
			}
		}

		// Each node has up to 4 neighbors, therefore no diagonal movement is possible
		for (int x = 0; x < sizeX; x++) {
			int idx = x * sizeY;
			for (int y = 0; y < sizeY; y++) {
				TiledNode n = nodes.get(idx + y);
				if (x > 0) setConnection(n, -1, 0);
				if (y > 0) setConnection(n, 0, -1);
				if (x < sizeX - 1) setConnection(n, 1, 0);
				if (y < sizeY - 1) setConnection(n, 0, 1);
			}
		}
	}

	public TiledNode getNode (int x, int y) {
		return nodes.get(x * sizeY + y);
	}

	public TiledNode getNode (int index) {
		return nodes.get(index);
	}

	private void setConnection (TiledNode n, int xOffset, int yOffset) {
		TiledNode target = getNode(n.x + xOffset, n.y + yOffset);
		if (target.type == TiledNode.TILE_FLOOR) n.getConnections().add(new TiledConnection(this, n, target));
	}
}
