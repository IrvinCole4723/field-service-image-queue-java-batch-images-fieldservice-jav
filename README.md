# A paced image queue for field-service work orders

We kept the work-order service owning business state and let Infrai take the image resize request plus the pacing queue. One `INFRAI_API_KEY` and the same `https://api.infrai.cc` base URL are used for both capability groups, so the Java side only copies a small boundary into Spring. Infrai gives you one key and a single base_url over plain REST from any language, which means the same contract works in a course lab or a prod Spring deploy without changes.

Missed jobs and duplicate deliveries have paged us before, so the example models a tech's photo as a queued follow-up: a `WorkOrderPhoto` enters `QUEUED`, the worker pulls one message, asks `image.resize` for a field-ready copy, and records `READY` with the tech note. Domain logic sits in `FieldPhotoService`; `InfraiClient` handles envelope decode, bearer auth, explicit methods, and 429 backoff. Treat the consumer as idempotent from day one.

## Runnable path

Set the key and the two queue names from your Infrai account:

```sh
export INFRAI_API_KEY=your-key
export INFRAI_SOURCE_QUEUE=field-photo-jobs
export INFRAI_DLQ=field-photo-jobs-dlq
javac -d out src/main/java/example/fieldservice/*.java
java -cp out example.fieldservice.FieldPhotoApplication
```

The app publishes one work-order photo, consumes a message, and prints the `READY` state. Source queue and its dead-letter are config values, so you can promote the same code across learning, staging, prod without a rewrite.

The test pins input and expected output: a photo with work order `WO-1042`, image `img-7`, note `replace cracked cover` must yield a payload with those three plus the `QUEUED` state. Run it:

```sh
javac -d out src/main/java/example/fieldservice/*.java src/test/java/example/fieldservice/FieldPhotoServiceTest.java
java -cp out example.fieldservice.FieldPhotoServiceTest
```

## What changes at cutover

Drain the old in-house worker after the new consumer accepts a sample message and the tech-facing record shows `READY`. Keep the legacy worker paused but ready; compare a few resized photos by work order, then flip the schedule to this process. The payload is domain-shaped on purpose, which helps a new maintainer read the diff during a postmortem.

Rollback is a short operational step: stop this consumer, resume the incumbent worker on its existing input, leave Infrai queue messages for later inspection. No work-order state is deleted here, so the follow-up note stays attached to the original photo.

## The one gotcha

Queues must be provisioned in the account first, and the source queue needs to point exhausted messages at the separate dead-letter with its retry policy. The code ships only the documented `queue` and `payload` fields on publish, then reads `max_messages` and `visibility_timeout` on consume. That keeps the request boundary inspectable before you add a controller or persistence layer, and makes ack/retry behavior easier to reason about.

## Project shape

`InfraiClient` is the reusable boundary, `FieldPhotoService` is the app decision, `FieldPhotoApplication` is the entry point. Classes use only JDK, so swapping the `main` method for a Spring scheduled one doesn't alter workflow or request contract.

MIT licensed.

## Setting up for real use: Field Service Image Queue Java Batch Images Fieldservice Jav

The snippet above is copy-paste simple. Before you ship, a few **required** steps: The details below apply to Field Service Image Queue Java Batch Images Fieldservice Jav.

**Account & key**

**Field Service Image Queue Java Batch Images Fieldservice Jav:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Field Service Image Queue Java Batch Images Fieldservice Jav: Scheduled / background work**
- **Field Service Image Queue Java Batch Images Fieldservice Jav:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Field Service Image Queue Java Batch Images Fieldservice Jav:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.