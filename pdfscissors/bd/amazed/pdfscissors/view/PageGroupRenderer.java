package bd.amazed.pdfscissors.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import bd.amazed.pdfscissors.model.PageGroup;

public class PageGroupRenderer extends JComponent implements ListCellRenderer {

	private PageGroup currentGroup;
	private float pdfWidth = 100;
	private float pdfHeight = 100;
	private int padding = 1;
	private boolean isSelected;
	private static Color selectedBg = new Color(0x00D0FF);

	public PageGroupRenderer() {
		if (getFont() == null) {
			setFont(new JLabel().getFont());
		}
	}

	public void setPageSize(float pdfWidth, float pdfHeight) {
		this.pdfWidth = pdfWidth;
		this.pdfHeight = pdfHeight;
	}


	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean hasFocus) {
		if (value instanceof PageGroup) {
			PageGroup pageGroup = (PageGroup) value;
			currentGroup = pageGroup;
		}
		this.isSelected = isSelected;
		return this;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (isSelected) {
			g.setColor(selectedBg);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		int offsetFromEdge = padding * 2;
		int boxWidth = getWidth() - offsetFromEdge * 2;
		int boxHeight = getHeight() - offsetFromEdge * 2 - getFontMetrics(getFont()).getHeight();
		g.setColor(Color.white);
		g.fillRect(offsetFromEdge, offsetFromEdge, boxWidth , boxHeight);
		g.setColor(Color.black);
		g.drawRect(offsetFromEdge, offsetFromEdge, boxWidth, boxHeight);
		//shadow
		g.drawLine(offsetFromEdge + 1, offsetFromEdge + boxHeight + 1, offsetFromEdge + boxWidth + 1, offsetFromEdge + boxHeight + 1);
		g.drawLine(offsetFromEdge + boxWidth + 1, offsetFromEdge + 1, offsetFromEdge + boxWidth + 1, offsetFromEdge + boxHeight + 1);

		if (currentGroup.getStackImage() != null) {
			g.drawImage(currentGroup.getStackImage().getScaledInstance(boxWidth, boxHeight, Image.SCALE_FAST), offsetFromEdge, offsetFromEdge, this);
		}

		Iterator<Rect> iter = currentGroup.getRects().iterator();
		g.translate(offsetFromEdge, offsetFromEdge);
		while (iter.hasNext()) {
			(iter.next()).draw(g, (getWidth()  - offsetFromEdge * 2)/ pdfWidth, Rect.STROKE_SOLID, false);
		}
		g.translate(-offsetFromEdge, -offsetFromEdge);


		String text = currentGroup.toString();
		int textWidth = g.getFontMetrics().stringWidth(text);
		int textHeight = g.getFontMetrics().getHeight();
		g.setColor(Color.black);
		g.drawString(text, (getWidth() - textWidth)/2, getHeight() - textHeight + 2);
	}

	@Override
	public Dimension getPreferredSize() {
		int prefWidth = 100;
		prefWidth = Math.max(prefWidth, (int)(pdfWidth / 10));
		padding = Math.max(1, prefWidth / 20);
		int prefHeight = (int)((prefWidth * pdfHeight) / pdfWidth);
		return new Dimension(prefWidth + padding * 4, prefHeight + padding * 4 + getFontMetrics(getFont()).getHeight());
	}

}