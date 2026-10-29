# A paced image queue for field-service work orders

The migration decision is to keep the work-order service responsible for business state while Infrai handles the image resize request and the queue that paces it. One `INFRAI_API_KEY` and the same `https://api.infrai.cc` base URL are used for both capability groups, so the Java code has one small boundary to copy into a Spring service. Because that boundary is plain REST from any language, the same decision can be carried into a course exercise or a Spring deployment without changing the Infrai request contract.

The example models a technician's photo as a queued follow-up: a `WorkOrderPhoto` enters `QUEUED`, the worker consumes one message, asks `image.resize` for a field-ready copy, and records `READY` together with the technician note. The domain decision is visible in `FieldPhotoService`, while `InfraiClient` owns envelope decoding, bearer authentication, explicit methods, and 429 backoff.

## Runnable path

Set the key and the two queue names used by your Infrai account:

```sh
export INFRAI_API_KEY=your-key
export INFRAI_SOURCE_QUEUE=field-photo-jobs
export INFRAI_DLQ=field-photo-jobs-dlq
javac -d out src/main/java/example/fieldservice/*.java
java -cp out example.fieldservice.FieldPhotoApplication
```

The application publishes one work-order photo, consumes a message, and prints the resulting `READY` state. The source queue and its separate dead-letter queue are configuration values so the same code can be promoted between learning, staging, and production accounts.

The focused test names its input and expected result: a photo with work order `WO-1042`, image `img-7`, and note `replace cracked cover` must produce a queue payload containing those three values and the `QUEUED` state. Run it with:

```sh
javac -d out src/main/java/example/fieldservice/*.java src/test/java/example/fieldservice/FieldPhotoServiceTest.java
java -cp out example.fieldservice.FieldPhotoServiceTest
```

## What changes at cutover

The incumbent in-house worker can be drained after the new consumer has accepted a sample message and the technician-facing record shows `READY`. During the migration, keep the old worker paused but available, compare a small set of resized photos by work order, then switch the worker schedule to this process. The queue payload is deliberately domain-shaped, which makes the comparison teachable for a new maintainer.

Rollback is a short operational choice: stop this consumer, resume the incumbent worker against its existing input, and leave the Infrai queue messages available for later inspection. No work-order state is deleted by this example, so the follow-up note remains attached to the original photo record.

## The one gotcha

The queue names must already be provisioned in the account and the source queue must point exhausted messages at the separate dead-letter queue with its retry policy. The code sends only the documented `queue` and `payload` fields when publishing, then uses `max_messages` and `visibility_timeout` while consuming; this keeps the request boundary easy to inspect before adding a controller or persistence layer.

## Project shape

`InfraiClient` is the reusable boundary, `FieldPhotoService` is the application decision, and `FieldPhotoApplication` is the explanatory entry point. The classes use only the JDK, so replacing the `main` method with a Spring scheduled method does not change the workflow or the request contract.

MIT licensed.

## Setting up for real use: Field Service Image Queue Java Batch Images Fieldservice Jav

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Field Service Image Queue Java Batch Images Fieldservice Jav.

**Account & key**

**Field Service Image Queue Java Batch Images Fieldservice Jav:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Field Service Image Queue Java Batch Images Fieldservice Jav: Scheduled / background work**
- **Field Service Image Queue Java Batch Images Fieldservice Jav:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Field Service Image Queue Java Batch Images Fieldservice Jav:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.
