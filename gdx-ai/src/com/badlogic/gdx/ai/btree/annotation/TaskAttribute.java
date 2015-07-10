
package com.badlogic.gdx.ai.btree.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaskAttribute {
	// The cool thing is the possibility here to add annotations fields
	// maybe to define if the parameter is required, or even
	// to define minimal or maximal values for numeric fields
	// for example
	public boolean required() default false;

	//public int maxValue();

	//public int minValue();
}
