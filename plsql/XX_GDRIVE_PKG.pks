CREATE OR REPLACE PACKAGE XX_GDRIVE_PKG
AS

PROCEDURE refresh_gdrive_filelist (p_request_id IN VARCHAR2,
                                   p_gdrive_filename IN VARCHAR2,
                                   p_share_with IN VARCHAR2,
                                   p_email_msg IN VARCHAR2
                                   );

END XX_GDRIVE_PKG;

/
