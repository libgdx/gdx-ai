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

package com.badlogic.gdx.ai.btree;

import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** A {@code Metadata} describes a task with information such as the minimum/maximum number of children and the set of its
 * attributes. Typically {@code Metadata} are used by behavior tree editors and parsers.
 * @author davebaol */
public class Metadata {
	int minChildren;
	int maxChildren;
	ObjectSet<String> attributes;

	/** Creates a {@code Metadata} for a leaf task with no attributes. */
	public Metadata () {
		this(0, 0, (ObjectSet<String>)null);
	}

	/** Creates a {@code Metadata} for a leaf task with the given attributes.
	 * @param attributes the attribute names */
	public Metadata (String... attributes) {
		this(0, 0, newObjectSet(attributes));
	}

	/** Creates a {@code Metadata} for a leaf task with the given attributes.
	 * @param attributes the attribute names */
	public Metadata (ObjectSet<String> attributes) {
		this(0, 0, attributes);
	}

	/** Creates a {@code Metadata} for a task having the given number of children and no attributes. If {@code numChildren} is
	 * negative the task accepts from 0 to {@link Integer.MAX_VALUE} children.
	 * @param numChildrean the number of children */
	public Metadata (int numChildrean) {
		this(numChildrean, numChildrean, (ObjectSet<String>)null);
	}

	/** Creates a {@code Metadata} for a task having the given number of children and the specified attributes. If
	 * {@code numChildren} is negative the task accepts from 0 to {@link Integer.MAX_VALUE} children.
	 * @param numChildrean the number of children
	 * @param attributes the attribute names */
	public Metadata (int numChildrean, String... attributes) {
		this(numChildrean, numChildrean, newObjectSet(attributes));
	}

	/** Creates a {@code Metadata} for a task having the given number of children and the specified attributes. If
	 * {@code numChildren} is negative the task accepts from 0 to {@link Integer.MAX_VALUE} children.
	 * @param numChildrean the number of children
	 * @param attributes the attribute names */
	public Metadata (int numChildrean, ObjectSet<String> attributes) {
		this(numChildrean, numChildrean, attributes);
	}

	/** Creates a {@code Metadata} for a task accepting from {@code minChildren} to {@code maxChildren} children and no attributes.
	 * @param minChildren the minimum number of children (defaults to 0 if negative)
	 * @param maxChildren the maximum number of children (defaults to {@link Integer.MAX_VALUE} if negative) */
	public Metadata (int minChildren, int maxChildren) {
		this(minChildren, maxChildren, (ObjectSet<String>)null);
	}

	/** Creates a {@code Metadata} for a task accepting from {@code minChildren} to {@code maxChildren} children and the given
	 * attributes.
	 * @param minChildren the minimum number of children (defaults to 0 if negative)
	 * @param maxChildren the maximum number of children (defaults to {@link Integer.MAX_VALUE} if negative)
	 * @param attributes the attribute names */
	public Metadata (int minChildren, int maxChildren, String... attributes) {
		this(minChildren, maxChildren, newObjectSet(attributes));
	}

	/** Creates a {@code Metadata} from another one (copy constructor)
	 * @param other the other metadata */
	public Metadata (Metadata other) {
		this(other, (String[])null, (String[])null);
	}

	/** Creates a {@code Metadata} from another one adding the specified attributes.
	 * @param other the other metadata
	 * @param addAttributes the attribute names to add */
	public Metadata (Metadata other, String... addAttributes) {
		this(other, addAttributes, (String[])null);
	}

	/** Creates a {@code Metadata} from another one adding the specified attributes.
	 * @param other the other metadata
	 * @param addAttributes the attribute names to add
	 * @param removeAttributes the attribute names to remove */
	public Metadata (Metadata other, String[] addAttributes, String... removeAttributes) {
		this(other.getMaxChildren(), other.getMaxChildren(), other.getAttributes() == null ? new ObjectSet<String>()
			: new ObjectSet<String>(other.getAttributes()));
		if (addAttributes != null) {
			for (int i = 0; i < addAttributes.length; i++)
				getAttributes().add(addAttributes[i]);
		}
		if (removeAttributes != null) {
			for (int i = 0; i < removeAttributes.length; i++)
				getAttributes().remove(removeAttributes[i]);
		}
	}

	/** Creates a {@code Metadata} for a task accepting from {@code minChildren} to {@code maxChildren} children and the given
	 * attributes.
	 * @param minChildren the minimum number of children (defaults to 0 if negative)
	 * @param maxChildren the maximum number of children (defaults to {@link Integer.MAX_VALUE} if negative)
	 * @param attributes the attribute names */
	public Metadata (int minChildren, int maxChildren, ObjectSet<String> attributes) {
		this.minChildren = minChildren < 0 ? 0 : minChildren;
		this.maxChildren = maxChildren < 0 ? Integer.MAX_VALUE : maxChildren;
		this.attributes = attributes;
	}

	/** @return the minimum number of children. */
	public int getMinChildren () {
		return minChildren;
	}

	/** @return the maximum number of children. */
	public int getMaxChildren () {
		return maxChildren;
	}

	/** @return the set of valid attributes. */
	public ObjectSet<String> getAttributes () {
		return attributes;
	}

	/** Returns {@code true} if the given attribute name is valid; {@code false} otherwise.
	 * @param name the attribute name */
	public boolean hasAttribute (String name) {
		ObjectSet<String> attr = attributes;
		return attr != null && attr.contains(name);
	}

	/** @return {@code true} if the task cannot have any children; {@code false} otherwise. */
	public boolean isLeaf () {
		return getMinChildren() == 0 && getMaxChildren() == 0;
	}

	private static ObjectSet<String> newObjectSet (String... attributes) {
		ObjectSet<String> attr = new ObjectSet<String>();
		attr.addAll(attributes);
		return attr;
	}

	/** Finds the {@code Metadata} of the task identified by the given class.
	 * @param actualTaskClass the class of the task
	 * @throws ReflectionException if no static field named {@code METADATA} is declared by the given class or one of its
	 *            superclasses */
	public static Metadata findMetadata (@SuppressWarnings("rawtypes") Class<? extends Task> actualTaskClass) {
		try {
			Field metadataField = ClassReflection.getField(actualTaskClass, "METADATA");
			metadataField.setAccessible(true);
			return (Metadata)metadataField.get(null);
		} catch (ReflectionException e) {
			return null;
		}
	}
}
