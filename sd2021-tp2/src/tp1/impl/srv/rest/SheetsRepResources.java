package tp1.impl.srv.rest;

import java.net.URI;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.Spreadsheet;
import tp1.api.service.java.Spreadsheets;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.impl.clt.RepSheetsClientFactory;
import tp1.impl.clt.SpreadsheetsClientFactory;
import tp1.impl.srv.common.JavaSpreadsheets;
import tp1.impl.utils.IP;

@Singleton
@Path(RestSpreadsheets.PATH)
public class SheetsRepResources extends RestResource implements RestSpreadsheets {
	private static Logger Log = Logger.getLogger(SheetsRepResources.class.getName());

	private boolean isPrimary;

	final Spreadsheets impl;
	private String primaryURI;

	public SheetsRepResources(int isPrimary, String primaryURI) {
		this.isPrimary = isPrimary == 1;
		this.primaryURI = primaryURI;

		var uri = String.format("https://%s:%d/rest%s", IP.hostAddress(), RepRestServer.PORT, PATH);
		impl = new JavaSpreadsheets(uri);
	}

	public String createSpreadsheet(Spreadsheet sheet, String password) {
		Log.info(String.format("REST createSpreadsheet: sheet = %s\n", sheet));

		if (isPrimary) {
			//affect Primary
			String result = super.resultOrThrow(impl.createSpreadsheet(sheet, password));
			//share changes
			String uri = "";
			var resultFromRep = RepSheetsClientFactory.with(uri).createSpreadsheet(sheet, password);
			//w8 for response
			
			return result;
		}else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		Log.info(String.format("REST deleteSpreadsheet: sheetId = %s\n", sheetId));

		super.resultOrThrow(impl.deleteSpreadsheet(sheetId, password));
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		Log.info(String.format("REST getSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		return super.resultOrThrow(impl.getSpreadsheet(sheetId, userId, password));
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		Log.info(String.format("REST shareSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		super.resultOrThrow(impl.shareSpreadsheet(sheetId, userId, password));
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		Log.info(String.format("REST unshareSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		super.resultOrThrow(impl.unshareSpreadsheet(sheetId, userId, password));
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		Log.info(String.format("REST updateCell: sheetId = %s, cell= %s, rawValue = %s, userId = %s\n", sheetId, cell,
				rawValue, userId));

		super.resultOrThrow(impl.updateCell(sheetId, cell, rawValue, userId, password));
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		Log.info(String.format("REST getSpreadsheetValues: sheetId = %s, userId = %s\n", sheetId, userId));

		return super.resultOrThrow(impl.getSpreadsheetValues(sheetId, userId, password));
	}

	@Override
	public void deleteSpreadsheets(String userId) {
		Log.info(String.format("REST deleteSpreadsheets: userId = %s\n", userId));

		super.resultOrThrow(impl.deleteSpreadsheets(userId));
	}

	@Override
	public String[][] fetchSpreadsheetValues(String sheetId, String userId) {
		Log.info(String.format("REST fetchSpreadsheetValues: sheetId = %s, userId: %s\n", sheetId, userId));

		return super.resultOrThrow(impl.fetchSpreadsheetValues(sheetId, userId));
	}

}
