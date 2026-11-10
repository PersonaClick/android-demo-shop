package com.personaclick.demoshop

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.personalization.R as SdkR
import com.personalization.api.responses.product.ImageUrlResized
import com.personalization.api.responses.product.Product
import com.personalization.ui.PersonalizationTheme
import com.personalization.ui.components.PersonalizationButton
import com.personalization.ui.components.PersonalizationEmptyState
import com.personalization.ui.components.PersonalizationLoader
import com.personalization.ui.components.PersonalizationProduct
import java.math.BigDecimal

const val SHOP_TITLE_TEXT = "Shop"
const val SORT_TITLE_TEXT = "Sort by"
const val ADD_TO_CART_TEXT = "Add to cart"
const val ADDED_TO_CART_TEXT = "Added to cart"
const val ERROR_TEXT = "Something went wrong. Check your connection and try again."
const val RETRY_TEXT = "Retry"

/**
 * API product → kit card. The kit's own mapper is internal to the SDK, so the app keeps a small one
 * by the same rules:
 * - price — `price_formatted`, otherwise the number with the currency;
 * - old price — `oldprice_formatted`, only when `oldprice` is greater than the price;
 * - discount badge — only next to an old price and only for a positive number (`discount`, otherwise
 *   `discount_percent`): `discount_formatted` for `discount`, otherwise "N%", with exactly one "-";
 * - image — `picture` (resized copy), otherwise `image_url`; links — `deeplink_android` and `url`.
 */
fun Product.toCard(actionText: CharSequence? = ADD_TO_CART_TEXT): PersonalizationProduct {
    // Gson leaves null in fields the answer lacks, the non-null types notwithstanding.
    val productId: String? = id
    val title: String? = name
    val priceText: String? = priceFormatted
    val currencyText: String? = currency
    val brandName: String? = brand
    val pictureUrl: String? = picture
    val originalUrl: String? = imageUrl
    val pageUrl: String? = url
    val hasOldPrice = (oldPrice ?: 0.0) > price && !oldPriceFormatted.isNullOrEmpty()
    val ownDiscount = discount?.takeIf { it > 0 }
    val discountNumber = (ownDiscount ?: discountPercent?.takeIf { it > 0 })?.takeIf { hasOldPrice }
    val discountText = discountNumber?.let { number ->
        val formatted = discountFormatted?.takeIf { ownDiscount != null }
            ?.trimStart { it == '-' || it.isWhitespace() }
            ?.takeIf { it.isNotEmpty() }
        "-" + (formatted ?: "$number%")
    }
    return PersonalizationProduct(
        id = productId.orEmpty(),
        name = title.orEmpty(),
        price = priceText?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(plainNumber(price), currencyText?.takeIf { it.isNotEmpty() }).joinToString(" "),
        imageUrl = pictureUrl?.takeIf { it.isNotEmpty() } ?: originalUrl?.takeIf { it.isNotEmpty() },
        brand = brandName?.takeIf { it.isNotEmpty() },
        oldPrice = if (hasOldPrice) oldPriceFormatted else null,
        discount = discountText,
        actionText = actionText,
        url = pageUrl?.takeIf { it.isNotEmpty() },
        deeplink = deeplinkAndroid?.takeIf { it.isNotEmpty() }
    )
}

/**
 * Picture of the product page: the original, big enough for the hero, otherwise the largest resized
 * copy, then `picture`. An https address first: a catalog may serve its originals over plain http and
 * its resized copies over https.
 */
fun Product.productPicture(): String? {
    // Gson leaves null in fields the answer lacks, the non-null types notwithstanding.
    val original: String? = imageUrl
    val resized: ImageUrlResized? = imageUrlResized
    val largest: String? = resized?.x520
    val medium: String? = resized?.x310
    val resizedPicture: String? = picture
    val candidates = listOfNotNull(original, largest, medium, resizedPicture)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return candidates.firstOrNull { it.startsWith("https:") } ?: candidates.firstOrNull()
}

/** `1.6499E7` → `16499000`, `90.0` → `90`. */
private fun plainNumber(value: Double): String =
    if (value.isFinite()) BigDecimal.valueOf(value).stripTrailingZeros().toPlainString() else value.toString()

private val mainHandler = Handler(Looper.getMainLooper())

/** SDK callbacks arrive on a background thread; views are touched on the main one. */
fun onMain(block: () -> Unit) {
    mainHandler.post(block)
}

fun Context.spacing(res: Int): Int = resources.getDimensionPixelSize(res)

/** Sizes the kit has no token for, taken from the shop's layout: rows of lists, the round buttons. */
const val ROW_MIN_HEIGHT_DP = 52
private const val CIRCLE_BUTTON_DP = 44
private const val ICON_DP = 24

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

val Context.isNight: Boolean
    get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/** Page background of the kit, so its components sit on the surface they were drawn for, light or dark. */
fun Context.pageBackground(): Int =
    PersonalizationTheme.color(this, SdkR.color.personalization_background_generic)

/**
 * The system bars the view sits under — [apply] gets the top (status bar, cutout) and the bottom
 * (navigation bar, or the keyboard when it is taller), now and on every change. The activity draws edge
 * to edge and its screens make room for the bars themselves. The insets go on to the siblings: every
 * screen of the stack gets them.
 */
