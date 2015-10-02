package com.badlogic.gdx.ai.btree.leaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.utils.random.FloatDistribution;
import com.badlogic.gdx.utils.TimeUtils;

public class Wait<T> extends LeafTask<T> {

	@TaskAttribute(required=true)
	public FloatDistribution seconds;
	
	private long startTime;
	private float timeout;
	
	public Wait () {
	}

	@Override
	public void start () {
		timeout = seconds.nextFloat();
		startTime = TimeUtils.nanoTime();
	}

	@Override
	public void run () {
		if ((TimeUtils.nanoTime() - startTime) / 1000000000f < timeout)
			running();
		else
			success();
	}

	@Override
	protected Task<T> copyTo (Task<T> task) {
		((Wait<T>)task).seconds = seconds;
		return task;
	}

}
