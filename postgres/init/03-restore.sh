#!/bin/bash
set -e
echo "Restoring COMPLETE database from SQL backup..."
psql -U postgres -d music_db -f /docker-entrypoint-initdb.d/02-restore-data.sql
