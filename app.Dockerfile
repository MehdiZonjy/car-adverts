FROM mehdizonjy/ivy2-cache

RUN mkdir /app
WORKDIR /app
COPY . .

RUN sbt dist
