package xx.oracle.apps.fnd.integrator;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import java.security.GeneralSecurityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.api.services.drive.Drive.Permissions;

public class XXGDriveUtil {
	/**
	 * Build and returns a Drive service object authorized with the service
	 * accounts.
	 *
	 * @return Drive service object that is ready to make requests.
	 */
	public static Drive getDriveService(String pServiceAccountEmail, String pServiceAccountPKCS12File)
			throws GeneralSecurityException, IOException, URISyntaxException {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		List<String> scopes = new ArrayList<String>();
		scopes.add(DriveScopes.DRIVE);
		GoogleCredential credential;
		credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
				.setServiceAccountId(pServiceAccountEmail).setServiceAccountScopes(scopes)
				.setServiceAccountPrivateKeyFromP12File(new java.io.File(pServiceAccountPKCS12File)).build();
		Drive service = new Drive.Builder(httpTransport, jsonFactory, null).setHttpRequestInitializer(credential)
				.build();
		return service;
	}

	public static void shareFileOrFolder(Drive service, String fileid, String sharewith, String emailMsg)
			throws IOException {
		Permission newPermission = new Permission();

		newPermission.setEmailAddress(sharewith);
		newPermission.setType("user");
		newPermission.setRole("writer");
		Permissions.Create create = service.permissions().create(fileid, newPermission);
		create.setEmailMessage(emailMsg);
		create.execute();
	}

	public static String getMimeType(String fileUrl) throws java.io.IOException, MalformedURLException {
		String type = null;
		URL u = new URL(fileUrl);
		URLConnection uc = null;
		uc = u.openConnection();
		type = uc.getContentType();
		return type;
	}

	public String sync2GDrive(String pServiceAccountEmail,
														String pServiceAccountPKCS12File,
														String orafile,
														String gDriveFolder,
														String sharewith,
														String emailMsg,
														String gdriveFile
														) {

		try {
			Drive client = getDriveService(pServiceAccountEmail, pServiceAccountPKCS12File);
			java.io.File file = new java.io.File(orafile);
			File body = new File();
			String fileName = gdriveFile;
			if (fileName == null) {
				fileName = file.getName();
			}
			String mimeType = getMimeType("file://" + file.getAbsolutePath());
			body.setName(fileName);
			body.setMimeType(mimeType);
			body.setParents(Collections.singletonList(gDriveFolder));

			FileContent mediaContent = new FileContent(mimeType, file);
			File fileUploaded = client.files().create(body, mediaContent).setFields("id, parents").execute();
			String fileId = fileUploaded.getId();

			shareFileOrFolder(client, fileUploaded.getId(), sharewith, emailMsg);

			System.out.println(fileId);

			return fileId;

		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return "-1";

	}
}
