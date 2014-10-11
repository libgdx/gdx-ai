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

	public Metadata () {
		this(0, 0, (ObjectSet<String>)null);
	}

	public Metadata (String... attributes) {
		this(0, 0, newObjectSet(attributes));
	}

	public Metadata (ObjectSet<String> attributes) {
		this(0, 0, attributes);
	}

	public Metadata (int numChildrean) {
		this(numChildrean, numChildrean, (ObjectSet<String>)null);
	}

	public Metadata (int numChildrean, String... attributes) {
		this(numChildrean, numChildrean, newObjectSet(attributes));
	}

	public Metadata (int numChildrean, ObjectSet<String> attributes) {
		this(numChildrean, numChildrean, attributes);
	}

	public Metadata (int minChildren, int maxChildren) {
		this(minChildren, maxChildren, (ObjectSet<String>)null);
	}

	public Metadata (int minChildren, int maxChildren, String... attributes) {
		this(minChildren, maxChildren, newObjectSet(attributes));
	}

	public Metadata (Metadata other) {
		this.minChildren = other.getMaxChildren();
		this.maxChildren = other.getMaxChildren();
		this.attributes = other.getAttributes() == null ? new ObjectSet<String>() : new ObjectSet<String>(other.getAttributes());
	}

	public Metadata (Metadata other, String... addAttributes) {
		this(other);
		for (int i = 0; i < addAttributes.length; i++)
			getAttributes().add(addAttributes[i]);
	}

	public Metadata (Metadata other, String[] addAttributes, String... removeAttributes) {
		this(other);
		if (addAttributes != null) {
			for (int i = 0; i < addAttributes.length; i++)
				getAttributes().add(addAttributes[i]);
		}
		for (int i = 0; i < removeAttributes.length; i++)
			getAttributes().remove(removeAttributes[i]);
	}

	public Metadata (int minChildren, int maxChildren, ObjectSet<String> attributes) {
		this.minChildren = minChildren < 0 ? 0 : minChildren;
		this.maxChildren = maxChildren < 0 ? Integer.MAX_VALUE : maxChildren;
		this.attributes = attributes;
	}

	public int getMinChildren () {
		return minChildren;
	}

	public int getMaxChildren () {
		return maxChildren;
	}

	public ObjectSet<String> getAttributes () {
		return attributes;
	}

	public boolean hasAttribute (String name) {
		ObjectSet<String> attr = attributes;
		return attr != null && attr.contains(name);
	}

	public boolean isLeaf () {
		return getMinChildren() == 0 && getMaxChildren() == 0;
	}

	private static ObjectSet<String> newObjectSet (String... attributes) {
		ObjectSet<String> attr = new ObjectSet<String>();
		attr.addAll(attributes);
		return attr;
	}

	public static Metadata findMetadata(@SuppressWarnings("rawtypes") Class<? extends Task> actualTaskClass) {
		try {
			Field metadataField = ClassReflection.getField(actualTaskClass, "METADATA");
			metadataField.setAccessible(true);
			return (Metadata)metadataField.get(null);
		} catch (ReflectionException e) {
			return null;
		}
	} 
}
