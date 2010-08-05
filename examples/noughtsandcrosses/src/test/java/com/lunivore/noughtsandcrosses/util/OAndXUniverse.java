package com.lunivore.noughtsandcrosses.util;

import org.lunivore.tyburn.WindowControl;

import com.lunivore.noughtsandcrosses.view.ComponentNames;


public class OAndXUniverse {

	private WindowControl windowControl;

	public OAndXUniverse() {
		windowControl = new WindowControl(ComponentNames.NOUGHTSANDCROSSES);
	}

	public WindowControl getControl() {
		return windowControl;
	}

	public void reset() throws Exception {
		windowControl = new WindowControl(ComponentNames.NOUGHTSANDCROSSES);
	}

	public void destroy() throws Exception {
		windowControl.closeWindow(); 
	}
}
