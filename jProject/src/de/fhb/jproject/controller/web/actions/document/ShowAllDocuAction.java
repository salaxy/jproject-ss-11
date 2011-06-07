package de.fhb.jproject.controller.web.actions.document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fhb.commons.web.HttpRequestActionBase;


/**
 * Action, die alle mitgeschickten Parameter ausgibt: 
 * <parametername>: <value>
 * 
 * @author klay
 */
public class ShowAllDocuAction extends HttpRequestActionBase {


	/* (non-Javadoc)
	 * @see de.fhb.music.controller.we.actions.HttpRequestActionBase#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void perform(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		
		/*TODO DELETE ACTION
		ShowDocuAction showDocuAction = new ShowDocuAction();
		showDocuAction.perform(req, resp);
		 * 
		 */
		req.setAttribute("contentFile", "showAllDocu.jsp");
		
		/*
		 * catch(Exception e){
		 *	req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		 * }
		 */
	}
}
