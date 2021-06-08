package tp1.api.service.java;

import tp1.api.Spreadsheet;

public interface RepSpreadsheets extends Spreadsheets {
	Result<String> createSpreadsheet_Rep(Spreadsheet sheet, String password );

	Result<Void> deleteSpreadsheet_Rep(String sheetId, String password);

	Result<Spreadsheet> getSpreadsheet_Rep(String sheetId , String userId, String password);
			
	Result<Void> shareSpreadsheet_Rep( String sheetId, String userId, String password);
	
	Result<Void> unshareSpreadsheet_Rep( String sheetId, String userId, String password);
	
	Result<Void> updateCell_Rep( String sheetId, String cell, String rawValue, String userId, String password);

	Result<String[][]> getSpreadsheetValues_Rep(String sheetId, String userId, String password);
}
