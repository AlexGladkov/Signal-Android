package solonsky.signal.twitter.activities

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_media.*
import com.arellomobile.mvp.MvpActivity
import com.arellomobile.mvp.presenter.InjectPresenter

import solonsky.signal.twitter.R
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Permission
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.presenters.MediaPresenter
import solonsky.signal.twitter.views.MediaView

/**
 * Created by neura on 31.08.17.
 */

class MediaActivity : MvpActivity(), MediaView {
    private val TAG = MediaActivity::class.java.simpleName

    @InjectPresenter
    lateinit var mPresenter: MediaPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        vv_gif.setVideoPath(AppData.MEDIA_URL)
        vv_gif.requestFocus()
        vv_gif.keepScreenOn = true
        vv_gif.setOnPreparedListener {
            mPresenter.prepareVideo(vv_gif.duration)
            sb_media.max = 100
        }
        vv_gif.setOnCompletionListener {
            mPresenter.stop()
        }

        sb_media.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mPresenter.onProgressChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mPresenter.onStartTrackingTouch()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mPresenter.onStopTrackingTouch()
            }

        })

        rl_media_main.setOnClickListener {
            mPresenter.contentClick()
        }

        btn_media_save.setOnClickListener {
            if ((Permission.checkSelfPermission(applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    Permission.checkSelfPermission(applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE))) {
                val downloadFiles = DownloadFiles(this@MediaActivity)
                downloadFiles.saveFile(AppData.MEDIA_URL, getString(R.string.download_url))
            } else {
                ActivityCompat.requestPermissions(this@MediaActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        Permission.WRITE_EXTERNAL_REQUEST)
            }
        }

        btn_media_back.setOnClickListener {
            onBackPressed()
        }

        btn_media_chrome.setOnClickListener {
            mPresenter.pause()
            Utilities.openLink(AppData.MEDIA_URL, this@MediaActivity)
        }

        btn_media_share.setOnClickListener {
        }

        btn_media_play.setOnClickListener {
            mPresenter.play()
            vv_gif.start()
        }

        btn_media_pause.setOnClickListener {
            mPresenter.pause()
            vv_gif.pause()
        }

        btn_media_rewind.setOnClickListener {
            mPresenter.rewind()
        }

        btn_media_forward.setOnClickListener {
            mPresenter.forward()
        }

        Handler().postDelayed({
            vv_gif.visibility = View.VISIBLE
            vv_gif.start()
            mPresenter.play()
        }, 300)

        Handler().postDelayed({
            mPresenter.contentClick()
        }, 2300)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onUpdateProgress(start: String) {
        txt_media_start.text = start
    }

    override fun onUpdateBar(value: Int) {
        sb_media.progress = value
    }

    override fun onUpdateEnd(end: String) {
        txt_media_end.text = end
    }

    override fun onSeekTo(mSec: Int) {
        vv_gif.seekTo(mSec)
    }

    override fun onSuspend() {
        btn_media_pause.visibility = View.GONE
        btn_media_play.visibility = View.VISIBLE
    }

    override fun onPlay() {
        btn_media_pause.visibility = View.VISIBLE
        btn_media_play.visibility = View.GONE
    }

    override fun hideOverlay() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        tb_media.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                tb_media.visibility = View.GONE
            }
        }).start()

        ll_media_container.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                ll_media_container.visibility = View.GONE
            }
        })
    }

    override fun showOverlay() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        tb_media.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                tb_media.visibility = View.VISIBLE
            }
        })

        ll_media_container.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                ll_media_container.visibility = View.VISIBLE
            }
        })
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)

