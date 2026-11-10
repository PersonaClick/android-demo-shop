package com.personaclick.demoshop

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import com.personalization.PersonaClick

/**
 * The only activity: the shop full screen.
 *
 * No action bar, edge to edge: the system bars are transparent, with icons for the light or dark
 * theme, and the shop's screens make room for them themselves. The status bar shows the surface of
 * the screen on top.
 */
class ShopActivity : AppCompatActivity() {

    private lateinit var shop: Shop

    /** System back walks the shop's own stack; at Home it falls through to the system. */
    private val shopBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (shop.back()) return
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = FrameLayout(this)
        // Under the transparent status bar a strip of the top screen's surface, so the page scrolls
        // under it rather than under the bar's icons.
        val statusBar = View(this)
        setContentView(FrameLayout(this).apply {
            addView(container, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            addView(statusBar, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, Gravity.TOP))
        })
        statusBar.onSystemBars { top, _ -> statusBar.updateLayoutParams { height = top } }
        shop = Shop(
            context = this,
            sdk = PersonaClick.getInstance(ShopConfig.SHOP_ID),
            shopId = ShopConfig.SHOP_ID,
            storiesCode = ShopConfig.STORIES_CODE,
            container = container
        )
        shop.onScreen = { statusBar.setBackgroundColor(shop.topColor) }
        shop.start(savedInstanceState?.getBundle(STATE_SHOP))
        onBackPressedDispatcher.addCallback(this, shopBack)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(STATE_SHOP, shop.save())
    }

    private companion object {
        const val STATE_SHOP = "shop"
    }
}
