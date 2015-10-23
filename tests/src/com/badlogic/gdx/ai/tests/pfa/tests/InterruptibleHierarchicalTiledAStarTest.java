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
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.pfa.HierarchicalPathFinder;
import com.badlogic.gdx.ai.pfa.PathFinderQueue;
import com.badlogic.gdx.ai.pfa.PathFinderRequest;
import com.badlogic.gdx.ai.pfa.PathFinderRequestControl;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.PathSmootherRequest;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.sched.LoadBalancingScheduler;
import com.badlogic.gdx.ai.tests.PathFinderTests;
import com.badlogic.gdx.ai.tests.pfa.PathFinderTestBase;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledManhattanDistance;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledNode;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledRaycastCollisionDetector;
import com.badlogic.gdx.ai.tests.pfa.tests.tiled.TiledSmoothableGraphPath;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

/** This test shows interruptible hierarchical pathfinding through a {@link PathFinderQueue}.
 * 
 * @author davebaol */
public class InterruptibleHierarchicalTiledAStarTest extends PathFinderTestBase implements Telegraph {

	final static float width = 8; // 5; // 10;

	final static int PF_REQUEST = 1;
	final static int PF_RESPONSE = 2;

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
	int numPaths;
	TiledManhattanDistance<HierarchicalTiledNode> heuristic;
	HierarchicalPathFinder<HierarchicalTiledNode> pathFinder;
	PathSmoother<HierarchicalTiledNode, Vector2> pathSmoother;
	PathSmootherRequest<HierarchicalTiledNode, Vector2> pathSmootherRequest;

	Pool<MyPathFinderRequest> requestPool;

	LoadBalancingScheduler scheduler;

	boolean smooth = false;
	boolean metrics = false;

	CheckBox checkDiagonal;
	CheckBox checkSmooth;
	CheckBox checkMetrics;
	Slider sliderMillisAvailablePerFrame;

