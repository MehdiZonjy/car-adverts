# Car Adverts REST service
## Running the Demo
make sure you have docker and docker-compose installed on your system.

 - *Tests* `./run-tests.sh`
 - *App* `./run-app.sh` (the service should be accessible on `http://localhost:9000/hello`)
 
both scripts will attempt to build the corresponding `docker-compose` stacks.

In order to speedup the build process i've created a baseimage [`mehdizonjy/ivy2-cache`](https://cloud.docker.com/repository/docker/mehdizonjy/ivy2-cache) containing the `~/.ivy2` cache that sbt can reuse.


*If you want to run the service locally without Docker, make sure dynamodb local is running on port `8000` with `-sharedDb` flag. Also be sure to [create Dynamodb Table](https://github.com/MehdiZonjy/car-adverts/blob/3384bac4a8267ce90f11893a338a120d37a06781/bootstrap-app.sh#L17)*

## Design Decisions
### Domain
I've attempted to create a domain model using ADTs.
There are two distinct entities in the domain. `NewCarAdvert` and `UsedCarAdvert`. [CarAdvert](https://github.com/MehdiZonjy/car-adverts/blob/master/app/models/CarAdvert.scala#L35) is a sum of both entites `app/models`.

One could argue that both case classes could be merged into one. However by explicitly declaring types
for each type of `car advert` we can express bossiness rules using the type system such as `NewCarAdverts` don't have a mileage field while it's required for `UsedCarAdvert`

### Cats
This is my first time using `cats` and I need to spend more time to get the hang of it, but i love it so far.

[IO](https://typelevel.org/cats-effect/datatypes/io.html) simplified working with code that has side-effects (such as CarAdvertsRepository) which could fail unpredictably (dynamodb provisioned capacity excceded) and helped with handling sync/async code.

[OptionT Monad Transformer](https://typelevel.org/cats/datatypes/optiont.html) has helped me simplify working with nested monads as in [Here](https://github.com/MehdiZonjy/car-adverts/blob/f0769bd7549e9f289a2a9a6d2e5f08b2f6277bb1/app/services/CarAdvertsService.scala#L129)


I'm still unsure about the error handling in Scala. It's seems there are many ways to handling exception `Try`, `Either` and now `IO`. I need to do more digging and research on this topic


## Limitations
- The current design doesn't allow changing `NewCarAdvert` to a `UsedCarAdvert`
- `Scan` doesn't return all the items in DynamoDB and pagination needs to be implemented to handle it.
- Scan on a dyanmodb table is inefficient and expensive. If usecases require ordering, filtering and complex queries perhabs Dynamodb isn't the best tool to use.
- The current Sorting implementation happens on the App level which isn't ideal for production. 
- It might be worth using `Action.async` in the controller handlers.
## Endpoint
##### GET /v1/caradverts?orderBy=id
returns list of `CarAdverts`.
`orderBy` is optional and can be one of (`id`,`title`, `fuel`, `mileage`, `firstRegistration`)
##### GET /v1/caradverts/:id
return signle `CarAdvert` by id
##### POST /v1/caradverts/new
Creates a new `NewCarAdvert`. 

Payload
```
{
  "title": "STRING",
  "fuel": "gasoline|diesel",
  "price": NUMBER,
}
```
##### POST /v1/caradverts/used
Creates a  `UsedCarAdvert`. 

Payload
```
{
  "title": "STRING",
  "fuel": "gasoline|diesel",
  "price": NUMBER,
  "mileage": NUMBER,
  "firstRegistration": "yyyy-MM-dd"
}
```
##### PUT /v1/caradverts/:id
Updates an existing `CarAdvert`. 

Payload (all fields are optional):
```
  "title": "STRING",
  "fuel": "gasoline|diesel",
  "price": NUMBER,
  "mileage": NUMBER,
  "firstRegistration": "yyyy-MM-dd"
```

##### DELETE /v1/caradverts/:id
Delete a `CarAdvert` by Id

