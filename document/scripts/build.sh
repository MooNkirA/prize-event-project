app=$1
mv /opt/app/$app/ROOT.jar /opt/app/$app/`date +"%Y%m%d-%H%M%S"`.jar
cp frontend/$app/target/$app-0.0.1-SNAPSHOT.jar /opt/app/$app/ROOT.jar
cat /opt/app/Dockerfile > /opt/app/$app/Dockerfile
docker build -t $app:$BUILD_NUMBER /opt/app/$app
