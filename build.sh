#!/bin/sh

BASE_DIR=`pwd`
SHELL_FILE_NAME="$BASE_DIR/mesos-chronos-submitter"

echo "\n\n++++++++++++ CLEAN ++++++\n"
CMD="mvn clean"
$CMD

echo "rm $SHELL_FILE_NAME if exists"
if [ -f "$SHELL_FILE_NAME" ];
then
    rm -rf "$SHELL_FILE_NAME"
fi

echo "\n\n+++++++++++++ PACKAGE +++++\n"
## build mesos-chronos-submitter
echo "package mesos-chronos-submitter"
CMD="mvn package"

$CMD

if [ $? -eq 0 ]; then
    echo "Compile mesos-chronos-submitter OK"
else
    echo "Compile Failed, Please check first"
    exit -1
fi

JAR_FILE="$BASE_DIR/target/mesos-chronos-submitter-0.01-SNAPSHOT-jar-with-dependencies.jar"

echo "check $JAR_FILE exists Or not"
if [ ! -e "$JAR_FILE" ]; then
    echo "Not found Jar $JAR_FILE"
    exit -1
else
    echo "Jar File OK"
fi

echo "\n\n+++++++++++++ BUILD +++++\n"
echo "Generate Bin File for mesos-chronos-submitter"

echo "Target File $SHELL_FILE_NAME"

echo "#!/bin/sh\n" >> $SHELL_FILE_NAME
echo 'self="$(cd "$(dirname "$0")" && pwd -P)"/"$(basename "$0")"' >> $SHELL_FILE_NAME
echo "java  \
-Dconfig.file=src/resources/application.conf \
-cp \$self \
com.weibo.datasys.submitter.Main \$@" >> $SHELL_FILE_NAME
echo "exit $?" >> $SHELL_FILE_NAME
cat $JAR_FILE >> $SHELL_FILE_NAME

chmod u=rwx,g=rx $SHELL_FILE_NAME

echo "Build mesos-chronos-submitter OK"
