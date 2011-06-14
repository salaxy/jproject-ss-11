package de.fhb.jproject.controller.web.actions.user;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.fhb.commons.web.HttpRequestActionBase;
import de.fhb.jproject.data.User;
import de.fhb.jproject.exceptions.ProjectException;
import de.fhb.jproject.manager.MainManager;
import javax.servlet.http.HttpSession;


/**
 * Action, die alle mitgeschickten Parameter ausgibt: 
 * <parametername>: <value>
 * 
 * @author klay
 */
public class ShowAllUserAction extends HttpRequestActionBase {

	private MainManager mainManager;
	private static final Logger logger = Logger.getLogger(ShowAllUserAction.class);

	/* (non-Javadoc)
	 * @see de.fhb.music.controller.we.actions.HttpRequestActionBase#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void perform(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		
		
		List <User> userList = null;
		User user = null;
	
		HttpSession session = req.getSession();
		//Manager holen
		mainManager=(MainManager) session.getAttribute("mainManager");
		String loginName = "";
		try {
			
			//Debugprint
			logger.info("perform(HttpServletRequest req, HttpServletResponse resp)");			
			
			
			
			//UserList holen
			try {
				userList=mainManager.getUserManager().showAllUser((User)session.getAttribute("aktUser"));
			
			}catch(NullPointerException e){
				logger.error(e.getMessage(), e);
			}	
			
			try{
				//Wenn loginName == null dann gib mir den ersten
				if (null == req.getParameter("loginName")) {
					loginName = userList.get(0).getLoginName();
				}else{
					loginName = req.getParameter("loginName");
				}
			} catch (IllegalArgumentException e) {
				throw new ProjectException("loginName ungültig "+e);
			}catch(NullPointerException e){
				logger.error("Keine User vorhanden!"+e.getMessage(), e);
			}
			
			try {
				//Manager in aktion
				user=mainManager.getUserManager().showUserInfo((User)session.getAttribute("aktUser"), 
																		 loginName);
			}catch (NullPointerException e) {
				logger.error(e.getMessage(), e);
			}
			
//			for( User user : userList){
//				System.out.println("User: "+user.getLoginName());
//			}

			//Daten dem Reqest mitgeben
			req.setAttribute("user", user);
			req.setAttribute("userList", userList);
			
			
			
			req.setAttribute("contentFile", "showAllUser.jsp");
		}catch (ProjectException e) {
			logger.error(e.getMessage(), e);
			req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		}
		
	}
}
