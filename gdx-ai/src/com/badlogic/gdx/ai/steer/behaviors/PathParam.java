package com.badlogic.gdx.ai.steer.behaviors;

/** A path parameter used by path following behaviors to keep the path status.
 * 
 * @author davebaol */
public interface PathParam {

	public float getDistance ();

	public void setDistance (float distance);
}