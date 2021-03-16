package com.restassured.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

import com.restassured.constants.Constants;

public class TestUtils {

	public static FileInputStream fs;
	public static XSSFWorkbook workbook;
	public static XSSFSheet sheet;
	public static List<String> testCases = new ArrayList<String>();
	public static List<String> runStatus = new ArrayList<String>();
	public static List<String> testDescription = new ArrayList<String>();
	public static List<String> invocationCount = new ArrayList<String>();
	public static List<String> priority = new ArrayList<String>();
	public static HashMap<Integer, String> rowAndTestCaseMap = new HashMap<Integer, String>();
	static int count = 0;

	public static void getRunStatus() throws Exception {
		try {
			fs = new FileInputStream(Constants.EXCELPATH);
			workbook = new XSSFWorkbook(fs);
			sheet = workbook.getSheet(Constants.RUNMANAGERSHEET);
			for (int i = 1; i <= getLastRowNum(Constants.RUNMANAGERSHEET); i++) {
				testCases.add(getCellContent(Constants.RUNMANAGERSHEET, i, "TestCaseName"));
				testDescription.add(getCellContent(Constants.RUNMANAGERSHEET, i, "Test Case Description"));
				runStatus.add(getCellContent(Constants.RUNMANAGERSHEET, i, "Execute"));
				invocationCount.add(getCellContent(Constants.RUNMANAGERSHEET, i, "InvocationCount"));
				priority.add(getCellContent(Constants.RUNMANAGERSHEET, i, "Priority"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static int getRowNumForRowName(String sheetname, String rowName) {
		int rownum = 0;
		sheet = workbook.getSheet(sheetname);
		for (int i = 1; i <= getLastRowNum(sheetname); i++) {
			if (rowName.equalsIgnoreCase(sheet.getRow(i).getCell(0).getStringCellValue())) {
				rownum = i;
				break;
			}
		}

		return rownum;
	}

	public static int getColumnNumForColumnName(String sheetname, String columnname) {
		int colnum = 0;
		sheet = workbook.getSheet(sheetname);
		for (int i = 0; i < getLastColumnNum(sheetname, 0); i++) {
			if (columnname.equalsIgnoreCase(sheet.getRow(0).getCell(i).getStringCellValue())) {
				colnum = i;
				break;
			}
		}

		return colnum;

	}

	public static int getLastRowNum(String sheetname) {
		return workbook.getSheet(sheetname).getLastRowNum();
	}

	public static int getLastColumnNum(String sheetname, int rownum) {
		return workbook.getSheet(sheetname).getRow(rownum).getLastCellNum();
	}

	public static String getCellContent(String sheetname, int rownum, int colnum) {
		sheet = workbook.getSheet(sheetname);
		int celltype = sheet.getRow(rownum).getCell(colnum).getCellType();
		Cell cell = sheet.getRow(rownum).getCell(colnum);
		String temp = "";
		if (celltype == Cell.CELL_TYPE_STRING) {
			temp = cell.getStringCellValue();
		} else if (celltype == Cell.CELL_TYPE_NUMERIC || celltype == Cell.CELL_TYPE_FORMULA) {
			temp = String.valueOf(cell.getNumericCellValue());
		} else if (celltype == cell.CELL_TYPE_BOOLEAN) {
			temp = String.valueOf(cell.getBooleanCellValue());
		} else if (celltype == Cell.CELL_TYPE_ERROR) {
			temp = String.valueOf(cell.getErrorCellValue());
		}
		return temp;

	}

	public static String getCellContent(String sheetname, int rownum, String columnname) {
		sheet = workbook.getSheet(sheetname);
		int colnum = getColumnNumForColumnName(sheetname, columnname);
		return getCellContent(sheetname, rownum, colnum);

	}

	public static String getCellContent(String sheetname, String rowname, String columnname) {
		sheet = workbook.getSheet(sheetname);
		int rownum = getRowNumForRowName(sheetname, rowname);
		int colnum = getColumnNumForColumnName(sheetname, columnname);

		return getCellContent(sheetname, rownum, colnum);

	}

	public static void setCellContent(String sheetname, int rownum, int colnum, String value) {
		sheet = workbook.getSheet(sheetname);
		if (rownum <= getLastRowNum(sheetname)) {
			sheet.getRow(rownum).createCell(colnum).setCellValue(value);
		} else {
			sheet.createRow(rownum).createCell(colnum).setCellValue(value);
		}
	}

	public static void setCellContent(String sheetname, String rowname, int colnum, String value) {
		sheet = workbook.getSheet(sheetname);
		setCellContent(sheetname, getRowNumForRowName(sheetname, rowname), colnum, value);
	}

	public static void setCellContent(String sheetname, int rownum, String colname, String value) {
		sheet = workbook.getSheet(sheetname);
		setCellContent(sheetname, rownum, getColumnNumForColumnName(sheetname, colname), value);
	}

	public static void setCellContent(String sheetname, String rowname, String colname, String value) {
		sheet = workbook.getSheet(sheetname);
		setCellContent(sheetname, getRowNumForRowName(sheetname, rowname),
				getColumnNumForColumnName(sheetname, colname), value);
	}

	@DataProvider(name = "dataProviderForIterations", parallel = false)
	public static Object[][] supplyDataForIterations(Method m) {

		try {
			if (count == 0) {
				TestUtils.getRunStatus();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getDataForDataprovider(Constants.EXCELPATH, Constants.TESTDATASHEETNAME, m.getName());
	}

	private static Object[][] getDataForDataprovider(String testdata, String sheetname, String testcasename) {

		int totalcolumns = getLastColumnNum(sheetname, 0);
		ArrayList<Integer> rowscount = getNumberofIterationsForATestCase(sheetname, testcasename);
		Object[][] b = new Object[rowscount.size()][1];
		Hashtable<String, String> table = null;
		for (int i = 1; i <= rowscount.size(); i++) {
			table = new Hashtable<String, String>();
			for (int j = 0; j < totalcolumns; j++) {
				table.put(getCellContent(sheetname, 0, j), getCellContent(sheetname, rowscount.get(i - 1), j));
				b[i - 1][0] = table;
			}
		}
		return b;
	}

	private static ArrayList<Integer> getNumberofIterationsForATestCase(String sheetname, String testcasename) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		for (int i = 1; i <= getLastRowNum(sheetname); i++) {
			if (testcasename.equalsIgnoreCase(getCellContent(sheetname, i, 0))) {
				if (getCellContent(sheetname, i, 1).equalsIgnoreCase("Yes")) {
					a.add(i);
				}
			}
		}

		return a;
	}

	public static int getRowNumForTheTestCase(String sheetname, String testcasename) {
		int rownum = 0;
		for (int i = 1; i <= getLastRowNum(sheetname); i++) {
			if (testcasename.equalsIgnoreCase(getCellContent(sheetname, i, 0))) {
				rownum = i;
				break;
			}
		}
		return rownum;
	}

	public static String encode(String string) {
		return Base64.getEncoder().encodeToString(string.getBytes());
	}

	public static String decode(String string) {
		return new String(Base64.getDecoder().decode(string));
	}
}
