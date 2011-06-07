package de.fhb.jproject.controller.web.actions.task;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.fhb.commons.web.HttpRequestActionBase;
import de.fhb.jproject.controller.web.actions.project.AddMemberAction;
import de.fhb.jproject.data.User;
import de.fhb.jproject.exceptions.ProjectException;
import de.fhb.jproject.manager.MainManager;
import java.sql.Date;
import javax.servlet.http.HttpSession;


/**
 * Action die angesprochen wird wenn eine Aufgabe einem Projekt hingefuegt wird
 * 
 * Hinweise:
 * - Parameter "date" MUSS die Form "yyyy-mm-dd" haben
 *  
 * STATUS:	FREIGEGEBEN 
 * URL: 	JProjectServlet?do=AddNewTask&projectName=ProjectName&titel=TestAufgabe&aufgabenStellung=Tue%20dies%20und%20das!&date=2011-06-02
 * @author  Andy Klay <klay@fh-brandenburg.de> 
 */
public class AddNewTaskAction extends HttpRequestActionBase {

	private MainManager mainManager;
	private static final Logger logger = Logger.getLogger(AddNewTaskAction.class);

	/* (non-Javadoc)
	 * @see de.fhb.music.controller.we.actions.HttpRequestActionBase#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void perform(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException{	
		HttpSession session = req.getSession();
		//Manager holen
		mainManager=(MainManager) session.getAttribute("mainManager");
		try {		
			
			//Debugprint
			logger.info("perform(HttpServletRequest req, HttpServletResponse resp)");
			logger.debug("Parameter: "
					+ "String projectName(" + req.getParameter("projectName") + "), "
					+ "String titel(" + req.getParameter("titel") + ")"
					+ "String aufgabenStellung(" + req.getParameter("aufgabenStellung") + ")"
					+ "Date date(" + req.getParameter("date") + ")"
					);
			
		
			//Manager in aktion
			mainManager.getTaskManager().addNewTask((User)session.getAttribute("aktUser"), req.getParameter("projectName"),
					req.getParameter("titel"),
					req.getParameter("aufgabenStellung"),
					//yyyy-mm-dd <<< muss sooo aussehen
					Date.valueOf(req.getParameter("date"))
					);
			
			
		}catch (ProjectException e) {
			logger.error(e.getMessage(), e);
			req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		}catch(NullPointerException e){
			logger.error(e.getMessage(), e);
			req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		}
		
	}
}
