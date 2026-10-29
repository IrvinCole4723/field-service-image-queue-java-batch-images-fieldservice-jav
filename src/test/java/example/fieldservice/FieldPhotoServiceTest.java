package example.fieldservice;

public final class FieldPhotoServiceTest {
    public static void main(String[] args) {
        WorkOrderPhoto input = new WorkOrderPhoto("WO-1042", "img-7", "replace cracked cover", PhotoState.QUEUED);
        WorkOrderPhoto queued = input.queued();
        String payload = queued.workOrderId() + "|" + queued.imageId() + "|" + queued.technicianNote() + "|" + queued.state();
        if (!payload.equals("WO-1042|img-7|replace cracked cover|QUEUED")) throw new AssertionError(payload);
        if (!queued.state().equals(PhotoState.QUEUED)) throw new AssertionError("photo must enter the queue");
        WorkOrderPhoto ready = queued.ready();
        if (!ready.state().equals(PhotoState.READY)) throw new AssertionError("processed photo must be ready");
        System.out.println("FieldPhotoServiceTest passed");
    }
}
