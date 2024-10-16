# Api Rate Limiter

## Build the application

We use Jib to build the docker image. Run the following command to build a local image.

```
 ./gradlew jibDockerBuild
```

### Deployent

Once the image is ready, you can run a local deployment with docker-compose.
Please make sure that the ports 8080, 8081 and 6379 are free, as the deployment will bind services to them

```
docker-compose up
```
Three services are deployed:

* The Redis Data base
* The target web application against which we are running the rate limiter
* The Api Rate limiter itself

### Configuration

Find the container name running redis

```
docker ps -a | grep apiratelimiter_redis
```

Launch the redis cli by running the following command, replacing `apiratelimiter_redis_1` with the container name that
 you found.

```
docker exec -it apiratelimiter_redis_1 redis-cli
```
Enter the following redis commands to configure the limits:

```
set global_limit "{\"m\":{\"l\":1000,\"n\":3}}"
set user_limit "{\"m\":{\"l\":2,\"s\":1}}"
set user_limit:user1 "{\"m\":{\"l\":100,\"s\":75}}"
```

The following keys are used
* `global_limit` key is used to configure the global limit for the entire system
* `user_limit` key configures the default limit per user
* `user_limit:user1` key configures the specific limit for the user with id user1

The value of each configuration has the following structure:
`"{\"<time_frame>\":{\"l\":<limit_value>,\"s\":<soft_limit_value>,\"n\":<concurrent_counters_number>}}"`

For now, the soft limit (`s`) is supported only for unique users requests, and the concurrent counters (`n`) is
 supported only for the global system requests limit.
 
The <time_frame> could take the following values:
* `s` for second
* `m` for minute
* `h` for hour
* `d` for day
* `y` for year

For example, the above settings can be understood like this:
* The system will not accept more than 1000 total requests from all users in 1 minute. The usage is counted by 3
 concurrent counters
* The system will not accept more than 2 requests from any user (excluding user1) in 1 minute. The user will be
 notified after the first request
* The system will not accept more than 100 requests for user1 in 1 minute. The user will be notified after the 75th
  request
  
### Testing

Once the deployment and configuration are ready, you can send http commands to `http://localhost:8080/limited?user_id
=<userId>`
The identity the user by ready the `user_id` parameter.
The http status `200` should be returned for successful request and status `429` for rejected ones.

Following is an example curl command

```
curl -i http://localhost:8080/limited?user_id=user1
```

After issuing few commands, you can check the soft limit notifications that have been emitted in Redis:

```
XREAD COUNT 10 STREAMS soft_limit_topic 0
```
