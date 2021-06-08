package tp1.impl.proxy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
import tp1.impl.clt.SheetsProxyClientFactory;
import tp1.impl.clt.SpreadsheetsClientFactory;
import tp1.impl.clt.UsersClientFactory;
import tp1.impl.engine.SpreadsheetEngineImpl;
import tp1.impl.proxy.args.UploadArgs;
import tp1.impl.proxy.args.UserEntry;
import tp1.impl.proxy.args.DeleteArgs;
import tp1.impl.proxy.args.DownloadArgs;
import tp1.impl.srv.Domain;
import tp1.impl.srv.common.JavaSpreadsheets;
import tp1.impl.srv.rest.SheetsProxyServer;

import tp1.impl.utils.IP;

public class SheetsResourcesProxy implements RestSpreadsheets {

	private static final String DROPBOX_FOLDER = "/SD-TP2";
	private static final String SHEETS_PATH = "/sheets";
	private static final String USERS_PATH = "/usersMap";
	private static final String apiKey = "qkwowwvw9cjgxcw";
	private static final String apiSecret = "ohnj1xpcazkvtg6";
	private static final String accessTokenStr = "P51dQLGFjpQAAAAAAAAAAe3lDMORWeu_Xpm48OQ7cZTwBNrNM5hwBN-sgpvgjU9B";
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	protected static final String OCTET_STREAM_TYPE = "application/octet-stream";
	private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DELETE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String DOWNLOAD_URL = "https://content.dropboxapi.com/2/files/download";

	private static final Pattern SPREADSHEETS_URI_PATTERN = Pattern.compile("(.+)/spreadsheets/(.+)");

	private OAuth20Service service;
	private OAuth2AccessToken accessToken;

	final String DOMAIN = '@' + Domain.get();
	private static final long USER_CACHE_CAPACITY = 100;
	private static final long USER_CACHE_EXPIRATION = 120;
	private static final long VALUES_CACHE_CAPACITY = 100;
	private static final long VALUES_CACHE_EXPIRATION = 20;
	private Gson json;
	private int idInc;
	private String baseUri;
	final SpreadsheetEngine engine;
	private static final Set<String> DUMMY_SET = new HashSet<>();

	private static Logger Log = Logger.getLogger(JavaSpreadsheets.class.getName());

	// final Map<String, Set<String>> userSheets = new ConcurrentHashMap<>();

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
		baseUri = String.format("https://%s:%d/rest%s", IP.hostAddress(), SheetsProxyServer.PORT, PATH);

