package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.data.ProductDirectory
import com.example.data.database.PromoNotification
import com.example.data.database.UserReview
import com.example.ui.components.AlNasrBrandingLogo
import com.example.ui.components.PerfumeBottleIllustration
import java.text.SimpleDateFormat
import java.util.*

sealed class AppScreen(val route: String, val label: String) {
    object Explore : AppScreen("explore", "Browse Scent")
    object AIProfiler : AppScreen("profiler", "AI Profiler")
    object Cart : AppScreen("cart", "Royal Bag")
    object Notifications : AppScreen("notifications", "Inbox")
}

@Composable
fun FragranceApp(
    viewModel: FragranceViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Explore) }
    
    // ViewModel state collections
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val activeNotificationBanner by viewModel.activeNotificationBanner.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showCheckoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
    ) {
        // Main Body Layout with safe notch padding
        Scaffold(
            bottomBar = {
                LuxuryBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    cartSize = cartItems.sumOf { it.quantity }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Screen Navigation Routing
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                    },
                    label = "screen_routing"
                ) { screen ->
                    when (screen) {
                        is AppScreen.Explore -> ExploreScreen(
                            viewModel = viewModel,
                            searchText = searchText,
                            selectedCategory = selectedCategory,
                            products = filteredProducts,
                            favorites = favorites
                        )
                        is AppScreen.AIProfiler -> AIProfilerScreen(
                            viewModel = viewModel
                        )
                        is AppScreen.Cart -> CartScreen(
                            viewModel = viewModel,
                            cartItems = cartItems,
                            onCheckout = { showCheckoutDialog = true }
                        )
                        is AppScreen.Notifications -> NotificationsScreen(
                            viewModel = viewModel,
                            notifications = notifications
                        )
                    }
                }
            }
        }

        // 🌟 Full-Screen Sliding Details Overlay
        AnimatedVisibility(
            visible = selectedProduct != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedProduct?.let { product ->
                DetailScreen(
                    product = product,
                    viewModel = viewModel,
                    isFavorited = favorites.contains(product.id),
                    onClose = { viewModel.selectProduct(null) }
                )
            }
        }

        // 🔔 OVERLAY BANNER HUD for Immediate Simulated Push Notification
        AnimatedVisibility(
            visible = activeNotificationBanner != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .zIndex(100f)
        ) {
            activeNotificationBanner?.let { banner ->
                NotificationHUDCard(
                    notification = banner,
                    onDismiss = { viewModel.dismissNotificationBanner() },
                    onView = {
                        viewModel.dismissNotificationBanner()
                        currentScreen = AppScreen.Notifications
                    }
                )
            }
        }

        // 🛍️ Checkout Thank You Dialog
        if (showCheckoutDialog) {
            Dialog(onDismissRequest = {
                showCheckoutDialog = false
                viewModel.checkoutCart()
            }) {
                CheckoutConfirmationDialog(onDismiss = {
                    showCheckoutDialog = false
                    viewModel.checkoutCart()
                })
            }
        }
    }
}

// -------------------------------------------------------------
// BOTIQUE CORE SHEETS & VISUAL IMPLEMENTATIONS
// -------------------------------------------------------------

