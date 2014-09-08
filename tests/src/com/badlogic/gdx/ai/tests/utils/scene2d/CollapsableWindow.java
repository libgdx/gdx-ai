package com.badlogic.gdx.ai.tests.utils.scene2d;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** A {@code CollapsableWindow} can be expanded/collapsed with a double click on the title bar.
 * 
 * @author Xoppa */
public class CollapsableWindow extends Window {
	private boolean collapsed;
	private float collapseHeight = 20f;
	private float expandHeight;

	public CollapsableWindow (String title, Skin skin) {
		super(title, skin);
		addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (getTapCount() == 2 && getHeight() - y <= getPadTop() && y < getHeight() && x > 0 && x < getWidth())
					toggleCollapsed();
			}
		});
	}

	public void expand () {
		if (!collapsed) return;
		setHeight(expandHeight);
		setY(getY() - expandHeight + collapseHeight);
		collapsed = false;
	}

	public void collapse () {
		if (collapsed) return;
		expandHeight = getHeight();
		setHeight(collapseHeight);
		setY(getY() + expandHeight - collapseHeight);
		collapsed = true;
		if (getStage() != null) getStage().setScrollFocus(null);
	}

	public void toggleCollapsed () {
		if (collapsed)
			expand();
		else
			collapse();
	}

	public boolean isCollapsed () {
		return collapsed;
	}
}