# Etapa 1: Construcción (Build)
FROM node:20-alpine AS build

WORKDIR /app

# Copia las dependencias e instálalas
COPY package*.json ./
RUN npm install

# Copia todo el código fuente y genera el build de producción
COPY . .
RUN npm run build

# Etapa 2: Servidor de producción (Nginx)
FROM nginx:alpine

# Copia los archivos estáticos generados por Vite (usualmente en la carpeta 'dist')
COPY --from=build /app/dist /usr/share/nginx/html

# Expón el puerto estándar de Nginx
EXPOSE 80

# Inicia el servidor Nginx
CMD ["nginx", "-g", "daemon off;"]