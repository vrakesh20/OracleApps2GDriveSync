XX_APPS_PASSWORD=$1

while [ "$XX_APPS_PASSWORD" = "" ]
do
    if [ "$XX_APPS_PASSWORD" = "" ];then
            echo "Enter Apps Password : "
            read XX_APPS_PASSWORD
    else
            XX_APPS_PASSWORD=""
    fi
done

cd sql
sqlplus apps/$XX_APPS_PASSWORD @XX_GDRIVEFILES_T.sql
cd ..

cd plsql
sqlplus apps/$XX_APPS_PASSWORD @XX_GDRIVE_PKG.pks
sqlplus apps/$XX_APPS_PASSWORD @XX_GDRIVE_PKG.pkb
cd ..

cd java
cp -rf xx $JAVA_TOP/
cd ..

cd $JAVA_TOP/xx/oracle/apps/fnd/integrator/
$IAS_ORACLE_HOME/appsutil/jdk/bin/javac -classpath $JAVA_TOP:$JAVA_TOP/lib/* XXGDriveUtil.java
$IAS_ORACLE_HOME/appsutil/jdk/bin/javac -classpath $JAVA_TOP:$JAVA_TOP/lib/* XXGDriveSync.java
cd ..
