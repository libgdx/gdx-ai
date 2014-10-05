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

package com.badlogic.gdx.ai.btree.parser.xml;

import java.io.IOException;
import java.io.Reader;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Metadata;
import com.badlogic.gdx.ai.btree.Node;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import com.badlogic.gdx.ai.btree.decorator.AlwaysSucceed;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.ai.btree.decorator.UntilFail;
import com.badlogic.gdx.ai.btree.decorator.UntilSuccess;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** @author davebaol */
public class XmlParser<E> {

	MyXmlReader<E> xmlReader;

	public XmlParser () {
		xmlReader = new MyXmlReader<E>();
	}

	public BehaviorTree<E> parse (Reader reader, E object) throws IOException {
		xmlReader.parse(reader);
		return new BehaviorTree<E>(xmlReader.root, object);
	}

	static class MyXmlReader<E> extends XmlReader {

		private static final ObjectMap<String, String> DEFAULT_IMPORTS = new ObjectMap<String, String>();
		static {
			Class<?>[] classes = new Class<?>[] {// @off - disable libgdx formatter
				AlwaysFail.class,
				AlwaysSucceed.class,
				Invert.class,
				Parallel.class,
				Selector.class,
				Sequence.class,
				UntilFail.class,
				UntilSuccess.class
			};
			// @on - enable libgdx formatter
			for (Class<?> c : classes) {
				String fqcn = c.getName();
				String cn = c.getSimpleName();
				DEFAULT_IMPORTS.put(cn, fqcn);
			}
		}

		private static final int TAG_NONE = -1;
		private static final int TAG_BEHAVIOR_TREE = 0;
		private static final int TAG_IMPORT = 1;
		private static final int TAG_ROOT = 2;

		private static final XmlTag[] TAGS = new XmlTag[] {// @off - disable libgdx formatter
			new XmlTag("BehaviorTree", TAG_NONE),
			new XmlTag("Import", TAG_BEHAVIOR_TREE),
			new XmlTag("Root", TAG_BEHAVIOR_TREE)
			// @on - enable libgdx formatter
		};

		ObjectMap<String, String> userImports = new ObjectMap<String, String>();

		Node<E> root;
		Array<StackedNode<E>> stack = new Array<StackedNode<E>>();
		int tagType;
		boolean isTask;

		@Override
		public Element parse (Reader reader) throws IOException {
			tagType = TAG_NONE;
			isTask = false;
			userImports.clear();
			root = null;
			stack.clear();
			return super.parse(reader);
		}

		@Override
		protected void open (String name) {
			if (tagType == TAG_ROOT)
				openTask(name);
			else
				openTag(name);
		}

		String importTask;
		String importAs;

		@Override
		protected void attribute (String name, String value) {
			if (isTask) {
				if (!attributeTask(name, value))
					throw new GdxRuntimeException(stack.peek().name + ": unknown attribute '" + name + "'");
			} else {
				if (!attributeTag(name, value))
					throw new GdxRuntimeException(TAGS[tagType].name + ": unknown attribute '" + name + "'");
			}
		}

		private boolean attributeTask (String name, String value) {
			StackedNode<E> stackedNode = stack.peek();
			if (!stackedNode.metadata.hasAttribute(name)) return false;
			Field attributeField = getField(stackedNode.node.getClass(), name);
			setField(attributeField, stackedNode.node, value);
			return true;
		}

