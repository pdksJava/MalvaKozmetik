package org.pdks.pdf.action;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class HeaderLowagie extends PdfPageEventHelper {

	protected Phrase header;

	public void setHeader(Phrase header) {
		this.header = header;
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte canvas = writer.getDirectContentUnder();
		if (header == null)
			header = new Phrase("Sayfa : " + document.getPageNumber());
		ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, header, 450, 30, 0);
	}
}
