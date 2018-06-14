# OracleApps2GDriveSync

OracleApps2GDriveSync is an Utility that integrates outputs of Oracle Applications' Concurrent Programs/Requests with Google Drive

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.
Please run install.sh file in your Oracle Apps Instance. It will do the following:
  Compile XX_GDRIVEFILES_T.sql
  Compile XX_GDRIVE_PKG.pks
  Compile XX_GDRIVE_PKG.pkb
  Deploy XXGDriveUtil.java
  Deploy XXGDriveSync.java
After that
  Configure XX_GDRIVE_CONFIG_LKP Lookup
  Configure XX_GDRIVE_PROGRAMS_LKP Lookup
  Configure the Concurrent Program

### Prerequisites

Necessary Google Drive libraries in your $JAVA_TOP/lib

### Installing

Refer to Getting Started Section

## Running the tests

Once the program is configured and added to request group, please run the concurrent program and test it

## Deployment

Additional notes about how to deploy this on a live system:
Login to https://console.developers.google.com/ and create a project with a service account and credentials.
Generate the p12 key for the service account. Place this keep file under $JAVA_TOP. This file will be used by our Java Programs.

## Authors

* **Rakesh Vagvala**
