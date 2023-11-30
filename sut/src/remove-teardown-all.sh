docker stop $(docker ps -q --filter ancestor="*eshop*")
docker container prune -f
docker volume prune --all -f