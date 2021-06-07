package tp1.impl.proxy;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ErrorCode.BAD_REQUEST;

import java.util.concurrent.ConcurrentHashMap;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.User;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.impl.proxy.args.UploadArgs;
import tp1.impl.srv.Domain;

public class SheetsResourcesProxy implements RestSpreadsheets{
	
	private static final String apiKey = "qkwowwvw9cjgxcw";
	private static final String apiSecret = "ohnj1xpcazkvtg6";
	private static final String accessTokenStr = "P51dQLGFjpQAAAAAAAAAAe3lDMORWeu_Xpm48OQ7cZTwBNrNM5hwBN-sgpvgjU9B";
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	protected static final String OCTET_STREAM_TYPE = "application/octet-stream";
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	final String DOMAIN = '@' + Domain.get();
	private int idInc;
	
	private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
	
	private Gson json;
	
	public SheetsResourcesProxy() {
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);
		json = new Gson();
		idInc=0;
	}

	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		// TODO
		if (badSheet(sheet) || password == null || wrongPassword(sheet.getOwner(), password))
			return null;
		
		OAuthRequest createSpreadsheet = new OAuthRequest(Verb.POST, UPLOAD_URL);
		createSpreadsheet.addHeader("Dropbox-API-Arg", json.toJson(new UploadArgs("/sheets", "add",false,true,false)) );
		createSpreadsheet.addHeader("Content-Type", OCTET_STREAM_TYPE);
		
		
		var sheetId = sheet.getOwner() + "-" + DOMAIN +"-"+(idInc++);
		sheet.setSheetId(sheetId);
		sheet.setSheetURL(String.format("%s/%s/%s", DOMAIN, "sheets", sheetId));
		sheet.setSharedWith(ConcurrentHashMap.newKeySet());
		
		//Load createSpreadsheet
		createSpreadsheet.setPayload(json.toJson(sheet));
		
		service.signRequest(accessToken, createSpreadsheet);
		
		Response r = null;
		
		try {
			r=service.execute(createSpreadsheet);
			
			if(r.getCode() != 200) {
				//erro
				System.err.println(r.getBody());
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
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
	
	private boolean badSheet(Spreadsheet sheet) {
		return sheet == null || !sheet.isValid();
	}
	
	private boolean wrongPassword(String userId, String password) {
		var user = getUser(userId);
		return user == null || !user.getPassword().equals(password);
	}
	
	private User getUser(String userId) {
//		try {
//			return users.get(userId);
//		} catch (Exception x) {
//			x.printStackTrace();
//			return null;
//		}
		//TODO-get from dropbox
		return null;
		
	}
	
}
