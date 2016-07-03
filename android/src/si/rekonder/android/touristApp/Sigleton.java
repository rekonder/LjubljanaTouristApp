package si.rekonder.android.touristApp;

import android.app.Application;

public class Sigleton extends Application {
    private int data;
    public int getData() {
        return data;
    }
    public void setData(int data) {
        this.data = data;
    }
}
