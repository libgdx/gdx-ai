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

package com.badlogic.gdx.ai.tests.utils.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.StringBuilder;

/** A label continuously showing the most up-to-date integer value prefixed by a constant string.
 * 
 * @author davebaol */
public abstract class IntValueLabel extends Label {

	int oldValue;
	int appendIndex;

	public IntValueLabel (CharSequence text, int initialValue, Skin skin) {
		this(text, initialValue, skin.get(LabelStyle.class));
	}

	public IntValueLabel (CharSequence text, int initialValue, Skin skin, String styleName) {
		this(text, initialValue, skin.get(styleName, LabelStyle.class));
	}

	public IntValueLabel (CharSequence text, int initialValue, Skin skin, String fontName, Color color) {
		this(text, initialValue, new LabelStyle(skin.getFont(fontName), color));
	}

	public IntValueLabel (CharSequence text, int initialValue, Skin skin, String fontName, String colorName) {
		this(text, initialValue, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
	}

	public IntValueLabel (CharSequence text, int initialValue, LabelStyle style) {
		super(text.toString() + initialValue, style);
		this.oldValue = initialValue;
		this.appendIndex = text.length();
	}

	public abstract int getValue();

	@Override
	public void act (float delta) {
		int newValue = getValue();
		if (oldValue != newValue) {
			oldValue = newValue;
			StringBuilder sb = getText();
			sb.setLength(appendIndex);
			sb.append(oldValue);
			invalidateHierarchy();
		}
		super.act(delta);
	}

}
