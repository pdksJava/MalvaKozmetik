package org.pdks.pdf.action;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderIText extends PdfPageEventHelper {

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
