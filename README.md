# Clean up stale torrents

Designed to clean up your torrents that are no longer used in any of your libraries. 

## support for cross-seeding

Nuclearr won't remove torrents that have cross-seeds that don't yet meet the requirements.

## support for hardlinks

Nuclearr will only remove torrents that have no files with existing hardlinks.

## Docker-compose

```
---
version: "3.8"
services:
  nuclearr:
    image: ghostwritertje/nuclearr:latest
    container_name: nuclearr
    user: PUID:PGID
    environment:
      JDK_JAVA_OPTIONS: >
        -XX:+UseSerialGC
        -Xms48m
        -Xmx256m
      TZ: Europe/Brussels
    ports:
      - "8080:8080"
    volumes:
      - /volume1/docker/nuclearr:/config
      - /volume1/Fast/Downloads:/downloads  #same mapping as your download client
   ```