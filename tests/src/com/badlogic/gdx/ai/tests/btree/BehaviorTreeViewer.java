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
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.OutputChunked;

/** @author davebaol */
public class BehaviorTreeViewer<E> extends Table {

	private static final int SUSPENDED = 0;
	private static final int RUNNING = 1;
	private static final int STEP = 2;

	private static String LABEL_STEP = "Step: ";

	private BehaviorTree<E> tree;
	private ObjectMap<Task<E>, TaskNode> taskNodes;
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

	public BehaviorTreeViewer (BehaviorTree<E> tree, Skin skin) {
		this(tree, true, skin);
	}

	public BehaviorTreeViewer (BehaviorTree<E> tree, boolean loadAndSave, Skin skin) {
		super(skin);
		this.tree = tree;
		step = 0;
		taskNodes = new ObjectMap<Task<E>, TaskNode>();
		tree.addListener(new BehaviorTree.Listener<E>() {
			@Override
			public void statusUpdated (Task<E> task, Task.Status previousStatus) {
				TaskNode tn = taskNodes.get(task);
				tn.updateStatus(previousStatus, step);
			}

			@Override
			public void childAdded (Task<E> task, int index) {
				TaskNode parentNode = taskNodes.get(task);
				Task<E> child = task.getChild(index);
				addToTree(displayTree, parentNode, child, null, 0);
				displayTree.expandAll();
			}
		});
		KryoUtils.initKryo();

		treeStatus = SUSPENDED;

		runDelaySlider = new Slider(0, 5, 0.01f, false, skin);
		runDelaySlider.setValue(.5f);

		runButton = new TextButton("Run", skin);
		runButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (treeStatus == SUSPENDED) {
					treeStatus = RUNNING;
					delay = runDelaySlider.getValue(); // this makes it start immediately
					runButton.setText("Suspend");
					stepButton.setDisabled(true);
					if (saveButton != null) saveButton.setDisabled(true);
					if (loadButton != null) loadButton.setDisabled(true);
				} else {
					treeStatus = SUSPENDED;
					runButton.setText("Run");
					stepButton.setDisabled(false);
					if (saveButton != null) saveButton.setDisabled(false);
					if (loadButton != null) loadButton.setDisabled(!saved);
				}
			}
		});

		stepButton = new TextButton("Step", skin);
		stepButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				treeStatus = STEP;
			}
		});

		if (loadAndSave) {
			loadButton = new TextButton("Load", skin);
			loadButton.setDisabled(true);
			loadButton.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					load();
				}
			});

			saveButton = new TextButton("Save", skin);
			saveButton.addListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					save();
					saved = true;
					loadButton.setDisabled(false);
				}
			});
		}

		stepLabel = new Label(new StringBuilder(LABEL_STEP + step), skin);

		this.row().height(20).fillX();
		this.add(runDelaySlider);
		this.add(runButton);
		this.add(stepButton);
		if (loadAndSave) {
			this.add(saveButton);
			this.add(loadButton);
		}
		this.add(stepLabel);
		this.row();
		displayTree = new Tree(skin);

		rebuildDisplayTree();

		this.add(displayTree).colspan(loadAndSave ? 6 : 4).grow();
	}

	public BehaviorTree<E> getBehaviorTree () {
		return tree;
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
		Array<BehaviorTree.Listener<E>> listeners = tree.listeners;
		tree.listeners = null;

		IntArray taskSteps = new IntArray();
		fill(taskSteps, (TaskNode)displayTree.getNodes().get(0));
		KryoUtils.save(new SaveObject<E>(tree, step, taskSteps));

		tree.listeners = listeners;

	}

	public void load () {
		@SuppressWarnings("unchecked")
		SaveObject<E> saveObject = KryoUtils.load(SaveObject.class);
		BehaviorTree<E> oldTree = tree;
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
		Task<E> root = tree.getChild(0);
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
				view.status.setText(status == Task.Status.FRESH ? "" : status.name());
			}
		}

		public boolean hasJustRun () {
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
				status.setColor(taskNode.hasJustRun() ? Color.YELLOW : Color.GRAY);
			}
		}
	}

	private int addToTree (Tree displayTree, TaskNode parentNode, Task<E> task, IntArray taskSteps, int taskStepIndex) {
		TaskNode node = new TaskNode(task, this, taskSteps == null ? step - 1 : taskSteps.get(taskStepIndex), getSkin());
		taskNodes.put(task, node);
		if (parentNode == null) {
			displayTree.add(node);
		} else {
			parentNode.add(node);
		}
		taskStepIndex++;
		for (int i = 0; i < task.getChildCount(); i++) {
			Task<E> child = task.getChild(i);
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
				kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
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
