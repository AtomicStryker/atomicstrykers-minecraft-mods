package atomicstryker.minions.client.render.region;

/**
 * Base region storage class. Provides
 * abstract methods for setting various
 * points in the region. 
 * 
 * @author yetanotherx
 * @author lahwran
 */
public abstract class BaseRegion {

    public BaseRegion() {
    }

    public void initialize() {
    }

    public abstract void render();

    public void setCuboidPoint(int id, int x, int y, int z) {
    }

    public void setPolygonPoint(int id, int x, int z) {
    }

    public void setEllipsoidCenter(int x, int y, int z) {
    }

    public void setEllipsoidRadii(double x, double y, double z) {
    }

    public void setMinMax(int min, int max) {
    }

    public void setCylinderCenter(int x, int y, int z) {
    }

    public void setCylinderRadius(double x, double z) {
    }

    public abstract RegionType getType();
}
