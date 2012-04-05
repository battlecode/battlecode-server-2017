package battlecode.analysis;

import battlecode.engine.signal.Signal;

public class AwesomenessSignal extends Signal {
    private static final long serialVersionUID = -5655877873179815593L;

    // Tweak to desired average awesomeness for relativeAwesomeness calculation
    private static final float AVE_AWESOMENESS = 60.0f;

    public float totalAwesomeness;
    // Number between [0,1] where .5 is average awesomeness
    public float relativeAwesomeness;
    public float centerX;
    public float centerY;
    public float radius;

    public AwesomenessSignal(float totalAwesomeness, float centerX, float centerY, float radius) {
        this.totalAwesomeness = totalAwesomeness;
        //relativeAwesomeness = relativeAwesomeness(totalAwesomeness);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public void updateAwesomeness(float totalAwesomeness) {
        this.totalAwesomeness = totalAwesomeness;
        //relativeAwesomeness = relativeAwesomeness(totalAwesomeness);
    }

    public void renormalize(float aveAwesomeness) {
        // Shift awesomeness to 0
        float x = 4 * (totalAwesomeness - aveAwesomeness) / aveAwesomeness;

        // Calculate sigmoid fuction to map value to [0, 1]
        // 1 / (1 + e^-x)
        relativeAwesomeness = (float) (1.0 / (1.0 + Math.exp(-x)));
    }
}