		private Field getField (Class<?> clazz, String name) {
			try {
				return ClassReflection.getField(clazz, name);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

		private void setField (Field field, Node<E> node, String value) {
			field.setAccessible(true);
			Object valueObject = null;
			Class<?> type = field.getType();
			if (type == int.class || type == Integer.class)
				valueObject = Integer.valueOf(value);
			else if (type == float.class || type == Float.class)
				valueObject = Float.valueOf(value);
			else if (type == boolean.class || type == Boolean.class)
				valueObject = Boolean.valueOf(value);
			else if (type == long.class || type == Long.class)
				valueObject = Long.valueOf(value);
			else if (type == double.class || type == Double.class)
				valueObject = Double.valueOf(value);
			else if (type == short.class || type == Short.class)
				valueObject = Short.valueOf(value);
			else if (type == char.class || type == Character.class) {
				if (value.length() != 1) throw new GdxRuntimeException("Invalid character '" + value + "'");
				valueObject = Character.valueOf(value.charAt(0));
			} else if (type == byte.class || type == Byte.class)
				valueObject = Byte.valueOf(value);
			else if (type == String.class)
				valueObject = value;
			else
				throw new GdxRuntimeException("Unknown type '" + type + "'");
			try {
				field.set(node, valueObject);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}

		private boolean attributeTag (String name, String value) {
			if (tagType == TAG_IMPORT) {
				if (name.equals("task"))
					importTask = value;
				else if (name.equals("as"))
					importAs = value;
				else
					return false;
			}
			return true;
		}

		@Override
		protected String entity (String name) {
			return super.entity(name);
		}

		@Override
		protected void text (String text) {
			throw new GdxRuntimeException("Behavior tree: unexpected text '" + text + "'");
		}

		@Override
		protected void close () {
			if (isTask) {
				// Pop the task from the stack
				StackedNode<E> stackedNode = stack.pop();

				// Check the minimum number of children
				int minChildren = stackedNode.metadata.getMinChildren();
				if (stackedNode.node.getChildCount() < minChildren)
					throw new GdxRuntimeException(stackedNode.name + ": not enough children (" + stackedNode.node.getChildCount()
						+ " < " + minChildren + ")");

				isTask = (stack.size != 0);
			} else {
				if (tagType == TAG_IMPORT) {
					addImport(importTask, importAs);
				}
				// Reset the tag type to the parent
				if (tagType != TAG_NONE) {
					tagType = TAGS[tagType].parentIndex;
				}
			}
		}

		private void addImport (String task, String as) {
			if (task == null) throw new GdxRuntimeException("Import: missing task class name.");
			if (as == null) {
				Class<?> clazz = null;
				try {
					clazz = ClassReflection.forName(task);
				} catch (ReflectionException e) {
					throw new GdxRuntimeException("Import: class not found '" + task + "'");
				}
				as = clazz.getSimpleName();
			}
			String className = getImport(as);
			if (className != null) throw new GdxRuntimeException("Import: alias '" + as + "' already used.");
			userImports.put(as, task);
		}

		private String getImport (String as) {
			String className = DEFAULT_IMPORTS.get(as);
			return className != null ? className : userImports.get(as);
		}

		private void openTag (String name) {
			for (int i = 0; i < TAGS.length; i++) {
				XmlTag tag = TAGS[i];
				if (name.equalsIgnoreCase(tag.name)) {
					if (tagType != tag.parentIndex) {
						String msg = "The tag '" + tag.name + "' must be ";
						if (tag.parentIndex == TAG_NONE)
							msg += "the root element";
						else
							msg += "a child of the tag '" + TAGS[tag.parentIndex].name + "'";
						throw new GdxRuntimeException(msg);
					}
					tagType = i;
					cleanTagAttributes();
					return;
				}
			}
			throw new GdxRuntimeException("Unknown tag '" + name + "'");
		}

		private void cleanTagAttributes () {
			if (tagType == TAG_IMPORT) {
				importTask = null;
				importAs = null;
			}
		}

		private void openTask (String name) {
			isTask = true;
			String className = getImport(name);
			if (className == null) className = name;
			try {
				@SuppressWarnings("unchecked")
				Node<E> node = (Node<E>)ClassReflection.newInstance(ClassReflection.forName(className));
				if (stack.size == 0) {
					if (root == null)
						root = node;
					else
						throw new GdxRuntimeException("Root: must have exactly 1 child.");
				}
				else {
					StackedNode<E> stackedParent = stack.peek();
					int maxChildren = stackedParent.metadata.getMaxChildren();
					if (stackedParent.node.getChildCount() >= maxChildren)
						throw new GdxRuntimeException(stackedParent.name + ": max number of children exceeded ("
							+ (stackedParent.node.getChildCount() + 1) + " > " + maxChildren + ")");
					// Add child task
					stackedParent.node.addChild(node);
				}
				stack.add(new StackedNode<E>(name, node));
			} catch (ReflectionException e) {
				throw new GdxRuntimeException("Cannot parse behavior tree!!!", e);
			}
		}

		private static class XmlTag {
			String name;
			int parentIndex;

			public XmlTag (String name, int parentIndex) {
				this.name = name;
				this.parentIndex = parentIndex;
			}
		}

		private static class StackedNode<E> {
			String name;
			Node<E> node;
			Metadata metadata;

			StackedNode (String name, Node<E> node) {
				this.name = name;
				this.node = node;
				this.metadata = node.getMetadata();
			}
		}
	}
}
