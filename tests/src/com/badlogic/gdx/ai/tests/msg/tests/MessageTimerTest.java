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

package com.badlogic.gdx.ai.tests.msg.tests;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.tests.MessageTests;
import com.badlogic.gdx.ai.tests.msg.MessageTestBase;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** A simple test to demonstrate timer messages.
 * @author davebaol */
public class MessageTimerTest extends MessageTestBase implements Telegraph {

	private static final String LOG_TAG = MessageTimerTest.class.getSimpleName();

	int msgCounter;
	float msgTimeStamp;
	boolean timerEnabled;

	public MessageTimerTest (MessageTests container) {
		super(container, "Timer");
	}

	@Override
	public String getHelpMessage () {
		return "Create a simple timer";
	}

	@Override
	public void create () {
		super.create();

		timerEnabled = true;

		TextButton button = new TextButton("Stop timer", container.skin);
		button.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				timerEnabled = !timerEnabled;
				((TextButton)actor).setText(timerEnabled ? "Stop Timer" : "Start Timer");
				if (timerEnabled) sendMessage(0f, msgCounter + 1);
			}
		});
		testTable.add(button);

		// Send the 1st message with no delay and counter 0
		sendMessage(0f, 0);
	}

	@Override
	public void update () {
		// Dispatch any delayed messages
		MessageManager.getInstance().update();
	}

	@Override
	public void draw () {
	}

	@Override
	public void dispose () {
		MessageManager.getInstance().clear();
		super.dispose();
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
