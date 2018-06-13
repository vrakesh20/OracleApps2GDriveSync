DROP SEQUENCE XX_GDRIVE_SEQ;

CREATE SEQUENCE XX_GDRIVE_SEQ start with 1 increment by 1;

DROP TABLE XX_GDRIVEFILES_T;

CREATE TABLE XX_GDRIVEFILES_T
  (
   file_id            NUMBER     primary key
  ,file_source        VARCHAR2(240)
  ,file_ref           VARCHAR2(240)
  ,file_like          VARCHAR2(240)
  ,ora_dirpath        VARCHAR2(255)
  ,ora_filename       VARCHAR2(255)
  ,source_request_id  NUMBER
  ,file_date          DATE
  ,share_with         VARCHAR2(2000)
  ,email_msg		      VARCHAR2(2000)
  ,gdrive_folderid    VARCHAR2(240)
  ,gdrive_foldername  VARCHAR2(240)
  ,gdrive_fileid      VARCHAR2(240)
  ,gdrive_filename    VARCHAR2(240)
  ,sync_status         VARCHAR2(1)
  ,status_message      VARCHAR2(2000)
  ,request_id          NUMBER
  ,created_by          NUMBER
  ,creation_date       DATE
  ,last_updated_by     NUMBER
  ,last_updated_date   DATE
);
