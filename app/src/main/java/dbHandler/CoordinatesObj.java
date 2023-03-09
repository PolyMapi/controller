package dbHandler;

public class CoordinatesObj {
    public int captureId;
    public float latitude;
    public float longitude;
    public String timestamp;

    public CoordinatesObj(int captureId, float latitude, float longitude, String timestamp) {
        this.captureId = captureId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
