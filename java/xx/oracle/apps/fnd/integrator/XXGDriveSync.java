package xx.oracle.apps.fnd.integrator;

import java.io.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.*;
import java.sql.*;

import oracle.apps.fnd.cp.request.CpContext;
import oracle.apps.fnd.cp.request.JavaConcurrentProgram;
import oracle.apps.fnd.cp.request.LogFile;
import oracle.apps.fnd.cp.request.OutFile;
import oracle.apps.fnd.cp.request.ReqCompletion;
import oracle.apps.fnd.util.NameValueType;
import oracle.apps.fnd.util.ParameterList;


public class XXGDriveSync implements JavaConcurrentProgram
{

  private static String retcode = "NORMAL";
  private static String errbuff = "";

  private static Connection mJConn;
  private static ParameterList lPara;
  private static ReqCompletion lRC;

  private static OutFile lOF;
  private static LogFile lLF;

  private static String pServiceAccountEmail = null;
  private static String pServiceAccountPKCS12File = null;
  private static String pRequestId = null;
  private static String pGDriveFileName = null;
  private static String pShareWith = null;
  private static String pEmailMsg = null;


  public static void refreshGDriveFileList(String requestId, String gDriveFileName, String shareWith, String emailMsg) {

	  lLF.writeln("", LogFile.STATEMENT);
	  lLF.writeln("Entering method refreshGDriveFileList", LogFile.STATEMENT);
	  long startTime = System.nanoTime();

	  String script = 		" begin XX_GDRIVE_PKG.refresh_gdrive_filelist(?,?,?,?);	commit; end;";
	  lLF.writeln("script="+script, LogFile.STATEMENT);
	  PreparedStatement stmt = null;

	  try {
	    stmt = mJConn.prepareStatement(script);
	    stmt.setString(1,requestId);
	    stmt.setString(2,gDriveFileName);
      stmt.setString(3,shareWith);
      stmt.setString(4,emailMsg);
	    stmt.executeUpdate();
	    stmt.close();
      retcode = "NORMAL";
	  }
	  catch(Exception e)
	  {
	      lLF.writeln("Error in refreshGDriveFileList is "+e.toString(), LogFile.STATEMENT);
	      retcode = "ERROR";
	      errbuff = e.toString();
	      e.printStackTrace();
	  }
	  finally
	  {
	    long endTime = System.nanoTime();
	    long duration = (endTime - startTime)/1000000000;
	    System.out.println("leaving method refreshGDriveFileList in " + duration + " seconds");
	  }
	}

    public static void inserttogdt(String p_file_source, String p_file_ref, String p_file_like
							   , String p_ora_dirpath, String element, String lastMod, String p_share_with, String p_email_msg
							   , String p_gdrive_folderid, String p_gdrive_foldername, String p_gdrive_filename, String status, String error_msg)
  {
    PreparedStatement lStmt1;
	String effDate = null;
	if(lastMod!=null) {
		effDate = "TO_DATE('19700101','YYYYMMDD') + ("+lastMod+"/1000/60/60/24)";
	}
    String lExecute1 =
      "begin "
			     + "INSERT INTO XX_GDRIVEFILES_T"
				 + "(seq_id"
				 + ",file_source"
				 + ",file_ref"
				 + ",file_like"
				 + ",ora_dirpath"
				 + ",ora_filename"
				 + ",file_date"
				 + ",share_with"
				 + ",email_msg"
				 + ",status"
				 + ",record_date"
				 + ",error_msg"
				 + ",gdrive_folderid"
         + ",p_gdrive_foldername"
				 + ",gdrive_filename"
				 + ")"
				 + "VALUES"
				 + "(XX_GDRIVE_SEQ.nextval"
				 + ",?"
				 + ",?"
				 + ",?"
				 + ",?"
				 + ",?"
				 + ",fnd_timezone_pub.adjust_datetime"
				 + "(date_time => " + effDate
				 + ",from_tz   => 'GMT'"
				 + ",to_tz     => fnd_timezones.get_server_timezone_code)"
				 + ",?"
				 + ", ?"
				 + ",?"
				 + ",SYSDATE"
				 + ",?"
				 + ",NVL(?"
				 +",hr_general.decode_lookup('XX_GDRIVE_CONFIG_LKP','GDRIVE_FOLDERID'))"
         +",hr_general.decode_lookup('XX_GDRIVE_CONFIG_LKP','GDRIVE_FOLDER'))"
				 + ",NVL(?,?)"
				 + ");"
			     + "commit; "
			     + "end;";

    try
    {
      lStmt1 = mJConn.prepareStatement(lExecute1);
	    lStmt1.setString(1, p_file_source);
      lStmt1.setString(2, p_file_ref);
      lStmt1.setString(3, p_file_like);
  	  lStmt1.setString(4, p_ora_dirpath);
  	  lStmt1.setString(5, element);
  	  lStmt1.setString(6, p_share_with);
  	  lStmt1.setString(7, p_email_msg);
  	  lStmt1.setString(8, status);
  	  lStmt1.setString(9, error_msg);
  	  lStmt1.setString(10, p_gdrive_folderid);
      lStmt1.setString(10, p_gdrive_foldername);
  	  lStmt1.setString(11, p_gdrive_filename);
  	  lStmt1.setString(12, element);
      int eStatus = lStmt1.executeUpdate();
      lStmt1.close();
      retcode = "NORMAL";
    } catch (Exception e1)
    {
      retcode = "ERROR";
      errbuff = e1.toString();
    }
}

