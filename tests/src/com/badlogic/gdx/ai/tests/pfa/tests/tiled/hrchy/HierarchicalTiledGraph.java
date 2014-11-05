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

import com.badlogic.gdx.ai.pfa.indexed.IndexedHierarchicalGraph;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.DungeonUtils;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.DungeonUtils.TwoLevelHierarchy;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.flat.FlatTiledNode;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledGraph;
import com.badlogic.gdx.math.Vector2;

/** A random generated graph representing a hierarchical tiled map.
 * 
 * @author davebaol */
public class HierarchicalTiledGraph extends IndexedHierarchicalGraph<HierarchicalTiledNode> implements
	TiledGraph<HierarchicalTiledNode> {
	private static final int LEVELS = 2;
	public static final int[] sizeX = {125, 3};
	public static final int[] sizeY = {75, 2};
	public static final int[] offset = {0, sizeX[0] * sizeY[0]};

	public boolean diagonal;
	public HierarchicalTiledNode startNode;

	public HierarchicalTiledGraph () {
		super(LEVELS, calculateTotalCapacity());
		this.diagonal = false;
		this.startNode = null;
	}

	@Override
	public void init (int roomCount, int roomMinSize, int roomMaxSize, int squashIterations) {
		int tilesX = sizeX[0];
		int tilesY = sizeY[0];
		int buildingsX = sizeX[1];
		int buildingsY = sizeY[1];

		TwoLevelHierarchy twoLevelHierarchy = DungeonUtils.generate2LevelHierarchy(tilesX, tilesY, buildingsX, buildingsY,
			(int)(roomCount - roomCount * .8f), (int)(roomCount + roomCount * .8f), roomMinSize, roomMaxSize, squashIterations);

		// Create nodes for level 0 (tiles)
		this.level = 0;
		int map[][] = twoLevelHierarchy.level0;
		for (int x = 0; x < tilesX; x++) {
			for (int y = 0; y < tilesY; y++) {
				nodes.add(new HierarchicalTiledNode(x, y, map[x][y], nodes.size, 4));
			}
		}

		// Create connections for level 0
		// Each node has up to 4 neighbors, therefore no diagonal movement is possible
		for (int x = 0; x < tilesX; x++) {
			int idx = x * tilesY;
			for (int y = 0; y < tilesY; y++) {
				HierarchicalTiledNode n = nodes.get(idx + y);
				if (x > 0) addLevel0Connection(n, -1, 0);
				if (y > 0) addLevel0Connection(n, 0, -1);
				if (x < tilesX - 1) addLevel0Connection(n, 1, 0);
				if (y < tilesY - 1) addLevel0Connection(n, 0, 1);
			}
		}

		// Create nodes for level 1 (buildings)
		this.level = 1;
		int xxx = tilesX / buildingsX;
		int yyy = tilesY / buildingsY;
		for (int x = 0; x < buildingsX; x++) {
			for (int y = 0; y < buildingsY; y++) {
				int x0 = x * xxx;
				int y0 = y * yyy;
				int x1 = x0 + xxx;
				int y1 = y0 + yyy;
				final HierarchicalTiledNode lowerLevelNode = findFloorTileClosestToCenterOfMass(this.level - 1, x0, y0, x1, y1);
				nodes.add(new HierarchicalTiledNode(x, y, map[x][y], nodes.size, 4) {
					@Override
					public HierarchicalTiledNode getLowerLevelNode () {
						return lowerLevelNode;
					}
				});
			}
		}

// System.out.println(DungeonUtils.mapToString(map));

		// Create connections for level 1
		// Each node has up to 2 neighbors
		for (int x = 0; x < buildingsX; x++) {
			for (int y = 0; y < buildingsY; y++) {
				HierarchicalTiledNode n = getNode(x, y);
				if (twoLevelHierarchy.level1Con1[x][y]) addLevel1BidirectionalConnection(n, 0, 1);
				if (twoLevelHierarchy.level1Con2[x][y]) addLevel1BidirectionalConnection(n, 1, 0);
			}
		}

	}

	private HierarchicalTiledNode getNodeAtLevel (int level, int x, int y) {
		return nodes.get(x * sizeY[level] + y + offset[level]);
	}

	private HierarchicalTiledNode findFloorTileClosestToCenterOfMass (int level, int x0, int y0, int x1, int y1) {
		// Calculate center of mass
		Vector2 centerOfMass = new Vector2(0, 0);
		int floorTiles = 0;
		for (int x = x0; x < x1; x++) {
			for (int y = y0; y < y1; y++) {
				HierarchicalTiledNode n = getNodeAtLevel(level, x, y);
				if (n.type == FlatTiledNode.TILE_FLOOR) {
					centerOfMass.add(n.x, n.y);
					floorTiles++;
				}
			}
		}
		int comx = (int)(centerOfMass.x / floorTiles);
		int comy = (int)(centerOfMass.y / floorTiles);
		HierarchicalTiledNode comTile = getNodeAtLevel(level, comx, comy);
		if (comTile.type == FlatTiledNode.TILE_FLOOR) return comTile;

		// Find floor tile closest to the center of mass
		float closetDist2 = Float.POSITIVE_INFINITY;
		HierarchicalTiledNode closestFloor = null;
		for (int x = x0; x < x1; x++) {
			for (int y = y0; y < y1; y++) {
				HierarchicalTiledNode n = getNodeAtLevel(level, x, y);
				if (n.type == FlatTiledNode.TILE_FLOOR) {
					float dist2 = Vector2.dst2(comTile.x, comTile.y, n.x, n.y);
					if (dist2 < closetDist2) {
						closetDist2 = dist2;
						closestFloor = n;
					}
				}
			}
		}

		return closestFloor;
	}

	@Override
	public HierarchicalTiledNode convertNodeBetweenLevels (int inputLevel, HierarchicalTiledNode node, int outputLevel) {
		if (inputLevel < outputLevel) {
			int newX = node.x / (sizeX[inputLevel] / sizeX[outputLevel]);
			int newY = node.y / (sizeY[inputLevel] / sizeY[outputLevel]);
			return nodes.get(newX * sizeY[outputLevel] + newY + offset[outputLevel]);
		}

		if (inputLevel > outputLevel) {
			return node.getLowerLevelNode();
		}

		return node;
	}

	@Override
	public HierarchicalTiledNode getNode (int x, int y) {
		return nodes.get(x * sizeY[level] + y + offset[level]);
	}

	@Override
	public HierarchicalTiledNode getNode (int index) {
		return nodes.get(index);
	}

	private void addLevel0Connection (HierarchicalTiledNode n, int xOffset, int yOffset) {
		HierarchicalTiledNode target = getNode(n.x + xOffset, n.y + yOffset);
		if (target.type == FlatTiledNode.TILE_FLOOR) n.getConnections().add(new HierarchicalTiledConnection(this, n, target));
	}

	private void addLevel1BidirectionalConnection (HierarchicalTiledNode n, int xOffset, int yOffset) {
		HierarchicalTiledNode target = getNode(n.x + xOffset, n.y + yOffset);
		n.getConnections().add(new HierarchicalTiledConnection(this, n, target));
		target.getConnections().add(new HierarchicalTiledConnection(this, target, n));
	}

	private static final int calculateTotalCapacity () {
		int capacity = 0;
		for (int i = 0; i < LEVELS; i++)
			capacity += sizeX[i] * sizeY[i];
		return capacity;
	}
}
