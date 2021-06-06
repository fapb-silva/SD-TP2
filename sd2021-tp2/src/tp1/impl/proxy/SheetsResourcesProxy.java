package tp1.impl.proxy;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tp1.api.Spreadsheet;
import tp1.api.service.rest.RestSpreadsheets;

public class SheetsResourcesProxy implements RestSpreadsheets{
	
	private static final String apiKey = "qkwowwvw9cjgxcw";
	private static final String apiSecret = "ohnj1xpcazkvtg6";
	private static final String accessTokenStr = "P51dQLGFjpQAAAAAAAAAAe3lDMORWeu_Xpm48OQ7cZTwBNrNM5hwBN-sgpvgjU9B";
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	
	public SheetsResourcesProxy() {
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);
		json = new Gson();
	}

	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSpreadsheets(String userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[][] fetchSpreadsheetValues(String sheetId, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
