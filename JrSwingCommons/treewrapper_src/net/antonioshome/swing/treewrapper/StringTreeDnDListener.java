/*
 * $Id: StringTreeDnDListener.java,v 1.2 2006/05/08 19:36:50 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper;

import java.util.EventListener;

/**
 * StringTreeDnDListener represents a listener that is informed when a String is about to be dropped into a node in a JTree.
 * 
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.2 $
 */
public interface StringTreeDnDListener extends EventListener {
	/**
	 * Invoked to verify that aSourceString may be dropped into aTargetNode inside aTargetTree.
	 * 
	 * @param anEvent
	 *            a StringTreeDnDEvent containing information about the data being dropped.
	 * @throws DnDVetoException
	 *             if the drag and drop operation is not valid.
	 */
	public void mayDrop(StringTreeDnDEvent anEvent) throws DnDVetoException;

	/**
	 * Invoked when the drop operation happens.
	 * 
	 * @param anEvent
	 *            a StringTreeDnDEvent the event containing information about the Drag and Drop operation.
	 * @throws DnDVetoException
	 *             if the drag and drop operation is not valid.
	 */
	public void drop(StringTreeDnDEvent anEvent) throws DnDVetoException;
}
