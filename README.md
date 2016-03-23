# bootle

bottle links so you can read them later

## Build

    gradle build

## Run in docker

    # create docker image
    gradle bottleDocker

    # run database
    docker run --name bottle_postgres --detach postgres:9.3
    docker run -it --link bottle_postgres:postgres --rm postgres:9.3 \
        sh -c 'exec psql -h "$POSTGRES_PORT_5432_TCP_ADDR" \
        -p "$POSTGRES_PORT_5432_TCP_PORT" -U postgres -c "create database bottle"'

    # run bottle
    docker run --link bottle_postgres:psql \
        --publish=9000:9000 \
        --env DATABASE_URL=postgresql://postgres:@psql:5432/bottle --rm -it bottle:0.1.0
