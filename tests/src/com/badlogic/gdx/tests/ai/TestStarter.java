package com.badlogic.gdx.tests.ai;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tests.ai.fsm.StateMachineTest;

public class TestStarter {
	public static void main (String[] argv) {
		ApplicationListener test = new StateMachineTest();		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.r = config.g = config.b = config.a = 8;
		config.width = 960;
		config.height = 600;
		new LwjglApplication(test, config);
	}
}
