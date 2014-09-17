/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;

/** A {@code TabbedPane} widget not full featured but somewhat reusable.
 * 
 * @author davebaol */
public class TabbedPane extends Table {
	private TabbedPaneStyle style;

	Table tabTitleTable;
	Stack tabBodyStack;
	int selectedIndex;
	int tabTitleAlign = Align.left;

	/** Creates a {@code TabbedPane} using the specified skin.
	 * @param skin the skin */
	public TabbedPane (Skin skin) {
		super(skin);
		initialize(skin.get(TabbedPaneStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified skin and alignment.
	 * @param skin the skin
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabbedPane (Skin skin, int tabTitleAlign) {
		super(skin);
		this.tabTitleAlign = tabTitleAlign;
		initialize(skin.get(TabbedPaneStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified skin and style name.
	 * @param skin the skin
	 * @param styleName the style name */
	public TabbedPane (Skin skin, String styleName) {
		super(skin);
		initialize(skin.get(styleName, TabbedPaneStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified skin, style name and alignment.
	 * @param skin the skin
	 * @param styleName the style name
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabbedPane (Skin skin, String styleName, int tabTitleAlign) {
		super(skin);
		this.tabTitleAlign = tabTitleAlign;
		initialize(skin.get(styleName, TabbedPaneStyle.class));
	}

	/** Creates a {@code TabbedPane} using the specified style.
	 * @param styleName the style name
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabbedPane (TabbedPaneStyle style) {
		initialize(style);
	}

	/** Creates a {@code TabbedPane} using the specified style and alignment.
	 * @param style the style
	 * @param tabTitleAlign the alignment for tab titles. Must be one of {@link Align#left}, {@link Align#center} or
	 *           {@link Align#right}. */
	public TabbedPane (TabbedPaneStyle style, int tabTitleAlign) {
		this.tabTitleAlign = tabTitleAlign;
		initialize(style);
	}

	/** Creates a {@code TabbedPane} without setting the style or size. At least a style must be set before using this tabbed pane. */
	public TabbedPane () {
		initialize();
	}

	private void initialize (TabbedPaneStyle style) {
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
		initialize();
	}

	private void initialize () {

		setTouchable(Touchable.enabled);
		tabTitleTable = new Table();
		tabBodyStack = new Stack();
		selectedIndex = -1;

		// Create 1st row
		Cell<?> leftCell = add(new Image(style.titleBegin));
		Cell<?> midCell = add(tabTitleTable);
		Cell<?> rightCell = add(new Image(style.titleEnd));
		switch (tabTitleAlign) {
		case Align.left:
			leftCell.width(((Image)leftCell.getActor()).getWidth()).bottom();
			midCell.left();
			rightCell.expandX().fillX().bottom();
			break;
		case Align.right:
			leftCell.expandX().fillX().bottom();
			midCell.right();
			rightCell.width(((Image)rightCell.getActor()).getWidth()).bottom();
			break;
		case Align.center:
			leftCell.expandX().fillX().bottom();
			midCell.center();
			rightCell.expandX().fillX().bottom();
			break;
		default:
			throw new IllegalArgumentException("TabbedPane align must be one of left, center, right");
		}

		// Create 2nd row
		row();
		Table t = new Table();
		t.setBackground(style.bodyBackground);
		t.add(tabBodyStack);
		add(t).colspan(3).expand().fill();
	}

	@Override
	public Table debug () {
		tabTitleTable.debug();
		return super.debug();
	}

	public void setStyle (TabbedPaneStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;

// Drawable background = style.up;
// if (background == null) {
// background = style.down;
// if (background == null) background = style.checked;
// }
// if (background != null) {
// padBottom(background.getBottomHeight());
// padTop(background.getTopHeight());
// padLeft(background.getLeftWidth());
// padRight(background.getRightWidth());
// }
		invalidateHierarchy();
	}

	/** Returns the tabbed pane's style. Modifying the returned style may not have an effect until
	 * {@link #setStyle(TabbedPaneStyle)} is called. */
	public TabbedPaneStyle getStyle () {
		return style;
	}

	public void addTab (String title, Actor actor) {
		int index = tabTitleTable.getCells().size;
		TabTitleButton button = new TabTitleButton(index, title, style);
		button.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				TabTitleButton tabTitleButton = (TabTitleButton)event.getListenerActor();
// if (tabTitleButton.isChecked())
				setSelectedIndex(tabTitleButton.index);
			}
		});
		tabTitleTable.add(button); // .uniform().fill(); // uniform gives tabs the same size
		tabBodyStack.add(actor);

		// Make sure the 1st tab is selected even after adding the tab
		// TODO
		// CAUTION: if you've added a ChangeListener before adding the tab
		// the following lines will fire 2 ChangeEvents.
		setSelectedIndex(index);
		setSelectedIndex(0);
	}

	public int getSelectedIndex () {
		return selectedIndex;
	}

	public void setSelectedIndex (int index) {
		if (selectedIndex == index) return;
		int tabs = tabTitleTable.getCells().size;
		if (selectedIndex >= 0 && selectedIndex < tabs) {
			setSelectedTab(false);
		}
		this.selectedIndex = index;
		if (selectedIndex >= 0 && selectedIndex < tabs) {
			setSelectedTab(true);
		}

		fireStateChanged();
	}

	private void setSelectedTab (boolean value) {
		TabTitleButton tabTitleButton = ((TabTitleButton)tabTitleTable.getCells().get(selectedIndex).getActor());
		tabTitleButton.setDisabled(value); // Can't toggle the selected tab
		tabTitleButton.setChecked(value);
		tabBodyStack.getChildren().get(selectedIndex).setVisible(value);
	}

	/** Sends a ChangeEvent, with this TabbedPane as the target, to each registered listener. This method is called each time there
	 * is a change to the selected index. */
	protected void fireStateChanged () {
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		changeEvent.setBubbles(false);
		fire(changeEvent);
		Pools.free(changeEvent);
	}

	private static class TabTitleButton extends TextButton {

		private int index;

// private TabTitleButton(int index, String text, Skin skin) {
// super(text, skin);
// this.index = index;
// }
//
// private TabTitleButton(int index, String text, Skin skin, String styleName) {
// super(text, skin, styleName);
// this.index = index;
// }
//
// private TabTitleButton(int index, String text, TextButtonStyle style) {
// super(text, style);
// this.index = index;
// }
//
// private TabTitleButton(int index, String text, Drawable up, Drawable checked, BitmapFont font) {
// super(text, new TextButtonStyle(up, up, checked, font));
// this.index = index;
// }

		private TabTitleButton (int index, String text, TabbedPaneStyle style) {
			super(text, toTextButtonStyle(style));
			this.index = index;
		}

		private static TextButtonStyle toTextButtonStyle (TabbedPaneStyle style) {
			TextButtonStyle buttonStyle = new TextButtonStyle(style.titleButtonUnselected, null, style.titleButtonSelected,
				style.font);
			buttonStyle.fontColor = style.fontColor;
			buttonStyle.downFontColor = style.downFontColor;
			buttonStyle.overFontColor = style.overFontColor;
			buttonStyle.checkedFontColor = style.checkedFontColor;
			buttonStyle.checkedOverFontColor = style.checkedOverFontColor;
			buttonStyle.disabledFontColor = style.disabledFontColor;
			return buttonStyle;
		}
	}

	/** The style for a {@link TabbedPane}. **/
	static public class TabbedPaneStyle {
		public Drawable titleBegin, titleEnd, bodyBackground;
		public Drawable titleButtonSelected, titleButtonUnselected;
		public BitmapFont font;
		/** Optional. */
		public Color fontColor, downFontColor, overFontColor, checkedFontColor, checkedOverFontColor, disabledFontColor;

		public TabbedPaneStyle () {
		}

		public TabbedPaneStyle (Drawable titleBegin, Drawable titleEnd, Drawable bodyBackground, Drawable titleButtonSelected,
			Drawable titleButtonUnselected, BitmapFont font) {
			this.titleBegin = titleBegin;
			this.titleEnd = titleEnd;
			this.bodyBackground = bodyBackground;
			this.titleButtonSelected = titleButtonSelected;
			this.titleButtonUnselected = titleButtonUnselected;
			this.font = font;
		}

		public TabbedPaneStyle (TabbedPaneStyle style) {
			this.titleBegin = style.titleBegin;
			this.titleEnd = style.titleEnd;
			this.bodyBackground = style.bodyBackground;
			this.titleButtonSelected = style.titleButtonSelected;
			this.titleButtonUnselected = style.titleButtonUnselected;
			this.font = style.font;

			if (style.fontColor != null) this.fontColor = new Color(style.fontColor);
			if (style.downFontColor != null) this.downFontColor = new Color(style.downFontColor);
			if (style.overFontColor != null) this.overFontColor = new Color(style.overFontColor);
			if (style.checkedFontColor != null) this.checkedFontColor = new Color(style.checkedFontColor);
			if (style.checkedOverFontColor != null) this.checkedFontColor = new Color(style.checkedOverFontColor);
			if (style.disabledFontColor != null) this.disabledFontColor = new Color(style.disabledFontColor);
		}
	}

}
