package org.pdks.session;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.jboss.seam.annotations.Name;

@Name("excelUtil")
public class ExcelUtil implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1634377377517034595L;
	public static String FONT_NAME = "Arial";
	public static Integer NORMAL_WEIGHT = 8;
	public static Integer BOLD_WEIGHT = 9;
	public static final String HEADER_COLOR = "excelHeaderColor";
	public static final String ROW_COLOR = "excelRowColor";
	public static final String ALIGN_CENTER = "alignCenter";
	public static final String ALIGN_RIGHT = "alignRight";
	public static final String EVEN_COLOR = "excelEvenColor";
	public static final String EVEN_DAY_COLOR = "excelEvenDayColor";
	public static final String ODD_COLOR = "excelOddColor";
	public static final String ODD_DAY_COLOR = "excelOddDayColor";
	public static final String COLOR = "color";
	public static final String BACKGROUND_COLOR = "backgroundColor";
	public static final String FORMAT_NUMBER = "formatNumber";
	public static final String FORMAT_TUTAR = "formatTutar";
	public static final String FORMAT_DATE = "formatDate";
	public static final String FORMAT_DATETIME = "formatDateTime";
	public static final String FORMAT_TIME = "formatTime";
	public static final String FORMAT_DATA_NUMBER = "#,##0";
	public static final String FORMAT_DATA_TUTAR = "#,##0.00";
	public static final String FORMAT_DATA_TIME = "h:mm";

	private static HashMap<String, HashMap<String, Integer[]>> colorMap = new HashMap<String, HashMap<String, Integer[]>>();

	static Logger logger = Logger.getLogger(ExcelUtil.class);

	/**
	 * @param rgb1
	 * @param rgb2
	 * @param rgb3
	 * @return
	 */
	public static XSSFColor getXSSFColor(Integer rgb1, Integer rgb2, Integer rgb3) {
		XSSFColor color = new XSSFColor(new byte[] { rgb1.byteValue(), rgb2.byteValue(), rgb3.byteValue() });
		return color;
	}

	/**
	 * @param firstRow
	 * @param firstCol
	 * @param lastRow
	 * @param lastCol
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * @param drawing
	 * @param anchor
	 * @param cell
	 * @param str1
	 */
	public static void setCellComment(Drawing drawing, ClientAnchor anchor, Cell cell, RichTextString str1) {
		if (str1 != null && cell != null) {
			Comment comment1 = drawing.createCellComment(anchor);
			comment1.setString(str1);
			cell.setCellComment(comment1);
		}
	}

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 */
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

	/**
	 * @param cell
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
	public static Double getSheetDoubleValue(Sheet sheet, int row, int col) throws Exception {
		Cell cell = getCell(sheet, row, col);
		Double value = cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ? new Double(cell.getNumericCellValue()) : null;
		return value;
	}

	/**
	 * @param cell
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public static Date getSheetDateValue(Cell cell, String pattern) throws Exception {
		Date value = null;
		try {
			value = cell != null ? cell.getDateCellValue() : null;
		} catch (Exception e) {
			value = PdksUtil.convertToJavaDate(getSheetStringValue(cell), pattern);
		}

		return value;
	}

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
	public static Date getSheetDateValue(Sheet sheet, int row, int col) throws Exception {
		Cell cell = getCell(sheet, row, col);
		Date value = cell != null ? cell.getDateCellValue() : null;
		return value;
	}

	/**
	 * @param cell
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * @param sheet
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * @param formatStr
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleDayOdd(String formatStr, Workbook wb) {
		CellStyle style = formatOddEven(formatStr, wb);
		HashMap<String, Integer[]> styleMap = setStyleColor(ODD_DAY_COLOR, style);
		if (styleMap != null)
			styleMap = null;
		return style;
	}

	/**
	 * @param formatStr
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleDayEven(String formatStr, Workbook wb) {
		CellStyle style = formatOddEven(formatStr, wb);
		HashMap<String, Integer[]> styleMap = setStyleColor(EVEN_DAY_COLOR, style);
		if (styleMap != null)
			styleMap = null;
		return style;
	}

	/**
	 * @param style
	 * @param alignment
	 * @return
	 */
	public static CellStyle setAlignment(CellStyle style, short alignment) {
		style.setAlignment(alignment);
		return style;
	}

	/**
	 * @param style
	 * @param boldweight
	 * @return
	 */
	public static XSSFCellStyle setBoldweight(CellStyle style, short boldweight) {
		XSSFCellStyle xssfCellStyle = (XSSFCellStyle) style;
		XSSFFont font = xssfCellStyle.getFont();
		font.setBoldweight(boldweight);
		return xssfCellStyle;
	}

	/**
	 * @param formatStr
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleOdd(String formatStr, Workbook wb) {
		CellStyle style = formatOddEven(formatStr, wb);
		HashMap<String, Integer[]> styleMap = setStyleColor(ODD_COLOR, style);
		if (!styleMap.containsKey(BACKGROUND_COLOR)) {
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			XSSFCellStyle styleOdd = (XSSFCellStyle) style;
			setFillForegroundColor(styleOdd, 219, 248, 219);
		}
		styleMap = null;
		return style;
	}

	/**
	 * @param formatStr
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleEven(String formatStr, Workbook wb) {
		CellStyle style = formatOddEven(formatStr, wb);
		HashMap<String, Integer[]> styleMap = setStyleColor(EVEN_COLOR, style);
		if (!styleMap.containsKey(BACKGROUND_COLOR)) {
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			XSSFCellStyle styleEven = (XSSFCellStyle) style;
			setFillForegroundColor(styleEven, 213, 228, 251);

		}

		styleMap = null;
		return style;
	}

	/**
	 * @param formatStr
	 * @param wb
	 * @return
	 */
	private static CellStyle formatOddEven(String formatStr, Workbook wb) {
		CellStyle style = getStyleData(wb);
		if (formatStr != null) {
			String str = null;
			if (formatStr.equals(FORMAT_NUMBER)) {
				style.setAlignment(CellStyle.ALIGN_RIGHT);
				str = FORMAT_DATA_NUMBER;
			} else if (formatStr.equals(FORMAT_TUTAR)) {
				style.setAlignment(CellStyle.ALIGN_RIGHT);
				str = FORMAT_DATA_TUTAR;
			} else if (formatStr.equals(FORMAT_TIME)) {
				style.setAlignment(CellStyle.ALIGN_CENTER);
				str = FORMAT_DATA_TIME;
			} else if (formatStr.equals(FORMAT_DATE)) {
				style.setAlignment(CellStyle.ALIGN_CENTER);
				str = PdksUtil.getDateFormat();
			} else if (formatStr.equals(FORMAT_DATETIME)) {
				style.setAlignment(CellStyle.ALIGN_CENTER);
				str = PdksUtil.getDateFormat() + " " + FORMAT_DATA_TIME;
			} else if (formatStr.equals(ALIGN_CENTER)) {
				style.setAlignment(CellStyle.ALIGN_CENTER);
				str = "";
			} else if (formatStr.equals(ALIGN_RIGHT)) {
				style.setAlignment(CellStyle.ALIGN_RIGHT);
				str = "";
			}
			if (str != null) {
				if (PdksUtil.hasStringValue(str)) {
					Integer formatType = getDataFormat(str, wb);
					if (formatType != null) {
						style.setDataFormat(formatType.shortValue());
					}
				}
			} else
				style.setAlignment(CellStyle.ALIGN_LEFT);

		}
		return style;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleDataCenter(Workbook wb) {
		CellStyle style = getStyleData(wb);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		return style;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleDataRight(Workbook wb) {
		CellStyle style = getStyleData(wb);
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		return style;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getStyleData(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
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
			boldweight = NORMAL_WEIGHT;
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
		Font font = createFont(wb, NORMAL_WEIGHT.shortValue(), FONT_NAME, Font.BOLDWEIGHT_NORMAL);
		style.setFont(font);
	}

	/**
	 * @param wb
	 * @param style
	 */
	public static void setFontNormalBold(Workbook wb, CellStyle style) {
		Font font = createFont(wb, NORMAL_WEIGHT.shortValue(), FONT_NAME, Font.BOLDWEIGHT_BOLD);
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
		Font font = setHeaderFont(BOLD_WEIGHT, wb);
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
		style.setFillForegroundColor(IndexedColors.WHITE.index);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setFont(font);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setWrapText(Boolean.TRUE);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		HashMap<String, Integer[]> styleMap = setStyleColor(HEADER_COLOR, style);
		if (styleMap != null)
			styleMap = null;
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
		CellStyle cellStyleTutar = getCellStyleFormat(FORMAT_DATA_TUTAR, Integer.parseInt(String.valueOf(CellStyle.ALIGN_RIGHT)), wb);
		return cellStyleTutar;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleNumber(Workbook wb) {
		CellStyle cellStyleTutar = getCellStyleFormat(FORMAT_DATA_NUMBER, Integer.parseInt(String.valueOf(CellStyle.ALIGN_RIGHT)), wb);
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

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleTimeStamp(Workbook wb) {
		CellStyle cellStyleDate = getCellStyleFormat(PdksUtil.getDateFormat() + " " + FORMAT_DATA_TIME, Integer.parseInt(String.valueOf(CellStyle.ALIGN_CENTER)), wb);
		return cellStyleDate;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleTime(Workbook wb) {
		CellStyle cellStyleDate = getCellStyleFormat(FORMAT_DATA_TIME, Integer.parseInt(String.valueOf(CellStyle.ALIGN_CENTER)), wb);
		return cellStyleDate;
	}

	/**
	 * @param wb
	 * @return
	 */
	public static CellStyle getCellStyleDate(Workbook wb) {
		CellStyle cellStyleDate = getCellStyleFormat(PdksUtil.getDateFormat(), Integer.parseInt(String.valueOf(CellStyle.ALIGN_CENTER)), wb);
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

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @return
	 */
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

	/**
	 * @param cellStyle
	 * @param rgb1
	 * @param rgb2
	 * @param rgb3
	 */
	public static void setFontColor(CellStyle cellStyle, Integer rgb1, Integer rgb2, Integer rgb3) {
		XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
		xssfCellStyle.getFont().setColor(getXSSFColor(rgb1, rgb2, rgb3));
	}

	/**
	 * @param cellStyle
	 * @param color
	 */
	public static void setFontColor(CellStyle cellStyle, Color color) {
		XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
		xssfCellStyle.getFont().setColor(new XSSFColor(color));
	}

	/**
	 * @param cellStyle
	 * @param rgb1
	 * @param rgb2
	 * @param rgb3
	 */
	public static void setFillForegroundColor(CellStyle cellStyle, Integer rgb1, Integer rgb2, Integer rgb3) {
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
		xssfCellStyle.setFillForegroundColor(getXSSFColor(rgb1, rgb2, rgb3));
	}

	/**
	 * @param type
	 * @param cellStyle
	 */
	protected static HashMap<String, Integer[]> setStyleColor(String type, CellStyle cellStyle) {
		HashMap<String, Integer[]> styleMap = new HashMap<String, Integer[]>();
		if (colorMap.containsKey(type)) {
			XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
			HashMap<String, Integer[]> map = colorMap.get(type);
			if (map.containsKey(BACKGROUND_COLOR)) {
				Integer[] colors = map.get(BACKGROUND_COLOR);
				if (colors != null && colors.length == 3) {
					cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
					XSSFColor color = getXSSFColor(colors[0], colors[1], colors[2]);
					if (colors != null) {
						xssfCellStyle.setFillForegroundColor(color);
						styleMap.put(BACKGROUND_COLOR, colors);
					}

				}

			}
			if (map.containsKey(COLOR)) {
				Integer[] colors = map.get(COLOR);
				if (colors != null && colors.length == 3) {
					XSSFColor color = getXSSFColor(colors[0], colors[1], colors[2]);
					if (color != null) {
						styleMap.put(COLOR, colors);
						xssfCellStyle.getFont().setColor(color);
					}

				}
			}
		}
		return styleMap;

	}

	/**
	 * @return the colorMap
	 */
	public static HashMap<String, HashMap<String, Integer[]>> getColorMap() {
		return colorMap;
	}

	/**
	 * @param colorMap
	 *            the colorMap to set
	 */
	public static void setColorMap(HashMap<String, HashMap<String, Integer[]>> colorMap) {
		ExcelUtil.colorMap = colorMap;
	}

}
