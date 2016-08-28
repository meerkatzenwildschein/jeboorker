package org.rr.jeborker.event;

public interface ApplicationEventListener {

	public void metaDataSheetSelectionChanged(ApplicationEvent evt);
	
	public void ebookItemSelectionChanged(ApplicationEvent evt);

	public void metaDataSheetContentChanged(ApplicationEvent evt);

	public void mainTreeVisibilityChanged(ApplicationEvent evt);
	
}
