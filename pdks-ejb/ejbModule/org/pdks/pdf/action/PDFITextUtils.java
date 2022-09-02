package org.pdks.pdf.action;

import java.io.Serializable;

import org.pdks.session.PdksUtil;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

public class PDFITextUtils implements Serializable {

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
		Paragraph paragraph = font != null ? new Paragraph(str, font) : new Paragraph(PdksUtil.setTurkishStr(str));
		if (alignment < 1)
			alignment = Element.ALIGN_LEFT;
		paragraph.setAlignment(alignment);
		return paragraph;
	}

	/**
	 * @param str
	 * @param font
	 * @return
	 */
	public static Phrase getPhrase(String str, Font font) {
		Phrase phrase = font != null ? new Phrase(str, font) : new Phrase(PdksUtil.setTurkishStr(str));
		return phrase;
	}

	/**
	 * @param fontname
	 * @param size
	 * @param style
	 * @param color
	 * @return
	 */
	public static Font getPdfFont(String fontname, int size, int style, BaseColor color) {
		Font font = null;
		try {
			font = FontFactory.getFont(fontname);
			if (font != null) {
				font.setSize(size);
				font.setStyle(style);
				if (color == null)
					color = BaseColor.BLACK;
				font.setColor(color);

			}

		} catch (Exception e) {
			 
		}

		return font;
	}

	/**
	 * @param str
	 * @param font
	 * @param alignment
	 * @param colspan
	 * @return
	 */
	public static PdfPCell getPdfCell(String str, Font font, int alignment, int colspan) {

		PdfPCell cell = null;
		try {
			cell = new PdfPCell(getPhrase(str, font));
			cell.setHorizontalAlignment(alignment);
			if (colspan > 1)
				cell.setColspan(colspan);

		} catch (Exception e) {

		}

		return cell;

	}

	/**
	 * @param str
	 * @param font
	 * @param alignment
	 * @return
	 */
	public static PdfPCell getPdfCellRowspan(String str, Font font, int alignment) {
		PdfPCell cell = getPdfCellRowspan(str, font, alignment, 1);
		return cell;
	}

	/**
	 * @param str
	 * @param font
	 * @param alignment
	 * @param rowspan
	 * @return
	 */
	public static PdfPCell getPdfCellRowspan(String str, Font font, int alignment, int rowspan) {

		PdfPCell cell = null;
		try {
			cell = new PdfPCell(getPhrase(str, font));
			if (rowspan > 1)
				cell.setRowspan(rowspan);
			cell.setHorizontalAlignment(alignment);
		} catch (Exception e) {

		}

		return cell;

	}

	public static PdfPCell getPdfCellNowrap(String str, Font font, int alignment) {

		PdfPCell cell = getPdfCell(str, font, alignment, 1);
		cell.setNoWrap(true);
		return cell;

	}

	public static PdfPCell getPdfCell(String str, Font font, int alignment) {

		PdfPCell cell = getPdfCell(str, font, alignment, 1);

		return cell;

	}

}
