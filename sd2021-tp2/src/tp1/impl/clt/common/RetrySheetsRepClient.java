package tp1.impl.clt.common;

import tp1.api.Spreadsheet;
import tp1.api.service.java.RepSpreadsheets;
import tp1.api.service.java.Result;

public class RetrySheetsRepClient extends RetryClient implements RepSpreadsheets {

	final RepSpreadsheets impl;

	public RetrySheetsRepClient( RepSpreadsheets impl ) {
		this.impl = impl;
	}
	
	@Override
	public Result<Spreadsheet> createSpreadsheet(Spreadsheet sheet, String password) {
		return reTry( () -> impl.createSpreadsheet(sheet, password));
	}

	@Override
	public Result<Spreadsheet> getSpreadsheet(String sheetId, String userId, String password) {
		return reTry( () -> impl.getSpreadsheet(sheetId, userId, password));
	}

	@Override
	public Result<String> deleteSpreadsheet(String sheetId, String password) {
		return reTry( () -> impl.deleteSpreadsheet(sheetId, password));
	}

	@Override
	public Result<Spreadsheet> shareSpreadsheet(String sheetId, String userId, String password) {
		return reTry( () -> impl.shareSpreadsheet(sheetId, userId, password));
	}

	@Override
	public Result<Spreadsheet> unshareSpreadsheet(String sheetId, String userId, String password) {
		return reTry( () -> impl.unshareSpreadsheet(sheetId, userId, password));
	}

	@Override
	public Result<Spreadsheet> updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		return reTry( () -> impl.updateCell(sheetId, cell, rawValue, userId, password));
	}

	@Override
	public Result<String[][]> getSpreadsheetValues(String sheetId, String userId, String password) {
		return reTry( () -> impl.getSpreadsheetValues(sheetId, userId, password));
	}

	@Override
	public Result<Void> deleteSpreadsheets(String userId) {
		return reTry( () -> impl.deleteSpreadsheets(userId));
	}

	@Override
	public Result<String[][]> fetchSpreadsheetValues(String sheetId, String userId) {
		return reTry( () ->impl.fetchSpreadsheetValues(sheetId, userId));
	}

	@Override
	public Result<String> postSpreadsheet_Rep(Spreadsheet sheet, String stringId) {
		return reTry( () -> impl.postSpreadsheet_Rep(sheet, stringId));
	}

	@Override
	public Result<Void> removeSpreadsheet_Rep(String sheetId) {
		return reTry( () -> impl.removeSpreadsheet_Rep(sheetId));
	}

	

	@Override
	public Result<String> putSpreadsheet_Rep(Spreadsheet sheet, String stringId) {
		return reTry( () -> impl.putSpreadsheet_Rep(sheet, stringId));
	}
	@Override
	public Result<Void> deleteSpreadsheets_Rep(String userId) {
		return reTry( () -> impl.deleteSpreadsheets_Rep(userId));
	}

	

	
}
