docker login -u victorrentea@gmail.com -p sesamopen
rem docker pull datastax/dse-server
docker pull cassandra
docker pull datastax/dse-studio
rem docker run -e DS_LICENSE=accept --memory 4g --name my-dse -d datastax/dse-server -g -s -k
rem docker run -e DS_LICENSE=accept --link my-dse -p 9091:9091 --memory 1g --name my-studio -d datastax/dse-studio

docker run -e DS_LICENSE=accept --memory 4g -p 9042:9042 --name my-dse -d cassandra -g -s -k
docker run -e DS_LICENSE=accept --link my-dse -p 8989:9091 --memory 1g --name my-studio -d datastax/dse-studio
