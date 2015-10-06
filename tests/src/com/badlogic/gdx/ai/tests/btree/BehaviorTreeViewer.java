/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.ai.tests.btree;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.OutputChunked;

/** @author davebaol */
public class BehaviorTreeViewer<T> extends Table {

	private static final int SUSPENDED = 0;
	private static final int RUNNING = 1;
	private static final int STEP = 2;

	private static String LABEL_STEP = "Step: ";

	private BehaviorTree<T> tree;
	private ObjectMap<Task<T>, TaskNode> taskNodes;
	private int step;

	private Label stepLabel;
	private Slider runDelaySlider;
	private TextButton runButton;
	private TextButton stepButton;
	private TextButton saveButton;
	private TextButton loadButton;
	private Tree displayTree;

	private int treeStatus;
	boolean saved;

	public BehaviorTreeViewer (BehaviorTree<T> tree, Skin skin) {
		super(skin);
		this.tree = tree;
		step = 0;
		taskNodes = new ObjectMap<Task<T>, TaskNode>();
		tree.addListener(new BehaviorTree.Listener<T>() {
			@Override
			public void statusUpdated (Task<T> task, Task.Status previousStatus) {
				TaskNode tn = taskNodes.get(task);
				tn.updateStatus(previousStatus, step);
			}

			@Override
			public void childAdded (Task<T> task, int index) {
				TaskNode parentNode = taskNodes.get(task);
				Task<T> child = task.getChild(index);
				addToTree(displayTree, parentNode, child, null, 0);
				displayTree.expandAll();
			}
		});
		KryoUtils.initKryo();

		treeStatus = SUSPENDED;

		runDelaySlider = new Slider(0, 5, 0.01f, false, skin);
		runDelaySlider.setValue(.5f);

		runButton = new TextButton("Run", skin);

		stepButton = new TextButton("Step", skin);

		loadButton = new TextButton("Load", skin);
		loadButton.setDisabled(true);

		saveButton = new TextButton("Save", skin);

		stepLabel = new Label(new StringBuilder(LABEL_STEP + step), skin);

		this.row().height(20).fillX();
		this.add(runDelaySlider);
		this.add(runButton);
		this.add(stepButton);
		this.add(saveButton);
		this.add(loadButton);
		this.add(stepLabel);
		this.row();
		displayTree = new Tree(skin);

		rebuildDisplayTree();

		this.add(displayTree).colspan(6).grow();

		saveButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				save();
				saved = true;
				loadButton.setDisabled(false);
			}
		});

		loadButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				load();
			}
		});

		stepButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				treeStatus = STEP; // step();
			}
		});

		runButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (treeStatus == SUSPENDED) {
					treeStatus = RUNNING;
					delay = runDelaySlider.getValue(); // this makes it start immediately
					runButton.setText("Suspend");
					stepButton.setDisabled(true);
					saveButton.setDisabled(true);
					loadButton.setDisabled(true);
				} else {
					treeStatus = SUSPENDED;
					runButton.setText("Run");
					stepButton.setDisabled(false);
					saveButton.setDisabled(false);
					loadButton.setDisabled(!saved);
				}
			}
		});
	}

	public void step () {
		step++;

		Gdx.app.log("BTV(" + getName() + ")", "Step " + step);

		updateStepLabel();

		tree.step();
	}

	private void updateStepLabel () {
		StringBuilder sb = stepLabel.getText();
		sb.setLength(LABEL_STEP.length());
		sb.append(step);
		stepLabel.invalidateHierarchy();
	}

	public void save () {
		Array<BehaviorTree.Listener<T>> listeners = tree.listeners;
		tree.listeners = null;

		IntArray taskSteps = new IntArray();
		fill(taskSteps, (TaskNode)displayTree.getNodes().get(0));
		KryoUtils.save(new SaveObject<T>(tree, step, taskSteps));

		tree.listeners = listeners;

	}

	public void load () {
		@SuppressWarnings("unchecked")
		SaveObject<T> saveObject = KryoUtils.load(SaveObject.class);
		BehaviorTree<T> oldTree = tree;
		tree = saveObject.tree;
		tree.listeners = oldTree.listeners;

		step = saveObject.step;
		updateStepLabel();
		rebuildDisplayTree(saveObject.taskSteps);
	}

	private void fill (IntArray taskSteps, TaskNode taskNode) {
		taskSteps.add(taskNode.step);
		for (Node child : taskNode.getChildren()) {
			fill(taskSteps, (TaskNode)child);
		}
	}

	private float delay;

	@Override
	public void act (float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (treeStatus == RUNNING) {
			delay += delta;
			if (delay > runDelaySlider.getValue()) {
				delay = 0;
				step();
			}
		} else if (treeStatus == STEP) {
			step();
			treeStatus = SUSPENDED;
		}
		super.act(delta);
	}

	private void rebuildDisplayTree () {
		rebuildDisplayTree(null);
	}

	private void rebuildDisplayTree (IntArray taskSteps) {
		displayTree.clear();
		taskNodes.clear();
		Task<T> root = tree.getChild(0);
		addToTree(displayTree, null, root, taskSteps, 0);
		displayTree.expandAll();
	}

	private static class TaskNode extends Tree.Node {

		public Task<?> task;
		public BehaviorTreeViewer<?> btViewer;
		public int step;

		public TaskNode (Task<?> task, BehaviorTreeViewer<?> btViewer, int step, Skin skin) {
			super(new View(task, skin));
			((View)getActor()).taskNode = this;
			this.task = task;
			this.btViewer = btViewer;
			this.step = step;
			updateStatus(null, step);
		}

		private void updateStatus (Task.Status previousStatus, int step) {
			this.step = step;
			Task.Status status = task.getStatus();
			if (status != previousStatus) {
				View view = (View)getActor();
				view.status.setText(status != null ? status.name() : "");
			}
		}

		public boolean isFresh () {
			return step == btViewer.step;
		}

		private static class View extends Table {
			Label name;
			Label status;
			TaskNode taskNode;

			public View (Task<?> task, Skin skin) {
				super(skin);
				this.name = new Label(task.getClass().getSimpleName(), skin);
				this.status = new Label("", skin);
				add(name);
				add(status).padLeft(10);
			}

			@Override
			public void act (float delta) {
				status.setColor(taskNode.isFresh() ? Color.YELLOW : Color.GRAY);
			}
		}
	}

	private int addToTree (Tree displayTree, TaskNode parentNode, Task<T> task, IntArray taskSteps, int taskStepIndex) {
		TaskNode node = new TaskNode(task, this, taskSteps == null ? step - 1 : taskSteps.get(taskStepIndex), getSkin());
		taskNodes.put(task, node);
		if (parentNode == null) {
			displayTree.add(node);
		} else {
			parentNode.add(node);
		}
		taskStepIndex++;
		for (int i = 0; i < task.getChildCount(); i++) {
			Task<T> child = task.getChild(i);
			taskStepIndex = addToTree(displayTree, node, child, taskSteps, taskStepIndex);
		}
		return taskStepIndex;
	}

	static class SaveObject<T> {
		BehaviorTree<T> tree;
		int step;
		IntArray taskSteps;

		SaveObject (BehaviorTree<T> tree, int step, IntArray taskSteps) {
			this.tree = tree;
			this.step = step;
			this.taskSteps = taskSteps;
		}
	}

	public static final class KryoUtils {

		private static Kryo kryo;
		private static final OutputChunked output = new OutputChunked();

		private KryoUtils () {
		}

		public static void initKryo () {
			if (kryo == null) {
				kryo = new Kryo();
				kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
				kryo.register(BehaviorTree.class);
				// FieldSerializer fieldSerializer = new FieldSerializer(kryo, BehaviorTree.class);
				// fieldSerializer.removeField("object");
				// kryo.register(BehaviorTree.class, fieldSerializer);
			}
		}

		public static void save (Object obj) {
			output.clear();
			kryo.writeObjectOrNull(output, obj, obj.getClass());
			// System.out.println(output.total());
		}

		public static <T> T load (Class<T> type) {
			Input input = new ByteBufferInput(output.getBuffer());
			return kryo.readObjectOrNull(input, type);
		}
	}
}
