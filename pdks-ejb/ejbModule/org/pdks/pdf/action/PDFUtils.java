package org.pdks.pdf.action;

import java.io.Serializable;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

public class PDFUtils implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4897717487256528074L;

	/**
	 * @param str
	 * @param font
	 * @return
	 */
	public static Paragraph getParagraph(String str, Font font, int alignment) {
		Paragraph paragraph = font != null ? new Paragraph(str, font) : new Paragraph(str);
		if (alignment < 1)
			alignment = Element.ALIGN_LEFT;
		paragraph.setAlignment(alignment);
		return paragraph;
	}

	/**
	 * @param i
	 * @param str
	 * @param font
	 * @return
	 */
	public static Cell getCellColspan(int i, String str, Font font) {
		Cell cell = null;
		try {
			cell = new Cell(getParagraph(str, font, 0));
			if (i > 1)
				cell.setColspan(i);
		} catch (BadElementException e) {

		}

		return cell;
	}

	/**
	 * @param i
	 * @param str
	 * @param font
	 * @return
	 */
	public static Cell getCell(String str, Font font) {
		Cell cell = null;
		try {
			cell = new Cell(getParagraph(str, font, Element.ALIGN_LEFT));
		} catch (BadElementException e) {

		}

		return cell;
	}

	/**
	 * @param i
	 * @param str
	 * @param font
	 * @return
	 */
	public static Cell getCell(String str, Font font, int alignment, int colspan) {
		Cell cell = null;
		try {
			cell = new Cell(getParagraph(str, font, alignment));
			cell.setHorizontalAlignment(alignment);
			if (colspan > 1)
				cell.setColspan(colspan);
		} catch (BadElementException e) {

		}

		return cell;
	}

	 
	/**
	 * @param str
	 * @param font
	 * @param alignment
	 * @param rowspan
	 * @return
	 */
	public static Cell getCellRowspan(String str, Font font, int alignment, int rowspan) {
		Cell cell = null;
		try {
			cell = new Cell(getParagraph(str, font, alignment));
			cell.setHorizontalAlignment(alignment);
			if (rowspan > 1)
				cell.setRowspan(rowspan);
		} catch (BadElementException e) {

		}

		return cell;
	}

	/**
	 * @param i
	 * @param str
	 * @param font
	 * @return
	 */
	public static Cell getCell(String str, Font font, int alignment) {
		Cell cell = getCell(str, font, alignment, 1);

		return cell;
	}

	/**
	 * @param colspan
	 * @param key
	 * @param str
	 * @param fontKey
	 * @param font
	 * @param newLine
	 * @return
	 */
	public static Cell getChunkCellAll(int colspan, String key, String str, Font fontKey, Font font, boolean newLine) {
		Cell cell1 = new Cell();
		Phrase phrase1 = new Phrase(10);

		Chunk chunkKey = key != null && fontKey != null ? new Chunk(key, fontKey) : null;

		Chunk chunkStr = str != null && font != null ? new Chunk(str, font) : null;
		if (chunkKey != null) {
			phrase1.add(chunkKey);
			if (newLine)
				phrase1.add(Chunk.NEWLINE);

		}

		if (chunkStr != null)
			phrase1.add(chunkStr);
		cell1.add(phrase1);
		if (colspan > 1)
			cell1.setColspan(colspan);

		return cell1;
	}

	/**
	 * @param colspan
	 * @param key
	 * @param str
	 * @param fontKey
	 * @param font
	 * @return
	 */
	public static Cell getChunkCell(int colspan, String key, String str, Font fontKey, Font font) {
		return getChunkCellAll(colspan, key, str, fontKey, font, false);
	}

	/**
	 * @param colspan
	 * @param key
	 * @param str
	 * @param fontKey
	 * @param font
	 * @return
	 */
	public static Cell getChunkBreakCell(int colspan, String key, String str, Font fontKey, Font font) {
		return getChunkCellAll(colspan, key, str, fontKey, font, true);
	}

}
