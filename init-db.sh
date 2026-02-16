#!/bin/bash
set -euo pipefail

until pg_isready -q -U "$POSTGRES_USER" -d postgres; do
  sleep 2
done

DB_EXISTS=$(psql -U "$POSTGRES_USER" -t -c "SELECT 1 FROM pg_database WHERE datname = 'hits_class_db'" | xargs)

if [[ "$DB_EXISTS" != "1" ]]; then
  psql -U "$POSTGRES_USER" -c "CREATE DATABASE hits_class_db;"
fi