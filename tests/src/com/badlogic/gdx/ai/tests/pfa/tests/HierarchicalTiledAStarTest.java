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

package com.badlogic.gdx.ai.tests.pfa.tests;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.pfa.HierarchicalPathFinder;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.tests.PathFinderTests;
import com.badlogic.gdx.ai.tests.pfa.PathFinderTestBase;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledManhattanDistance;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledRaycastCollisionDetector;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledSmoothableGraphPath;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.flat.FlatTiledNode;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.hrchy.HierarchicalTiledGraph;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.hrchy.HierarchicalTiledNode;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.TimeUtils;

/** This test shows how the {@link HierarchicalPathFinder} can be used on a hierarchical tiled map with two levels. It also shows
 * how to use a {@link PathSmoother} on the found path to reduce the zigzag.
 * 
 * @author davebaol */
public class HierarchicalTiledAStarTest extends PathFinderTestBase {

	final static float width = 8; // 5; // 10;

	final static int NUM_PATHS = 10;

	ShapeRenderer renderer;
	Vector3 tmpUnprojection = new Vector3();

	int lastScreenX;
	int lastScreenY;
	int lastEndTileX;
	int lastEndTileY;
	int startTileX;
	int startTileY;

	HierarchicalTiledGraph worldMap;
	TiledSmoothableGraphPath<HierarchicalTiledNode>[] paths;
	TiledManhattanDistance<HierarchicalTiledNode> heuristic;
	HierarchicalPathFinder<HierarchicalTiledNode> pathFinder;
	PathSmoother<HierarchicalTiledNode, Vector2> pathSmoother;

	boolean smooth = false;
	boolean metrics = false;

	CheckBox checkDiagonal;
	CheckBox checkSmooth;
	CheckBox checkMetrics;

