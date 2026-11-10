# PersonaClick Android SDK — demo shop

A small shop app built from the PersonaClick Android SDK's UI kit and its public methods: home with
instant search, categories and a recommender block, category pages with filters and sorting, product
pages with recommendations, and search results. "Add to cart" tracks the `cart` event and shows the
kit's toast; there is no cart screen.

The SDK is published on JitPack:

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// app/build.gradle
implementation 'com.github.personaclick:android-sdk:v2.47.0'
```

Everything the app does with the SDK lives in
[`app/src/main/java/com/personaclick/demoshop`](app/src/main/java/com/personaclick/demoshop) and
reads in a few minutes.

## What it demonstrates

| Scenario | Where to look |
| --- | --- |
| SDK initialization, once per process | [`ShopApplication.kt`](app/src/main/java/com/personaclick/demoshop/ShopApplication.kt) |
| Instant search: suggestions, categories, products | [`ShopSearchBar.kt`](app/src/main/java/com/personaclick/demoshop/ShopSearchBar.kt) |
| Recommender block on home, top-level categories | [`ShopHome.kt`](app/src/main/java/com/personaclick/demoshop/ShopHome.kt) |
| Category page: filters, sorting, grid / list | [`ShopCategory.kt`](app/src/main/java/com/personaclick/demoshop/ShopCategory.kt), [`ShopSort.kt`](app/src/main/java/com/personaclick/demoshop/ShopSort.kt) |
| Search results | [`ShopSearch.kt`](app/src/main/java/com/personaclick/demoshop/ShopSearch.kt) |
| Product page, product view tracking, similar and recently viewed blocks | [`ShopProduct.kt`](app/src/main/java/com/personaclick/demoshop/ShopProduct.kt) |
| "Add to cart": the `cart` event and the kit's toast | [`Shop.kt`](app/src/main/java/com/personaclick/demoshop/Shop.kt) |
| Dashboard popups | nothing to write: the SDK shows them over the screen by default |

## Running the demo

Requires JDK 17 and the Android SDK (Android Studio, or `ANDROID_HOME` pointing at it).

```sh
./gradlew :app:installDebug
```

Debug builds print every SDK request to logcat under the `ShopHttp` tag.

## Configuration

The shop id and the recommender block codes are in
[`ShopConfig.kt`](app/src/main/java/com/personaclick/demoshop/ShopConfig.kt). They point at the
PersonaClick demo shop — replace them with your own from the dashboard:

| Constant | What it is |
| --- | --- |
| `SHOP_ID` | your shop identifier |
| `STREAM` | analytics stream, `android` |
| `HOME_POPULAR_CODE` | "Popular products" on home |
| `PRODUCT_SIMILAR_CODE` | "Similar products" on the product page |
| `PRODUCT_RECENTLY_VIEWED_CODE` | "Recently viewed" on the product page |
| `STORIES_CODE` | stories block on home; empty — no stories bar |

The demo shop runs an A/B test on "Popular products": segment B gets products, segment A an empty
block, which the kit hides. The segment is picked once per install, so clear the app's data to get
the other one.

The app has no push notifications, so it ships no Firebase configuration.

## Layout

```
app/src/main/java/com/personaclick/demoshop/
├── ShopConfig.kt        shop id, stream, block codes
├── ShopApplication.kt   SDK initialization
├── ShopActivity.kt      the only activity, edge to edge
├── Shop.kt              the shop: its screens and the cart
├── ShopNavigator.kt     the shop's own stack of screens with back
├── ShopHome.kt          home
├── ShopSearchBar.kt     instant search on home
├── ShopSearch.kt        search results
├── ShopCategory.kt      category page
├── ShopProduct.kt       product page
├── ShopSort.kt          the sort sheet of search and category
└── ShopSupport.kt       product → card mapping, kit text, buttons, loading and error state
```
