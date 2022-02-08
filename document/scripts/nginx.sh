docker rm -f nginx
docker run --name nginx -v /opt/data/nginx/html:/usr/share/nginx/html:ro -v /opt/app/back/upload:/usr/share/nginx/upload:ro -v /opt/data/nginx/nginx.conf:/etc/nginx/nginx.conf:ro -p 80:80 --privileged=true -d nginx
