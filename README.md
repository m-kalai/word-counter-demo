# Sliding Window Word Counter Demo

Demo application used to demonstrate simple time-based sliding window counter implementation.

To be able to run this you need binary emitting events in this format
```json
{"event":{"event_type":"typeA","word":"some words"},"timestamp":1658904070963}
```

### HTTP Interface

http://localhost:9000

### How to run
App can be run from your favourite IDE or from SBT.
```shell
sbt "run /path/to/executable"
```
or
```shell
sbt "runMain wcd.Main /path/to/executable"
```
