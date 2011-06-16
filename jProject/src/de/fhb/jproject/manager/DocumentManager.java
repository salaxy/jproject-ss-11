package de.fhb.jproject.manager;

import de.fhb.jproject.data.DAFactory;
import de.fhb.jproject.data.Document;
import de.fhb.jproject.data.JProjectPersistentManager;
import de.fhb.jproject.data.Member;
import de.fhb.jproject.data.Project;
import de.fhb.jproject.data.User;
import de.fhb.jproject.exceptions.ProjectException;
import de.fhb.jproject.repository.da.DocumentDA;
import de.fhb.jproject.repository.da.MemberDA;
import de.fhb.jproject.repository.da.ProjectDA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.orm.PersistentException;
import org.orm.PersistentSession;

public class DocumentManager {

	private DocumentDA docuDA;
	private ProjectDA projectDA;
	private String path="F:/";
	
	private static final Logger logger = Logger.getLogger(ProjectManager.class);
	
	public DocumentManager(){
		
		
		projectDA = DAFactory.getDAFactory().getProjectDA();
		docuDA = DAFactory.getDAFactory().getDocumentDA();
	}
	
	// !!! Dokument Actions !!!
	
	public void addNewDocu(User aktUser, Project aktProject, List<FileItem> fields)throws ProjectException{
		clearSession();
		
		logger.info("addNewDocu()");
		//logger.debug("String projectName("+aktProject.getName()+")");//TODO
		
		Document docu=null;
		Project project = null;
		
		
		clearSession();
		//EIGENTLICHE AKTIONEN
		try {
			project=projectDA.getProjectByORMID(aktProject.getName());
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Project nicht finden! "+ e1.getMessage());
		}catch (NullPointerException e) {
			throw new ProjectException("Keine projectName mitgegeben! "+ e.getMessage());
		}
		
		Iterator<FileItem> it = fields.iterator();
		while (it.hasNext()) {
			
			FileItem fileItem = it.next();	
			
			if (fileItem.getFieldName().equals("do")){
				fileItem = it.next();
			}
			
			logger.debug("File "+ fileItem.getName());
			//docu erzeugen und parameter setzen
			docu=docuDA.createDocument();
			//project setzen
			docu.setProject(project);
			//setzen weiterer attribute
			docu.setDateiname(fileItem.getName());
			
			//docu speichern
			try {
				// alles speichern
				docuDA.save(docu);
				saveDocument(fileItem);
			} catch (PersistentException e) {
				throw new ProjectException("Konnte Document nicht speichern! "+ e.getMessage());
			}
			catch (IOException e) {
				throw new ProjectException("Konnte Document nicht speichern! "+ e.getMessage());
			}
		}
		
	}
		
	public void deleteDocu(User aktUser, int docuId, String projectName)throws ProjectException {
		
		Document docu = null;
		File docuFile=null;
		
		//debuglogging
		logger.info("deleteDocu()");
		logger.debug("String projectName("+projectName+")");
		logger.debug("int taskId("+docuId+")");
		
		
		clearSession();

		//EIGENTLICHE AKTIONEN
		
		//hole den task
		try {
			docu=docuDA.getDocumentByORMID(docuId);
		} catch (PersistentException e) {
			throw new ProjectException("Kann Document nicht finden! "+ e.getMessage());
		}catch (NullPointerException e) {
			throw new ProjectException("Keine DocuId mitgegeben! "+ e.getMessage());
		}catch(IllegalArgumentException e){
			throw new ProjectException("DocuId fehlerhaft! "+ e.getMessage());
		}
		
		docuFile=new File(path + docu.getDateiname());
		
		//loeschen
		try {	
			clearSession();
			//task loeschen
			docuDA.delete(docu);
			docuFile.delete();
		} catch (PersistentException e) {
			throw new ProjectException("Kann Document nicht loeschen! "+ e.getMessage());
		}	
		
		
	}
	
	public void downloadDocu(){}
	
	public List<Document> showAllDocu(User aktUser, String projectName)throws ProjectException{
		
		Project project=null;
		Member memAktUser=null;
		
		clearSession();
		
		//debuglogging
		logger.info("showAllTasks()");
		
		//projekt holen
		try {
			project=projectDA.getProjectByORMID(projectName);
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Projekt nicht finden! "+ e1.getMessage());
		}
		//TODO return as SetCollection & fix duplicated entrys
		return Arrays.asList(project.document.toArray());
		
	}
	
	public void updateDocu(User aktUser, Project aktProject, List<FileItem> fields, int docuID)throws ProjectException{
		
		clearSession();
		
		logger.info("updateDocu()");
		//logger.debug("String projectName("+aktProject.getName()+")");//TODO
		
		
		
		Iterator<FileItem> it = fields.iterator();
		while (it.hasNext()) {
			
			FileItem fileItem = it.next();	
			
			if (fileItem.getFieldName().equals("do")){
				fileItem = it.next();
			}
			
			logger.debug("File "+ fileItem.getName());
			
			//docu speichern
			try {
				// alles speichern
				saveDocument(fileItem);
			} catch (IOException e) {
				throw new ProjectException("Konnte Document nicht speichern! "+ e.getMessage());
			}
		}
		
	}
	//TODO show as String
	public Document showDocu(User aktUser, String projectName, int documentId)throws ProjectException{return null;}
	
	private void clearSession() throws ProjectException{
		try {
			PersistentSession session;		
			//Session holen
			session = JProjectPersistentManager.instance().getSession();
			//und bereinigen
			session.clear();
		} catch (PersistentException e) {
			throw new ProjectException("Konnte Session nicht clearen! "+ e.getMessage());
		}
		
	}
	
	private void saveDocument(FileItem fileItem) throws IOException{
		File file = new File(path + fileItem.getName());
		FileOutputStream out = new FileOutputStream(file);
		byte[] data = new byte[1024];
	    int length=0;
	    InputStream in = fileItem.getInputStream();
		
		if (file.exists()) {
			file.delete();
		}
		
	    file.createNewFile();
		
		//solange ich noch daten von inputstream erhalte speicher
	    do {
	    	length=in.read(data);
	    	out.write(data, 0, length);
	    } while (length == 1024);
	    out.close();
	}
	
}
