package Visualization.Components;

/**
 * location data used to place buttons with both relative and absolute position
 *
 * @author Jelle Schukken
 */
public class LocationPacket {
    public float screenPosX, screenPosY;
    public int offsetX, offsetY;

    public LocationPacket(float screenPosX, float screenPosY, int offsetX, int offsetY){
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.screenPosX = screenPosX;
        this.screenPosY = screenPosY;
    }
}
