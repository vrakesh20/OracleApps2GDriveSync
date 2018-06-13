CREATE OR REPLACE PACKAGE BODY XX_GDRIVE_PKG
AS

   PROCEDURE refresh_gdrive_filelist (p_request_id IN VARCHAR2,
                                      p_gdrive_filename IN VARCHAR2,
                                      p_share_with IN VARCHAR2,
                                      p_email_msg IN VARCHAR2
                                      )
   AS
   BEGIN
   INSERT INTO XX_GDRIVEFILES_T
    (
      file_id
     ,file_source
     ,file_ref
     ,file_like
     ,ora_dirpath
     ,ora_filename
     ,source_request_id
     ,file_date
     ,share_with
     ,email_msg
     ,gdrive_folderid
     ,gdrive_foldername
     ,gdrive_fileid
     ,gdrive_filename
     ,sync_status
     ,status_message
     ,request_id
     ,created_by
     ,creation_date
     ,last_updated_by
     ,last_updated_date
    )
    SELECT
      XX_GDRIVE_SEQ.nextval AS file_id
     ,flv.attribute1 AS file_source
     ,flv.attribute2 AS file_ref
     ,flv.attribute3  AS file_like
     ,substr(decode(fcp.output_file_type, 'XML', nvl(cro.file_name, fcr.outfile_name), fcr.outfile_name),1,instr(decode(fcp.output_file_type, 'XML', nvl(cro.file_name, fcr.outfile_name), fcr.outfile_name),'/','-1')) AS ora_dirpath
     ,substr(decode(fcp.output_file_type, 'XML', nvl(cro.file_name, fcr.outfile_name), fcr.outfile_name),instr(decode(fcp.output_file_type, 'XML', nvl(cro.file_name, fcr.outfile_name), fcr.outfile_name),'/','-1')+1) AS ora_filename
     ,fcr.request_id AS source_request_id
     ,fcr.actual_completion_date AS file_date
     ,NVL(p_share_with,flv.attribute4) AS share_with
     ,NVL(p_email_msg,flv.attribute5) AS email_msg
     ,flv.attribute6 AS gdrive_folderid
     ,flv.attribute7 AS gdrive_foldername
     ,NULL AS gdrive_fileid
     ,NVL(p_gdrive_filename,substr(decode(fcp.output_file_type, 'XML', nvl(cro.file_name, fcr.outfile_name), fcr.outfile_name),instr(decode(fcp.output_file_type, 'XML', nvl(cro.file_name, fcr.outfile_name), fcr.outfile_name),'/','-1')+1)) || NVL2(mt.file_ext,'.'||mt.file_ext,'') AS gdrive_filename
     ,'N' AS sync_status
     ,NULL AS status_message
     ,fnd_global.conc_request_id AS request_id
     ,fnd_global.user_id AS created_by
     ,sysdate AS creation_date
     ,fnd_global.user_id AS last_updated_by
     ,sysdate AS last_updated_date
    FROM fnd_concurrent_requests fcr,
         FND_CONC_REQ_OUTPUTS  cro,
         FND_MIME_TYPES_TL mime,
         FND_MIME_TYPES mt,
         fnd_concurrent_programs fcp,
         fnd_lookup_values flv
    WHERE 1                         = 1
    AND fcp.concurrent_program_id   = fcr.concurrent_program_id
    AND flv.lookup_type             = 'XX_GDRIVE_PROGRAMS_LKP'
    AND flv.lookup_code             = fcp.concurrent_program_name
    AND fcr.request_id              = NVL(p_request_id,fcr.request_id)
    AND mime.file_format_code(+) = cro.file_type
    and mime.mime_type = mt.mime_type(+)
    and fcr.request_id = cro.concurrent_request_id(+)
    and fcr.concurrent_program_id = fcp.concurrent_program_id;
   COMMIT;
   EXCEPTION WHEN OTHERS THEN
    NULL;
   END;

END XX_GDRIVE_PKG;


/
