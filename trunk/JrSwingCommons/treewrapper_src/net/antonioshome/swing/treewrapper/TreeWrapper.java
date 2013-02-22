/*
 * $Id: TreeWrapper.java,v 1.6 2006-06-06 10:18:48 antonio Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */
package net.antonioshome.swing.treewrapper;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.logging.Level;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;

/**
 * TreeWrapper is used to handle drag and drop and popup menus in any JTree.
 * 
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: antonio $
 * @version $Revision: 1.6 $
 */
public class TreeWrapper {
	/**
	 * This to avoid excesive creation of objects in invocation.
	 */
	private static Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	/**
	 * This to avoid excessive reflection to find the "getTransferable" method.
	 */
	private static Method getTransferableMethod = null;
	/**
	 * This to avoid excesive creation of objects in invocation.
	 */
	private static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	protected JTree tree;
	protected DnDCellRendererProxy rendererProxy;
	protected DragSource dragSource;
	protected DropTarget dropTarget;
	protected MutableTreeNode dropNode;
	protected EventListenerList listeners;
	protected CustomPopupHandler customPopupHandler;

	/**
	 * Creates a new instance of TreeWrapper.
	 * 
	 * @param aJTree
	 *            the JTree to which DnD support is added.
	 */
	public TreeWrapper(JTree aJTree) {
		tree = aJTree;
		tree.setDoubleBuffered(true);
		listeners = new EventListenerList();
		initHandler();
	}

	/**
	 * Adds a new TreeTreeDnDListener to the list of listeners.
	 * 
	 * @param listener
	 *            a TreeTreeDnDListener to be informed of DnD events.
	 */
	public void addTreeTreeDnDListener(TreeTreeDnDListener listener) {
		listeners.add(TreeTreeDnDListener.class, listener);
	}

	/**
	 * Removes the given TreeTreeDnDListener from the list of listeners.
	 * 
	 * @param listener
	 *            a TreeTreeDnDListener not to be informed of DnD events.
	 */
	public void removeTreeTreeDnDListener(TreeTreeDnDListener listener) {
		listeners.remove(TreeTreeDnDListener.class, listener);
	}

	/**
	 * Adds a new StringTreeDnDListener to the list of listeners.
	 * 
	 * @param listener
	 *            a StringTreeDnDListener to be informed of DnD events.
	 */
	public void addStringTreeDnDListener(StringTreeDnDListener listener) {
		listeners.add(StringTreeDnDListener.class, listener);
	}

	/**
	 * Removes the given StringTreeDnDListener from the list of listeners.
	 * 
	 * @param listener
	 *            a StringTreeDnDListener not to be informed of DnD events.
	 */
	public void removeStringTreeDnDListener(StringTreeDnDListener listener) {
		listeners.remove(StringTreeDnDListener.class, listener);
	}

