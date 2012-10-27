package nl.siegmann.epublib.viewer;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.siegmann.epublib.browsersupport.NavigationEvent;
import nl.siegmann.epublib.browsersupport.NavigationEventListener;
import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

import org.apache.commons.lang.StringUtils;

/**
 * Creates a JTree for navigating a Book via its Table of Contents.
 * 
 * @author paul
 *
 */
public class TableOfContentsPane extends JPanel implements NavigationEventListener {

	private static final long serialVersionUID = 2277717264176049700L;
	
	private Map<String, Collection<DefaultMutableTreeNode>> href2treeNode = new HashMap<String, Collection<DefaultMutableTreeNode>>();
	private JScrollPane scrollPane;
	private Navigator navigator;
	private JTree tree;
	
	/**
	 * Creates a JTree that displays all the items in the table of contents from the book in SectionWalker.
	 * Also sets up a selectionListener that updates the SectionWalker when an item in the tree is selected.
	 * 
	 * @param navigator
	 * @return
	 */
	public TableOfContentsPane(Navigator navigator) {
		super(new GridLayout(1, 0));
		this.navigator = navigator;
		navigator.addNavigationEventListener(this);

		this.scrollPane = new JScrollPane();
		add(scrollPane);
		initBook(navigator.getBook());
	}
	
	/**
	 * Wrapper around a TOCReference that gives the TOCReference's title when toString() is called
	 * .createTableOfContentsTree
	 * @author paul
	 *
	 */
	private static class TOCItem {
		private TOCReference tocReference;
		
		public TOCItem(TOCReference tocReference) {
			super();
			this.tocReference = tocReference;
		}
		
		public TOCReference getTOCReference() {
			return tocReference;
		}

		public String toString() {
			return tocReference.getTitle();
		}
	}
	
	private void addToHref2TreeNode(Resource resource, DefaultMutableTreeNode treeNode) {
		if (resource == null || StringUtils.isBlank(resource.getHref())) {
			return;
		}
		Collection<DefaultMutableTreeNode> treeNodes = href2treeNode.get(resource.getHref());
		if (treeNodes == null) {
			treeNodes = new ArrayList<DefaultMutableTreeNode>();
			href2treeNode.put(resource.getHref(), treeNodes);
		}
		treeNodes.add(treeNode);
	}
	
	private DefaultMutableTreeNode createTree(Book book) {
		TOCItem rootTOCItem = new TOCItem(new TOCReference(book.getTitle(), book.getCoverPage()));
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(rootTOCItem);
		addToHref2TreeNode(book.getCoverPage(), top);
		createNodes(top, book);
		return top;
	}
	
	private void createNodes(DefaultMutableTreeNode top, Book book) {
		addNodesToParent(top, book.getTableOfContents().getTocReferences());
	}
	
	private void addNodesToParent(DefaultMutableTreeNode parent, List<TOCReference> tocReferences) {
		if (tocReferences == null) {
			return;
		}
		for (TOCReference tocReference: tocReferences) {
			TOCItem tocItem = new TOCItem(tocReference);
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(tocItem);
			addToHref2TreeNode(tocReference.getResource(), treeNode);
			addNodesToParent(treeNode, tocReference.getChildren());
			parent.add(treeNode);
		}
	}

	
	@Override
	public void navigationPerformed(NavigationEvent navigationEvent) {
		if (this == navigationEvent.getSource()) {
			return;
		}
		if (navigationEvent.isBookChanged()) {
			initBook(navigationEvent.getCurrentBook());
			return;
		}
		if (this.tree == null) {
			return;
		}
		if (navigationEvent.getCurrentResource() == null) {
			return;
		}
		Collection<DefaultMutableTreeNode> treeNodes = href2treeNode.get(navigationEvent.getCurrentResource().getHref());
		if (treeNodes == null || treeNodes.isEmpty()) {
			if (navigationEvent.getCurrentSpinePos() == (navigationEvent.getOldSpinePos() + 1)) {
				return;
			}
			tree.setSelectionPath(null);
			return;
		}
		for (DefaultMutableTreeNode treeNode: treeNodes) {
			TreeNode[] path = treeNode.getPath();
			TreePath treePath = new TreePath(path);
			tree.setSelectionPath(treePath);
		}
	}

	private void initBook(Book book) {
		if (book == null) {
			return;
		}
		this.tree = new JTree(createTree(book));
		tree.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent me) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				TOCItem tocItem = (TOCItem) node.getUserObject();
				navigator.gotoResource(tocItem.getTOCReference().getResource(), tocItem.getTOCReference().getFragmentId(), TableOfContentsPane.this);
			}
		});

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//		tree.setRootVisible(false);
		tree.setSelectionRow(0);
		this.scrollPane.getViewport().removeAll();
		this.scrollPane.getViewport().add(tree);
	}
}
