package org.pdks.session;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jboss.seam.annotations.Name;

@Name("excelUtil")
public class ExcelUtil implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1634377377517034595L;
	private static String FONT_NAME = "Arial";
	private static Integer BOLDWEIGHT = 8;

	static Logger logger = Logger.getLogger(ExcelUtil.class);

	public static CellRangeAddress getRegion(int firstRow, int firstCol, int lastRow, int lastCol) throws Exception {
		CellRangeAddress cellRangeAddress = null;
		try {
			cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}

		return cellRangeAddress;
	}

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
	public static String getSheetStringValueTry(Sheet sheet, int row, int col) throws Exception {
		String value = null;

		try {
			value = getSheetStringValue(sheet, row, col);
		} catch (Exception e) {
			Double d = getSheetDoubleValue(sheet, row, col);
			if (d != null)
				value = String.valueOf(d.longValue());

		}
		return value;
	}

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static Date getSheetDateValueTry(Sheet sheet, int row, int col, String pattern) throws Exception {
		Date tarih = null;
		try {
			tarih = ExcelUtil.getSheetDateValue(sheet, row, col);
		} catch (Exception e) {
			tarih = PdksUtil.convertToJavaDate(ExcelUtil.getSheetStringValue(sheet, row, col), pattern);
		}
		return tarih;
	}

	/**
	 * @param wb
	 * @param punto
	 * @param fontName
	 * @param boldweight
	 * @return
	 */
	public static Font createFont(Workbook wb, short fontHeightInPoint, String fontName, short boldweight) {
		Font headerFont = wb.createFont();
		headerFont.setFontHeightInPoints(fontHeightInPoint);
		headerFont.setFontName(fontName);
		headerFont.setBoldweight(boldweight);

		return headerFont;
	}

	public static Cell getCell(Sheet sheet, int row, int col) {
		Cell cell = null;
		try {
			if (sheet != null)
				cell = sheet.getRow(row).getCell(col);
		} catch (Exception e) {
			cell = null;
		}
		return cell;
	}

	public static Double getSheetDoubleValue(Cell cell) throws Exception {
		Double value = null;
		if (cell != null) {
			value = cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ? new Double(cell.getNumericCellValue()) : null;
			if (value == null) {
				String str = getSheetStringValue(cell);
				if (str.indexOf(",") > 0) {
					if (str.indexOf(".") > 0)
						str = PdksUtil.replaceAll(str, ".", ".");
					str = PdksUtil.replaceAll(str, ",", ".");
				}
				value = Double.parseDouble(str);
			}
		}

		return value;
	}

	public static Double getSheetDoubleValue(Sheet sheet, int row, int col) throws Exception {
		Cell cell = getCell(sheet, row, col);
		Double value = cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ? new Double(cell.getNumericCellValue()) : null;
		return value;
	}

	public static Date getSheetDateValue(Cell cell, String pattern) throws Exception {
		Date value = null;
		try {
			value = cell != null ? cell.getDateCellValue() : null;
		} catch (Exception e) {
			value = PdksUtil.convertToJavaDate(getSheetStringValue(cell), pattern);
		}

		return value;
	}

	public static Date getSheetDateValue(Sheet sheet, int row, int col) throws Exception {
		Cell cell = getCell(sheet, row, col);
		Date value = cell != null ? cell.getDateCellValue() : null;
		return value;
	}

	public static String getSheetStringValue(Cell cell) throws Exception {
		String value = null;
		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING)
			value = cell.getStringCellValue().trim();
		else if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
			value = "" + new Double(cell.getNumericCellValue()).longValue();
		else
			throw new Exception("not char cell");
		return value;
	}

	public static String getSheetStringValue(Sheet sheet, int row, int col) throws Exception {
		Cell cell = getCell(sheet, row, col);
		String value = null;
		if (cell != null) {
			if (cell.getCellType() == Cell.CELL_TYPE_STRING)
				value = cell.getStringCellValue().trim();
			else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
				value = String.valueOf(cell.getBooleanCellValue());
			else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				Double veri = cell.getNumericCellValue();
				if (veri != null)
					value = String.valueOf(veri.longValue());
			}
		}

		return value;
	}

	public static CellStyle getStyleData(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		setFontNormal(wb, style);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setWrapText(Boolean.TRUE);
		return style;
	}

	/**
	 * @param fontHeightInPoint
	 * @param boldweight
	 * @param style
	 * @param wb
	 */
	public static void setFont(Integer fontHeightInPoint, Integer boldweight, CellStyle style, Workbook wb) {
		if (fontHeightInPoint == null)
			boldweight = BOLDWEIGHT;
		if (boldweight == null)
			boldweight = Integer.parseInt(String.valueOf(Font.BOLDWEIGHT_NORMAL));
		Font font = createFont(wb, fontHeightInPoint.shortValue(), FONT_NAME, boldweight.shortValue());
		style.setFont(font);
	}

	/**
	 * @param wb
	 * @param style
	 */
	public static void setFontNormal(Workbook wb, CellStyle style) {
		Font font = createFont(wb, BOLDWEIGHT.shortValue(), FONT_NAME, Font.BOLDWEIGHT_NORMAL);
		style.setFont(font);
	}

	/**
	 * @param wb
	 * @param style
	 */
	public static void setFontBold(Workbook wb, CellStyle style) {
		Font font = createFont(wb, BOLDWEIGHT.shortValue(), FONT_NAME, Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
	}

	/**
	 * @param fontHeightInPoint
	 * @param wb
	 * @return
	 */
	public static Font setHeaderFont(Integer fontHeightInPoint, Workbook wb) {
		Font font = createFont(wb, fontHeightInPoint.shortValue(), FONT_NAME, HSSFFont.BOLDWEIGHT_BOLD);
		return font;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static Font setHeaderFont(Workbook wb) {
		Font font = setHeaderFont(11, wb);
		return font;
	}

	/**
	 * @param fontHeightInPoint
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleHeader(Integer fontHeightInPoint, Workbook wb) {
		CellStyle style = wb.createCellStyle();
		Font font = setHeaderFont(fontHeightInPoint, wb);
	 	style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setWrapText(Boolean.TRUE);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		return style;

	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleHeader(Workbook wb) {
		CellStyle style = getStyleHeader(11, wb);

		return style;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleTutar(Workbook wb) {
		CellStyle cellStyleTutar = getCellStyleFormat("#,##0.00", Integer.parseInt(String.valueOf(HSSFCellStyle.ALIGN_RIGHT)), wb);
		return cellStyleTutar;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleNumber(Workbook wb) {
		CellStyle cellStyleTutar = getCellStyleFormat("#,##0", Integer.parseInt(String.valueOf(HSSFCellStyle.ALIGN_RIGHT)), wb);
		return cellStyleTutar;
	}

	/**
	 * @param formatStr
	 * @param wb
	 * @return
	 */
	public static Integer getDataFormat(String formatStr, Workbook wb) {
		Integer format = null;
		if (PdksUtil.hasStringValue(formatStr) && wb != null)
			try {
				DataFormat df = wb.createDataFormat();
				format = Integer.parseInt(String.valueOf(df.getFormat(formatStr)));
			} catch (Exception e) {
				format = null;
			}

		return format;
	}

	// /**
	// * @param formatStr
	// * @param wb
	// * @return
	// */
	// public static CellStyle getCellStyleTutar(String formatStr, Workbook wb) {
	// CellStyle cellStyleTutar = getCellStyleFormat(formatStr, Integer.parseInt(String.valueOf(HSSFCellStyle.ALIGN_RIGHT)), wb);
	// return cellStyleTutar;
	// }

	public static CellStyle getCellStyleTimeStamp(Workbook wb) {
		CellStyle cellStyleDate = getCellStyleFormat(PdksUtil.getDateFormat() + " h:mm", Integer.parseInt(String.valueOf(HSSFCellStyle.ALIGN_CENTER)), wb);
		return cellStyleDate;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleTime(Workbook wb) {
		CellStyle cellStyleDate = getCellStyleFormat("h:mm", Integer.parseInt(String.valueOf(HSSFCellStyle.ALIGN_CENTER)), wb);
		return cellStyleDate;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleDate(Workbook wb) {
		CellStyle cellStyleDate = getCellStyleFormat(PdksUtil.getDateFormat(), Integer.parseInt(String.valueOf(HSSFCellStyle.ALIGN_CENTER)), wb);
		return cellStyleDate;
	}

	/**
	 * @param format
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleFormat(String formatStr, Integer alignment, Workbook wb) {
		CellStyle cellStyle = getStyleData(wb);
		if (alignment != null)
			cellStyle.setAlignment(alignment.shortValue());
		Integer format = getDataFormat(formatStr, wb);

		if (format != null)
			cellStyle.setDataFormat(format.shortValue());
		return cellStyle;

	}

	/**
	 * @param wb
	 * @param sheetName
	 * @param yatay
	 * @return
	 */
	public static Sheet createSheet(Workbook wb, String sheetName, boolean yatay) {
		Sheet sheet = wb.createSheet(sheetName);
		sheet.setFitToPage(Boolean.TRUE);
		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
		printSetup.setLandscape(yatay);
		printSetup.setHeaderMargin((double) .25);
		printSetup.setFooterMargin((double) .25);
		sheet.setMargin(HSSFSheet.TopMargin, (double) .50);
		sheet.setMargin(HSSFSheet.BottomMargin, (double) .50);
		sheet.setMargin(HSSFSheet.LeftMargin, (double) .20);
		sheet.setMargin(HSSFSheet.RightMargin, (double) .20);

		// logger.info(printSetup.get+" "+ printSetup.getFooterMargin());

		return sheet;

	}

	public static Cell getCell(Sheet sheet, int rowNo, int columnNo, CellStyle style) {
		Row row = sheet.getRow(rowNo);
		if (row == null)
			row = sheet.createRow(rowNo);
		Cell cell = row.createCell(columnNo);
		// cell.set setEncoding(HSSFCell.ENCODING_UTF_16);

		if (style != null)
			cell.setCellStyle(style);

		return cell;
	}

}
