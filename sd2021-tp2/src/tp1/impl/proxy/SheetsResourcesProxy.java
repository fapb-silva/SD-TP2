package tp1.impl.proxy;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.ErrorCode.BAD_REQUEST;
import static tp1.api.service.java.Result.ErrorCode.FORBIDDEN;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.User;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.engine.AbstractSpreadsheet;
import tp1.engine.CellRange;
import tp1.engine.SpreadsheetEngine;
import tp1.impl.clt.SpreadsheetsClientFactory;
import tp1.impl.clt.UsersClientFactory;
import tp1.impl.engine.SpreadsheetEngineImpl;
import tp1.impl.proxy.args.UploadArgs;
import tp1.impl.proxy.args.DeleteArgs;
import tp1.impl.proxy.args.DownloadArgs;
import tp1.impl.srv.Domain;
import tp1.impl.srv.common.JavaSpreadsheets;

public class SheetsResourcesProxy implements RestSpreadsheets {

	private static final String apiKey = "qkwowwvw9cjgxcw";
	private static final String apiSecret = "ohnj1xpcazkvtg6";
	private static final String accessTokenStr = "P51dQLGFjpQAAAAAAAAAAe3lDMORWeu_Xpm48OQ7cZTwBNrNM5hwBN-sgpvgjU9B";
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	protected static final String OCTET_STREAM_TYPE = "application/octet-stream";
	private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DELETE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String DOWNLOAD_URL = "https://content.dropboxapi.com/2/files/download";

	private OAuth20Service service;
	private OAuth2AccessToken accessToken;

	final String DOMAIN = '@' + Domain.get();
	private static final long USER_CACHE_CAPACITY = 100;
	private static final long USER_CACHE_EXPIRATION = 120;
	private static final long VALUES_CACHE_CAPACITY = 100;
	private static final long VALUES_CACHE_EXPIRATION = 20;
	private Gson json;
	private int idInc;
	final SpreadsheetEngine engine;
	private static final Set<String> DUMMY_SET = new HashSet<>();

	private static Logger Log = Logger.getLogger(JavaSpreadsheets.class.getName());
	
	final Map<String, Set<String>> userSheets = new ConcurrentHashMap<>();

	LoadingCache<String, User> users = CacheBuilder.newBuilder().maximumSize(USER_CACHE_CAPACITY)
			.expireAfterWrite(USER_CACHE_EXPIRATION, TimeUnit.SECONDS).build(new CacheLoader<>() {
				@Override
				public User load(String userId) throws Exception {
					return UsersClientFactory.get().fetchUser(userId).value();
				}
			});

	Cache<String, String[][]> sheetValuesCache = CacheBuilder.newBuilder().maximumSize(VALUES_CACHE_CAPACITY)
			.expireAfterWrite(VALUES_CACHE_EXPIRATION, TimeUnit.SECONDS).build();

	public SheetsResourcesProxy(boolean clean) {
		engine = SpreadsheetEngineImpl.getInstance();
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);
		json = new Gson();
		idInc = 0;
		if (clean) {
			OAuthRequest cleanStorage = new OAuthRequest(Verb.POST, DELETE_URL);
			cleanStorage.addHeader("Content-Type", OCTET_STREAM_TYPE);
			cleanStorage.setPayload(json.toJson(new DeleteArgs("")));
		}
	}

	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		if (badSheet(sheet) || password == null || wrongPassword(sheet.getOwner(), password))
			return null;

		var sheetId = sheet.getOwner() + "-" + DOMAIN + "-" + (idInc++);


