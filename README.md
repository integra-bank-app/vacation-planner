# Vacation Planner

## Code style

The project uses Checkstyle for code style validation.

To run the code style check locally:

cd vacation-planner-backend
./gradlew check

Checkstyle also runs automatically with:

./gradlew build

## OpenAPI code generation

To generate OpenAPI classes for frontend run the "Generate api client" configuration while the backend is running.

If ng-openapi-gen is not found install it using `npm -i ng-openapi-gen`