	private void initHandler() {
		tree.addPropertyChangeListener(new RendererChangeListener());
		tree.setCellRenderer(new DnDCellRendererProxy(tree.getCellRenderer()));
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY_OR_MOVE, new TreeDragGestureListener());
		dropTarget = new AutoScrollingTreeDropTarget(tree, new TreeDropTargetListener());
		tree.addMouseListener(new PopupChooserMouseListener());
		ToolTipManager.sharedInstance().registerComponent(tree);
	}

	class PopupChooserMouseListener extends MouseAdapter {

		// @Override
		public void mousePressed(MouseEvent e) {
			verifyPopupTrigger(e);
		}

		// @Override
		public void mouseReleased(MouseEvent e) {
			verifyPopupTrigger(e);
		}

		private void verifyPopupTrigger(MouseEvent e) {
			if (customPopupHandler != null && e.isPopupTrigger()) {
				Point point = e.getPoint();
				TreePath path = tree.getClosestPathForLocation(point.x, point.y);
				if (path != null) {
					tree.getSelectionModel().setSelectionPath(path);
					TreeNode node = (TreeNode) path.getLastPathComponent();
					JPopupMenu menu = customPopupHandler.getMenuAt(tree, node);
					if (menu != null) {
						menu.show(tree, point.x, point.y);
					}
				}
			}
		}
	}

	/**
	 * Listens for changes in the cell renderer of the tree, and updates it with a DnDCellRendererProxy to allow for DnD borders.
	 */
	class RendererChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();

			if (name.equals(JTree.CELL_RENDERER_PROPERTY)) {
				TreeCellRenderer renderer = tree.getCellRenderer();
				if (!(renderer instanceof DnDCellRendererProxy)) {
					rendererProxy = new DnDCellRendererProxy(renderer);
					tree.setCellRenderer(rendererProxy);
					tree.repaint();
				} else
					rendererProxy = (DnDCellRendererProxy) renderer;
			}
		}
	}

	/**
	 * Internal class that implements DragGestureListener.
	 */
	class TreeDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {
			// If tree is disabled then discard drag from it
			if (!tree.isEnabled())
				return;

			// Select the node that the user is trying to drag, if any.
			TreePath draggedPath = tree.getClosestPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			if (draggedPath == null)
				return;
			TreeNode node = (TreeNode) draggedPath.getLastPathComponent();
			if (node instanceof MutableTreeNode && (node.getParent() != null) && (node.getParent() instanceof MutableTreeNode))
				;
			else
				return;

			// Prepare the node for transfer
			TransferableTreeNode transferableNode = new TransferableTreeNode(tree, (MutableTreeNode) node, tree.isExpanded(draggedPath));
			rendererProxy.setDraggedNode(node);

			// Initialize the drag. If isDragImageSupported then build a transparent image
			BufferedImage image = null;
			Point imageOffset = null;
			// Create an image with the dragged node.
			TreeCellRenderer renderer = rendererProxy.getOriginalTreeCellRenderer();
			Rectangle dragBounds = tree.getPathBounds(draggedPath);
			imageOffset = new Point(dge.getDragOrigin().x - dragBounds.x, dge.getDragOrigin().y - dragBounds.y);
			Component component = renderer.getTreeCellRendererComponent(tree, node, false, tree.isExpanded(draggedPath), node.isLeaf(), 0, false);
			component.setSize(dragBounds.width, dragBounds.height);
			image = new BufferedImage(dragBounds.width, dragBounds.height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = image.createGraphics();
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.75f));
			component.paint(g2d);
			g2d.dispose();

			// Initiate the drag
			if (DragSource.isDragImageSupported()) {
				dragSource.startDrag(dge, null, image, imageOffset, transferableNode, new TreeDragSourceListener());
			} else {
				dragSource.startDrag(dge, null, transferableNode, new TreeDragSourceListener());
			}
		}
	}

	/**
	 * Internal class that implements DragSourceListener.
	 */
	class TreeDragSourceListener implements DragSourceListener {
		public void dragExit(DragSourceEvent dse) {
			// dropNode = null;
			// rendererProxy.setDropNode( null );
			// tree.repaint();
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

		public void dragOver(DragSourceDragEvent dsde) {
		}

		public void dragEnter(DragSourceDragEvent dsde) {
		}

		public void dragDropEnd(DragSourceDropEvent dsde) {
			resetDragAndDrop();
		}
	}

	class TreeDropTargetListener implements DropTargetListener {
		public void drop(DropTargetDropEvent dtde) {
			TreePath dropPath = tree.getClosestPathForLocation(dtde.getLocation().x, dtde.getLocation().y);

			if (!tree.isEnabled() || dropPath == null) {
				dtde.rejectDrop();
				dtde.dropComplete(false);
				resetDragAndDrop();
				return;
			}

			dropNode = (MutableTreeNode) dropPath.getLastPathComponent();

			// Handle dropping java JVM local objects (tree nodes)
			try {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				// Are we dropping a JVM local object?
				TransferableTreeNode ttn = (TransferableTreeNode) dtde.getTransferable().getTransferData(TransferableTreeNode.getJavaJVMLocalObjectFlavor());
				JTree sourceTree = ttn.getSourceTree();
				MutableTreeNode sourceNode = ttn.getSourceNode();
				if (mayDropHere(sourceTree, sourceNode, dropNode)) {
					dtde.dropComplete(dropNodes(ttn.getSourceTree(), ttn.getSourceNode(), tree, dropNode,
							(dtde.getDropAction() & DnDConstants.ACTION_MOVE) != 0));
					if (ttn.isNodeWasExpanded()) {
						DefaultTreeModel targetModel = (DefaultTreeModel) tree.getModel();
						tree.expandPath(new TreePath(targetModel.getPathToRoot(ttn.getSourceNode())));
					}

					resetDragAndDrop();
				} else {
					try {
						dtde.rejectDrop();
					} catch (Exception e) {
						// An exception may be thrown here if the user leaves and enters the window.
						// This is an exceptional case.
					}
					dtde.dropComplete(false);
					resetDragAndDrop();
					return;
				}
			} catch (UnsupportedFlavorException ufe) {
				// So this is not a JVM local object, maybe it's a String...
				try {
					String droppedString = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
					if (mayDropHere(droppedString, tree, dropPath)) {
						dtde.dropComplete(dropString(droppedString));
						resetDragAndDrop();
					} else {
						dtde.rejectDrop();
						dtde.dropComplete(false);
						resetDragAndDrop();
						return;
					}
				} catch (Exception exception) {
					dtde.rejectDrop();
					dtde.dropComplete(false);
					resetDragAndDrop();
					return;
				}
			} catch (Exception e) {
				// ClassCastException: So this is a JVM local object but not a TransferableTreeNode, right? Well, then just discard
				// IOException: So there's a problem deserializing this, right? Well, then just discard
				e.printStackTrace();
				dtde.rejectDrop();
				dtde.dropComplete(true);
				resetDragAndDrop();
				return;
			}
		}

		private boolean dropString(String droppedString) {
			// Get the mutable TreeModel
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

			// Ask the listeners to handle this drop
			boolean doItOurselves = true;
			EventListener[] listeners = TreeWrapper.this.listeners.getListeners(StringTreeDnDListener.class);
			if (listeners != null && listeners.length > 0) {
				try {
					StringTreeDnDEvent event = new StringTreeDnDEvent(droppedString, tree, dropNode);
					for (int i = 0; i < listeners.length; i++) {
						((StringTreeDnDListener) listeners[i]).drop(event);
					}
				} catch (DnDVetoException exception) {
					doItOurselves = true;
				}
			}
			if (doItOurselves) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(droppedString);

				MutableTreeNode parent = (MutableTreeNode) dropNode.getParent();
				if (dropNode.isLeaf()) {
					int index = parent.getIndex(dropNode);
					model.insertNodeInto(newNode, parent, index + 1);
				} else {
					model.insertNodeInto(newNode, dropNode, 0); // dropNode.getChildCount() );
				}
			}
			return true;
		}

		private boolean dropNodes(JTree aSourceTree, MutableTreeNode aSourceNode, JTree aTargetTree, MutableTreeNode aDropNode, boolean move) {
			// Get the mutable TreeModel
			DefaultTreeModel sourceModel = (DefaultTreeModel) aSourceTree.getModel();
			DefaultTreeModel targetModel = (DefaultTreeModel) aTargetTree.getModel();

			boolean doItOurselves = true;
			EventListener[] listeners = TreeWrapper.this.listeners.getListeners(TreeTreeDnDListener.class);
			if (listeners != null && listeners.length > 0) {
				try {
					TreeTreeDnDEvent event = new TreeTreeDnDEvent(aSourceTree, aSourceNode, aTargetTree, aDropNode);
					for (int i = 0; i < listeners.length; i++) {
						((TreeTreeDnDListener) listeners[i]).drop(event);
					}
				} catch (DnDVetoException exception) {
					doItOurselves = false;
				}
			}
			if (doItOurselves) {
				MutableTreeNode sourceNodeCopy = aSourceNode;
				if (move) {
					sourceModel.removeNodeFromParent(aSourceNode);
				} else {
					sourceNodeCopy = recursivelyCopyNodes(targetModel, aSourceNode);
				}
				// Attach the draggedNode into the new parent
				MutableTreeNode parent = (MutableTreeNode) aDropNode.getParent();
				if (aDropNode.isLeaf() && parent != null) {
					int index = parent.getIndex(aDropNode);
					targetModel.insertNodeInto(sourceNodeCopy, parent, index + 1);
				} else {
					targetModel.insertNodeInto(sourceNodeCopy, aDropNode, 0);// aDropNode.getChildCount() );
				}
			}
			return true;
		}

		private DefaultMutableTreeNode recursivelyCopyNodes(DefaultTreeModel aModel, TreeNode aNode) {
			DefaultMutableTreeNode copy = new DefaultMutableTreeNode(aNode.toString());
			copy.setAllowsChildren(aNode.getAllowsChildren());
			if (aNode.getChildCount() != 0) {
				@SuppressWarnings("unchecked")
				Enumeration<TreeNode> children = (Enumeration<TreeNode>) aNode.children();
				while (children.hasMoreElements()) {
					TreeNode child = (TreeNode) children.nextElement();
					DefaultMutableTreeNode childCopy = recursivelyCopyNodes(aModel, child);
					aModel.insertNodeInto(childCopy, copy, copy.getChildCount());
					childCopy.setParent(copy);
				}
			}
			return copy;
		}

		public void dragExit(DropTargetEvent dte) {
			dropNode = null;
			rendererProxy.setDropNode(null);
			tree.repaint();
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		private Transferable getTransferable(DropTargetDragEvent dtde) {
			try {
				DropTargetContext context = dtde.getDropTargetContext();
				if (getTransferableMethod == null) {
					getTransferableMethod = context.getClass().getDeclaredMethod("getTransferable", EMPTY_CLASS_ARRAY);
					getTransferableMethod.setAccessible(true);
				}
				return (Transferable) getTransferableMethod.invoke(context, EMPTY_OBJECT_ARRAY);
			} catch (Exception e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "getTransferable failed", e);
				return null;
			}
		}

		/** This node to avoid too many invocations to dragOver */
		private TreeNode lastDragOverNode = null;

		public void dragOver(DropTargetDragEvent dtde) {
			if (!tree.isEnabled()) {
				dtde.rejectDrag();
				return;
			}

			// Is this a valid node for dropping?
			TreePath dropPath = tree.getClosestPathForLocation(dtde.getLocation().x, dtde.getLocation().y);

			TreeNode currentDropNode = (TreeNode) dropPath.getLastPathComponent();

			if (dropPath == null || currentDropNode == null || currentDropNode.equals(lastDragOverNode)) {
				return;
			} else {
				lastDragOverNode = currentDropNode;
			}

			Transferable transferable = getTransferable(dtde); // dtde.getTransferable();

			boolean mayDropHere = false;

			try {
				// WARNING: getTransferable available only on JDK 1.5
				TransferableTreeNode transferredNode = (TransferableTreeNode) transferable.getTransferData(TransferableTreeNode.getJavaJVMLocalObjectFlavor());
				JTree sourceTree = transferredNode.getSourceTree();
				MutableTreeNode sourceNode = transferredNode.getSourceNode();
				if (mayDropHere(sourceTree, sourceNode, dropPath)) {
					dropNode = (MutableTreeNode) dropPath.getLastPathComponent();
					if (!tree.isExpanded(dropPath))
						tree.expandPath(dropPath);
					mayDropHere = true;
				} else {
					dropNode = null;
				}
			} catch (UnsupportedFlavorException ufe) {
				// Oh, this is not a TransferableTreeNode, so maybe is a String, maybe?
				try {
					String sourceText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
					if (mayDropHere(sourceText, tree, dropPath)) {
						dropNode = (MutableTreeNode) dropPath.getLastPathComponent();
						if (!tree.isExpanded(dropPath))
							tree.expandPath(dropPath);
						mayDropHere = true;
					} else {
						dropNode = null;
					}
				} catch (Exception e) {
					// Well, whatever, just discard
					dropNode = null;
				}
			} catch (Exception e) {
				// IOException: Oh, there's a problem with serialization. Maybe a classloader issue? Well, ummm... just discard this and say no
				// ClassCastException: Oh, user is transferring a JVM object but not a TransferableTreeNode, well, umm... just discard and say no
				dropNode = null;
			}
			rendererProxy.setDropAllowed(mayDropHere);
			rendererProxy.setDropNode((TreeNode) dropPath.getLastPathComponent());
			tree.repaint();
			if (!mayDropHere) {
				dtde.rejectDrag();
			} else {
				dtde.acceptDrag(dtde.getDropAction());
			}
			tree.repaint();
		}

		public void dragEnter(DropTargetDragEvent dtde) {
			dragOver(dtde);
			// dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE );
		}
	}

	/**
	 * Invoked to decide if a given String can be dropped in the last path component of the given path.
	 * 
	 * @param aSourceString
	 *            the String being dragged.
	 * @param aPath
	 *            the path to drop into.
	 * @return true to allow the drop operation, false otherwise.
	 */
	private boolean mayDropHere(String aSourceString, JTree aTargetTree, TreePath aPath) {
		return mayDropHere(aSourceString, aTargetTree, (TreeNode) aPath.getLastPathComponent());
	}

	/**
	 * Invoked to decide if a given String can be dropped in the last path component of the given path.
	 * 
	 * @param aSourceString
	 *            the String being dragged.
	 * @param aPath
	 *            the path to drop into.
	 * @return true to allow the drop operation, false otherwise.
	 */
	private boolean mayDropHere(String aSourceString, JTree aTargetTree, TreeNode aNode) {
		EventListener[] listeners = this.listeners.getListeners(StringTreeDnDListener.class);
		if (listeners != null && listeners.length > 0) {
			try {
				StringTreeDnDEvent event = new StringTreeDnDEvent(aSourceString, aTargetTree, aNode);
				for (int i = 0; i < listeners.length; i++) {
					((StringTreeDnDListener) listeners[i]).mayDrop(event);
				}
			} catch (DnDVetoException exception) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Invoked to decide if draggedNode can be dropped in the last path component of the given path.
	 * 
	 * @param aSourceTree
	 *            the source tree.
	 * @param aSourceNode
	 *            the source node.
	 * @param aPath
	 *            the path to drop into.
	 * @return true to allow the drop operation, false otherwise.
	 */
	private boolean mayDropHere(JTree aSourceTree, MutableTreeNode aSourceNode, TreePath aPath) {
		if (aPath == null)
			return false;
		return mayDropHere(aSourceTree, aSourceNode, (TreeNode) aPath.getLastPathComponent());
	}

	/**
	 * Invoked to decide if draggedNode can be dropped into aNode.
	 * 
	 * @param aSourceTree
	 *            the source tree.
	 * @param aSourceNode
	 *            the source node.
	 * @param aNode
	 *            the node to drop into.
	 * @return true to allow the drop operation, false to avoid it.
	 */
	private boolean mayDropHere(JTree aSourceTree, MutableTreeNode aSourceNode, TreeNode aNode) {
		boolean mayDropHere = (aNode != aSourceNode) && (aNode instanceof MutableTreeNode)
				&& ((aNode.getParent() == null) || (aNode.getParent() instanceof MutableTreeNode)) && (tree.getModel() instanceof DefaultTreeModel)
				&& !((tree == aSourceTree) && isAncestorOf(aSourceNode, aNode));

		if (mayDropHere) {
			// Ask listeners
			EventListener[] listeners = this.listeners.getListeners(TreeTreeDnDListener.class);
			if (listeners != null && listeners.length > 0) {
				try {
					TreeTreeDnDEvent event = new TreeTreeDnDEvent(aSourceTree, aSourceNode, tree, aNode);
					for (int i = 0; i < listeners.length; i++) {
						((TreeTreeDnDListener) listeners[i]).mayDrop(event);
					}
				} catch (DnDVetoException exception) {
					mayDropHere = false;
				}
			}
		}

		return mayDropHere;
	}

	/**
	 * See if aPossibleParent is ancestor of aNode
	 */
	private static boolean isAncestorOf(TreeNode aPossibleParent, TreeNode aNode) {
		if (aPossibleParent == null || aNode.getParent() == null)
			return false;
		else if (aNode.getParent() == aPossibleParent)
			return true;
		else
			return isAncestorOf(aPossibleParent, aNode.getParent());
	}

	private void resetDragAndDrop() {
		dropNode = null;
		rendererProxy.setDraggedNode(null);
		rendererProxy.setDropAllowed(false);
		rendererProxy.setDropNode(null);
		tree.repaint();
	}

	/**
	 * Returns the current CustomPopupHandler being used to handle popup gestures.
	 * 
	 * @return the current CustomPopupHandler or null if there's no popup menu handler.
	 */
	public CustomPopupHandler getCustomPopupHandler() {
		return this.customPopupHandler;
	}

	/**
	 * Setter for property customPopupHandler.
	 * 
	 * @param customPopupHandler
	 *            New value of property customPopupHandler.
	 */
	public void setCustomPopupHandler(CustomPopupHandler customPopupHandler) {
		this.customPopupHandler = customPopupHandler;
	}

}