//		var sheetId = sheet.getOwner() + "-" + DOMAIN +"-"+(idInc++);
		sheet.setSheetId(sheetId);
		sheet.setSheetURL(String.format("%s/%s", DOMAIN, sheetId));
		sheet.setSharedWith(ConcurrentHashMap.newKeySet());

		// Load createSpreadsheet
		return proxyUploadSheet(sheetId,sheet);
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		if (badParam(sheetId))
			return;

		var sheet = proxyDownloadSheet(sheetId);

		if (sheet == null)
			return;

		if (badParam(password) || wrongPassword(sheet.getOwner(), password))
			return;

		
			proxyDeleteSheet(sheetId);
			userSheets.computeIfAbsent(sheet.getOwner(), (k) -> ConcurrentHashMap.newKeySet()).remove(sheetId);
		

	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		// TODO
		return null;
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		if (badParam(sheetId) || badParam(userId))
			return null;

		var sheet = proxyDownloadSheet(sheetId);
		if (sheet == null)
			return null;

		if (badParam(password) || wrongPassword(userId, password) || !sheet.hasAccess(userId, DOMAIN))
			return null;

		var values = getComputedValues(sheetId);
		if (values != null)
			return values;
		else
			return null;
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		// TODO

	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		// TODO

	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		// TODO

	}

	@Override
	public void deleteSpreadsheets(String userId) {
		var _userSheets = userSheets.getOrDefault(userId, DUMMY_SET);
		for (var sheetId : _userSheets) {
			proxyDeleteSheet(sheetId);
		}
		_userSheets.clear();


	}

	@Override
	public String[][] fetchSpreadsheetValues(String sheetId, String userId) {
		if (badParam(sheetId) || badParam(userId))
			return null;

		var sheet = proxyDownloadSheet(sheetId);
		if (sheet == null)
			return null;

		if (!sheet.hasAccess(userId, DOMAIN))
			return null;

		var values = getComputedValues(sheetId);
		if (values != null)
			return values;
		
		return null;
	}

	private boolean badParam(String str) {
		return str == null || str.length() == 0;
	}

	private boolean badSheet(Spreadsheet sheet) {
		return sheet == null || !sheet.isValid();
	}

	private boolean wrongPassword(String userId, String password) {
		var user = getUser(userId);
		return user == null || !user.getPassword().equals(password);
	}

	private User getUser(String userId) {
		try {
			return users.get(userId);
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}

	}

	private String[][] getComputedValues(String sheetId) {
		try {
			var values = sheetValuesCache.getIfPresent(sheetId);
			if (values == null) {
				var sheet = proxyDownloadSheet(sheetId);
				values = engine.computeSpreadsheetValues(new SpreadsheetProxyAdaptor(sheet));
				sheetValuesCache.put(sheetId, values);
			}
			return values;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}

	class SpreadsheetProxyAdaptor implements AbstractSpreadsheet {

		final Spreadsheet sheet;

		SpreadsheetProxyAdaptor(Spreadsheet sheet) {
			this.sheet = sheet;
		}

		@Override
		public int rows() {
			return sheet.getRows();
		}

		@Override
		public int columns() {
			return sheet.getColumns();
		}

		@Override
		public String sheetId() {
			return sheet.getSheetId();
		}

		@Override
		public String cellRawValue(int row, int col) {
			return sheet.getCellRawValue(row, col);
		}

		@Override
		public String[][] getRangeValues(String sheetURL, String range) {
			var x = resolveRangeValues(sheetURL, range, sheet.getOwner() + DOMAIN);
			Log.info("getRangeValues:" + sheetURL + " for::: " + range + "--->" + x);
			return x;
		}
	}

	public String[][] resolveRangeValues(String sheetUrl, String range, String userId) {

		String[][] values = null;
		var sheet = proxyDownloadSheet(url2Id(sheetUrl));
		if (sheet != null)
			values = getComputedValues(sheet.getSheetId());
		else {
			
			sheet = proxyDownloadExternalSheet(sheetUrl);
				values = fetchSpreadsheetValues(url2Id(sheetUrl), userId);
				if (values!=null) {
					sheetValuesCache.put(sheetUrl, values);
				
			}
			values = sheetValuesCache.getIfPresent(sheetUrl);
		}
		return values == null ? null : new CellRange(range).extractRangeValuesFrom(values);
	}

	private String url2Id(String url) {
		int i = url.lastIndexOf('/');
		return url.substring(i + 1);
	}
	
	private String proxyUploadSheet(String sheetId, Spreadsheet sheet) {
		OAuthRequest createSpreadsheet = new OAuthRequest(Verb.POST, UPLOAD_URL);
		createSpreadsheet.addHeader("Dropbox-API-Arg",
				json.toJson(new UploadArgs("/" + DOMAIN + "/" + sheetId, "add", false, false, false)));
		createSpreadsheet.addHeader("Content-Type", OCTET_STREAM_TYPE);
		createSpreadsheet.setPayload(json.toJson(sheet));
		service.signRequest(accessToken, createSpreadsheet);

		Response r = null;
		try {
			r = service.execute(createSpreadsheet);

			if (r.getCode() != 200) {
				// erro
				System.err.println(r.getBody());
				throw new WebApplicationException(Status.BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sheetId;
	}
	
	private Spreadsheet proxyDownloadSheet(String sheetId) {
		OAuthRequest getSpreadsheet = new OAuthRequest(Verb.POST, DOWNLOAD_URL);
		getSpreadsheet.addHeader("Dropbox-API-Arg", json.toJson(new DownloadArgs("/" + DOMAIN + "/" + sheetId)));
		service.signRequest(accessToken, getSpreadsheet);

		Response r = null;
		Spreadsheet sheet = null;
		try {
			r = service.execute(getSpreadsheet);

			if (r.getCode() != 200) {
				// erro
				System.err.println(r.getBody());
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
			 sheet = json.fromJson(r.getBody(), Spreadsheet.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sheet;
		
	}
	private void proxyDeleteSheet(String sheetId) {
		OAuthRequest deleteSpreadsheet = new OAuthRequest(Verb.POST, DELETE_URL);
		deleteSpreadsheet.addHeader("Dropbox-API-Arg", json.toJson(new DeleteArgs("/" + DOMAIN + "/" + sheetId)));
		service.signRequest(accessToken, deleteSpreadsheet);
		try {
			service.execute(deleteSpreadsheet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private String proxyUpdateSheet(String sheetId, Spreadsheet sheet) {
		OAuthRequest createSpreadsheet = new OAuthRequest(Verb.POST, UPLOAD_URL);
		createSpreadsheet.addHeader("Dropbox-API-Arg",
				json.toJson(new UploadArgs("/" + DOMAIN + "/" + sheetId, "update", false, false, false)));
		createSpreadsheet.addHeader("Content-Type", OCTET_STREAM_TYPE);
		createSpreadsheet.setPayload(json.toJson(sheet));
		service.signRequest(accessToken, createSpreadsheet);

		Response r = null;
		try {
			r = service.execute(createSpreadsheet);

			if (r.getCode() != 200) {
				// erro
				System.err.println(r.getBody());
				throw new WebApplicationException(Status.BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sheetId;
	}
	private Spreadsheet proxyDownloadExternalSheet(String sheetUrl) {
		OAuthRequest getSpreadsheet = new OAuthRequest(Verb.POST, DOWNLOAD_URL);
		getSpreadsheet.addHeader("Dropbox-API-Arg", json.toJson(new DownloadArgs(sheetUrl)));
		service.signRequest(accessToken, getSpreadsheet);

		Response r = null;
		Spreadsheet sheet = null;
		try {
			r = service.execute(getSpreadsheet);

			if (r.getCode() != 200) {
				// erro
				System.err.println(r.getBody());
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
			 sheet = json.fromJson(r.getBody(), Spreadsheet.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sheet;
		
	}

}
