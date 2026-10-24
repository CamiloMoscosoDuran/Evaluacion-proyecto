# Usa una imagen base oficial de Python
FROM python:3.10-slim

# Instalar dependencias del sistema (necesarias para ODBC/SQL Server)
RUN apt-get update && apt-get install -y \
    curl gnupg2 apt-transport-https unixodbc-dev \
    && rm -rf /var/lib/apt/lists/*

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo de dependencias e instálalas
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia el resto del código del proyecto
COPY . .

# ESTA ES LA LÍNEA MÁGICA: Le dice a Python dónde buscar el módulo 'app'
ENV PYTHONPATH=/app

# Expón el puerto
EXPOSE 5000

# Comando corregido
CMD ["python", "-m", "app.main"]