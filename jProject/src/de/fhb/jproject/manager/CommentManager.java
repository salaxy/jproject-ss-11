package de.fhb.jproject.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.orm.PersistentException;
import org.orm.PersistentSession;

import de.fhb.jproject.data.Comment;
import de.fhb.jproject.data.CommentDocument;
import de.fhb.jproject.data.CommentProject;
import de.fhb.jproject.data.CommentSourcecode;
import de.fhb.jproject.data.CommentTask;
import de.fhb.jproject.data.DAFactory;
import de.fhb.jproject.data.Document;
import de.fhb.jproject.data.JProjectPersistentManager;
import de.fhb.jproject.data.Member;
import de.fhb.jproject.data.Project;
import de.fhb.jproject.data.Sourcecode;
import de.fhb.jproject.data.Task;
import de.fhb.jproject.data.User;
import de.fhb.jproject.exceptions.ProjectException;
import de.fhb.jproject.repository.da.CommentDA;
import de.fhb.jproject.repository.da.CommentDocumentDA;
import de.fhb.jproject.repository.da.CommentProjectDA;
import de.fhb.jproject.repository.da.CommentSourcecodeDA;
import de.fhb.jproject.repository.da.CommentTaskDA;
import de.fhb.jproject.repository.da.DocumentDA;
import de.fhb.jproject.repository.da.MemberDA;
import de.fhb.jproject.repository.da.ProjectDA;
import de.fhb.jproject.repository.da.SourcecodeDA;
import de.fhb.jproject.repository.da.TaskDA;

public class CommentManager {
	
	private ProjectRolesManager projectRolesManager;
	private GlobalRolesManager globalRolesManager;
	
	private DocumentDA documentDA;
	private MemberDA memberDA;
	private SourcecodeDA sourcecodeDA;
	private CommentDA commentDA;
	private CommentDocumentDA commentDocumentDA;
	private CommentSourcecodeDA commentSourcecodeDA;
	private TaskDA taskDA;
	private CommentTaskDA commentTaskDA;
	private ProjectDA projectDA;
	private CommentProjectDA commentProjectDA;
	
	private static final Logger logger = Logger.getLogger(CommentManager.class);
	
	
	
	public CommentManager(ProjectRolesManager projectRolesManager,GlobalRolesManager globalRolesManager){
		documentDA = DAFactory.getDAFactory().getDocumentDA();
		memberDA = DAFactory.getDAFactory().getMemberDA();
		sourcecodeDA = DAFactory.getDAFactory().getSourcecodeDA();
		commentDA = DAFactory.getDAFactory().getCommentDA();
		commentDocumentDA = DAFactory.getDAFactory().getCommentDocumentDA();
		commentSourcecodeDA = DAFactory.getDAFactory().getCommentSourcecodeDA();
		taskDA = DAFactory.getDAFactory().getTaskDA();
		commentTaskDA = DAFactory.getDAFactory().getCommentTaskDA();
		projectDA = DAFactory.getDAFactory().getProjectDA();
		commentProjectDA = DAFactory.getDAFactory().getCommentProjectDA();
		
		this.projectRolesManager=projectRolesManager;
		this.globalRolesManager=globalRolesManager;
	}
	
	// !!! Comment Actions !!!
	
