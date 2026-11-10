package com.personaclick.demoshop

/**
 * Everything the app needs to know about the shop. Replace the values with your own: the shop id and
 * the block codes come from your PersonaClick dashboard.
 */
object ShopConfig {

    /**
     * Shop identifier from your PersonaClick dashboard.
     * This is a demo shop — replace it with your own.
     */
    const val SHOP_ID = "42a4cd11ebab3b0454778d18d4f3d5"

    /**
     * Analytics stream. Passing the platform is the common choice: it keeps iOS and Android
     * statistics apart.
     */
    const val STREAM = "android"

    /*
     * Recommender blocks found on the storefront of this shop, demo.personaclick.com. They are set up
     * in the dashboard; replace them with your own.
     */

    /**
     * Home: "Popular products". The shop runs an A/B test on it: segment B gets products, segment A
     * an empty block, which the kit's block hides.
     */
    const val HOME_POPULAR_CODE = "3ced4017b680fee0d8d153bad996ff55"

    /** Product page: "Similar products", driven by the product on screen. */
    const val PRODUCT_SIMILAR_CODE = "aabc899d3db3d71c4032832e4291c16a"

    /** Product page: "Recently viewed". */
    const val PRODUCT_RECENTLY_VIEWED_CODE = "09db715d69d1284832a0ddeb2cf62ceb"

    /**
     * Stories block of the shop's home. The storefront has none, so it is empty and home shows no
     * stories bar; put a block code here to get one.
     */
    const val STORIES_CODE = ""
}
