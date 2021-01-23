export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-16.jdk/Contents/Home
export NATIVE_HOME="`pwd`/native"
export JVM_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:+FlightRecorder -Dforeign.restricted=permit -Dcom.sun.management.jmxremote -XX:StartFlightRecording=dumponexit=true,settings=profile,filename=`pwd`/jigsaw-events.jfr"

MODULE_PATH="modules"
MODULE_NAME="rtaware.jigsaw"
LAUNCHER_NAME="jigsaw"
MAIN_CLASS="Jigsaw"
PACKAGE_PATH="/com/rtaware/jigsaw"
PACKAGE_NAME="com.rtaware.jigsaw"
PACKAGE_TYPE="dmg"
PACKAGE_VERSION="1.0"
APP_NAME="Jigsaw"
COPYRIGHT_TEXT="Copyright © 2021"

echo "-----------------------------------------------"
echo "CleanUp"
echo "-----------------------------------------------"
rm -rf "jigsaw-events.jfr"
rm -rf "./${APP_NAME}.app"
rm -rf jigsaw


echo "-----------------------------------------------"
echo "Compile Native"
echo "-----------------------------------------------"
cd ./native
gcc -c -fpic add.c
gcc -shared -o add.so add.o
cd ..


echo "-----------------------------------------------"
echo "Java Version"
echo "-----------------------------------------------"
echo "`${JAVA_HOME}/bin/java -version`"

echo "-----------------------------------------------"
echo "Build Module"
echo "-----------------------------------------------"
"${JAVA_HOME}"/bin/javac \
    --add-modules jdk.incubator.foreign \
	-d  "${MODULE_PATH}"/"${MODULE_NAME}" \
        "${MODULE_PATH}"/"${MODULE_NAME}"/module-info.java \
        "${MODULE_PATH}"/"${MODULE_NAME}"/"${PACKAGE_PATH}"/record/JigsawRecord.java \
        "${MODULE_PATH}"/"${MODULE_NAME}"/"${PACKAGE_PATH}"/event/JigsawEvent.java \
        "${MODULE_PATH}"/"${MODULE_NAME}"/"${PACKAGE_PATH}"/Jigsaw.java

echo "-----------------------------------------------"
echo "Module Dependencies"
echo "-----------------------------------------------"
"${JAVA_HOME}"/bin/jdeps \
    --module-path "${MODULE_PATH}" --module "${MODULE_NAME}"


echo "-----------------------------------------------"
echo "jLink Module"
echo "-----------------------------------------------"
"${JAVA_HOME}"/bin/jlink 					 \
    --launcher "${LAUNCHER_NAME}"="${MODULE_NAME}"/"${PACKAGE_NAME}.${MAIN_CLASS}" \
    --add-options="${JVM_OPTS}"	             \
    --module-path "${MODULE_PATH}" 						 \
    --add-modules "${MODULE_NAME}"  					 \
    --output "${LAUNCHER_NAME}" 

echo "-----------------------------------------------"
echo "Package Application"
echo "-----------------------------------------------"
"${JAVA_HOME}"/bin/jpackage         \
--name "${APP_NAME}"                \
--module-path "${MODULE_PATH}"      \
--app-version "${PACKAGE_VERSION}"  \
--vendor "Rtaware Inc."             \
--copyright "${COPYRIGHT_TEXT}"     \
--type "${PACKAGE_TYPE}"            \
--icon ./resources/Jigsaw.icns      \
--java-options "${JVM_OPTS}"        \
-m "${MODULE_NAME}"/"${PACKAGE_NAME}.${MAIN_CLASS}"
#--verbose                          \
echo "-----------------------------------------------"
echo "Install Application"
echo "-----------------------------------------------"

sudo hdiutil attach "${APP_NAME}-${PACKAGE_VERSION}.${PACKAGE_TYPE}"
cp -r "/Volumes/${APP_NAME}/${APP_NAME}.app" .
sudo hdiutil detach "/Volumes/${APP_NAME}"

echo "-----------------------------------------------"
echo "Open JPackaged Application"
echo "-----------------------------------------------"
export NATIVE_HOME="`pwd`/native"
open ./"${APP_NAME}.app"

