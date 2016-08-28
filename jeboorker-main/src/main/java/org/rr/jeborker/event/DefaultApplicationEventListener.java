package org.rr.jeborker.event;

public class DefaultApplicationEventListener implements ApplicationEventListener {

	@Override
	public void metaDataSheetSelectionChanged(ApplicationEvent evt) {}
	
	@Override
	public void ebookItemSelectionChanged(ApplicationEvent evt) {}
	
	@Override
	public void metaDataSheetContentChanged(ApplicationEvent evt) {}

	@Override
	public void mainTreeVisibilityChanged(ApplicationEvent evt) {}
	
}
