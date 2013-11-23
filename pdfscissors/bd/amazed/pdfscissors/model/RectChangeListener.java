package bd.amazed.pdfscissors.model;

import java.awt.Rectangle;

import bd.amazed.pdfscissors.view.Rect;

public interface RectChangeListener {
	/**
	 * 
	 * @param repaintArea area in canvas to repaint. can be null to indicate repaint whole area
	 */
	public void rectUpdated(Rect updatedRect, Rectangle repaintArea);
}
