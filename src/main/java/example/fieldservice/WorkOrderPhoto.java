package example.fieldservice;

public record WorkOrderPhoto(String workOrderId, String imageId, String technicianNote, PhotoState state) {
    public WorkOrderPhoto queued() {
        return new WorkOrderPhoto(workOrderId, imageId, technicianNote, PhotoState.QUEUED);
    }

    public WorkOrderPhoto ready() {
        return new WorkOrderPhoto(workOrderId, imageId, technicianNote, PhotoState.READY);
    }
}
