package tp1.impl.srv.rest;

import java.net.URI;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.Spreadsheet;
import tp1.api.service.java.RepSpreadsheets;
import tp1.api.service.rest.RepRestSpreadsheets;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.impl.clt.RepSheetsClientFactory;
import tp1.impl.discovery.Discovery;
import tp1.impl.srv.common.JavaRepSpreadsheets;
import tp1.impl.utils.IP;

@Singleton
@Path(RestSpreadsheets.PATH)
public class SheetsRepResources extends RestResource implements RepRestSpreadsheets {
	private static final String SERVICE_NAME = "sheets_rep";

	private static Logger Log = Logger.getLogger(SheetsRepResources.class.getName());

	private boolean isPrimary;

	final RepSpreadsheets impl;
	private String primaryURI;

	public SheetsRepResources(int isPrimary, String primaryURI) {
		this.isPrimary = isPrimary == 1;
		this.primaryURI = primaryURI;

		var uri = String.format("https://%s:%d/rest%s", IP.hostAddress(), RepRestServer.PORT, PATH);
		impl = new JavaRepSpreadsheets(uri);
	}

	public String createSpreadsheet(Spreadsheet sheet, String password) {
		Log.info(String.format("REST createSpreadsheet: sheet = %s\n", sheet));

		if (isPrimary) {
			// affect Primary
			Spreadsheet resultSheet = super.resultOrThrow(impl.createSpreadsheet(sheet, password));
			String sheetId = resultSheet.getSheetId();
			// share changes
			URI[] uris = Discovery.getInstance().findUrisOf(SERVICE_NAME, 1);
			int counterRep = 0;

			for (URI i : uris) {
				String stringURI = i.toString();

				try {
					var resultFromRep = RepSheetsClientFactory.with(stringURI).postSpreadsheet_Rep(resultSheet, sheetId);
					if (resultFromRep.isOK())
						counterRep++;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			// w8 for response
			if (counterRep < uris.length)
				Log.info("Counter of replicas who eard--------------------" + counterRep);

			return sheetId;
		} else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}

	@Override
	public String postSpreadsheet_Rep(Spreadsheet sheet, String password) {
		Log.info(String.format("REST createSpreadsheet: sheet = %s\n", sheet));

		return super.resultOrThrow(impl.postSpreadsheet_Rep(sheet, password));

	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		Log.info(String.format("REST createSpreadsheet: sheet = %s\n", sheetId));
		if (isPrimary) {
			// affect Primary
			super.resultOrThrow(impl.deleteSpreadsheet(sheetId, password));
			// share changes
			URI[] uris = Discovery.getInstance().findUrisOf(SERVICE_NAME, 1);
			int counterRep = 0;

			for (URI i : uris) {
				String stringURI = i.toString();

				try {
					var resultFromRep = RepSheetsClientFactory.with(stringURI).removeSpreadsheet_Rep(sheetId);
					if (resultFromRep.isOK())
						counterRep++;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			// w8 for response
			if (counterRep < uris.length)
				Log.info("Counter of replicas who eard--------------------" + counterRep);

		} else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());

	}

	@Override
	public void removeSpreadsheet_Rep(String sheetId) {
		Log.info(String.format("REST deleteSpreadsheet: sheetId = %s\n", sheetId));

		super.resultOrThrow(impl.removeSpreadsheet_Rep(sheetId));
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		Log.info(String.format("REST getSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		if (isPrimary) {
			// affect Primary
			return super.resultOrThrow(impl.getSpreadsheet(sheetId, userId, password));
		} else//sends to primary
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		Log.info(String.format("REST shareSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		if (isPrimary) {
			// affect Primary
			Spreadsheet sheet = super.resultOrThrow(impl.shareSpreadsheet(sheetId, userId, password));
			// share changes
			URI[] uris = Discovery.getInstance().findUrisOf(SERVICE_NAME, 1);
			int counterRep = 0;

			for (URI i : uris) {
				String stringURI = i.toString();

				try {
					var resultFromRep = RepSheetsClientFactory.with(stringURI).putSpreadsheet_Rep(sheet, sheetId);
					if (resultFromRep.isOK())
						counterRep++;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			// w8 for response
			if (counterRep < uris.length)
				Log.info("Counter of replicas who eard--------------------" + counterRep);

		} else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}

	@Override
	public void putSpreadsheet_Rep(Spreadsheet sheet, String sheetId) {
		//Log.info(String.format("REST shareSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		super.resultOrThrow(impl.putSpreadsheet_Rep(sheet, sheetId));
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		Log.info(String.format("REST unshareSpreadsheet: sheetId = %s, userId = %s\n", sheetId, userId));

		if (isPrimary) {
			// affect Primary
			Spreadsheet sheet = super.resultOrThrow(impl.unshareSpreadsheet(sheetId, userId, password));
			// share changes
			URI[] uris = Discovery.getInstance().findUrisOf(SERVICE_NAME, 1);
			int counterRep = 0;

			for (URI i : uris) {
				String stringURI = i.toString();

				try {
					var resultFromRep = RepSheetsClientFactory.with(stringURI).putSpreadsheet_Rep(sheet, sheetId);
					if (resultFromRep.isOK())
						counterRep++;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			// w8 for response
			if (counterRep < uris.length)
				Log.info("Counter of replicas who eard--------------------" + counterRep);

		} else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());

	}


	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		Log.info(String.format("REST updateCell: sheetId = %s, cell= %s, rawValue = %s, userId = %s\n", sheetId, cell,
				rawValue, userId));

		if (isPrimary) {
			// affect Primary
			Spreadsheet sheet = super.resultOrThrow(impl.updateCell(sheetId, cell, rawValue, userId, password));
			// share changes
			URI[] uris = Discovery.getInstance().findUrisOf(SERVICE_NAME, 1);
			int counterRep = 0;

			for (URI i : uris) {
				String stringURI = i.toString();

				try {
					var resultFromRep = RepSheetsClientFactory.with(stringURI).putSpreadsheet_Rep(sheet, sheetId);
					if (resultFromRep.isOK())
						counterRep++;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			// w8 for response
			if (counterRep < uris.length)
				Log.info("Counter of replicas who eard--------------------" + counterRep);

		} else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}



	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		Log.info(String.format("REST getSpreadsheetValues: sheetId = %s, userId = %s\n", sheetId, userId));
		if (isPrimary) {
			// affect Primary
			return super.resultOrThrow(impl.getSpreadsheetValues(sheetId, userId, password));
		} else//sends to primary
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}

	@Override
	public void deleteSpreadsheets(String userId) {
		if (isPrimary) {
			// affect Primary
			super.resultOrThrow(impl.deleteSpreadsheets(userId));
			// share changes
			URI[] uris = Discovery.getInstance().findUrisOf(SERVICE_NAME, 1);
			int counterRep = 0;

			for (URI i : uris) {
				String stringURI = i.toString();

				try {
					var resultFromRep = RepSheetsClientFactory.with(stringURI).deleteSpreadsheets_Rep(userId);
					if (resultFromRep.isOK())
						counterRep++;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			// w8 for response
			if (counterRep < uris.length)
				Log.info("Counter of replicas who eard--------------------" + counterRep);

		} else
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
		
	}

	@Override
	public String[][] fetchSpreadsheetValues(String sheetId, String userId) {
		Log.info(String.format("REST fetchSpreadsheetValues: sheetId = %s, userId: %s\n", sheetId, userId));
		if (isPrimary) {
		return super.resultOrThrow(impl.fetchSpreadsheetValues(sheetId, userId));
		} else//sends to primary
			throw new WebApplicationException(Response.temporaryRedirect(URI.create(primaryURI)).build());
	}

	@Override
	public void deleteSpreadsheets_Rep(String userId) {
		super.resultOrThrow(impl.deleteSpreadsheets(userId));	
	}

	/*
	 * notas: primeira fase-
	 * 
	 * segunda fase- -fazer a gestao de quem é primary-zookeeper
	 */

}
