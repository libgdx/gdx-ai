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

package com.badlogic.gdx.ai.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.tests.utils.GdxAiTest;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.StringBuilder;

/** A simple test to demonstrate timer messages.
 * @author davebaol */
public class MessageTimerTest extends GdxAiTest implements Telegraph {

	public static void main (String[] argv) {
		launch(new MessageTimerTest());
	}

	private static final String LOG_TAG = MessageTimerTest.class.getSimpleName();
	
	Stage stage;
	Label fpsLabel;
	int msgCounter;
	float msgTimeStamp;
	boolean timerEnabled;

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		timerEnabled = true;

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage();

		Stack stack = new Stack();
		stage.addActor(stack);
		stack.setSize(stage.getWidth(), stage.getHeight());

		Table table = new Table(skin);
		stack.add(table);

		fpsLabel = new Label("", skin);
		table.add(fpsLabel);

		table.row();

		TextButton button = new TextButton("Stop timer", skin);
		button.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				timerEnabled = !timerEnabled;
				((TextButton)actor).setText(timerEnabled ? "Stop Timer" : "Start Timer");
				if (timerEnabled) sendMessage(0f, msgCounter + 1);
			}
		});
		table.add(button);

		Gdx.input.setInputProcessor(stage);

		// Send the 1st message with no delay and counter 0
		sendMessage(0f, 0);
	}

	@Override
	public void render () {
		// Update time
		GdxAI.getTimepiece().update(Gdx.graphics.getDeltaTime());

		// Dispatch any delayed messages
		MessageManager.getInstance().update();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		StringBuilder sb = fpsLabel.getText();
		sb.setLength(0);
		sb.append("Counter: ").append(msgCounter).append("; timestamp: ").append(msgTimeStamp);
		fpsLabel.invalidateHierarchy();

		stage.act();
		stage.draw();
	}

	@Override
	public boolean handleMessage (Telegram msg) {
		this.msgCounter = (Integer)msg.extraInfo;
		this.msgTimeStamp = msg.getTimestamp();
		GdxAI.getLogger().info(LOG_TAG, "Counter: " + msgCounter + "; timestamp: " + msgTimeStamp);
		if (timerEnabled) {
			float lag = GdxAI.getTimepiece().getTime() - msg.getTimestamp();
			lag -= (int)lag; // take the decimal part only (in case the lag is > 1)
			float delay = 1f - lag;
			sendMessage(delay, msgCounter + 1);
		}
		return true;
	}

	private void sendMessage (float delay, int counter) {
		MessageManager.getInstance().dispatchMessage(delay, this, this, 1, counter);
	}

}
