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
import com.badlogic.gdx.ai.tests.utility.Dog;
import com.badlogic.gdx.ai.tests.utility.RestAction;
import com.badlogic.gdx.ai.tests.utility.WanderAction;
import com.badlogic.gdx.ai.tests.utils.GdxAiTestUtils;
import com.badlogic.gdx.ai.utility.*;
import com.badlogic.gdx.ai.utility.consideration.Consideration;
import com.badlogic.gdx.ai.utility.evaluator.LinearEvaluator;
import com.badlogic.gdx.ai.utility.evaluator.PowerEvaluator;
import com.badlogic.gdx.math.Vector2;

/** A simple test to demonstrate state machines combined with message handling.
 * @author davebaol */
public class UtilityTest extends ApplicationAdapter {

	public static void main (String[] argv) {
		GdxAiTestUtils.launch(new UtilityTest());
	}

	float elapsedTime;
	DecisionMaker<Dog> decisionMaker;
	Dog dog;

	@Override
	public void create () {

		elapsedTime = 0;


		Consideration tirednessConsideration = new Consideration<Dog>("TirednessConsideration", 1) {
			{
				init();
			}

			private void init() {
				Vector2 ptA = new Vector2(0, 1);
				Vector2 ptB = new Vector2(30, 0);
				evaluator = new PowerEvaluator(ptA, ptB, 2f);
				utility = new Utility(0,getWeight(),getRank());
			}

			@Override
			public void consider(Dog dog) {
				float value = evaluator.evaluate(dog.energy);
				utility.setValue(value);
				GdxAI.getLogger().info("tirednessConsideration", "Evaluting Energy levels " + dog.energy + " with Utilty " + value);
			}

		};

		Consideration curiousityConsideration  = new Consideration<Dog>("PlayConsideration", 0){
			{
				init();
			}

			private void init() {
				Vector2 ptA = new Vector2(0f, 0.5f);
				Vector2 ptB = new Vector2(100, 0.5f);
				evaluator = new LinearEvaluator(ptA, ptB);
				utility = new Utility(0,getWeight(),getRank());
			}

			@Override
			public void consider(Dog dog) {
				float value = evaluator.evaluate(dog.curiousity);
				utility.setValue(value);

				GdxAI.getLogger().info("curiousityConsideration", "Evaluting Curiousity " + dog.curiousity + " with Utilty " + value);
			}
		};

		//drink option triggered by thirst consideration
//        Option<UtilityActor> drinkOption = new Option<Dog>();
//        drinkOption.setAction(drinkAction);
//        drinkOption.addConsideration(thirstConsideration);


		//
		WanderAction playAction = new WanderAction();
		RestAction restAction = new RestAction();

		//rest optoin triggered by tiredness consideration
		Option<Dog> restOption = new Option<Dog>();
		restOption.setAction(restAction);
		restOption.addConsideration(tirednessConsideration);

		Option<Dog> playOption = new Option<Dog>();
		playOption.setAction(playAction);
		playOption.addConsideration(curiousityConsideration);

		Behaviour<Dog> behaviour = new Behaviour<Dog>("Dog");
		behaviour.addOption(restOption);
		behaviour.addOption(playOption);

		UtilityAI<Dog> utilityAI = new UtilityAI<Dog>();
		utilityAI.addBehaviour(behaviour);

		dog = new Dog("Doggy");
		decisionMaker = new DecisionMaker<Dog>(utilityAI, dog);


	}

	@Override
	public void render () {
		float delta = Gdx.graphics.getDeltaTime();
		elapsedTime += delta;

		// Update time
		GdxAI.getTimepiece().update(delta);

		if (elapsedTime > 0.8f) {
			decisionMaker.think();
			decisionMaker.update();


			dog.update(elapsedTime);

			elapsedTime = 0;
		}
	}
}
