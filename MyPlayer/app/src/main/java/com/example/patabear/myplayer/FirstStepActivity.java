package com.example.patabear.myplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class FirstStepActivity extends AppCompatActivity {
    int scene = 1;
    ImageButton imageButton;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_step);

        imageButton = (ImageButton) findViewById(R.id.click);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NextScene();
            }
        });
    }

    private void NextScene() {
        if(scene == 1)
        {
            scene++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageButton.setBackground(this.getDrawable(R.drawable.folder_list));
            }
            else
                imageButton.setBackground(this.getResources().getDrawable(R.drawable.folder_list));
        }
        else if(scene == 2)
        {
            scene++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageButton.setBackground(this.getDrawable(R.drawable.music_play));
            }
            else
                imageButton.setBackground(this.getResources().getDrawable(R.drawable.music_play));
        }
        else if(scene == 3)
        {
            scene++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageButton.setBackground(this.getDrawable(R.drawable.music_add));
            }
            else
                imageButton.setBackground(this.getResources().getDrawable(R.drawable.music_add));
        }
        else if(scene == 4)
        {
            scene++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageButton.setBackground(this.getDrawable(R.drawable.konata_end));
            }
            else
                imageButton.setBackground(this.getResources().getDrawable(R.drawable.konata_end));
        }
        else if(scene >=5)
        {
            this.finish();
            //Intent intent = new Intent(this, MainActivity.class);
            //this.startActivity(intent);
        }
    }
}