	/**
	 * kommentieren eines Dokuments
	 */
	public void commentDocu(User aktUser, int documentId, String inhalt)
	throws ProjectException{ 	
		
		Member memAktUser=null;	
		CommentDocument commentDocu=null;
		Comment comment=null;
		Document document=null;
		
		
		//debuglogging
		logger.info("commentDocu()");
		logger.debug("int documentId("+documentId+")"
				+"String inhalt("+inhalt+")");	
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		
		//document holen (und implizit damit auch das Project)
		try {
			document=documentDA.loadDocumentByORMID(documentId);
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Dokument nicht finden! "+ e1.getMessage());
		}catch (NullPointerException e) {
			throw new ProjectException("Keine documentId mitgegeben! "+ e.getMessage());
		}catch(IllegalArgumentException e){
			throw new ProjectException("DocumentId fehlerhaft! "+ e.getMessage());
		}	
			
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedCommentDocuAction(aktUser.getGlobalRole())){
			
			//Member des aktuellen Users holen
			// TODO HIER IST DAS PROBLEM WARUM ER DIE EINTRAEGE DOPPELT MACHT!!!!!!!!!DER MEMBER IST SCHULD!!!!
			try {
				memAktUser=memberDA.loadMemberByORMID(aktUser, document.getProject());
				//RECHTE-ABFRAGE Projekt
				if(!(projectRolesManager.isAllowedCommentDocuAction(memAktUser.getProjectRole()))){
					throw new ProjectException("Sie haben keine Rechte dieses Dokument zu kommentieren!");
				}
				//memberDA.save(memAktUser);
				//System.out.println("Member: "+memAktUser.getUserId());
				
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}	
		}
		
		//EIGENTLICHE AKTIONEN
		
		//commentdocu erstellen
		commentDocu=commentDocumentDA.createCommentDocument();
		commentDocu.setDocument(document);
		
		//comment erstellen
		comment=commentDA.createComment(); 
		comment.setEntry(inhalt);
		comment.setUser(aktUser);
		comment.setCommentDocument(commentDocu);
		
		//Comment speichern
		try {		
			clearSession();
			
			//Referenz auf Comment setzen
			commentDocu.setComment(comment);
			//Comment speichern
			commentDA.save(comment);
			
			//CommentDocu speichern
			commentDocumentDA.save(commentDocu);
			clearSession();
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
		
	}
			