	public InterruptibleHierarchicalTiledAStarTest (PathFinderTests container) {
		super(container, "Interruptible Hierarchical Tiled A*");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create () {
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
		numPaths = 0;
		heuristic = new TiledManhattanDistance<HierarchicalTiledNode>();
		IndexedAStarPathFinder<HierarchicalTiledNode> levelPathFinder = new IndexedAStarPathFinder<HierarchicalTiledNode>(worldMap,
			true);
		pathFinder = new HierarchicalPathFinder<HierarchicalTiledNode>(worldMap, levelPathFinder);
		pathSmoother = new PathSmoother<HierarchicalTiledNode, Vector2>(new TiledRaycastCollisionDetector<HierarchicalTiledNode>(
			worldMap));
		pathSmootherRequest = new PathSmootherRequest<HierarchicalTiledNode, Vector2>();

		requestPool = new Pool<MyPathFinderRequest>() {
			@Override
			protected MyPathFinderRequest newObject () {
				return new MyPathFinderRequest();
			}
		};
		PathFinderQueue<HierarchicalTiledNode> pathFinderQueue = new PathFinderQueue<HierarchicalTiledNode>(pathFinder);
		MessageManager.getInstance().addListener(pathFinderQueue, PF_REQUEST);

		scheduler = new LoadBalancingScheduler(100);
		scheduler.add(pathFinderQueue, 1, 0);

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
		detailTable.row();
		addSeparator(detailTable);

		detailTable.row();
		sliderMillisAvailablePerFrame = new Slider(0.1f, 40f, 0.1f, false, container.skin);
		sliderMillisAvailablePerFrame.setValue(16);
		final Label labelMillisAvailablePerFrame = new Label("Millis Available per Frame [["
			+ sliderMillisAvailablePerFrame.getValue() + "]", container.skin);
		detailTable.add(labelMillisAvailablePerFrame);
		detailTable.row();
		sliderMillisAvailablePerFrame.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				labelMillisAvailablePerFrame.setText("Millis Available per Frame [[" + sliderMillisAvailablePerFrame.getValue() + "]");
			}
		});
		Table sliderMapfTable = new Table();
		sliderMapfTable.add(new Label("[RED]-[]  ", container.skin));
		sliderMapfTable.add(sliderMillisAvailablePerFrame);
		sliderMapfTable.add(new Label("  [RED]+[]", container.skin));
		detailTable.add(sliderMapfTable);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		long timeToRun = (long)(sliderMillisAvailablePerFrame.getValue() * 1000000f);
		scheduler.run(timeToRun);

		renderer.begin(ShapeType.Filled);
		int level = 0;
		worldMap.setLevel(level);
		int xMax = HierarchicalTiledGraph.sizeX[level];
		int yMax = HierarchicalTiledGraph.sizeY[level];
		for (int x = 0; x < xMax; x++) {
			for (int y = 0; y < yMax; y++) {
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

		// Draw path nodes
		for (int p = 0; p < numPaths; p++) {
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
			for (int p = 0; p < numPaths; p++) {
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
		scheduler = null;

		MessageManager.getInstance().clear();
	}

	public Camera getCamera () {
		return container.stage.getViewport().getCamera();
	}

	@Override
	public boolean handleMessage (Telegram telegram) {
		switch (telegram.message) {
		case PF_RESPONSE: // PathFinderQueue will call us directly, no need to register for this message
			if (PathFinderRequestControl.DEBUG) {
				@SuppressWarnings("unchecked")
				PathFinderQueue<HierarchicalTiledNode> pfQueue = (PathFinderQueue<HierarchicalTiledNode>)telegram.sender;
				System.out.println("pfQueue.size = " + pfQueue.size());
			}
			MyPathFinderRequest pfr = (MyPathFinderRequest)telegram.extraInfo;
			TiledSmoothableGraphPath<HierarchicalTiledNode> path = paths[pfr.pathIndex];
			int n = path.getCount();
			if (n > 0 && pfr.pathFound && pfr.endNode != path.get(n - 1)) {
				pfr.startNode = path.get(n - 1);
				if(pfr.pathIndex + 1 < paths.length) {
					pfr.pathIndex++;
				}
				pfr.resultPath = paths[pfr.pathIndex];
				pfr.changeStatus(PathFinderRequest.SEARCH_NEW);
				numPaths = pfr.pathIndex;
			} else {
				requestPool.free(pfr);
				numPaths = pfr.pathIndex + 1;
			}
			break;
		}
		return true;
	}

	private void updatePath (boolean forceUpdate) {
		getCamera().unproject(tmpUnprojection.set(lastScreenX, lastScreenY, 0));
		int tileX = (int)(tmpUnprojection.x / width);
		int tileY = (int)(tmpUnprojection.y / width);
		if (forceUpdate || tileX != lastEndTileX || tileY != lastEndTileY) {
			worldMap.setLevel(0);
			HierarchicalTiledNode startNode = worldMap.getNode(startTileX, startTileY);
			HierarchicalTiledNode endNode = worldMap.getNode(tileX, tileY);
			if (forceUpdate || endNode.type == TiledNode.TILE_FLOOR) {
				if (endNode.type == TiledNode.TILE_FLOOR) {
					lastEndTileX = tileX;
					lastEndTileY = tileY;
				} else {
					endNode = worldMap.getNode(lastEndTileX, lastEndTileY);
				}

				if (metrics)
					if (PathFinderRequestControl.DEBUG)
						System.out.println("------------ Hierarchical Indexed A* Path Finder Metrics ------------");

				requestNewPathFinding(startNode, endNode, 0);
			}
		}
	}

	private void requestNewPathFinding (HierarchicalTiledNode startNode, HierarchicalTiledNode endNode, int pathIndex) {
		TiledSmoothableGraphPath<HierarchicalTiledNode> path = paths[pathIndex];

		MyPathFinderRequest pfRequest = requestPool.obtain();
		pfRequest.startNode = startNode;
		pfRequest.endNode = endNode;
		pfRequest.heuristic = heuristic;
		pfRequest.resultPath = path;
		pfRequest.pathIndex = pathIndex;
		pfRequest.responseMessageCode = PF_RESPONSE;
		MessageManager.getInstance().dispatchMessage(this, PF_REQUEST, pfRequest);
	}

	/** An {@link InputProcessor} that allows you to define a path to find.
	 * 
	 * @autor davebaol */
	static class TiledHierarchicalAStarInputProcessor extends InputAdapter {
		InterruptibleHierarchicalTiledAStarTest test;

		public TiledHierarchicalAStarInputProcessor (InterruptibleHierarchicalTiledAStarTest test) {
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
			case '-':
				test.sliderMillisAvailablePerFrame.setValue(test.sliderMillisAvailablePerFrame.getValue()
					- test.sliderMillisAvailablePerFrame.getStepSize());
				break;
			case '+':
				test.sliderMillisAvailablePerFrame.setValue(test.sliderMillisAvailablePerFrame.getValue()
					+ test.sliderMillisAvailablePerFrame.getStepSize());
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

	class MyPathFinderRequest extends PathFinderRequest<HierarchicalTiledNode> implements Poolable {
		boolean smoothFinished;
		int pathIndex;

		public MyPathFinderRequest () {
		}

		@Override
		public boolean initializeSearch (long timeToRun) {
			resultPath.clear();
			worldMap.startNode = startNode;
			return true;
		}

		@Override
		public boolean finalizeSearch (long timeToRun) {
			if (statusChanged) {
				if (PathFinderRequestControl.DEBUG)
					System.out.println("MyPathFinderRequest.finalizeSearch[" + pathIndex
						+ "]: statusChanged **********************************");
				pathSmootherRequest.refresh(paths[pathIndex]);
				smoothFinished = false;
				if (pathFound) {
					HierarchicalTiledNode l1Start = worldMap.convertNodeBetweenLevels(0, startNode, 1);
					SmoothableGraphPath<HierarchicalTiledNode, Vector2> path = paths[pathIndex];
					int nodeCount = path.getCount();
					if (nodeCount > 0 && endNode != path.get(nodeCount - 1)) {
						for (int i = 1; i < nodeCount; i++) {
							if (worldMap.convertNodeBetweenLevels(0, path.get(i), 1) != l1Start) {
								path.truncatePath(i + 1);
								break;
							}
						}
					}
				}
			}
			if (pathFound) {
				if (PathFinderRequestControl.DEBUG)
					System.out.println("MyPathFinderRequest.finalizeSearch[" + pathIndex
						+ "]: pathFound **********************************");
				if (smooth && !smoothFinished) {
					worldMap.setLevel(0);
					smoothFinished = pathSmoother.smoothPath(pathSmootherRequest, timeToRun);
					if (!smoothFinished) return false;
				}
				numPaths = pathIndex + 1;
			}
			return true;
		}

		@Override
		public void reset () {
			this.startNode = null;
			this.endNode = null;
			this.heuristic = null;
			this.resultPath = null;
			this.client = null;
		}
	}
}
