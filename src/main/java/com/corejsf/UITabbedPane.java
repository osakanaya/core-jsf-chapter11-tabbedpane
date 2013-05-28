package com.corejsf;

import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;

@FacesComponent("com.corejsf.TabbedPane")
public class UITabbedPane extends UICommand {
	private enum PropertyKeys {
		content
	};
	
	public String getContent() {
		return (String)getStateHelper().get(PropertyKeys.content);
	}
	
	public void setContent(String newValue) {
		getStateHelper().put(PropertyKeys.content, newValue);
	}
}
