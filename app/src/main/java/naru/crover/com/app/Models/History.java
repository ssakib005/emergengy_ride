package naru.crover.com.app.Models;

public class History {

    private String cost;
    private String destination;
    private String driverId;
    private String driverRating;
    private String riderId;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(String driverRating) {
        this.driverRating = driverRating;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }
}
