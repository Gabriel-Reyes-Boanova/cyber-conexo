# Nginx sem privilégios de root (escuta na 8080 dentro do container)
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY nginx/proxy_params.conf /etc/nginx/proxy_params.conf
COPY public/ /usr/share/nginx/html/
EXPOSE 8080
