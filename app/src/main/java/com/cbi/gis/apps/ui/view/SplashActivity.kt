package com.cbi.gis.apps.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.cbi.gis.apps.R
import com.cbi.gis.apps.utils.AppUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_splash.ivSplash
import kotlinx.android.synthetic.main.activity_splash.lottieSplash

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtils.transparentStatusNavBar(window)
        setContentView(R.layout.activity_splash)

        val fadeInAnimation = YoYo.with(Techniques.FadeIn).duration(750)
        fadeInAnimation.onEnd {
            val moveUpAnimation = ObjectAnimator.ofFloat(ivSplash, "translationY", -200f)
            moveUpAnimation.duration = 750
            moveUpAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    lottieSplash.visibility = View.VISIBLE
                    Handler().postDelayed({
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }, 2000)
                }
            })
            moveUpAnimation.start()
        }
        fadeInAnimation.playOn(ivSplash)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}