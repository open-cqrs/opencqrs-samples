# Verwende das offizielle PostgreSQL Basis-Image
FROM postgres:17.1

# Optionale Umgebungsvariablen für Datenbankeinrichtung
ENV POSTGRES_DB=mydatabase
ENV POSTGRES_USER=myuser
ENV POSTGRES_PASSWORD=mypassword

# Optional: Kopieren von Initialisierungsskripten
# COPY init.sql /docker-entrypoint-initdb.d/

# Standardmäßig wird Port 5432 exponiert
EXPOSE 5432

# Das Entrypoint-Skript wird automatisch vom offiziellen Image bereitgestellt