@Composable
fun ExploreScreen(
    viewModel: FragranceViewModel,
    searchText: String,
    selectedCategory: String?,
    products: List<Product>,
    favorites: List<String>
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Welcome Header Profile Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlNasrBrandingLogo(modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AL NASR",
                        style = TextStyle(
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Text(
                        text = "Attar & Fragrance Boutique",
                        style = TextStyle(color = Color(0xFFB59350), fontSize = 11.sp, letterSpacing = 0.5.sp)
                    )
                }
            }

            // Quick trigger button to test the push notifications system smoothly!
            IconButton(
                onClick = { viewModel.triggerSimulatedPushNotification() },
                modifier = Modifier
                    .background(Color(0x27D4AF37), CircleShape)
                    .border(1.dp, Color(0x7ED4AF37), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Simulate Push Notification",
                    tint = Color(0xFFD4AF37)
                )
            }
        }

        // Luxury Callout Banner (Golden Sands / Eid Offers)
        MarketingSlider(onResumeProfiler = {
            viewModel.triggerSimulatedPushNotification()
        })

        // Elegant Search Frame (Requested)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            TextField(
                value = searchText,
                onValueChange = { viewModel.setSearchText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar")
                    .border(
                        BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
                        RoundedCornerShape(12.dp)
                    ),
                placeholder = {
                    Text(
                        text = "Search Premium Oud, Rose, Musk notes...",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFB59350)
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchText("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Color.LightGray
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFFAF6EE),
                    unfocusedTextColor = Color(0xFFFAF6EE),
                    focusedContainerColor = Color(0xFF151515),
                    unfocusedContainerColor = Color(0xFF121212),
                    disabledContainerColor = Color(0xFF121212),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Category Selection Row
        val categories = listOf("All", "Premium Oud", "Sacred Musk", "Floral Blend", "Exotic Warmth", "Citrus Fresh", "Oriental Blend")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val displayCat = if (cat == "All") null else cat
                val isSelected = selectedCategory == displayCat
                Surface(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.selectCategory(displayCat)
                    },
                    modifier = Modifier.testTag("category_pill_${cat.lowercase().replace(" ", "_")}"),
                    shape = RoundedCornerShape(32.dp),
                    color = if (isSelected) Color(0xFFD4AF37) else Color(0xFF1E1E1E),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else Color(0xFFD4AF37).copy(alpha = 0.25f)
                    )
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else Color(0xFFFAF6EE),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Product Grid Layout
        if (products.isEmpty()) {
            EmptyResultsPlaceholder()
        } else {
            Text(
                text = "EXQUISITE COLLECTION",
                color = Color(0xFFB59350),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 10.dp)
            )

            // Renders product cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Split in pairs of 2 for grid look
                val chunked = products.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (product in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductGridCard(
                                    product = product,
                                    isFav = favorites.contains(product.id),
                                    onSelect = { viewModel.selectProduct(product) },
                                    onToggleFav = { viewModel.toggleFavorite(product.id) },
                                    onQuickAdd = {
                                        viewModel.addToCart(
                                            product.id,
                                            if (product.isOil) "12ml Attar Oil" else "100ml Cologne Spray",
                                            product.price
                                        )
                                    }
                                )
                            }
                        }
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketingSlider(onResumeProfiler: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF151515),
                            Color(0xFF2E2211),
                            Color(0xFF151515)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.65f)) {
                    Surface(
                        color = Color(0xFFD4AF37),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "OFFICIAL PARTNERSHIP OFFER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Claim Kingly Decanter Gift",
                        color = Color(0xFFFAF6EE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Press notifications bell icon to receive voucher codes",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onResumeProfiler,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Text(
                            text = "Test Push ✔",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    isFav: Boolean,
    onSelect: () -> Unit,
    onToggleFav: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp)
            .testTag("product_card_${product.id}")
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                // 🏺 CUSTOM ANIMATED BOTTLE GRAPHICAL CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D0D0D)),
                    contentAlignment = Alignment.Center
                ) {
                    PerfumeBottleIllustration(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp),
                        bottleColor = Color(android.graphics.Color.parseColor(product.bottleColorHex)),
                        bottleGradientColor = Color(android.graphics.Color.parseColor(product.bottleGradientHex))
                    )

                    // Oil / Spray Indicator Capsule
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = if (product.isOil) "Attar Oil" else "Eau Spray",
                            color = Color(0xFFD4AF37),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Favorite and Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = product.rating.toString(),
                            color = Color(0xFFFAF6EE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFav) Color(0xFFDE7047) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Title and notes list
                Text(
                    text = product.name,
                    color = Color(0xFFFAF6EE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Top Notes: ${product.notesTop}",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 1.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Pricing and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        color = Color(0xFFD4AF37),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onQuickAdd,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFD4AF37), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Add to Bag",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyResultsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No Fragrances Found",
            tint = Color(0xFF8C6D35),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Scent Discovered",
            color = Color(0xFFFAF6EE),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Try refining your search text or selecting a different brand category capsule.",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
        )
    }
}