	/**
	 * 
	 * @param sourcecodeId
	 * @param inhalt
	 * @throws ProjectException
	 */
	public void commentSource(User aktUser, int sourcecodeId, String inhalt)
	throws ProjectException{ 	
		
		Member memAktUser=null;	
		CommentSourcecode commentSource=null;
		Comment comment=null;
		Sourcecode sourcecode=null;
		
		
		//debuglogging
		logger.info("commentSource()");
		logger.debug("int sourcecodeId("+sourcecodeId+")"
				+"String inhalt("+inhalt+")");	
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		
		//sourcecode holen (und implizit damit auch das Project)
		try {
			sourcecode=sourcecodeDA.getSourcecodeByORMID(sourcecodeId);
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Sourcecode nicht finden! "+ e1.getMessage());
		}catch (NullPointerException e) {
			throw new ProjectException("Keine sourcecodeId mitgegeben! "+ e.getMessage());
		}catch(IllegalArgumentException e){
			throw new ProjectException("sourcecodeId fehlerhaft! "+ e.getMessage());
		}	
			
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedCommentSourceAction(aktUser.getGlobalRole())){
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, sourcecode.getProject());
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedCommentSourceAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte diesen Sourcecode zu kommentieren!");
			}	
		}		

		
		//EIGENTLICHE AKTIONEN
		
		//CommentSourcecode erstellen
		commentSource=commentSourcecodeDA.createCommentSourcecode();
		commentSource.setSourcecode(sourcecode);
		
		//comment erstellen
		comment=commentDA.createComment(); 
		comment.setEntry(inhalt);
		comment.setUser(aktUser);
		comment.setCommentSourcecode(commentSource);
		
		//Comment speichern
		try {		
			clearSession();
			//Member speichern
			commentDA.save(comment);
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
		
		//referenz auf comment setzen
		commentSource.setComment(comment);
		
		//und CommentSourcecode speichern
		try {		
			clearSession();
			// speichern
			commentSourcecodeDA.save(commentSource);
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
		
		
		
	}
	
	/**
	 * 
	 * @param aktUser
	 * @param taskId
	 * @param inhalt
	 * @throws ProjectException
	 */
	public void commentTask(User aktUser, int taskId, String inhalt)	
	throws ProjectException{ 	
		
		Member memAktUser=null;	
		CommentTask commentTask=null;
		Comment comment=null;
		Task task=null;
		
		
		//debuglogging
		logger.info("commentTask()");
		logger.debug("int taskId("+taskId+")"
				+"String inhalt("+inhalt+")");	
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		
		//Task holen (und implizit damit auch das Project)
		try {
			task=taskDA.getTaskByORMID(taskId);
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Task nicht finden! "+ e1.getMessage());
		}catch (NullPointerException e) {
			throw new ProjectException("Keine taskId mitgegeben! "+ e.getMessage());
		}catch(IllegalArgumentException e){
			throw new ProjectException("taskId fehlerhaft! "+ e.getMessage());
		}	
			
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedCommentTaskAction(aktUser.getGlobalRole())){
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, task.getProject());
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedCommentTaskAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte diese Aufgabe zu kommentieren!");
			}	
		}		

		
		//EIGENTLICHE AKTIONEN
		
		//commentdocu erstellen
		commentTask=commentTaskDA.createCommentTask();
		commentTask.setTask(task);
		
		//comment erstellen
		comment=commentDA.createComment(); 
		comment.setEntry(inhalt);
		comment.setUser(aktUser);
		comment.setCommentTask(commentTask);
		
		//Comment speichern
		try {		
			clearSession();
			//Member speichern
			commentDA.save(comment);
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
		
		//referenz auf comment setzen
		commentTask.setComment(comment);
		
		//und CommentTaskspeichern
		try {		
			clearSession();
			// speichern
			commentTaskDA.save(commentTask);
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
	}
	

	/**
	 * 
	 * @param aktUser
	 * @param projectName
	 * @param inhalt
	 * @throws ProjectException
	 */
	public void commentProject(User aktUser, String projectName, String inhalt)	
	throws ProjectException{ 	
		
		Member memAktUser=null;	
		CommentProject commentProject=null;
		Comment comment=null;
		Project project=null;
		
		
		//debuglogging
		logger.info("commentProject()");
		logger.debug("String projectName("+projectName+")"
				+"String inhalt("+inhalt+")");	
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		
		//Project holen
		try {
			project=projectDA.getProjectByORMID(projectName);
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Project nicht finden! "+ e1.getMessage());
		}catch (NullPointerException e) {
			throw new ProjectException("Keine projectName mitgegeben! "+ e.getMessage());
		}
			
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedCommentProjectAction(aktUser.getGlobalRole())){
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, project);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedCommentProjektAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte dieses Projekt zu kommentieren!");
			}	
		}		

		
		//EIGENTLICHE AKTIONEN
		
		//commentdocu erstellen
		commentProject=commentProjectDA.createCommentProject();
		commentProject.setProject(project);
		
		//comment erstellen
		comment=commentDA.createComment(); 
		comment.setEntry(inhalt);
		comment.setUser(aktUser);
		comment.setCommentProject(commentProject);
		
		//Comment speichern
		try {		
			clearSession();
			//Member speichern
			commentDA.save(comment);
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
		
		//referenz auf comment setzen
		commentProject.setComment(comment);
		
		//und CommentProject speichern
		try {		
			clearSession();
			// speichern
			commentProjectDA.save(commentProject);
		} catch (PersistentException e) {
			
			throw new ProjectException("Konnte comment nicht speichern! "+ e.getMessage());
		}
		
	}
	
	public void deleteComment(){
		
		//TODO ANTWORT: Admin, Ersteller und project-Leader
	}
	
	
	public void updateComment(){
		//TODO ANTWORT: Admin, Ersteller und Leader
	}
	
	/**
	 * Alle Kommentare eines Dokuments holen
	 * @param aktUser
	 * @param projectName
	 * @param documentId
	 * @return
	 * @throws ProjectException
	 */
	public List<Comment> showAllComments41Docu(User aktUser, String projectName, int documentId)
	throws ProjectException{
		
		List<Comment> list=new ArrayList<Comment>();
		CommentDocument[] commentDocument=null;
		Comment c=null;
		Project project=null;
		Member memAktUser=null;	
		
		//debuglogging
		logger.info("showAllComments41Docu()");
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		

			
		//RECHTE-ABFRAGE Global
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedShowAllComments41DocuAction(aktUser.getGlobalRole())){
			
			//Project holen
			try {
				project=projectDA.getProjectByORMID(projectName);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Project nicht finden! "+ e1.getMessage());
			}catch (NullPointerException e) {
				throw new ProjectException("Keine projectName mitgegeben! "+ e.getMessage());
			}
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, project);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedShowAllComments41DocuAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte diese Comments anzuzeigen!");
			}	
		}	
		
		
		//EIGENTLICHE AKTIONEN
		
		//holen der commentDocument
		try {
			commentDocument=commentDocumentDA.listCommentDocumentByQuery("DocumentID="+documentId,"CommentID" );
		} catch (PersistentException e) {
			System.out.println("Fehler");
			throw new ProjectException("Kann CommentDocument nicht finden! "+ e.getMessage());
		}
		
		//holen der comments selbst
		for(int i=0;i<commentDocument.length;i++){
			try {
				c=commentDA.getCommentByORMID(commentDocument[i].getCommentId());
			} catch (PersistentException e) {
				throw new ProjectException("Kann Comment nicht finden! "+ e.getMessage());
			}
			//zur liste hinzufuegen
			list.add(c);
		}
		
		return list;
	}
	
	
	/**
	 * 
	 * @param aktUser
	 * @param projectName
	 * @param sourcecodeId
	 * @throws ProjectException
	 */
	public List<Comment>  showAllComments41Source(User aktUser, String projectName, int sourcecodeId)
	throws ProjectException{
		
		List<Comment> list=new ArrayList<Comment>();
		CommentSourcecode[] commentSourcecode=null;
		Comment c=null;
		Project project=null;
		Member memAktUser=null;	
		
		//debuglogging
		logger.info("showAllComments41Source()");
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		

			
		//RECHTE-ABFRAGE Global
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedShowAllComments41DocuAction(aktUser.getGlobalRole())){
			
			//Project holen
			try {
				project=projectDA.getProjectByORMID(projectName);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Project nicht finden! "+ e1.getMessage());
			}catch (NullPointerException e) {
				throw new ProjectException("Keine projectName mitgegeben! "+ e.getMessage());
			}
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, project);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedShowAllComments41DocuAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte diese Comments anzuzeigen!");
			}	
		}	
		
		
		//EIGENTLICHE AKTIONEN
		
		//holen der CommentSourcecode
		try {
			commentSourcecode=commentSourcecodeDA.listCommentSourcecodeByQuery("SourcecodeID="+sourcecodeId,"CommentID" );
		} catch (PersistentException e) {
			System.out.println("Fehler");
			throw new ProjectException("Kann CommentSourcecode nicht finden! "+ e.getMessage());
		}
		
		//holen der comments selbst
		for(int i=0;i<commentSourcecode.length;i++){
			try {
				c=commentDA.getCommentByORMID(commentSourcecode[i].getCommentId());
			} catch (PersistentException e) {
				throw new ProjectException("Kann Comment nicht finden! "+ e.getMessage());
			}
			//zur liste hinzufuegen
			list.add(c);
		}
		
		return list;
	}
	
	/**
	 * 
	 * @param aktUser
	 * @param projectName
	 * @param taskId
	 * @return
	 * @throws ProjectException
	 */
	public List<Comment> showAllComments41Task(User aktUser, String projectName, int taskId)
	throws ProjectException{
		
		List<Comment> list=new ArrayList<Comment>();
		CommentTask[] commentTask=null;
		Comment c=null;
		Project project=null;
		Member memAktUser=null;	
		
		//debuglogging
		logger.info("showAllComments41Task()");
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		

			
		//RECHTE-ABFRAGE Global
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedShowAllComments41DocuAction(aktUser.getGlobalRole())){	
			
			//Project holen
			try {
				project=projectDA.getProjectByORMID(projectName);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Project nicht finden! "+ e1.getMessage());
			}catch (NullPointerException e) {
				throw new ProjectException("Keine projectName mitgegeben! "+ e.getMessage());
			}
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, project);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedShowAllComments41DocuAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte diese Comments anzuzeigen!");
			}	
		}	
		
		
		//EIGENTLICHE AKTIONEN
		
		//holen der commentTask
		try {
			commentTask=commentTaskDA.listCommentTaskByQuery("TaskID="+taskId,"CommentID" );
		} catch (PersistentException e) {
			System.out.println("Fehler");
			throw new ProjectException("Kann CommentTask nicht finden! "+ e.getMessage());
		}
		
		//holen der comments selbst
		for(int i=0;i<commentTask.length;i++){
			try {
				c=commentDA.getCommentByORMID(commentTask[i].getCommentId());
			} catch (PersistentException e) {
				throw new ProjectException("Kann Comment nicht finden! "+ e.getMessage());
			}
			//zur liste hinzufuegen
			list.add(c);
		}
		
		return list;
	}
	
	/**
	 * 
	 * @param aktUser
	 * @param projectName
	 * @return
	 * @throws ProjectException
	 */
	public List<Comment> showAllComments41Project(User aktUser, String projectName)
	throws ProjectException{
		
		List<Comment> list=new ArrayList<Comment>();
		CommentProject[] commentProject=null;
		Comment c=null;
		Project project=null;
		Member memAktUser=null;	
		
		//debuglogging
		logger.info("showAllComments41Project()");
		
        //abfrage ob user eingeloggt
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
			
		//RECHTE-ABFRAGE Global
		//wenn user nicht Admin ist dann Member holen und Abfrage der Rechte im Projekt
		if(!globalRolesManager.isAllowedShowAllComments41DocuAction(aktUser.getGlobalRole())){
			
			//Project holen
			try {
				project=projectDA.getProjectByORMID(projectName);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Project nicht finden! "+ e1.getMessage());
			}catch (NullPointerException e) {
				throw new ProjectException("Keine projectName mitgegeben! "+ e.getMessage());
			}
			
			//Member des aktuellen Users holen
			try {
				memAktUser=memberDA.getMemberByORMID(aktUser, project);
			} catch (PersistentException e1) {
				throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
			}
			
			//RECHTE-ABFRAGE Projekt
			if(!(projectRolesManager.isAllowedShowAllComments41DocuAction(memAktUser.getProjectRole()))){
				throw new ProjectException("Sie haben keine Rechte diese Comments anzuzeigen!");
			}	
		}	
		
		
		//EIGENTLICHE AKTIONEN
		
		//holen der commentDocument
		try {
			commentProject=commentProjectDA.listCommentProjectByQuery("Project="+projectName,"CommentID" );
		} catch (PersistentException e) {
			System.out.println("Fehler");
			throw new ProjectException("Kann CommentProject nicht finden! "+ e.getMessage());
		}
		
		//holen der comments selbst
		for(int i=0;i<commentProject.length;i++){
			try {
				c=commentDA.getCommentByORMID(commentProject[i].getCommentId());
			} catch (PersistentException e) {
				throw new ProjectException("Kann Comment nicht finden! "+ e.getMessage());
			}
			//zur liste hinzufuegen
			list.add(c);
		}
		
		return list;
	}
	
	
	private void clearSession() throws PersistentException{
		PersistentSession session;		
		//Session holen
		session = JProjectPersistentManager.instance().getSession();
		//und bereinigen
		session.clear();
	}
}