		if (clean) {
			OAuthRequest cleanStorage = new OAuthRequest(Verb.POST, DELETE_URL);
			cleanStorage.addHeader("Content-Type", JSON_CONTENT_TYPE);
			cleanStorage.setPayload(json.toJson(new DeleteArgs(DROPBOX_FOLDER)));
			service.signRequest(accessToken, cleanStorage);
			try {
				service.execute(cleanStorage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		if (badSheet(sheet) || password == null || wrongPassword(sheet.getOwner(), password))
			throw new WebApplicationException(Status.BAD_REQUEST);

		var sheetId = sheet.getOwner() + "-" + DOMAIN + "-" + (idInc++);

//		var sheetId = sheet.getOwner() + "-" + DOMAIN +"-"+(idInc++);
		sheet.setSheetId(sheetId);
		sheet.setSheetURL(String.format("%s/%s", baseUri, sheetId));
		sheet.setSharedWith(ConcurrentHashMap.newKeySet());

		// Load createSpreadsheet
		UserEntry userSheets = proxyDownloadUser(sheet.getOwner());
		if (userSheets == null)
			userSheets = new UserEntry();
		userSheets.addCreatedSheet(sheetId);
		proxyUploadUser(sheet.getOwner(), userSheets);
		Log.info("****UPLOAD WITH URL |" + sheet.getSheetURL() + "|*****");
		return proxyUploadSheet(sheetId, sheet);
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		if (badParam(sheetId))
			throw new WebApplicationException(Status.BAD_REQUEST);

		var sheet = proxyDownloadSheet(sheetId);

		if (sheet == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		if (badParam(password) || wrongPassword(sheet.getOwner(), password))
			throw new WebApplicationException(Status.FORBIDDEN);

		proxyDeleteSheet(sheetId);
		UserEntry userSheets = proxyDownloadUser(sheet.getOwner());
		if (userSheets != null) {
			userSheets.removeDeletedSheet(sheetId);
			proxyUploadUser(sheet.getOwner(), userSheets);
		}

	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		if (badParam(sheetId) || badParam(userId))
			throw new WebApplicationException(Status.BAD_REQUEST);
		Log.info("TRYING TO GET " + sheetId);
		var sheet = proxyDownloadSheet(sheetId);

		if (sheet == null || userId == null || getUser(userId) == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		if (badParam(password) || wrongPassword(userId, password) || !sheet.hasAccess(userId, DOMAIN))
			throw new WebApplicationException(Status.FORBIDDEN);
		else
			return sheet;
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		if (badParam(sheetId) || badParam(userId))
			throw new WebApplicationException(Status.BAD_REQUEST);

		var sheet = proxyDownloadSheet(sheetId);
		if (sheet == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		if (badParam(password) || wrongPassword(userId, password) || !sheet.hasAccess(userId, DOMAIN))
			throw new WebApplicationException(Status.FORBIDDEN);
		Log.info("******1.GOT SHEET*****");
		// String sheetPath = DROPBOX_FOLDER+"/"+id2Domain(sheetId)+"/"+sheetId;
		var values = getComputedValues(sheetId);
		if (values != null)
			return values;
		else
			throw new WebApplicationException(Status.BAD_REQUEST);
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		if (badParam(sheetId) || badParam(userId) || badParam(cell) || badParam(rawValue))
			throw new WebApplicationException(Status.BAD_REQUEST);

		var sheet = proxyDownloadSheet(sheetId);

		if (sheet == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		if (badParam(password) || wrongPassword(userId, password))
			throw new WebApplicationException(Status.FORBIDDEN);

		sheet.setCellRawValue(cell, rawValue);
		proxyUpdateSheet(sheetId, sheet);
		sheetValuesCache.invalidate(sheetId);
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		if (badParam(sheetId) || badParam(userId))
			throw new WebApplicationException(Status.BAD_REQUEST);

		var sheet = proxyDownloadSheet(sheetId);

		if (sheet == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		if (badParam(password) || wrongPassword(sheet.getOwner(), password))
			throw new WebApplicationException(Status.FORBIDDEN);

		if (!sheet.getSharedWith().add(userId))
			throw new WebApplicationException(Status.CONFLICT);
		else
			proxyUpdateSheet(sheetId, sheet);

	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		if (badParam(sheetId) || badParam(userId))
			throw new WebApplicationException(Status.BAD_REQUEST);

		var sheet = proxyDownloadSheet(sheetId);

		if (sheet == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		if (badParam(password) || wrongPassword(sheet.getOwner(), password))
			throw new WebApplicationException(Status.FORBIDDEN);

		if (sheet.getSharedWith().remove(userId)) {
			sheetValuesCache.invalidate(sheetId);
			proxyUpdateSheet(sheetId, sheet);
		} else
			throw new WebApplicationException(Status.FORBIDDEN);

	}

	@Override
	public void deleteSpreadsheets(String userId) {
		var _userSheets = proxyDownloadUser(userId);
		if (_userSheets != null) {
			Set<String> entries = _userSheets.getSet();
			for (var sheetId : entries) {
				proxyDeleteSheet(sheetId);
			}
			proxyDeleteUser(userId);

		}

	}

	@Override
	public String[][] fetchSpreadsheetValues(String sheetId, String userId) {

		if (badParam(sheetId) || badParam(userId))
			throw new WebApplicationException(Status.BAD_REQUEST);
		Log.info("******11.GETTING EXTERNAL SHEET FROM FETCH*****");
		var sheet = proxyDownloadSheet(sheetId);
		if (sheet == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		Log.info("******12. DID NOT GET EXTERNAL SHEET FROM FETCH*****");

		if (!sheet.hasAccess(userId, DOMAIN))
			throw new WebApplicationException(Status.FORBIDDEN);
		Log.info("******12.GOT EXTERNAL SHEET FROM FETCH*****");
		var values = getComputedValues(sheetId);
		if (values != null) {
			Log.info("******13.GOT VALUES*****");
			return values;
		}
		Log.info("******13.DID NOT GET VALUES*****");
		throw new WebApplicationException(Status.BAD_REQUEST);
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
			// String sheetId= url2Id(sheetPath);
			var values = sheetValuesCache.getIfPresent(sheetId);
			if (values == null) {
				var sheet = proxyDownloadSheet(sheetId);
				Log.info("******2.COMPUTED VALUE - GOT SHEET*****");
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
			Log.info("******4.ADAPTER - Getting stuff from |" + sheetURL + "|*****");
			var x = resolveRangeValues(sheetURL, range, sheet.getOwner() + DOMAIN);
			Log.info("getRangeValues:" + sheetURL + " for::: " + range + "--->" + x);
			return x;
		}
	}

	public String[][] resolveRangeValues(String sheetUrl, String range, String userId) {

		String[][] values = null;
		String sheetId = url2Id(sheetUrl);
		// String sheetPath = DROPBOX_FOLDER+"/"+DOMAIN+"/"+sheetId;
		var sheet = proxyDownloadSheet(sheetId);
		if (sheet != null) {
			Log.info("******5.SHEET FOUND IN DOMAIN*****");
			values = getComputedValues(sheetId);
		} else {
			var m = SPREADSHEETS_URI_PATTERN.matcher(sheetUrl);
			if (m.matches()) {

				var uri = m.group(1);
				sheetId = m.group(2);
				var resultProxy = SheetsProxyClientFactory.with(uri).fetchSpreadsheetValues(sheetId, userId);
				if (resultProxy.isOK()) {
					values = resultProxy.value();
					sheetValuesCache.put(sheetUrl, values);
				} else {
					var result = SpreadsheetsClientFactory.with(uri).fetchSpreadsheetValues(sheetId, userId);
					if (result.isOK()) {
						values = result.value();
						sheetValuesCache.put(sheetUrl, values);
					}
				}
			}
			values = sheetValuesCache.getIfPresent(sheetUrl);

			// Log.info("******5.SHEET NOT FOUND IN DOMAIN*****");
			// Log.info("******6.SERACH SHEET, PATH: "+sheetPath+"*****");
//			sheet = proxyDownloadExternalSheet(sheetPath);
//			Log.info("******9.GOT EXTERNAL SHEET*****");
//				values = fetchSpreadsheetValues(sheetId, userId);
//				if (values!=null) {
//					sheetValuesCache.put(sheetUrl, values);
//				
//			}
//			values = sheetValuesCache.getIfPresent(sheetUrl);
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
				json.toJson(new UploadArgs(DROPBOX_FOLDER + SHEETS_PATH + "/" + DOMAIN + "/" + sheetId, "add", false,
						false, false)));
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
		getSpreadsheet.addHeader("Dropbox-API-Arg",
				json.toJson(new DownloadArgs(DROPBOX_FOLDER + SHEETS_PATH + "/" + DOMAIN + "/" + sheetId)));
		getSpreadsheet.addHeader("Content-Type", OCTET_STREAM_TYPE);
		service.signRequest(accessToken, getSpreadsheet);

		Response r = null;
		Spreadsheet sheet = null;
		try {
			r = service.execute(getSpreadsheet);

			if (r.getCode() != 200) {
				// erro
				Log.info("DID NOT GET SHEET");
				Log.info("SHEET ERROR: " + r.getBody());
				return null;
			}
			sheet = json.fromJson(r.getBody(), Spreadsheet.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sheet;

	}

	private void proxyDeleteSheet(String sheetId) {
		OAuthRequest deleteSpreadsheet = new OAuthRequest(Verb.POST, DELETE_URL);
		deleteSpreadsheet.addHeader("Content-Type", JSON_CONTENT_TYPE);
		deleteSpreadsheet
				.setPayload(json.toJson(new DeleteArgs(DROPBOX_FOLDER + SHEETS_PATH + "/" + DOMAIN + "/" + sheetId)));
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
				json.toJson(new UploadArgs(DROPBOX_FOLDER + "/" + DOMAIN + SHEETS_PATH + "/" + sheetId, "overwrite",
						false, false, false)));
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

	private String proxyUploadUser(String userId, UserEntry sheets) {
		OAuthRequest createUser = new OAuthRequest(Verb.POST, UPLOAD_URL);
		createUser.addHeader("Dropbox-API-Arg",
				json.toJson(new UploadArgs(DROPBOX_FOLDER + USERS_PATH + "/" + DOMAIN + "/" + userId, "overwrite",
						false, false, false)));
		createUser.addHeader("Content-Type", OCTET_STREAM_TYPE);
		createUser.setPayload(json.toJson(sheets));
		service.signRequest(accessToken, createUser);

		Response r = null;
		try {
			r = service.execute(createUser);

			if (r.getCode() != 200) {
				// erro
				System.err.println(r.getBody());
				throw new WebApplicationException(Status.BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return userId;
	}

	private void proxyDeleteUser(String userId) {
		OAuthRequest deleteUser = new OAuthRequest(Verb.POST, DELETE_URL);
		deleteUser.addHeader("Content-Type", JSON_CONTENT_TYPE);
		deleteUser.setPayload(json.toJson(new DeleteArgs(DROPBOX_FOLDER + USERS_PATH + "/" + DOMAIN + "/" + userId)));
		service.signRequest(accessToken, deleteUser);
		try {
			service.execute(deleteUser);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private UserEntry proxyDownloadUser(String userId) {
		OAuthRequest getUser = new OAuthRequest(Verb.POST, DOWNLOAD_URL);
		getUser.addHeader("Dropbox-API-Arg",
				json.toJson(new DownloadArgs(DROPBOX_FOLDER + USERS_PATH + "/" + DOMAIN + "/" + userId)));
		getUser.addHeader("Content-Type", OCTET_STREAM_TYPE);
		service.signRequest(accessToken, getUser);

		Response r = null;
		UserEntry sheet = null;
		try {
			r = service.execute(getUser);

			if (r.getCode() != 200) {
				// erro
				Log.info("DID NOT GET SHEET");
				Log.info("SHEET ERROR: " + r.getBody());
				return null;
			}
			sheet = json.fromJson(r.getBody(), UserEntry.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sheet;

	}
}
