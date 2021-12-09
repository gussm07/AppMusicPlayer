package edu.ieu.appmusicplayer;

import static android.graphics.Color.WHITE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class activity_player extends AppCompatActivity {
    Button btnplay, btnnext, btnprev, btnff, btnfr;
    TextView txtsname, txtstart, txtstop;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setTitle("NOW PLAYING");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff   = findViewById(R.id.btnff);
        btnfr   = findViewById(R.id.btnfr);
        txtsname =findViewById(R.id.txtsn);
        txtstart = findViewById(R.id.txtstart);
        txtstop = findViewById(R.id.txtstop);
        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageview);



        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();

        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = mediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        /* HILO PARA CORRER LA BARRA DE MUSICA CON LA DURACION */
        updateseekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;
                while(currentposition<totalDuration){
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenTime = createTime(mediaPlayer.getCurrentPosition());
                txtstart.setText(currenTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        btnplay.setOnClickListener(view -> {
            if(mediaPlayer.isPlaying())
            {
                btnplay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                mediaPlayer.pause();
            }
            else{
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                mediaPlayer.start();
            }
        });
        /* NEXT LISTENER*/
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnnext.performClick();

            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if(audiosessionId != -1)
        {
            visualizer.setAudioSessionId(audiosessionId);
        }


        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                AnimationForward(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId != -1)
                {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                AnimationBackward(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if(audiosessionId != -1)
                {
                    visualizer.setAudioSessionId(audiosessionId);
                }
            }
        });

        /*BOTON FAST FORWARD INCREMENTA 5 SEGUNDOS AL SER PRESIONADO*/
        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
                }
            }
        });

        /*BOTON FAST REWIND DECREMENTA 5 SEGUNDOS AL SER PRESIONADO*/
        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
                }
            }
        });
    }
    /* ANIMAR LA IMAGEN DE MUSICA CON GIRO DE 0 A 360GRADOS */
    public void AnimationForward(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    /* ANIMAR LA IMAGEN DE MUSICA CON GIRO DE 360 A 0 GRADOS */
    public void AnimationBackward(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation",360f,0f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";

        if(sec <10){
            time+="0";
        }
        time+=sec;

        return time;
    }
}