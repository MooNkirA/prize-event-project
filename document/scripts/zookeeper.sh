docker run --name zookeeper -v /opt/data/zksingle:/data -p 2181:2181 -e ZOO_LOG4J_PROP="INFO,ROLLINGFILE"  -d zookeeper:3.4.13