// -------------------------------------------------------------
// USER RATING & DETAILED PRODUCT VIEW INTERACTIVE OVERLAYS
// -------------------------------------------------------------

@Composable
fun DetailScreen(
    product: Product,
    viewModel: FragranceViewModel,
    isFavorited: Boolean,
    onClose: () -> Unit
) {
    val reviewList by viewModel.selectedProductReviews.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Interactive custom state variables
    var selectedVolumeSize by remember { mutableStateOf(if (product.isOil) "12ml Regal Decanter" else "100ml Majestic Spray") }
    val basePriceMultiplier = if (selectedVolumeSize.contains("12ml") || selectedVolumeSize.contains("100ml")) 1.0 else 0.65
    val activePrice = product.price * basePriceMultiplier

    var showRatingForm by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF090909),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = Color(0xFFFAF6EE)
                    )
                }

                Text(
                    text = product.category.uppercase(),
                    color = Color(0xFFB59350),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp
                )

                IconButton(
                    onClick = { viewModel.toggleFavorite(product.id) },
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Add to Favorites",
                        tint = if (isFavorited) Color(0xFFDE7047) else Color.LightGray
                    )
                }
            }

            // Big Perfume Model Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF151515), Color(0xFF0C0C0C))
                        )
                    )
                    .border(BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.15f)), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                PerfumeBottleIllustration(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(16.dp),
                    bottleColor = Color(android.graphics.Color.parseColor(product.bottleColorHex)),
                    bottleGradientColor = Color(android.graphics.Color.parseColor(product.bottleGradientHex))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text Info & Bio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = product.name,
                    color = Color(0xFFFAF6EE),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "RatingStar",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${product.rating} / 5.0",
                        color = Color(0xFFD4AF37),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "(${reviewList.size + 12} Verified Connoisseurs)",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = product.description,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // BOTANICAL SCENT NOTES TREE
                Text(
                    text = "OLFACTORY NOTE PYRAMID",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                ScentNoteTierCard("👑 Top Notes", product.notesTop, Color(0xFFE5C158))
                ScentNoteTierCard("🌱 Heart Notes", product.notesHeart, Color(0xFF8C6D35))
                ScentNoteTierCard("🍂 Base Notes", product.notesBase, Color(0xFF4E3612))

                Spacer(modifier = Modifier.height(20.dp))

                // VOLUME SIZE TOGGLE
                Text(
                    text = "SELECT APOTHECARY VOLUME",
                    color = Color(0xFFFAF6EE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val option1 = if (product.isOil) "6ml Travel Vial" else "50ml Classic Spray"
                val option2 = if (product.isOil) "12ml Regal Decanter" else "100ml Majestic Spray"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VolumeButton(
                        text = option1,
                        isSelected = selectedVolumeSize == option1,
                        onClick = { selectedVolumeSize = option1 },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        text = option2,
                        isSelected = selectedVolumeSize == option2,
                        onClick = { selectedVolumeSize = option2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // PERSISTED REVIEWS AND RATINGS SECTION (Requested Feature)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CUSTOM REVIEWS (${reviewList.size})",
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )

                    Button(
                        onClick = { showRatingForm = !showRatingForm },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (showRatingForm) "Dismiss Rating" else "+ Submit Review",
                            fontSize = 10.sp,
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic Stars Rating Form Panel
                AnimatedVisibility(
                    visible = showRatingForm,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    WriteReviewPanel(
                        onSubmit = { name, rating, comment ->
                            viewModel.addReview(product.id, name, rating, comment)
                            showRatingForm = false
                        }
                    )
                }

                // List of persisted guest reviews
                if (reviewList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Be the first to leave a verified review of this majestic formulation!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        reviewList.forEach { review ->
                            ReviewItemRow(review = review)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Floating Bottom Sticky CTA Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616).copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedVolumeSize,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", activePrice)}",
                            color = Color(0xFFD4AF37),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.addToCart(product.id, selectedVolumeSize, activePrice)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(165.dp)
                            .height(44.dp)
                            .testTag("add_to_bag_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Cart Icon Detail",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Acquire Scent",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScentNoteTierCard(tierName: String, notesListString: String, tierColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color(0xFF2E2211)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tierColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = tierName,
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = notesListString,
                    color = Color(0xFFFAF6EE),
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun VolumeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF221A0F) else Color(0xFF121212)
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFFD4AF37) else Color(0xFF222222)
        ),
        modifier = modifier.height(44.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFFD4AF37) else Color(0xFFFAF6EE),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WriteReviewPanel(
    onSubmit: (String, Float, String) -> Unit
) {
    var reviewerName by remember { mutableStateOf("") }
    var selectedStarCount by remember { mutableStateOf(5f) }
    var reviewComment by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color(0x3DD4AF37)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WRITE DIARY ENTRY",
                color = Color(0xFFB59350),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Star selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scent Merit: ",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Review Star $i",
                            tint = if (i <= selectedStarCount) Color(0xFFD4AF37) else Color.DarkGray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { selectedStarCount = i.toFloat() }
                                .padding(2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name Field
            OutlinedTextField(
                value = reviewerName,
                onValueChange = { reviewerName = it },
                label = { Text("Your Renowned Name", color = Color.Gray, fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFFAF6EE),
                    unfocusedTextColor = Color(0xFFFAF6EE),
                    focusedBorderColor = Color(0xFFD4AF37),
                    unfocusedBorderColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_name_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Comment Field
            OutlinedTextField(
                value = reviewComment,
                onValueChange = { reviewComment = it },
                label = { Text("Olfactory Description / Impression", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFFAF6EE),
                    unfocusedTextColor = Color(0xFFFAF6EE),
                    focusedBorderColor = Color(0xFFD4AF37),
                    unfocusedBorderColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .testTag("review_comment_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (reviewComment.isNotBlank()) {
                        onSubmit(reviewerName, selectedStarCount, reviewComment)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(36.dp)
                    .testTag("submit_review_button")
            ) {
                Text(
                    text = "Submit Commendation",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ReviewItemRow(review: UserReview) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color(0xFF222222)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userName,
                    color = Color(0xFFFAF6EE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Review Star Row",
                            tint = if (i <= review.rating) Color(0xFFD4AF37) else Color.DarkGray,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.comment,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// -------------------------------------------------------------
// AI SCENT SPELL & PROFILE CHAMBER (Gemini REST Integration)
// -------------------------------------------------------------

@Composable
fun AIProfilerScreen(viewModel: FragranceViewModel) {
    val profilerInput by viewModel.profilerInput.collectAsStateWithLifecycle()
    val responseText by viewModel.profilerResponse.collectAsStateWithLifecycle()
    val isLoading by viewModel.profilerLoading.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val sparkOptions = listOf(
        "Spicy incense and warm oud for winter evening VIP party",
        "Clean Taif rose & citrus fresh splash for daily active workflow",
        "Magnetic dark deer musk, velvet sweetness for date nights",
        "Cozy sweet warm orange, incense aroma for autumn rain styling"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI Title Header
        AlNasrBrandingLogo(modifier = Modifier.size(84.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI SCENT PROFILER",
            color = Color(0xFFD4AF37),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "Consult the Royal Al Nasr Aromatherapist",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Premium Profiling Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Describe your desired vibe, outfit style, personality, or occasion:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = profilerInput,
                    onValueChange = { viewModel.setProfilerInput(it) },
                    placeholder = {
                        Text(
                            text = "Write freely: e.g., 'I want to feel powerful, mysterious yet fresh for an executive evening gala...'",
                            color = Color.DarkGray,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFAF6EE),
                        unfocusedTextColor = Color(0xFFFAF6EE),
                        focusedBorderColor = Color(0xFFD4AF37),
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .testTag("ai_assistant_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion chips
                Text(
                    text = "QUICK SPARKS:",
                    color = Color.Gray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sparkOptions.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D0D0D), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color(0x35D4AF37), RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.setProfilerInput(tag)
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "✦ $tag",
                                color = Color(0xFFB59350),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Invoke buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (responseText != null || profilerInput.isNotEmpty()) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.clearProfiler()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                            border = BorderStroke(1.dp, Color(0x7ED4AF37)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                "Clear",
                                color = Color(0xFFFAF6EE),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.runScentConsultation()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoading && profilerInput.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("submit_ai_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Consult AI Scent",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Invoke Royal Alchemist",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Response visual layout (Scrolling Paper Style Bounded Box)
        AnimatedVisibility(
            visible = isLoading || responseText != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Blending oils and balancing notes inside the Al Nasr laboratory...",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (responseText != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F1A)),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THE ROYAL RECOMMENDATION",
                                color = Color(0xFFD4AF37),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                        }

                        HorizontalDivider(
                            color = Color(0x35D4AF37),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Text(
                            text = responseText ?: "",
                            color = Color(0xFFFAF6EE),
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.testTag("ai_response_text")
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CART SHEETS & ROYAL BAG SUMMARY
// -------------------------------------------------------------

@Composable
fun CartScreen(
    viewModel: FragranceViewModel,
    cartItems: List<com.example.data.database.CartItem>,
    onCheckout: () -> Unit
) {
    val totalAmount = cartItems.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ROYAL SHOPPING BAG",
                    color = Color(0xFFFAF6EE),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Verify your customized selection",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { viewModel.checkoutCart() },
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), CircleShape)
                    .size(36.dp),
                enabled = cartItems.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Basket",
                    tint = if (cartItems.isNotEmpty()) Color(0xFFDE7047) else Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Empty Bag",
                    tint = Color(0xFF8C6D35),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your Bag is Unfilled",
                    color = Color(0xFFFAF6EE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acquire rare attars, explore notes in the catalog, or use AI Profiler to discover custom pairings.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    val resolvedProduct = ProductDirectory.getProductById(item.productId)
                    resolvedProduct?.let { product ->
                        CartItemRow(
                            product = product,
                            item = item,
                            onRemove = { viewModel.removeFromCart(item.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtotal Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal Sum", color = Color.Gray, fontSize = 13.sp)
                        Text("$${String.format("%.2f", totalAmount)}", color = Color(0xFFFAF6EE), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Boutique Delivery Delivery", color = Color.Gray, fontSize = 12.sp)
                        Text("COMPLIMENTARY", color = Color(0xFFD4AF37), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", color = Color(0xFFFAF6EE), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$${String.format("%.2f", totalAmount)}",
                            color = Color(0xFFD4AF37),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_button")
                    ) {
                        Text(
                            text = "Complete Regal purchase",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    product: Product,
    item: com.example.data.database.CartItem,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, Color(0xFF222222)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D0D0D)),
                contentAlignment = Alignment.Center
            ) {
                PerfumeBottleIllustration(
                    modifier = Modifier.size(56.dp).padding(4.dp),
                    bottleColor = Color(android.graphics.Color.parseColor(product.bottleColorHex)),
                    bottleGradientColor = Color(android.graphics.Color.parseColor(product.bottleGradientHex))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    color = Color(0xFFFAF6EE),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Size Selection: ${item.size}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                Text(
                    text = "QTY: ${item.quantity}  ×  $${String.format("%.2f", item.price)}",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove item from basket",
                    tint = Color.Gray
                )
            }
        }
    }
}

// -------------------------------------------------------------
// HISTORIC SYSTEM INBOX & NOTIFICATION ALERTS SHEET
// -------------------------------------------------------------

@Composable
fun NotificationsScreen(
    viewModel: FragranceViewModel,
    notifications: List<PromoNotification>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AL NASR NEWSROOM",
                    color = Color(0xFFFAF6EE),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Push alerts and member promotions history",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { viewModel.clearAllNotifications() },
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), CircleShape)
                    .size(36.dp),
                enabled = notifications.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Flush Alerts Logs",
                    tint = if (notifications.isNotEmpty()) Color(0xFFDE7047) else Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger dynamic simulation button inside feed
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181510)),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.7f)) {
                    Text(
                        text = "TEST NOTIFICATION PIPELINE",
                        color = Color(0xFFD4AF37),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Simulate Instant Push Alert",
                        color = Color(0xFFFAF6EE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Triggers a simulated notification. Watch it slide down! Added to DB logs.",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }

                Button(
                    onClick = { viewModel.triggerSimulatedPushNotification() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("simulate_push_button")
                ) {
                    Text(
                        "Trigger",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.MarkEmailRead,
                    contentDescription = "No alerts",
                    tint = Color(0xFF8C6D35),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Exclusive Inbox Is Empty",
                    color = Color(0xFFFAF6EE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Press 'Trigger' above to simulate a new push arrival or discount promotion instantly.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    NotificationHistoryRow(notification = notif)
                }
            }
        }
    }
}

@Composable
fun NotificationHistoryRow(notification: PromoNotification) {
    val dateString = remember(notification.timestamp) {
        val format = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
        format.format(Date(notification.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        border = BorderStroke(0.5.dp, Color(0xFF222222)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (notification.badge) {
                        "EXCLUSIVE" -> Color(0xFF8C6D35)
                        "NEW ARRIVAL" -> Color(0xFF0E4D64)
                        else -> Color(0xFFB59350)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = notification.badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = dateString,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notification.title,
                color = Color(0xFFFAF6EE),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = notification.description,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// -------------------------------------------------------------
// REUSABLE SUB-COMPONENTS & LAYOUT PARTS
// -------------------------------------------------------------

@Composable
fun LuxuryBottomBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    cartSize: Int
) {
    val items = listOf(AppScreen.Explore, AppScreen.AIProfiler, AppScreen.Cart, AppScreen.Notifications)
    
    // Explicitly add safe system navigation padding
    NavigationBar(
        containerColor = Color(0xFF111111),
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(BorderStroke(0.5.dp, Color(0xFF222222)))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { screen ->
            val isSelected = currentScreen.route == screen.route
            val icon = when (screen) {
                is AppScreen.Explore -> if (isSelected) Icons.Filled.Storefront else Icons.Outlined.Storefront
                is AppScreen.AIProfiler -> if (isSelected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
                is AppScreen.Cart -> if (isSelected) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag
                is AppScreen.Notifications -> if (isSelected) Icons.Filled.Mail else Icons.Outlined.Mail
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Box {
                        Icon(
                            imageVector = icon,
                            contentDescription = screen.label,
                            tint = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        if (screen is AppScreen.Cart && cartSize > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 10.dp, y = (-4).dp)
                                    .background(Color(0xFFDE7047), CircleShape)
                                    .size(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartSize.toString(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFFD4AF37) else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF222222)
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}

@Composable
fun NotificationHUDCard(
    notification: PromoNotification,
    onDismiss: () -> Unit,
    onView: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onView)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFD4AF37), CircleShape)
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Banner Bell Alert",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFFDE7047),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "NEW PUSH ALERT • ${notification.badge}",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = notification.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.description,
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Banner HUD",
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CheckoutConfirmationDialog(onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1814)),
        border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlNasrBrandingLogo(modifier = Modifier.size(96.dp))
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = "PURCHASE COMMENDED",
                color = Color(0xFFD4AF37),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your sovereign request is received. Our master perfumists are hand-wrapping your crystal fragrances in fine velvet, to be delivered to your palace of residence under complimentary dispatch.",
                color = Color(0xFFFAF6EE),
                fontSize = 11.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("dismiss_checkout_dialog")
            ) {
                Text(
                    text = "Return to Scent Chambers",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
