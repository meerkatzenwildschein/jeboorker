package org.bounce.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMUtils {
	public static boolean isWhiteSpace(Text node) {
		return node.getData().trim().length() == 0;
	}

	public static String getXPath(Node node) {
		StringBuilder builder = new StringBuilder();
		while (node != null) {
			if (node instanceof Comment) {
				builder.insert(0, "/comment()");
			} else if (node instanceof Text) {
				builder.insert(0, "/text()");
			} else if (node instanceof ProcessingInstruction) {
				builder.insert(0, "/processing-instruction()");
			} else if (node instanceof Element) {
				builder.insert(0, "/" + getQName((Element) node));
			} else if (node instanceof Attr) {
				builder.insert(0, "/@" + getQName((Attr) node));
			}
			node = node.getParentNode();
		}

		return builder.toString();
	}

	public static String getUniqueXPath(Node node) {
		StringBuilder builder = new StringBuilder();
		while (node != null) {
			if (node instanceof Comment) {
				builder.insert(0, "/comment()["+getXPathIndex(node)+"]");
			} else if (node instanceof Text) {
				builder.insert(0, "/text()["+getXPathIndex(node)+"]");
			} else if (node instanceof ProcessingInstruction) {
				builder.insert(0, "/processing-instruction()["+getXPathIndex(node)+"]");
			} else if (node instanceof Element) {
				builder.insert(0, "/" + getQName((Element) node)+"["+getXPathIndex(node)+"]");
			} else if (node instanceof Attr) {
				builder.insert(0, "/@" + getQName((Attr) node));
			}
			node = node.getParentNode();
		}

		return builder.toString();
	}
	
	private static int getXPathIndex(Node node) {
		Node parent = node.getParentNode();
		int index = 1;
		
		if (parent != null) {
			NodeList list = parent.getChildNodes();
			
			for (int i = 0; i < list.getLength(); i++) {
				if (node == list.item(i)) {
					break;
				}
				
				if (node.getNodeType() == list.item(i).getNodeType()) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						if (getQName((Element)node).equals(getQName((Element)list.item(i)))) {
							index++;
						}
					} else {
						index++;
					}
				}
			}
		}		
		
		return index;
	}

	public static boolean isMixed(Element element) {
		boolean elementFound = false;
		boolean textFound = false;

		NodeList nodes = element.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if (node instanceof Element) {
				elementFound = true;
			} else if (node instanceof Text) {
				if (!isWhiteSpace((Text) node)) {
					textFound = true;
				}
			}

			if (textFound && elementFound) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param parent
	 *            the parent element.
	 * @param name
	 *            the name of the tag to match on. The special value "*" matches
	 *            all tags.
	 * @return the first matching element.
	 */
	public Element getElementByTagName(Element parent, String name) {
		Element result = null;

		NodeList list = parent.getElementsByTagName(name);
		if (list.getLength() > 0) {
			result = (Element) list.item(0);
		}

		return result;
	}

	/**
	 * @param parent
	 *            the parent element.
	 * @param namespaceURI
	 *            The namespace URI of the elements to match on. The special
	 *            value "*" matches all namespaces.
	 * @param localName
	 *            The local name of the elements to match on. The special value
	 *            "*" matches all local names.
	 * @return the first matching element.
	 */
	public Element getElementByTagNameNS(Element parent, String namespaceURI, String localName) {
		Element result = null;

		NodeList list = parent.getElementsByTagNameNS(namespaceURI, localName);
		if (list.getLength() > 0) {
			result = (Element) list.item(0);
		}

		return result;
	}

	public static String getName(Attr attribute) {
		if (attribute.getLocalName() == null) {
			return attribute.getName();
		}

		return attribute.getLocalName();
	}

	public static String getName(Element element) {
		if (element.getLocalName() == null) {
			return element.getTagName();
		}

		return element.getLocalName();
	}

	public static String getQName(Element element) {
		if (element.getTagName() != null) {
			return element.getTagName();
		}

		return element.getLocalName();
	}

	public static String getQName(Attr attribute) {
		if (attribute.getLocalName() != null) {
			return attribute.getName();
		}

		return attribute.getLocalName();
	}

	public static boolean hasContent(Element element) {
		return element.getChildNodes().getLength() > 0;
	}

	public static String getText(Element element) {
		StringBuilder text = new StringBuilder();
		NodeList nodes = element.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Text) {
				text.append(((Text) nodes.item(i)).getData());
			}
		}

		return text.toString().trim();
	}

	public static boolean isTextOnly(Element element) {
		NodeList nodes = element.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (!(nodes.item(i) instanceof Text)) {
				return false;
			}
		}

		return true;
	}
}
