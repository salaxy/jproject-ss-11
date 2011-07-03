package de.fhb.jproject.controller.web.actions.user;

import de.fhb.jproject.exceptions.ProjectException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.fhb.commons.web.HttpRequestActionBase;
import de.fhb.jproject.data.User;
import de.fhb.jproject.manager.MainManager;
import javax.servlet.http.HttpSession;


/**
 * Action, die alle mitgeschickten Parameter ausgibt: 
 * <parametername>: <value>
 * 
 * @author klay
 */
public class LoginAction extends HttpRequestActionBase {


	private MainManager mainManager;
	private static final Logger logger = Logger.getLogger(LoginAction.class);

	/* (non-Javadoc)
	 * @see de.fhb.music.controller.we.actions.HttpRequestActionBase#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void perform(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		
		HttpSession session = req.getSession();
		//Manager holen
		mainManager = new MainManager()/*(MainManager) session.getAttribute("mainManager")*/;
		User user = null;

		
		try {
			
			//Debugprint
			logger.info("perform(HttpServletRequest req, HttpServletResponse resp)");
			logger.debug("Parameter: "
					+ "String loginName(" + req.getParameter("loginName") + "), "
					+ "String password(" + req.getParameter("password") + ")"
					);
			
			String loginName = req.getParameter("loginName").toLowerCase();
			String password = req.getParameter("password");
			
			/* 5maligen Haswert des Passworts ermitteln */
			/* TODO zufälligen Salt aus der Datenbank lesen */
			for (int i = 0; i < 5; i++) {
				password = ""+password.hashCode();
			}
			
			//Manager in aktion
			user = mainManager.getUserManager().login(loginName, password);
			synchronized(session){

				session.setAttribute("aktUser", user.getLoginName());
				session.setAttribute("mainManager", mainManager);

				
				//RECHTE-ABFRAGE Global
				if(mainManager.getGlobalRolesManager().isAllowedOpenAdminconsoleAction(loginName)){
					session.setAttribute("isAdmin", true);
				}
			}
			
			logger.debug("Erfolgreich eingeloggt!");
			req.setAttribute("triedLogin", false);
		}catch (ProjectException e) {
			logger.error(e.getMessage(), e);
			req.setAttribute("triedLogin", true);
			req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		}
	}
}
