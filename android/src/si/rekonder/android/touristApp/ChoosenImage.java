package si.rekonder.android.touristApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ChoosenImage extends   Activity {
    /*
    * Show right image with right text. Right image decide value in our singelton.
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosen_image);

        ImageView img=(ImageView)findViewById(R.id.widget45);
        img.setBackgroundResource(R.drawable.nuk);
        Sigleton sin = (Sigleton) getApplicationContext();
        int detected = sin.getData();
        TextView tv=(TextView)findViewById(R.id.textView1);
        if(detected ==  1) {
            img.setBackgroundResource(R.drawable.univerza);
            tv.setText(getResources().getString(R.string.univerza_desc));
        } else if(detected == 2) {
            img.setBackgroundResource(R.drawable.zvezda);
            tv.setText(getResources().getString(R.string.zvezda_desc));
        } else if(detected == 3) {
            img.setBackgroundResource(R.drawable.vodnik);
            tv.setText(getResources().getString(R.string.vodnik_desc));
        } else if(detected == 4) {
            img.setBackgroundResource(R.drawable.grad);
            tv.setText(getResources().getString(R.string.grad_desc));
        } else if(detected == 5) {
            img.setBackgroundResource(R.drawable.zmajski);
            tv.setText(getResources().getString(R.string.zmajski_desc));
        } else if(detected == 6) {
            img.setBackgroundResource(R.drawable.nuk);
            tv.setText(getResources().getString(R.string.nuk_desc));
        } else if(detected == 7) {
            img.setBackgroundResource(R.drawable.presern);
            tv.setText(getResources().getString(R.string.presern_desc));
        } else if(detected == 8) {
            img.setBackgroundResource(R.drawable.mestna);
            tv.setText(getResources().getString(R.string.mestna_desc));
        } else if(detected == 9) {
            img.setBackgroundResource(R.drawable.franciskanska);
            tv.setText(getResources().getString(R.string.franciskanska_desc));
        } else if(detected == 10) {
            img.setBackgroundResource(R.drawable.robbov);
            tv.setText(getResources().getString(R.string.robbov_desc));
        }
    }

}
