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

package com.badlogic.gdx.ai.tests.btree.dog;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.math.MathUtils;

/** @author implicit-invocation
 * @author davebaol */
public class Dog {

	public String name;
	public String brainLog;
	private BehaviorTree<Dog> behaviorTree;

	public Dog (String name) {
		this(name, null);
	}

	public Dog (String name, BehaviorTree<Dog> btree) {
		this.name = name;
		this.brainLog = name + " brain";
		this.behaviorTree = btree;
		if (btree != null) btree.setObject(this);
	}

	public BehaviorTree<Dog> getBehaviorTree () {
		return behaviorTree;
	}

	public void setBehaviorTree (BehaviorTree<Dog> behaviorTree) {
		this.behaviorTree = behaviorTree;
	}

	public void bark () {
		if (MathUtils.randomBoolean())
			log("Arf arf");
		else
			log("Woof");
	}

	public void startWalking () {
		log("Let's find a nice tree");
	}

	public void randomlyWalk () {
		log("SNIFF SNIFF - Dog walks randomly around!");
	}

	public void stopWalking () {
		log("This tree smells good :)");
	}

	public Boolean markATree (int i) {
		if (i == 0) {
			log("Swoosh....");
			return null;
		}
		if (MathUtils.randomBoolean()) {
			log("MUMBLE MUMBLE - Still leaking out");
			return Boolean.FALSE;
		}
		log("I'm ok now :)");
		return Boolean.TRUE;
	}

//	private boolean urgent = false;
//
//	public boolean isUrgent () {
//		return urgent;
//	}
//
//	public void setUrgent (boolean urgent) {
//		this.urgent = urgent;
//	}

	public void log (String msg) {
		GdxAI.getLogger().info(name, msg);
	}

	public void brainLog (String msg) {
		GdxAI.getLogger().info(brainLog, msg);
	}

}
