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

package com.badlogic.gdx.ai.tests;


import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.tests.btree.dog.Dog;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.OutputChunked;

/** @author davebaol */
public class BehaviorTreeViewer extends Game implements Screen {

	public static void main (String[] argv) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
		config.width = 960;
		config.height = 600;
		new LwjglApplication(new BehaviorTreeViewer(), config);
	}

	private static final int SUSPENDED = 0;
	private static final int RUNNING = 1;
	private static final int STEP = 2;

	private static String LABEL_STEP = "Step: ";

	private Stage stage;
	private Skin skin;
	private Label stepLabel;
	private Slider runDelaySlider;
	private TextButton runButton;
	private TextButton stepButton;
	private TextButton saveButton;
	private TextButton loadButton;
	private Tree displayTree;
	private Dog dog;
	private ObjectMap<Task<Dog>, TaskNode> taskNodes;
	private static int step;

	private int treeStatus;
	boolean saved;

	public BehaviorTreeViewer () {
	}

	@Override
	public void create () {
		step = 0;
		taskNodes = new ObjectMap<Task<Dog>, TaskNode>(); 
		BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE);
		BehaviorTree<Dog> tree = parser.parse(Gdx.files.internal("data/dog.tree"), null);
		dog = new Dog("Dog 1", tree);
		tree.addListener(new BehaviorTree.Listener<Dog>() {
			@Override
			public void statusUpdated (Task<Dog> task, Task.Status previousStatus) {
				TaskNode tn = taskNodes.get(task);
				tn.updateStatus(previousStatus, step);
			}
		});
		KryoUtils.initKryo();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		treeStatus = SUSPENDED;

		runDelaySlider = new Slider(0, 5, 0.01f, false, skin);
		runDelaySlider.setValue(.5f);

		runButton = new TextButton("Run", skin);

		stepButton = new TextButton("Step", skin);

		loadButton = new TextButton("Load", skin);
		loadButton.setDisabled(true);

		saveButton = new TextButton("Save", skin);

		stepLabel = new Label(new StringBuilder(LABEL_STEP + step), skin);

		stage = new Stage(new ScreenViewport());

		Table table = new Table();
		table.row().height(20).fillX();
		table.add(runDelaySlider);
		table.add(runButton);
		table.add(stepButton);
		table.add(saveButton);
		table.add(loadButton);
		table.add(stepLabel);
		table.row();
		displayTree = new Tree(skin);

		redrawTree();

		table.add(displayTree).colspan(3).fillX().fillY().expand(true, true);

		stage.addActor(table);
		table.setFillParent(true);

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

		Gdx.input.setInputProcessor(stage);

		setScreen(this);
	}

	public void step () {
		step++;
		
		updateStepLabel();

		dog.getBehaviorTree().step();
	}

	private void updateStepLabel() {
		StringBuilder sb = stepLabel.getText(); 
		sb.setLength(LABEL_STEP.length());
		sb.append(step);
		stepLabel.invalidateHierarchy();
	}

	public void save () {
		Array<BehaviorTree.Listener<Dog>> listeners = dog.getBehaviorTree().listeners;
		dog.getBehaviorTree().listeners = null;
		
		IntArray taskSteps = new IntArray();
		fill(taskSteps, (TaskNode)displayTree.getNodes().get(0));
		KryoUtils.save(new SaveObject<Dog>(dog, step, taskSteps));
		
		dog.getBehaviorTree().listeners = listeners;
		
	}
	
	public void load () {
		@SuppressWarnings("unchecked")
		SaveObject<Dog> saveObject = KryoUtils.load(SaveObject.class);
		Dog oldDog = dog;
		dog = saveObject.entity;
		dog.getBehaviorTree().listeners = oldDog.getBehaviorTree().listeners;

		step = saveObject.step;
		updateStepLabel();
		rebuildDisplayTree(saveObject.taskSteps);
	}

	private void fill(IntArray taskSteps, TaskNode taskNode) {
		taskSteps.add(taskNode.step);
		for (Node child : taskNode.getChildren()) {
			fill(taskSteps, (TaskNode)child);
		}
	}

	@Override
	public void show () {
	}

	private float delay;

	@Override
	public void render (float delta) {
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
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void hide () {
	}

	@Override
	public void dispose () {
		stage.dispose();
	}

	private void redrawTree () {
		rebuildDisplayTree(null);
	}

	private void rebuildDisplayTree (IntArray taskSteps) {
		displayTree.clear();
		taskNodes.clear();
		Task<Dog> root = dog.getBehaviorTree().getChild(0);
		addToTree(displayTree, null, root, taskSteps, 0);
		displayTree.expandAll();
	}

	private static class TaskNode extends Tree.Node {

		public Task<?> task;
		public int step;

		public TaskNode (Task<?> task, int step, Skin skin) {
			super(new View(task, skin));
			((View)getActor()).taskNode = this;
			this.task = task;
			this.step = step;
			updateStatus(null, step);
		}
		
		public void updateStatus (Task.Status previousStatus, int step) {
			this.step = step;
			Task.Status status = task.getStatus();
			if (status != previousStatus) {
				View view = (View)getActor();
				view.status.setText(status != null ? status.name() : "");
			}
		}
		
		private static class View extends Table {
			Label name;
			Label status;
	      TaskNode taskNode;

			public View(Task<?> task, Skin skin) {
				super(skin);
				this.name = new Label(task.getClass().getSimpleName(), skin);
				this.status = new Label("", skin);
				add(name);
				add(status);
			}
			
			@Override
			public void act(float delta) {
				status.setColor(taskNode.step == BehaviorTreeViewer.step? Color.YELLOW : Color.GRAY);
			}
		}
	}

	private void addToTree (Tree displayTree, TaskNode parentNode, Task<Dog> task, IntArray taskSteps, int taskStepIndex) {
		TaskNode node = new TaskNode(task, taskSteps == null ? step - 1 : taskSteps.get(taskStepIndex), skin);
		taskNodes.put(task, node);
		if (parentNode == null) {
			displayTree.add(node);
		} else {
			parentNode.add(node);
		}
		for (int i = 0; i < task.getChildCount(); i++) {
			Task<Dog> child = task.getChild(i);
			addToTree(displayTree, node, child, taskSteps, taskStepIndex + 1);
		}
	}
	
	static class SaveObject<T> {
		T entity;
		int step;
		IntArray taskSteps;
		
		SaveObject(T entity, int step, IntArray taskSteps) {
			this.entity = entity;
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
			kryo = new Kryo();
			kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
			kryo.register(Dog.class);
	// FieldSerializer fieldSerializer = new FieldSerializer(kryo, BehaviorTree.class);
	// fieldSerializer.removeField("object");
	// kryo.register(BehaviorTree.class, fieldSerializer);
		}

		public static void save (Object obj) {
			output.clear();
			kryo.writeObjectOrNull(output, obj, obj.getClass());
			System.out.println(output.total());
		}

		public static <T> T load (Class<T> type) {
			Input input = new ByteBufferInput(output.getBuffer());
			return kryo.readObjectOrNull(input, type);
		}
	}
}
