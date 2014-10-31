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
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder.Metrics;
import com.badlogic.gdx.ai.tests.PathFinderTests;
import com.badlogic.gdx.ai.tests.pfa.PathFinderTestBase;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledGraph;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledManhattanDistance;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledNode;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledRaycastCollisionDetector;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledSmoothableGraphPath;
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

/** This test shows how the {@link IndexedAStarPathFinder} can be used on a tiled map with no diagonal movement. It also shows how
 * to use a {@link PathSmoother} on the found path to reduce the zigzag.
 * 
 * @author davebaol */
public class TiledAStarTest extends PathFinderTestBase {

	final static float width = 8; // 5; // 10;

	ShapeRenderer renderer;
	Vector3 tmpUnprojection = new Vector3();

	int lastScreenX;
	int lastScreenY;
	int lastEndTileX;
	int lastEndTileY;
	int startTileX;
	int startTileY;

	TiledGraph worldMap;
	TiledSmoothableGraphPath path;
	TiledManhattanDistance heuristic;
	IndexedAStarPathFinder<TiledNode> pathfinder;
	PathSmoother<TiledNode, Vector2> pathSmoother;

	boolean smooth = false;

	CheckBox checkDiagonal;
	CheckBox checkSmooth;
	CheckBox checkMetrics;

	public TiledAStarTest (PathFinderTests container) {
		super(container, "Tiled A*");
	}

	@Override
	public void create (Table table) {
		lastEndTileX = -1;
		lastEndTileY = -1;
		startTileX = 1;
		startTileY = 1;

		worldMap = new TiledGraph();
		path = new TiledSmoothableGraphPath();
		heuristic = new TiledManhattanDistance();
		pathfinder = new IndexedAStarPathFinder<TiledNode>(worldMap, true);
		pathSmoother = new PathSmoother<TiledNode, Vector2>(new TiledRaycastCollisionDetector(worldMap));

		renderer = new ShapeRenderer();
		inputProcessor = new TiledAStarInputProcessor(this);

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
		checkMetrics.setChecked(pathfinder.metrics != null);
		checkMetrics.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				pathfinder.metrics = checkBox.isChecked() ? new Metrics() : null;
				updatePath(true);
			}
		});
		detailTable.add(checkMetrics);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		renderer.begin(ShapeType.Filled);
		for (int x = 0; x < TiledGraph.sizeX; x++) {
			for (int y = 0; y < TiledGraph.sizeY; y++) {
				switch (worldMap.getNode(x, y).type) {
				case TiledNode.TILE_FLOOR:
					renderer.setColor(Color.WHITE);
					break;
				case TiledNode.TILE_WALL:
					renderer.setColor(Color.GRAY);
					break;
				default:
					renderer.setColor(Color.BLACK);
					break;
				}
				renderer.rect(x * width, y * width, width, width);
			}
		}

		renderer.setColor(Color.RED);
		int nodeCount = path.getCount();
		for (int i = 0; i < nodeCount; i++) {
			TiledNode node = path.nodes.get(i);
			renderer.rect(node.x * width, node.y * width, width, width);
		}
		if (smooth) {
			renderer.end();
			renderer.begin(ShapeType.Line);
			float hw = width / 2f;
			if (nodeCount > 0) {
				TiledNode prevNode = path.nodes.get(0);
				for (int i = 1; i < nodeCount; i++) {
					TiledNode node = path.nodes.get(i);
					renderer.line(node.x * width + hw, node.y * width + hw, prevNode.x * width + hw, prevNode.y * width + hw);
					prevNode = node;
				}
			}
		}
		renderer.end();
	}

	@Override
	public void dispose () {
		renderer.dispose();

		worldMap = null;
		path = null;
		heuristic = null;
		pathfinder = null;
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
			TiledNode startNode = worldMap.getNode(startTileX, startTileY);
			TiledNode endNode = worldMap.getNode(tileX, tileY);
			if (forceUpdate || endNode.type == TiledNode.TILE_FLOOR) {
				if (endNode.type == TiledNode.TILE_FLOOR) {
					lastEndTileX = tileX;
					lastEndTileY = tileY;
				} else {
					endNode = worldMap.getNode(lastEndTileX, lastEndTileY);
				}
				path.clear();
				worldMap.startNode = startNode;
				long startTime = nanoTime();
				pathfinder.searchNodePath(startNode, endNode, heuristic, path);
				if (pathfinder.metrics != null) {
					float elapsed = (TimeUtils.nanoTime() - startTime) / 1000000f;
					System.out.println("----------------- Indexed A* Path Finder Metrics -----------------");
					System.out.println("Visited nodes................... = " + pathfinder.metrics.visitedNodes);
					System.out.println("Open list additions............. = " + pathfinder.metrics.openListAdditions);
					System.out.println("Open list peak.................. = " + pathfinder.metrics.openListPeak);
					System.out.println("Path finding elapsed time (ms).. = " + elapsed);
				}
				if (smooth) {
					startTime = nanoTime();
					pathSmoother.smoothPath(path);
					if (pathfinder.metrics != null) {
						float elapsed = (TimeUtils.nanoTime() - startTime) / 1000000f;
						System.out.println("Path smoothing elapsed time (ms) = " + elapsed);
					}
				}
			}
		}
	}

	private long nanoTime () {
		return pathfinder.metrics == null ? 0 : TimeUtils.nanoTime();
	}

	/** An {@link InputProcessor} that allows you to define a path to find.
	 * 
	 * @autor davebaol */
	static class TiledAStarInputProcessor extends InputAdapter {
		TiledAStarTest test;

		public TiledAStarInputProcessor (TiledAStarTest test) {
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
			TiledNode startNode = test.worldMap.getNode(tileX, tileY);
			if (startNode.type == TiledNode.TILE_FLOOR) {
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
