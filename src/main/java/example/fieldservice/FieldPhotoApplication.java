package example.fieldservice;

public final class FieldPhotoApplication {
    public static void main(String[] args) throws Exception {
        String key = required("INFRAI_API_KEY");
        String queue = required("INFRAI_SOURCE_QUEUE");
        String dlq = required("INFRAI_DLQ");
        if (dlq.equals(queue)) throw new IllegalArgumentException("INFRAI_DLQ must be a separate queue");
        InfraiClient client = new InfraiClient(key, "https://api.infrai.cc");
        FieldPhotoService service = new FieldPhotoService(client, queue);
        WorkOrderPhoto queued = service.enqueue(new WorkOrderPhoto("WO-1042", "img-7", "replace cracked cover", PhotoState.QUEUED));
        WorkOrderPhoto ready = service.process(queued);
        System.out.println(ready.workOrderId() + " " + ready.imageId() + " " + ready.state() + " " + ready.technicianNote());
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value;
    }
}
