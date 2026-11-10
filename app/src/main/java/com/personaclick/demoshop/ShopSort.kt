package com.personaclick.demoshop

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.personalization.R as SdkR
import com.personalization.ui.PersonalizationTheme
import com.personalization.ui.widgets.PersonalizationListingScreen

/** A sort of the shop's picker: the label and what goes to the screen's `sortBy` / `sortDir`. */
private class ShopSort(val label: String, val by: String?, val dir: String?)

private val SHOP_SORTS = listOf(
    ShopSort("Relevance", null, null),
    ShopSort("Price: low to high", "price", "asc"),
    ShopSort("Price: high to low", "price", "desc"),
    ShopSort("Discount", "discount", "desc")
)

/**
 * The sort picker of Search and Category — the kit draws none, its sort button only asks the app. So a
 * bottom sheet of the platform on the kit's surface and type: "Sort by", then the sorts, the current one
 * checked. A tap picks and closes; the choice goes to the screen, which asks for the first page again.
 */
fun pickSort(context: Context, screen: PersonalizationListingScreen) {
    val current = SHOP_SORTS.indexOfFirst { it.by == screen.sortBy && it.dir == screen.sortDir }
    val dialog = BottomSheetDialog(context, R.style.ThemeOverlay_Shop_SortSheet)
    val sheetContext = dialog.context
    val edge = sheetContext.spacing(SdkR.dimen.personalization_spacing_padding_modal)
    val list = LinearLayout(sheetContext).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, edge, 0, sheetContext.spacing(SdkR.dimen.personalization_spacing_md))
    }
    list.addView(
        sheetContext.kitText(
            SdkR.dimen.personalization_font_size_lg, SdkR.dimen.personalization_line_height_lg,
            SdkR.color.personalization_text_primary, emphasized = true
        ).apply {
            text = SORT_TITLE_TEXT
            setPadding(edge, 0, edge, sheetContext.spacing(SdkR.dimen.personalization_spacing_md))
        },
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    )
    SHOP_SORTS.forEachIndexed { index, sort ->
        list.addView(
            sortRow(sheetContext, sort.label, checked = index == current) {
                screen.sortBy = sort.by
                screen.sortDir = sort.dir
                dialog.dismiss()
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }
    dialog.setContentView(list)
    // From API 27 the navigation bar takes dark icons: the sheet runs under a transparent bar.
    dialog.window?.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 }?.let { window ->
        @Suppress("DEPRECATION")
        window.navigationBarColor = ContextCompat.getColor(context, android.R.color.transparent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !context.isNight
    }
    dialog.behavior.skipCollapsed = true
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    dialog.show()
}

/** A row of the sort sheet: the label, and the brand check mark on the right when it is the current sort. */
private fun sortRow(context: Context, label: String, checked: Boolean, onClick: () -> Unit): View =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = context.dp(ROW_MIN_HEIGHT_DP)
        val edge = context.spacing(SdkR.dimen.personalization_spacing_padding_modal)
        val gap = context.spacing(SdkR.dimen.personalization_spacing_lg)
        setPadding(edge, gap, edge, gap)
        setSelectableBackground()
        addView(
            context.kitText(
                SdkR.dimen.personalization_font_size_base, SdkR.dimen.personalization_line_height_base,
                SdkR.color.personalization_text_primary
            ).apply { text = label },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        addView(
            AppCompatImageView(context).apply {
                setImageResource(R.drawable.ic_shop_check)
                ImageViewCompat.setImageTintList(
                    this, PersonalizationTheme.colorStateList(context, SdkR.color.personalization_brand_primary)
                )
                visibility = if (checked) View.VISIBLE else View.INVISIBLE
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginStart = gap
            }
        )
        isSelected = checked
        setOnClickListener { onClick() }
    }