	public HierarchicalTiledAStarTest (PathFinderTests container) {
		super(container, "Hierarchical Tiled A*");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create (Table table) {
		lastEndTileX = -1;
		lastEndTileY = -1;
		startTileX = 1;
		startTileY = 1;

		// Create the map
		worldMap = new HierarchicalTiledGraph();
		int roomCount = 100;
		int roomMinSize = 2;
		int roomMaxSize = 8;
		int squashIterations = 100;
		worldMap.init(roomCount, roomMinSize, roomMaxSize, squashIterations);

		paths = (TiledSmoothableGraphPath<HierarchicalTiledNode>[])new TiledSmoothableGraphPath[NUM_PATHS];
		for (int i = 0; i < NUM_PATHS; i++) {
			paths[i] = new TiledSmoothableGraphPath<HierarchicalTiledNode>();
		}
		heuristic = new TiledManhattanDistance<HierarchicalTiledNode>();
		IndexedAStarPathFinder<HierarchicalTiledNode> levelPathFinder = new IndexedAStarPathFinder<HierarchicalTiledNode>(worldMap,
			true);
		pathFinder = new HierarchicalPathFinder<HierarchicalTiledNode>(worldMap, levelPathFinder);
		pathSmoother = new PathSmoother<HierarchicalTiledNode, Vector2>(new TiledRaycastCollisionDetector<HierarchicalTiledNode>(
			worldMap));

		renderer = new ShapeRenderer();
		inputProcessor = new TiledHierarchicalAStarInputProcessor(this);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		checkSmooth = new CheckBox("[RED]S[]mooth Path", container.skin);
		checkSmooth.setChecked(smooth);
		checkSmooth.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				smooth = checkBox.isChecked();
				updatePath(true);
			}
		});
		detailTable.add(checkSmooth);

		detailTable.row();
		checkDiagonal = new CheckBox("Prefer [RED]D[]iagonal", container.skin);
		checkDiagonal.setChecked(worldMap.diagonal);
		checkDiagonal.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				worldMap.diagonal = checkBox.isChecked();
				updatePath(true);
			}
		});
		detailTable.add(checkDiagonal);

		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		checkMetrics = new CheckBox("Calculate [RED]M[]etrics", container.skin);
		checkMetrics.setChecked(metrics);
		checkMetrics.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				metrics = checkBox.isChecked();
				updatePath(true);
			}
		});
		detailTable.add(checkMetrics);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		renderer.begin(ShapeType.Filled);
		int level = 0;
		worldMap.setLevel(level);
		int xMax = HierarchicalTiledGraph.sizeX[level];
		int yMax = HierarchicalTiledGraph.sizeY[level];
		for (int x = 0; x < xMax; x++) {
			for (int y = 0; y < yMax; y++) {
				switch (worldMap.getNode(x, y).type) {
				case FlatTiledNode.TILE_FLOOR:
					renderer.setColor(Color.WHITE);
					break;
				case FlatTiledNode.TILE_WALL:
					renderer.setColor(Color.GRAY);
					break;
				default:
					renderer.setColor(Color.BLACK);
					break;
				}
				renderer.rect(x * width, y * width, width, width);
			}
		}

		// Draw path nodes
		for (int p = 0; p < NUM_PATHS; p++) {
			TiledSmoothableGraphPath<HierarchicalTiledNode> path = paths[p];
			int nodeCount = path.getCount();
			if (nodeCount == 0) break;
			renderer.setColor(p % 2 == 0 ? Color.RED : Color.ORANGE);
			for (int i = 0; i < nodeCount; i++) {
				HierarchicalTiledNode node = path.nodes.get(i);
				renderer.rect(node.x * width, node.y * width, width, width);
			}
		}

		if (smooth) {
			renderer.end();
			renderer.begin(ShapeType.Line);
			// Draw lines between path nodes
			for (int p = 0; p < NUM_PATHS; p++) {
				TiledSmoothableGraphPath<HierarchicalTiledNode> path = paths[p];
				int nodeCount = path.getCount();
				if (nodeCount > 0) {
					float hw = width / 2f;
					HierarchicalTiledNode prevNode = path.nodes.get(0);
					renderer.setColor(p % 2 == 0 ? Color.RED : Color.ORANGE);
					for (int i = 1; i < nodeCount; i++) {
						HierarchicalTiledNode node = path.nodes.get(i);
						renderer.line(node.x * width + hw, node.y * width + hw, prevNode.x * width + hw, prevNode.y * width + hw);
						prevNode = node;
					}
				}
			}
		}

		// Draw the lower level node of the buildings (usually a tile close to the center of mass)
		level = 1;
		worldMap.setLevel(level);
		xMax = HierarchicalTiledGraph.sizeX[level];
		yMax = HierarchicalTiledGraph.sizeY[level];
		renderer.end();
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.MAROON);
		float hw = width * .5f;
		for (int x = 0; x < xMax; x++) {
			for (int y = 0; y < yMax; y++) {
				HierarchicalTiledNode lln = worldMap.getNode(x, y).getLowerLevelNode();
				renderer.circle(lln.x * width + hw, lln.y * width + hw, hw);
			}
		}

		renderer.end();
	}

	@Override
	public void dispose () {
		renderer.dispose();

		worldMap = null;
		paths = null;
		heuristic = null;
		pathFinder = null;
		pathSmoother = null;
	}

	public Camera getCamera () {
		return container.stage.getViewport().getCamera();
	}

	private void updatePath (boolean forceUpdate) {
		getCamera().unproject(tmpUnprojection.set(lastScreenX, lastScreenY, 0));
		int tileX = (int)(tmpUnprojection.x / width);
		int tileY = (int)(tmpUnprojection.y / width);
		if (forceUpdate || tileX != lastEndTileX || tileY != lastEndTileY) {
			worldMap.setLevel(0);
			HierarchicalTiledNode startNode = worldMap.getNode(startTileX, startTileY);
			HierarchicalTiledNode endNode = worldMap.getNode(tileX, tileY);
			if (forceUpdate || endNode.type == FlatTiledNode.TILE_FLOOR) {
				if (endNode.type == FlatTiledNode.TILE_FLOOR) {
					lastEndTileX = tileX;
					lastEndTileY = tileY;
				} else {
					endNode = worldMap.getNode(lastEndTileX, lastEndTileY);
				}

				if (metrics)
					System.out.println("------------ Hierarchical Indexed A* Path Finder Metrics ------------");

				OUTER:
				for (int p = 0; p < NUM_PATHS; p++) {
					TiledSmoothableGraphPath<HierarchicalTiledNode> path = paths[p];
					path.clear();
					worldMap.startNode = startNode;
					long startTime = nanoTime();
					boolean found = pathFinder.searchNodePath(startNode, endNode, heuristic, path);
					if (metrics) {
						float elapsed = (TimeUtils.nanoTime() - startTime) / 1000000f;
						System.out.println("<<<Subpath " + p + ">>>");
//						System.out.println("Visited nodes................... = " + pathfinder.metrics.visitedNodes);
//						System.out.println("Open list additions............. = " + pathfinder.metrics.openListAdditions);
//						System.out.println("Open list peak.................. = " + pathfinder.metrics.openListPeak);
						System.out.println("Path finding elapsed time (ms).. = " + elapsed);
					}
					
					if (!found) break;

					HierarchicalTiledNode n = worldMap.convertNodeBetweenLevels(0, startNode, 1);
					int nodeCount = path.getCount();
					if (nodeCount > 0 && endNode == path.get(nodeCount - 1)) {
						if (smooth) {
							startTime = nanoTime();
							pathSmoother.smoothPath(path);
							if (metrics) {
								float elapsed = (TimeUtils.nanoTime() - startTime) / 1000000f;
								System.out.println("Path smoothing elapsed time (ms) = " + elapsed);
							}
						}
						paths[p + 1].clear();
						break;
					}
					for (int i = 1; i < nodeCount; i++) {
						if (worldMap.convertNodeBetweenLevels(0, path.get(i), 1) != n) {
							startNode = path.get(i);
							path.truncatePath(i);
							if (smooth) {
								startTime = nanoTime();
								pathSmoother.smoothPath(path);
								if (metrics) {
									float elapsed = (TimeUtils.nanoTime() - startTime) / 1000000f;
									System.out.println("Path smoothing elapsed time (ms) = " + elapsed);
								}
							}
							continue OUTER;
						}
					}
				}
			}
		}
	}

	private long nanoTime () {
		return metrics? TimeUtils.nanoTime() : 0;
	}

	/** An {@link InputProcessor} that allows you to define a path to find.
	 * 
	 * @autor davebaol */
	static class TiledHierarchicalAStarInputProcessor extends InputAdapter {
		HierarchicalTiledAStarTest test;

		public TiledHierarchicalAStarInputProcessor (HierarchicalTiledAStarTest test) {
			this.test = test;
		}

		@Override
		public boolean keyTyped (char character) {
			switch (character) {
			case 'm':
			case 'M':
				test.checkMetrics.toggle();
				break;
			case 'd':
			case 'D':
				test.checkDiagonal.toggle();
				break;
			case 's':
			case 'S':
				test.checkSmooth.toggle();
				break;
			}
			return true;
		}

		@Override
		public boolean touchUp (int screenX, int screenY, int pointer, int button) {
			test.getCamera().unproject(test.tmpUnprojection.set(screenX, screenY, 0));
			int tileX = (int)(test.tmpUnprojection.x / width);
			int tileY = (int)(test.tmpUnprojection.y / width);
			test.worldMap.setLevel(0);
			HierarchicalTiledNode startNode = test.worldMap.getNode(tileX, tileY);
			if (startNode.type == FlatTiledNode.TILE_FLOOR) {
				test.startTileX = tileX;
				test.startTileY = tileY;
				test.updatePath(true);
			}
			return true;
		}

		@Override
		public boolean mouseMoved (int screenX, int screenY) {
			test.lastScreenX = screenX;
			test.lastScreenY = screenY;
			test.updatePath(false);
			return true;
		}
	}
}
