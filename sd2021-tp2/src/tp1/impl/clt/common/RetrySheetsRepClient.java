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
	public Result<String> createSpreadsheet(Spreadsheet sheet, String password) {
		return reTry( () -> impl.createSpreadsheet(sheet, password));
	}

	@Override
	public Result<Spreadsheet> getSpreadsheet(String sheetId, String userId, String password) {
		return reTry( () -> impl.getSpreadsheet(sheetId, userId, password));
	}

	@Override
	public Result<Void> deleteSpreadsheet(String sheetId, String password) {
		return reTry( () -> impl.deleteSpreadsheet(sheetId, password));
	}

	@Override
	public Result<Void> shareSpreadsheet(String sheetId, String userId, String password) {
		return reTry( () -> impl.shareSpreadsheet(sheetId, userId, password));
	}

	@Override
	public Result<Void> unshareSpreadsheet(String sheetId, String userId, String password) {
		return reTry( () -> impl.unshareSpreadsheet(sheetId, userId, password));
	}

	@Override
	public Result<Void> updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
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
	public Result<String> createSpreadsheet_Rep(Spreadsheet sheet, String password) {
		return reTry( () -> impl.createSpreadsheet_Rep(sheet, password));
	}

	@Override
	public Result<Void> deleteSpreadsheet_Rep(String sheetId, String password) {
		return reTry( () -> impl.deleteSpreadsheet_Rep(sheetId, password));
	}

	@Override
	public Result<Spreadsheet> getSpreadsheet_Rep(String sheetId, String userId, String password) {
		return reTry( () -> impl.getSpreadsheet_Rep(sheetId, userId, password));
	}

	@Override
	public Result<Void> shareSpreadsheet_Rep(String sheetId, String userId, String password) {
		return reTry( () -> impl.shareSpreadsheet_Rep(sheetId, userId, password));
	}

	@Override
	public Result<Void> unshareSpreadsheet_Rep(String sheetId, String userId, String password) {
		return reTry( () -> impl.unshareSpreadsheet_Rep(sheetId, userId, password));
	}

	@Override
	public Result<Void> updateCell_Rep(String sheetId, String cell, String rawValue, String userId, String password) {
		return reTry( () -> impl.updateCell_Rep(sheetId, cell, rawValue, userId, password));
	}

	@Override
	public Result<String[][]> getSpreadsheetValues_Rep(String sheetId, String userId, String password) {
		return reTry( () -> impl.getSpreadsheetValues_Rep(sheetId, userId, password));
	}
}
