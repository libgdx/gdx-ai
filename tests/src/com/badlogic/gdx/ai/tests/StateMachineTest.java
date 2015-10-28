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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.tests.fsm.Bob;
import com.badlogic.gdx.ai.tests.fsm.Elsa;
import com.badlogic.gdx.ai.tests.utils.GdxAiTestUtils;

/** A simple test to demonstrate state machines combined with message handling.
 * @author davebaol */
public class StateMachineTest extends ApplicationAdapter {

	public static void main (String[] argv) {
		GdxAiTestUtils.launch(new StateMachineTest());
	}

	Bob bob;
	Elsa elsa;
	float elapsedTime;

	@Override
	public void create () {

		elapsedTime = 0;

		// Create Bob and his wife
		bob = new Bob();
		elsa = new Elsa(bob);
		bob.setElsa(elsa);

	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();
		elapsedTime += delta;

		// Update time
		GdxAI.getTimepiece().update(delta);

		if (elapsedTime > 0.8f) {
			// Update Bob and his wife
			bob.update(elapsedTime);
			elsa.update(elapsedTime);

			// Dispatch any delayed messages
			MessageManager.getInstance().update();

			elapsedTime = 0;
		}
	}
}
