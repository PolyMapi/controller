package dbHandler;

public class CoordinatesObj {
    public int captureId;
    public double latitude;
    public double longitude;
    public String timestamp;

    public CoordinatesObj(int captureId, double latitude, double longitude, String timestamp) {
        this.captureId = captureId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
