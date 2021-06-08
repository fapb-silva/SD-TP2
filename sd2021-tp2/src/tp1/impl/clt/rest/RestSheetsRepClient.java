package tp1.impl.clt.rest;

import java.net.URI;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.service.java.RepSpreadsheets;
import tp1.api.service.java.Result;
import tp1.api.service.rest.RestSpreadsheets;

public class RestSheetsRepClient extends RestClient implements RepSpreadsheets {
	private static final String PASSWORD = "password";
	private static final String USERID = "userId";
	private static final String VALUES = "/values";
	private static final String SHEETS = "/sheets";
	private static final String FETCH = "/fetch";
	private static final String REP = "/rep";

	public RestSheetsRepClient(URI serverUri) {
		super(serverUri, RestSpreadsheets.PATH);
	}

	@Override
	public Result<Spreadsheet> createSpreadsheet(Spreadsheet sheet, String password) {
		Response r = target
				.queryParam(PASSWORD, password)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.post( Entity.entity(sheet, MediaType.APPLICATION_JSON));
		return super.responseContents(r, Status.OK, new GenericType<Spreadsheet>() {});
	}

	@Override
	public Result<String> deleteSpreadsheet(String sheetId, String password) {
		Response r = target
				.path("/" + sheetId)
				.queryParam(PASSWORD, password)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.delete();
		return super.responseContents(r, Status.OK, new GenericType<String>() {});
	}

	@Override
	public Result<Spreadsheet> getSpreadsheet(String sheetId, String userId, String password) {
		Response r = target.path("/")
				.path(sheetId)
				.queryParam(PASSWORD, password)
				.queryParam(USERID, userId)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.get();
		return super.responseContents(r, Status.OK, new GenericType<Spreadsheet>() {});
	}

	@Override
	public Result<Spreadsheet> shareSpreadsheet(String sheetId, String userId, String password) {
		Response r = target.path(String.format("/%s/share/%s", sheetId, userId))
				.queryParam(PASSWORD, password)
				.request()
				.post(Entity.json(""));
		return super.responseContents(r, Status.OK, new GenericType<Spreadsheet>() {});
	}

	@Override
	public Result<Spreadsheet> unshareSpreadsheet(String sheetId, String userId, String password) {
		Response r = target.path(String.format("/%s/share/%s", sheetId, userId))
				.queryParam(PASSWORD, password)
				.request()
				.delete();
		return super.responseContents(r, Status.OK, new GenericType<Spreadsheet>() {});
	}

	@Override
	public Result<Spreadsheet> updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		Response r = target.path(String.format("/%s/%s", sheetId, cell))
				.queryParam(PASSWORD, password)
				.queryParam(USERID, userId)
				.request()
				.put(Entity.entity(MediaType.APPLICATION_JSON, rawValue));
		
		return super.responseContents(r, Status.OK, new GenericType<Spreadsheet>() {});
	}

	@Override
	public Result<String[][]> getSpreadsheetValues(String sheetId, String userId, String password) {
		Response r = target
				.path(sheetId).path(VALUES)
				.queryParam(PASSWORD, password)
				.queryParam(USERID, userId)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.get();
		
		return super.responseContents(r, Status.OK, new GenericType<String[][]>() {});
	}

	@Override
	public Result<Void> deleteSpreadsheets( String userId) {
		Response r = target
				.path(userId).path(SHEETS)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.delete();
		
		return super.verifyResponse(r, Status.NO_CONTENT);
	}

	@Override
	public Result<String[][]> fetchSpreadsheetValues(String sheetId, String userId) {
		Response r = target.path( sheetId).path(FETCH)
				.queryParam(USERID, userId)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.get();
		
		return super.responseContents(r, Status.OK, new GenericType<String[][]>() {});
	}

	//_________________________________________________________REP________________________________________________
	
	@Override
	public Result<String> postSpreadsheet_Rep(Spreadsheet sheet, String sheetId) {
		Response r = target
				.path(REP)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.post( Entity.entity(sheet, MediaType.APPLICATION_JSON));
		return super.responseContents(r, Status.OK, new GenericType<String>() {});
	}

	@Override
	public Result<Void> removeSpreadsheet_Rep(String sheetId) {
		Response r = target
				.path(REP+"/" + sheetId)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.delete();
		return super.responseContents(r, Status.NO_CONTENT, null);
	}


	@Override
	public Result<String> putSpreadsheet_Rep(Spreadsheet sheet, String sheetId) {
		Response r = target.path(REP+String.format("/%s/share", sheetId))
				.request()
				.put(Entity.entity(sheet, MediaType.APPLICATION_JSON));
		return super.responseContents(r, Status.OK, new GenericType<String>() {});
	}

	@Override
	public Result<Void> deleteSpreadsheets_Rep(String userId) {
		Response r = target
				.path(userId).path(SHEETS)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.delete();
		
		return super.verifyResponse(r, Status.NO_CONTENT);
	}




}
