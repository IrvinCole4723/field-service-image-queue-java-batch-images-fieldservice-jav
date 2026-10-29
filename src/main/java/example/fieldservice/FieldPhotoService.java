package example.fieldservice;

import java.io.IOException;

public final class FieldPhotoService {
    private final InfraiClient infrai;
    private final String sourceQueue;

    public FieldPhotoService(InfraiClient infrai, String sourceQueue) {
        this.infrai = infrai;
        this.sourceQueue = sourceQueue;
    }

    public WorkOrderPhoto enqueue(WorkOrderPhoto photo) throws IOException, InterruptedException {
        WorkOrderPhoto queued = photo.queued();
        String payload = queued.workOrderId() + "|" + queued.imageId() + "|" + queued.technicianNote() + "|" + queued.state();
        infrai.publish(sourceQueue, payload);
        return queued;
    }

    public WorkOrderPhoto process(WorkOrderPhoto queued) throws IOException, InterruptedException {
        infrai.consume(sourceQueue);
        infrai.resize(queued.imageId());
        return queued.ready();
    }
}
