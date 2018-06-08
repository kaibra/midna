trap 'echo EXIT: killing background-jobs now && kill $(jobs -p)' EXIT
./gradlew bootRun &
cd src/client && npm run build-watch

