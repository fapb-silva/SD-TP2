package tp1.api.service.java;

import tp1.api.Spreadsheet;

public interface RepSpreadsheets{
	Result<Spreadsheet> createSpreadsheet(Spreadsheet sheet, String password );

	Result<String> deleteSpreadsheet(String sheetId, String password);

	Result<Spreadsheet> getSpreadsheet(String sheetId , String userId, String password);
			
	Result<Spreadsheet> shareSpreadsheet( String sheetId, String userId, String password);
	
	Result<Spreadsheet> unshareSpreadsheet( String sheetId, String userId, String password);
	
	Result<Spreadsheet> updateCell( String sheetId, String cell, String rawValue, String userId, String password);

	Result<String[][]> getSpreadsheetValues(String sheetId, String userId, String password);
		
	Result<Void> deleteSpreadsheets( String userId );
	
	Result<String[][]> fetchSpreadsheetValues( String sheetId, String userId );
	
	//REP calls
	
	Result<String> putSpreadsheet_Rep(Spreadsheet sheet, String sheetId );

	Result<Void> removeSpreadsheet_Rep(String sheetId);
			



}
