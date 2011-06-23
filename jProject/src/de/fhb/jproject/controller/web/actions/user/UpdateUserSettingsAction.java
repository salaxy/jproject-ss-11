package de.fhb.jproject.controller.web.actions.user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.fhb.commons.web.HttpRequestActionBase;
import de.fhb.jproject.data.User;
import de.fhb.jproject.exceptions.ProjectException;
import de.fhb.jproject.manager.MainManager;
import java.io.IOException;
import javax.servlet.http.HttpSession;


/**
 * Action die angesprochen wird wenn eine User seine Daten aenderst
 * 
 * STATUS:	FREIGEGEBEN 
 * URL: 	JProjectServlet?do=UpdateUserSettings&nachname=MeinNeuerNachname&vorname=MeinNeuerVorname&sprache=NeueSprache&neuesPasswortEins=pw&neuesPasswortZwei=pw&altesPasswort=password
 * drei parameter fehlen !! neuIcq, neuSkype, neutelefon
 * @author  Andy Klay <klay@fh-brandenburg.de>
 */
public class UpdateUserSettingsAction extends HttpRequestActionBase {

	private MainManager mainManager;
	private static final Logger logger = Logger.getLogger(UpdateUserSettingsAction.class);

	/* (non-Javadoc)
	 * @see de.fhb.music.controller.we.actions.HttpRequestActionBase#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void perform(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		
		HttpSession session = req.getSession();
		//Manager holen
		mainManager=(MainManager) session.getAttribute("mainManager");
		
		try {
			
			//Debugprint
			logger.info("perform(HttpServletRequest req, HttpServletResponse resp)");
			
			//Parameter laden
			String aktUser = (String) session.getAttribute("aktUser");
			String loginName = req.getParameter("loginName"); 
			
			//EINGABEFEHLER ABFANGEN
			//abfrage ob user eingeloggt
			if(aktUser == null){
				throw new ProjectException("Sie sind nicht eingeloggt!");
			}
			//RECHTE-ABFRAGE Global
			if(!mainManager.getGlobalRolesManager().isAllowedUpdateUserSettingsAction(aktUser)){
				if(!aktUser.equals(loginName)){
					throw new ProjectException("Sie haben keine Rechte zum updaten der UserSettings!");
				}
			}

			//Manager in aktion
			mainManager.getUserManager().updateUserSettings(loginName, 
															req.getParameter("nachname"), 
															req.getParameter("vorname"), 
															/*req.getParameter("neuIcq")*/null, 
															/*req.getParameter("neuSkype")*/null, 
															/*req.getParameter("neutelefon")*/null, 
															req.getParameter("sprache"), 
															req.getParameter("neuesPasswortEins"), 
															req.getParameter("neuesPasswortZwei")/*, 
															req.getParameter("altesPasswort")*/);

			
			//setzen der Parameter
			
			try {
				String[] param = new String[1];
				param[0] = "loginName="+loginName;
				super.redirect(req, resp, (String)session.getAttribute("aktServlet"), "ShowUserSettings", param);
			} catch (IOException e) {
				logger.error("Konnte Redirect nicht ausführen! "+e.getMessage(), e);
			}
		}catch (ProjectException e) {
			logger.error(e.getMessage(), e);
			req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		}
	}
}
