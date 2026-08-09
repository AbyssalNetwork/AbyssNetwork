FROM golang:1.26-alpine AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /abyss-api .

FROM alpine:3.21
RUN apk add --no-cache ca-certificates tzdata
COPY --from=build /abyss-api /usr/local/bin/abyss-api
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/abyss-api"]
