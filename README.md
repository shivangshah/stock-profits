# Stock Profit Calculator

This application provides a runtime API to lookup historic stock prices (DAILY) for a given stock ticker (eg: AAPL, GOOG etc.) 
and calculate the best sell and best buy days including the profit that could have been made within the client given time window.

The default time window is currently set to 180 days from the current date. Optionally, the clients can provide "from" and "to" dates
while making the API call. 

The application makes use of the `Alpha Vantage` Free API to load historic stock prices. The stock prices are then cached for the 
lifespan of the application to avoid making unnecessary API calls for static data.

The `apiKey` is already provided in `application.properties` and can be changed if anyone wants to use their own

The implementation itself is kept isolated through abstraction (interface) so if ever need be, the implementation can be swapped out with 
a different provider as long as the same interface can be implemented and autowired (or marked as primary bean)

# BONUS !! 

- Providing default 180 day historic lookup of best buy + sell dates.
- Also providing the ability to input time window (to/from dates) from the client instead of always using default. Provides more flexibility to the API
- Integrated with spring sleuth which might come handy later for distributed tracing (in microservices world)
- Unit & Spring Integration(EndToEnd) tests provided to mimic exactly all the possible usecases (and edgecases) through API calls
- Used Spock for BDD-style testing
- Swagger Support provided (more details below)
- 100% code coverage for branch, method, class & line - Coverage gates in place at maven build level

# Scope for improvements

- Can research more on public APIs that provide historic stock prices based on time window (didn't find any)
- Based on above, also spend more time looking into handling date/times better (the API response is somewhat messed up)
- Bare-bone exception handling provided with a global catch (controllerAdvice). Some more time can be spent on making that better
- Authentication/Authorization can be added in the future


# Assumptions

- Allowed to use any publicly available API for historic stocks
- Only taking into consideration the `high` prices for any given day
- Sell date will always be AFTER the buy date

# Building the application

Maven wrapper is already included so all that needs to be done is: 

`./mvnw clean install`

or if you want to build without tests

`./mvnw clean install -DskipTests`

# Test Reports

Spock test reports can be found here: `target/spock-reports/index.html`
Jacoco coverage report can be found here: `/target/test-results/coverage/jacoco/index.html`

# Running the application

Once built, being a spring boot application, you can easily run the application as: 

`java -jar target/stock-profit-calculator.jar`


# Swagger Support

I have also added support for `Swagger 2.0` using `springfox`. All you have to do once the service is up is:
- Navigate to `http://localhost:8080/swagger-ui.html`
- Expand the `Stock Profit Calculator Service`
- Single `GET` API will show up. Expand to get detailed documentation on the APIs and try it out from the UI itself !
- Additionally, if you are interested in pulling up API contracts (eventually can be used for `Contract Testing`), 
you can navigate here: `http://localhost:8080/api-docs`


# Libraries used

- Spring Actuator - Get built-in capabilities for debug endpoints (such as `/health`, `/info` etc)
- Spring Web - All MVC related
- Spring cloud Sleuth used to demonstrate distributed tracing in microservices architecture (pay attention to trace/span in logs)
- Maven - For building purposes
- Spock Test Framework - For BDD test approach
- Springfox-Swagger - Swagger 2.0 support

# List of TODOS

- [x] Basic Implementation and workflow working
- [x] Get caching working so as to not overwhelm the actual APIs
- [x] Add integration test cases (Spock)
- [x] Getting spring sleuth to work for distributed tracing
- [x] Update Logging
- [x] Exception Handling (handled by spring boot magic & Controller advice !)
- [x] Update Documentation
- [x] Swagger Support