fun View.onSystemBars(apply: (top: Int, bottom: Int) -> Unit) {
    apply(0, 0)
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        apply(bars.top, maxOf(bars.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom))
        insets
    }
    // A screen pushed onto a shown stack is not handed the insets by itself.
    doOnAttach { ViewCompat.requestApplyInsets(it) }
}

/** Ripple of the platform on a tap, over whatever the view sits on. */
fun View.setSelectableBackground() {
    val value = TypedValue()
    if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)) {
        setBackgroundResource(value.resourceId)
    }
}

/**
 * A rounded rectangle of a kit colour, the corners top-left, top-right, bottom-right, bottom-left. Equal
 * corners go as one radius: only such an outline can clip the view's content.
 */
fun Context.surface(colorRes: Int, vararg corners: Float): GradientDrawable = GradientDrawable().apply {
    setColor(PersonalizationTheme.color(this@surface, colorRes))
    if (corners.distinct().size == 1) {
        cornerRadius = corners[0]
    } else {
        cornerRadii = floatArrayOf(
            corners[0], corners[0], corners[1], corners[1], corners[2], corners[2], corners[3], corners[3]
        )
    }
}

/** Text in the kit's type scale and colours — the product page has no ready kit component for these. */
fun Context.kitText(
    fontSize: Int,
    lineHeight: Int,
    color: Int,
    emphasized: Boolean = false,
    strikeThrough: Boolean = false
): AppCompatTextView = AppCompatTextView(this).apply {
    includeFontPadding = false
    typeface = PersonalizationTheme.typeface(context, emphasized)
    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(fontSize))
    TextViewCompat.setLineHeight(this, resources.getDimensionPixelSize(lineHeight))
    setTextColor(PersonalizationTheme.color(context, color))
    if (strikeThrough) paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

/**
 * The round back button over the product photo: the chevron on the kit's page background. It comes with
 * its size in [FrameLayout.LayoutParams].
 */
fun Context.circleBackButton(onBack: () -> Unit): View = circleButton(
    // The kit has the chevron to the right only; turned round, it points back.
    SdkR.drawable.personalization_ic_angle_large_right, SdkR.color.personalization_background_generic,
    "Back", mirrored = true, onClick = onBack
)

/**
 * A round icon button of the shop's chrome: a 44 circle of a kit surface with the icon in the primary
 * text colour, [label] for accessibility. It comes with its size in [FrameLayout.LayoutParams].
 */
fun Context.circleButton(
    icon: Int,
    surfaceRes: Int,
    label: String,
    mirrored: Boolean = false,
    onClick: () -> Unit
): View = FrameLayout(this).apply {
    val size = dp(CIRCLE_BUTTON_DP)
    layoutParams = FrameLayout.LayoutParams(size, size)
    background = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(PersonalizationTheme.color(context, surfaceRes))
    }
    // The ripple of the icon's view stays inside the circle.
    outlineProvider = ViewOutlineProvider.BACKGROUND
    clipToOutline = true
    addView(AppCompatImageView(context).apply {
        setImageResource(icon)
        scaleType = ImageView.ScaleType.FIT_CENTER
        val inset = (size - dp(ICON_DP)) / 2
        setPadding(inset, inset, inset, inset)
        if (mirrored && resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR) scaleX = -1f
        ImageViewCompat.setImageTintList(
            this, PersonalizationTheme.colorStateList(context, SdkR.color.personalization_text_primary)
        )
        setSelectableBackground()
        isDuplicateParentStateEnabled = true
    }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
    contentDescription = label
    isClickable = true
    isFocusable = true
    setOnClickListener { onClick() }
}

/**
 * Loading and failure of a screen or a section, from kit parts: the loader, or the empty-state text
 * with a Retry button under it (the same pair the catalog shows for a failed first page), centred in
 * the area it is given.
 */
class ShopStatus(context: Context) : FrameLayout(context) {

    private val loader = PersonalizationLoader(context)
    private val message = PersonalizationEmptyState(context)
    private val retry = PersonalizationButton(context)
    private val failure = LinearLayout(context)

    init {
        val gap = context.spacing(SdkR.dimen.personalization_spacing_xl3)
        setPadding(0, gap, 0, gap)
        addView(loader, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        retry.size = PersonalizationButton.Size.MD
        retry.buttonView = PersonalizationButton.ButtonView.SECONDARY
        retry.iconStart = SdkR.drawable.personalization_ic_arrow_rotate_cw
        retry.text = RETRY_TEXT
        failure.orientation = LinearLayout.VERTICAL
        failure.gravity = Gravity.CENTER_HORIZONTAL
        failure.addView(message, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        failure.addView(retry, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(failure, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        isVisible = false
    }

    fun loading() {
        isVisible = true
        loader.isVisible = true
        failure.isVisible = false
    }

    fun failed(onRetry: () -> Unit) {
        isVisible = true
        loader.isVisible = false
        message.text = ERROR_TEXT
        retry.setOnClickListener { onRetry() }
        failure.isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
