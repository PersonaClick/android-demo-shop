package com.personaclick.demoshop

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.personalization.OnClickListener
import com.personalization.Product
import com.personalization.R as SdkR
import com.personalization.SDK
import com.personalization.api.OnApiCallbackListener
import com.personalization.api.models.tracking.TrackingItem
import com.personalization.ui.PersonalizationTheme
import com.personalization.ui.components.PersonalizationToast
import org.json.JSONObject

/**
 * The shop — put together from the UI kit and the public SDK methods, the way a host app would
 * build one:
 * - Home ([ShopHome]) — search behind a button, the stories bar (when a stories block is set), a
 *   recommender block and the shop's top-level categories;
 * - Category ([ShopCategory]) — the kit's category screen: `getCategory` with filters and sorting;
 * - Product ([ShopProduct]) — `getProductInfo`, the view tracked, similar and recently viewed
 *   products under it;
 * - Search ([ShopSearch]) — the kit's search results screen.
 *
 * Home has the instant search field behind its search button ([ShopSearchBar]); Search and Category
 * sort with the shop's sheet ([pickSort]). There is no app bar over the shop: every screen starts
 * under the status bar.
 *
 * There is no cart screen: "Add to cart" anywhere tracks the event and shows the kit's toast.
 * Attribution of a recommender block is set by the block itself before it hands over a product.
 */
class Shop(
    val context: Context,
    val sdk: SDK,
    val shopId: String,
    val storiesCode: String,
    container: FrameLayout
) {
    private val navigator = ShopNavigator(container, ::screen) { onScreen?.invoke() }

    /** The screen on top changed. */
    var onScreen: (() -> Unit)? = null

    /** Shows the saved stack, or Home. */
    fun start(state: Bundle?) = navigator.restore(state)

    fun save(): Bundle = navigator.save()

    fun open(route: ShopRoute) = navigator.push(route)

    /** A screen's own back button. */
    fun close() {
        navigator.pop()
    }

    /** System back; `false` — the shop is at Home with nothing open. */
    fun back(): Boolean = navigator.back()

    /** Colour of the top edge of the screen on top: the status bar over the shop takes it. */
    val topColor: Int
        get() = PersonalizationTheme.color(
            context, navigator.topScreen?.topSurface ?: SdkR.color.personalization_background_generic
        )

    /**
     * Clicks of the shop's stories: links stay with the SDK (it opens them itself); a product opens
     * its page here — `false`, the app handled it.
     */
    val storiesClicks: OnClickListener = object : OnClickListener {
        override fun onClick(url: String): Boolean = true

        override fun onClick(product: Product): Boolean {
            open(ShopRoute.Product(product.id))
            return false
        }
    }

    /** The cart of the demo: the `cart` event and a toast, no cart screen. */
    fun addToCart(anchor: View, itemId: String, price: Double? = null) {
        sdk.tracking.addToCart(
            item = TrackingItem(id = itemId, price = price),
            listener = object : OnApiCallbackListener() {
                override fun onSuccess(response: JSONObject?) {
                    Log.d(TAG, "addToCart($itemId) sent")
                }

                override fun onError(code: Int, msg: String?) {
                    Log.w(TAG, "addToCart($itemId) failed: $code $msg")
                }
            }
        )
        val toast = PersonalizationToast.show(anchor, ADDED_TO_CART_TEXT)
        // The kit keeps the toast clear of the system bars; a bar pinned to the bottom of the screen
        // (the product page's price and "Add to cart") is the app's, so the app takes the toast above it.
        navigator.topScreen?.bottomBar?.takeIf { it.isShown }?.let { bar ->
            toast?.updateLayoutParams<FrameLayout.LayoutParams> {
                bottomMargin = context.spacing(SdkR.dimen.personalization_spacing_xl) + bar.height
            }
        }
    }

    private fun screen(route: ShopRoute, restored: Boolean): ShopScreen = when (route) {
        ShopRoute.Home -> ShopHome(this)
        is ShopRoute.Category -> ShopCategory(this, route)
        is ShopRoute.Product -> ShopProduct(this, route, trackView = !restored)
        is ShopRoute.Search -> ShopSearch(this, route)
    }

    private companion object {
        const val TAG = "Shop"
    }
}
