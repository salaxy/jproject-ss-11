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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.orm.PersistentException;
import org.orm.PersistentSession;

public class DocumentManager {

	private ProjectRolesManager projectRolesManager;
	private GlobalRolesManager globalRolesManager;
	private MemberDA memberDA;
	private DocumentDA docuDA; 
	
	private static final Logger logger = Logger.getLogger(ProjectManager.class);
	
	public DocumentManager(ProjectRolesManager projectRolesManager, GlobalRolesManager globalRolesManager){
		
		this.projectRolesManager=projectRolesManager;
		this.globalRolesManager=globalRolesManager;
		memberDA = DAFactory.getDAFactory().getMemberDA();
		docuDA = DAFactory.getDAFactory().getDocumentDA();
	}
	
	// !!! Dokument Actions !!!
	
	public void addNewDocu(User aktUser, Project aktProject, List<FileItem> fields)throws ProjectException{
		clearSession();
		Member memAktUser=null;	
		Document docu=null;
		
		logger.info("addNewDocu()");
		logger.debug("String projectName("+aktProject.getName()+")");
		
		if(aktUser == null){
            throw new ProjectException("Sie sind nicht eingeloggt!");
        }
		
		if(!globalRolesManager.isAllowedAddNewDocuAction(aktUser.getGlobalRole())){
			//Projekt-Rolle des aktuellen Users holen
			memAktUser = getMember(aktUser, aktProject);
			
			//RECHTE-ABFRAGE Projekt
			if(!projectRolesManager.isAllowedAddNewDocuAction(memAktUser.getProjectRole())){
				throw new ProjectException("Sie haben keine Rechte zum hinzufuegen eines Dokumentes!");
			}		
		}
		
		clearSession();
		//EIGENTLICHE AKTIONEN
		
		Iterator<FileItem> it = fields.iterator();
		while (it.hasNext()) {
			
			FileItem fileItem = it.next();	
			logger.debug("File "+ fileItem.getName());
			//docu erzeugen und parameter setzen
			docu=docuDA.createDocument();
			//project setzen
			docu.setProject(aktProject);
			//setzen weiterer attribute
			docu.setDateiname(getFilename(fileItem.getName()));
			
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
		
	public void deleteDocu(User aktUser, int taskId, String projectName){}
	
	public void downloadDocu(){}
	
	public void showAllDocu(){}
	
	public void updateDocu(){}
	
	public void showDocu(){}
	
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
	private Member getMember(User aktUser, Project project)throws ProjectException{
		Member aktMember = null;
		try {
			aktMember=memberDA.getMemberByORMID(aktUser, project);
		} catch (PersistentException e1) {
			throw new ProjectException("Konnte Member nicht finden! "+ e1.getMessage());
		}
		return aktMember;
	}
	
	private void saveDocument(FileItem fileItem) throws IOException{
		File file = new File(getFilename(fileItem.getName()));
		FileOutputStream out = new FileOutputStream(file);
		byte[] data = new byte[1024];
	    int offset = 0;
	    int length=0;
	    InputStream in = fileItem.getInputStream();
	    
	    //solange ich noch daten von inputstream erhalte speicher
	    while ((length=in.read(data))== 1024){
	    	out.write(data, offset, length);
	    	offset +=1024;
	    }
	    out.write(data,offset,length);	
	}
	
	private String getFilename(String pfad){
		int tempi=0;
		// suche '\'
		for (int i=pfad.length(); i>0; i--){
			if (pfad.charAt(i)==92){
				tempi=i;
			}
		}
		return pfad.substring(tempi+1);
	}
	
}
