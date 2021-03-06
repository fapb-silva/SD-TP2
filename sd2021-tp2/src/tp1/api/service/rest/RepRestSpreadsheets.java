package tp1.api.service.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tp1.api.Spreadsheet;


@Path(RepRestSpreadsheets.PATH)
public interface RepRestSpreadsheets extends RestSpreadsheets{

static final String PATH="/spreadsheets";
	
	/**
	 * Creates a new spreadsheet. The sheetId and sheetURL are generated by the server.
	 * After being created, the size of the spreadsheet is not modified.
	 * @param sheet - the spreadsheet to be created.
	 * @param password - the password of the owner of the spreadsheet.
	 * 
	 * @return 200 the sheetId; 
	 * 		   400 otherwise.
	 */
	@POST
	@Path("/rep")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String postSpreadsheet_Rep(Spreadsheet sheet, String sheetId );

	
	/**
	 * Deletes a spreadsheet.  Only the owner can call this method.
	 * 
	 * @param sheetId - the sheet to be deleted.
	 * @param password - the password of the owner of the spreadsheet.
	 * 
	 * @return 204 if the sheet was successful.
	 *			404 if no sheet exists with the given sheetId.
	 *          403 if the password is incorrect.
	 *			400 otherwise.
	 */
	@DELETE
	@Path("/rep/{sheetId}")
	void removeSpreadsheet_Rep(@PathParam("sheetId") String sheetId);

	/**
	 * Retrieve a spreadsheet.
	 * 	
	 * @param sheetId - The  spreadsheet being retrieved.
	 * @param userId - The user performing the operation.
	 * @param password - The password of the user performing the operation.
	 * 
	 * @return 200 and the spreadsheet
	 *		   404 if no sheet exists with the given sheetId, or the userId does not exist.
	 *         403 if the password is incorrect.
	 * 		   400 otherwise
	 */
	@PUT
	@Path("/rep/{sheetId}")
	@Consumes(MediaType.APPLICATION_JSON)
	void putSpreadsheet_Rep(Spreadsheet sheet, @PathParam("sheetId") String sheetId);
	
	@DELETE
	@Path("/rep/{userId}/sheets")
	@Consumes(MediaType.APPLICATION_JSON)
	void deleteSpreadsheets_Rep(@PathParam("userId") String userId);

	
}	
