package de.fhb.jproject.controller.web.actions.document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fhb.commons.web.HttpRequestActionBase;
import de.fhb.jproject.data.Project;
import de.fhb.jproject.data.User;
import de.fhb.jproject.exceptions.ProjectException;
import de.fhb.jproject.manager.MainManager;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

import org.apache.commons.fileupload.FileItem;


/**
 * Action, die alle mitgeschickten Parameter ausgibt: 
 * <parametername>: <value>
 * 
 * @author klay
 */
public class AddNewDocuAction extends HttpRequestActionBase {
private MainManager mainManager;
	private static final Logger logger = Logger.getLogger(AddNewDocuAction.class);

	/* (non-Javadoc)
	 * @see de.fhb.music.controller.we.actions.HttpRequestActionBase#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void perform(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException{	
		HttpSession session = req.getSession();
		//Manager holen
		mainManager=(MainManager) session.getAttribute("mainManager");
		Project aktProject = null;
		List<FileItem> fields = null;
		boolean isMultipartContent;
		try {		
			
			//Debugprint
			logger.info("perform(HttpServletRequest req, HttpServletResponse resp)");
			/*TODO logger.debug("Parameter: "
					+ "String projectName(" + req.getParameter("projectName") + "), "
					+ "String titel(" + req.getParameter("titel") + ")"
					+ "String aufgabenStellung(" + req.getParameter("aufgabenStellung") + ")"
					+ "Date date(" + req.getParameter("date") + ")"
					);
			 * 
			 */
			
			
			
			try {
				synchronized(session){
					//Manager in aktion
					mainManager.getDocumentManager().addNewDocu((User)session.getAttribute("aktUser"),
																(Project)session.getAttribute("aktProject"), 
																(List<FileItem>)req.getAttribute("data"));
				}
			}catch(NullPointerException e){
				logger.error(e.getMessage(), e);
			}
			
			
			req.setAttribute("contentFile", "showAllDocu.jsp");
		}catch (ProjectException e) {
			logger.error(e.getMessage(), e);
			req.setAttribute("contentFile", "error.jsp");
			req.setAttribute("errorString", e.getMessage());
		}
		
	}
}
