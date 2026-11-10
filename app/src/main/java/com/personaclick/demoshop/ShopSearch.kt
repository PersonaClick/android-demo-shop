package com.personaclick.demoshop

import android.view.View
import com.personalization.ui.widgets.PersonalizationSearchResultsScreen

/**
 * Search results for a submitted phrase: the kit's screen as it is. The screen runs the search, its
 * paging and filters itself and tracks `search`; a tap remembers the `full_search` source and opens the
 * product page. Sorting is the shop's sheet ([pickSort]).
 */
class ShopSearch(private val shop: Shop, route: ShopRoute.Search) : ShopScreen {

    private val screen = PersonalizationSearchResultsScreen(shop.context).apply {
        shopId = shop.shopId
        productActionText = ADD_TO_CART_TEXT
        onSortClick = { pickSort(context, this) }
        onBack = { shop.close() }
        onProductClick = { product -> shop.open(ShopRoute.Product(product.id)) }
        onProductAction = { product -> shop.addToCart(this, product.id, product.price) }
        query = route.query
    }

    override val view: View = listingFrame(screen)

    override fun onBack(): Boolean = screen.closeOpenFilters()
}