  public static void uploadFiles()
  {
	  lLF.writeln("Trying to share files registered in XX_GDRIVEFILES_T ...", LogFile.STATEMENT);
  	  PreparedStatement lStmt2;
      String lQuery =
        " select * from XX_GDRIVEFILES_T where request_id = fnd_global.conc_request_id";
	  //lLF.writeln(lQuery, LogFile.STATEMENT);
      try
      {
        lStmt2 = mJConn.prepareStatement(lQuery);
        ResultSet lRs = lStmt2.executeQuery();

        //lOF.writeln("Here is the list of files that needs to be shared ");

        while (lRs.next())
        {
          String targetFile = lRs.getString(5) + lRs.getString(6);
          lLF.writeln("Copying file " + targetFile + " to Google Drive", LogFile.STATEMENT);
		  		String gDrivefileId = "-1";

		  	  XXGDriveUtil gdriveutil = new XXGDriveUtil();
					gDrivefileId = gdriveutil.sync2GDrive(
																										pServiceAccountEmail,
																										pServiceAccountPKCS12File,
																										targetFile,                 // orafile
																										lRs.getString(11),          // gDriveFolder
																										lRs.getString(9),           // sharewith
																										lRs.getString(10),          // emailMsg
																										lRs.getString(14)           // gdriveFile
																								);

          String status = null;
          if(gDrivefileId!=null && !"-1".equals(gDrivefileId)) { status = "SHARED"; } else {status = "ERROR";}
          lLF.writeln("Updating the status of file " + lRs.getString(6) + " to " + status, LogFile.STATEMENT);
          updateStatus(lRs.getInt(1),gDrivefileId, status);//"SHARED"
        }
        lStmt2.close();
        retcode = "NORMAL";
      } catch (Exception e2)
      {
        retcode = "ERROR";
        errbuff = e2.toString();
      }
	  //lLF.writeln("done", LogFile.STATEMENT);
  }

  public static void updateStatus(Integer pFileId, String gDrivefileId, String status)
  {
      PreparedStatement lStmt3;
      String lExecute2 =
        "begin " + "update  XX_GDRIVEFILES_T " + "set status = ?, gdrive_fileid = ? " +
        "where file_id = ?; " + " commit; end;";
	  //lLF.writeln(lExecute2, LogFile.STATEMENT);

      try
      {
        lStmt3 = mJConn.prepareStatement(lExecute2);
				lStmt3.setString(1, status);
				lStmt3.setString(2, gDrivefileId);
        lStmt3.setInt(3, pFileId);
        int eStatus = lStmt3.executeUpdate();
        lStmt3.close();
      } catch (Exception e3)
      {
        retcode = "ERROR";
        errbuff = e3.toString();
      }
	  //lLF.writeln("done", LogFile.STATEMENT);
  }

  public void runProgram(CpContext pCpContext)
  {

    // get the JDBC connection object
    mJConn = pCpContext.getJDBCConnection();
    // get parameter list object from CpContext
    lPara = pCpContext.getParameterList();
    // get ReqCompletion object from CpContext
    lRC = pCpContext.getReqCompletion();

    // get OutFile object from CpContext
    lOF = pCpContext.getOutFile();
    // get LogFile object from CpContext
    lLF = pCpContext.getLogFile();

    lLF.writeln("Entering  runProgram : ", LogFile.STATEMENT);

    while (lPara.hasMoreElements())
    {
      NameValueType aNVT = lPara.nextParameter();
      if (aNVT.getName().equals("Service Account Email"))
      {
        pServiceAccountEmail = aNVT.getValue();
      }
      if (aNVT.getName().equals("Service Account PKCS12 File"))
      {
        pServiceAccountPKCS12File = aNVT.getValue(); // googledriveconfig.p12
      }
	    if (aNVT.getName().equals("Request Id"))
      {
        pRequestId = aNVT.getValue();
      }
      if (aNVT.getName().equals("GDrive File Name"))
      {
        pGDriveFileName = aNVT.getValue();
      }
      if (aNVT.getName().equals("Share With"))
      {
        pShareWith = aNVT.getValue();
      }
      if (aNVT.getName().equals("Email Msg"))
      {
        pEmailMsg = aNVT.getValue();
      }
    }

	lLF.writeln("*************************************************************", LogFile.STATEMENT);
  lLF.writeln("Service Account Email          : " + pServiceAccountEmail, LogFile.STATEMENT);
  lLF.writeln("Service Account PKCS12 File    : " + pServiceAccountPKCS12File, LogFile.STATEMENT);
	lLF.writeln("Request Id                     : " + pRequestId, LogFile.STATEMENT);
  lLF.writeln("GDrive File Name               : " + pGDriveFileName, LogFile.STATEMENT);
  lLF.writeln("Share With                     : " + pShareWith, LogFile.STATEMENT);
  lLF.writeln("Email Msg                      : " + pEmailMsg, LogFile.STATEMENT);
	lLF.writeln("*************************************************************", LogFile.STATEMENT);

  refreshGDriveFileList(pRequestId, pGDriveFileName, pShareWith, pEmailMsg);

    if ("NORMAL".equals(retcode))
    {
	     uploadFiles();
    }

    if ("NORMAL".equals(retcode))
    {
      lRC.setCompletion(ReqCompletion.NORMAL, "Request Completed Normal");
    }
	else
    {
      lLF.writeln("Error in program : " + errbuff, LogFile.STATEMENT);
      lRC.setCompletion(ReqCompletion.ERROR, retcode);
    }

	pCpContext.releaseJDBCConnection();

    lLF.writeln("Leaving  runProgram : ", LogFile.STATEMENT);

  }
}
