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

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;

/** A raycast collision detector used for path smoothing against a {@link TiledGraph}.
 * 
 * @param <N> Type of node, either flat or hierarchical, extending the {@link TiledNode} class
 * 
 * @author davebaol */
public class TiledRaycastCollisionDetector<N extends TiledNode<N>> implements RaycastCollisionDetector<Vector2> {
	TiledGraph<N> worldMap;

	public TiledRaycastCollisionDetector (TiledGraph<N> worldMap) {
		this.worldMap = worldMap;
	}

	// See http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
	@Override
	public boolean collides (Ray<Vector2> ray) {
		int x0 = (int)ray.start.x;
		int y0 = (int)ray.start.y;
		int x1 = (int)ray.end.x;
		int y1 = (int)ray.end.y;

		int tmp;
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			// Swap x0 and y0
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			// Swap x1 and y1
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		if (x0 > x1) {
			// Swap x0 and x1
			tmp = x0;
			x0 = x1;
			x1 = tmp;
			// Swap y0 and y1
			tmp = y0;
			y0 = y1;
			y1 = tmp;
		}

		int deltax = x1 - x0;
		int deltay = Math.abs(y1 - y0);
		int error = 0;
		int y = y0;
		int ystep = (y0 < y1 ? 1 : -1);
		for (int x = x0; x <= x1; x++) {
			N tile = steep ? worldMap.getNode(y, x) : worldMap.getNode(x, y);
			if (tile.type != TiledNode.TILE_FLOOR) return true; // We've hit a wall
			error += deltay;
			if (error + error >= deltax) {
				y += ystep;
				error -= deltax;
			}
		}

		return false;
	}

	@Override
	public boolean findCollision (Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
		throw new UnsupportedOperationException();
	}
}
