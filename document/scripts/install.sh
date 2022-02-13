app=$1
version=$2
port=$3

docker service rm $app
docker service create --name $app -p $port:8080 --hostname localhost $app:$version