//        viewModel = MediaViewModel("00:00", "00:00", true, true)
//        binding = DataBindingUtil.setContentView<ActivityGifBinding>(this, R.layout.activity_gif)
//        binding!!.setModel(viewModel)
//
//        mActivity = this
//
//
//
//        binding!!.sbMedia.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            internal var isStartTracking = false
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                touched = true
//                isStartTracking = true
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                isStartTracking = false
//            }
//
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                if (isStartTracking) {
//                    val currentPosition = ((progress.toFloat() / 100f) * binding!!.vvGif.getDuration().toFloat()).toInt()
//                    binding!!.vvGif.seekTo(currentPosition)
//                }
//            }
//        })
//
//        binding!!.vvGif.setVideoPath(AppData.MEDIA_URL)
//        binding!!.vvGif.setKeepScreenOn(true)
//        binding!!.vvGif.requestFocus()
//        binding!!.vvGif.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
//            override fun onPrepared(mp: MediaPlayer) {
//                val duration = binding!!.vvGif.getDuration()
//                val millis = duration / 1000
//                val seconds = millis % 60
//                val minutes = millis / 60
//                viewModel!!.setTimeEnd((if (minutes < 10) "0" + minutes else minutes) + ":" + (if (seconds < 10) "0" + seconds else seconds))
//                binding!!.sbMedia.setMax(100)
//            }
//        })
//
//        binding!!.vvGif.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
//            public override fun onCompletion(mp: MediaPlayer) {
//                viewModel!!.setPlay(true)
//                binding!!.sbMedia.setProgress(100)
//
//                if (AppData.MEDIA_TYPE == Flags.MEDIA_TYPE.GIF) {
//                    binding!!.getClick().onPlayClick(binding!!.vvGif)
//                }
//            }
//        })
//
//        binding!!.vvGif.postDelayed(object : Runnable {
//            public override fun run() {
//                binding!!.vvGif.start()
//                binding!!.vvGif.setVisibility(View.VISIBLE)
//                viewModel!!.setPlay(false)
//                startCounting()
//            }
//        }, 300)
//
//        Handler().postDelayed(object : Runnable {
//            public override fun run() {
//                if (!touched) {
//                    viewModel!!.setOverlay(false)
//                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                    binding!!.tbMedia.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
//                        public override fun onAnimationEnd(animation: Animator) {
//                            super.onAnimationEnd(animation)
//                            binding!!.tbMedia.setVisibility(View.GONE)
//                        }
//                    }).start()
//                    binding!!.llMediaContainer.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
//                        public override fun onAnimationEnd(animation: Animator) {
//                            super.onAnimationEnd(animation)
//                            binding!!.llMediaContainer.setVisibility(View.GONE)
//                        }
//                    })
//                }
//            }
//        }, 2300)
//
//        binding!!.setClick(object : MediaViewModel.MediaClickHandler {
//            public override fun onPlayClick(v: View) {
//                touched = true
//                binding!!.vvGif.start()
//                viewModel!!.setPlay(false)
//                startCounting()
//            }
//
//            public override fun onRewindClick(v: View) {
//                touched = true
//                val position = binding!!.vvGif.getCurrentPosition()
//                var diff = position - (1000 * 5)
//
//                diff = if (diff < 0) 0 else diff
//                val progress = ((diff.toFloat() / binding!!.vvGif.getDuration().toFloat()) * 100f).toInt()
//                val millis = diff / 1000
//                val seconds = millis % 60
//                val minutes = millis / 60
//
//                viewModel!!.setTimeStart((if (minutes < 10) "0" + minutes else minutes) + ":" + (if (seconds < 10) "0" + seconds else seconds))
//                binding!!.vvGif.seekTo(diff)
//                binding!!.sbMedia.setProgress(progress)
//            }
//
//            public override fun onForwardClick(v: View) {
//                touched = true
//                val position = binding!!.vvGif.getCurrentPosition()
//                var diff = position + (1000 * 5)
//
//                diff = if (diff > binding!!.vvGif.getDuration()) binding!!.vvGif.getDuration() else diff
//                val progress = ((diff.toFloat() / binding!!.vvGif.getDuration().toFloat()) * 100f).toInt()
//                val millis = diff / 1000
//                val seconds = millis % 60
//                val minutes = millis / 60
//
//                viewModel!!.setTimeStart((if (minutes < 10) "0" + minutes else minutes) + ":" + (if (seconds < 10) "0" + seconds else seconds))
//                binding!!.vvGif.seekTo(diff)
//                binding!!.sbMedia.setProgress(progress)
//            }
//
//            public override fun onPauseClick(v: View) {
//                touched = true
//                binding!!.vvGif.pause()
//                viewModel!!.setPlay(true)
//            }
//
//            public override fun onBackClick(v: View) {
//                touched = true
//                onBackPressed()
//            }
//
//            public override fun onSaveClick(v: View) {
//                touched = true
//                if ((Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) && Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE))) {
//                    val downloadFiles = DownloadFiles(mActivity)
//                    downloadFiles.saveFile(AppData.MEDIA_URL, getString(R.string.download_url))
//                } else {
//                    ActivityCompat.requestPermissions(this@MediaActivity, arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Permission.WRITE_EXTERNAL_REQUEST)
//                }
//            }
//
//            public override fun onShareClick(v: View) {
//                touched = true
//            }
//
//            public override fun onChromeClick(v: View) {
//                touched = true
//                Utilities.openLink(AppData.MEDIA_URL, mActivity)
//            }
//
//            public override fun onContentClick(v: View) {
//                touched = true
//                viewModel!!.setOverlay(!viewModel!!.isOverlay())
//                if (viewModel!!.isOverlay()) {

//                } else {

//                }
//            }
//        })
//    }
}
