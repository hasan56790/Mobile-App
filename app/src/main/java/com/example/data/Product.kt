package com.example.data

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val notesTop: String,    // Comma-separated notes, e.g. "Saffron, Taif Rose"
    val notesHeart: String,  // Comma-separated notes, e.g. "Cambodian Oud, Patchouli"
    val notesBase: String,   // Comma-separated notes, e.g. "Ambergris, Sandalwood"
    val isOil: Boolean,      // Toggles between Attar pure oil and Eau De Parfum spray
    val bottleColorHex: String, // Dynamic tint color for custom beautiful bottle rendering
    val bottleGradientHex: String // Secondary color for gradient shading on custom bottles
)

object ProductDirectory {
    val items = listOf(
        Product(
            id = "oud_al_nasr",
            name = "Oud Al Nasr (Royal Attar)",
            category = "Premium Oud",
            description = "A majestic, deep blend of authentic Malaysian Oud, warm smoking incense, tobacco leaves, and rich glowing golden amber. Formulated as a concentrated, high-viscosity pure oil that clings to the skin with monumental longevity.",
            price = 120.0,
            rating = 4.9,
            notesTop = "Saffron, Bulgarian Rose, Nutmeg",
            notesHeart = "Malaysian Oud, Tobacco Leaf, Patchouli",
            notesBase = "Golden Amber, Guaiac Wood, Sandalwood",
            isOil = true,
            bottleColorHex = "#B8860B", // Dark Goldenrod
            bottleGradientHex = "#8B6508" // Deep gold
        ),
        Product(
            id = "musk_al_ghazal",
            name = "Musk Al Ghazal",
            category = "Sacred Musk",
            description = "A warm, velvet-smooth black musk recreation that pays homage to ancient Arabian perfumery. Laced with top notes of juicy dark forest berries and mellow honeycomb, softening into a raw, hypnotic skin scent.",
            price = 85.0,
            rating = 4.8,
            notesTop = "Wild Blackberry, Damask Plum, Anise",
            notesHeart = "Sacred Deer Musk Accord, White Honeycomb",
            notesBase = "Warm Leather, Black Tea, Cedarwood",
            isOil = true,
            bottleColorHex = "#4A0E17", // Rich Crimson Black
            bottleGradientHex = "#1F050A" // Jet obsidian crimson
        ),
        Product(
            id = "sultani_rose",
            name = "Sultani Rose & Jasmine",
            category = "Floral Blend",
            description = "A sublime, majestic floral celebration showcasing freshly harvested Taif Rose petals and white night-blooming jasmine flowers. Interwoven with clean vanilla pod absolute and deep earthy orris butter.",
            price = 95.0,
            rating = 4.9,
            notesTop = "Taif Rose, Bergamot Peel, Lemon Blossom",
            notesHeart = "Sambac Jasmine, Royal Geranium, Orris Butter",
            notesBase = "Sandalwood Cream, Madagascar Vanilla, White Musk",
            isOil = true,
            bottleColorHex = "#C71585", // Medium Violet Red
            bottleGradientHex = "#700540" // Night magenta
        ),
        Product(
            id = "golden_dunes",
            name = "Golden Dunes Spray",
            category = "Exotic Warmth",
            description = "An exquisite French-Arabian crossover capturing the radiant warmth and dry spice of dunes under the setting sun. Sparkles with hot pink pepper-cardamom and slowly dries down to a rich, resinous vanilla and cinnamon core.",
            price = 110.0,
            rating = 4.7,
            notesTop = "Green Cardamom, Pink Pepper, Grapefruit",
            notesHeart = "Ceylon Cinnamon, Labdanum Resin, Nutmeg",
            notesBase = "Amber Essence, Sandalwood Mysore, Tonka Bean",
            isOil = false,
            bottleColorHex = "#EEB422", // Golden amber
            bottleGradientHex = "#CD9B1D" // Warm dark amber
        ),
        Product(
            id = "aura_of_victory",
            name = "Aura of Victory (Al Nasr EDP)",
            category = "Citrus Fresh",
            description = "An empowering, fresh Eau De Parfum celebrating triumph and modern dynamics. Juicy, crisp Calabrian bergamot and green apple open up to a smoky, sophisticated base of vetiver and rare ambergris.",
            price = 130.0,
            rating = 4.6,
            notesTop = "Calabrian Bergamot, Green Apple, Mint Leaf",
            notesHeart = "Smoky Vetiver, Patchouli Coeur, Birch Wood",
            notesBase = "Oceanic Ambergris, Oakmoss, Oakwood Musk",
            isOil = false,
            bottleColorHex = "#0E4D64", // Deep teal blue
            bottleGradientHex = "#041F2A" // Deep ocean midnight tint
        ),
        Product(
            id = "saba_warm_amber",
            name = "Saba Warm Amber",
            category = "Oriental Blend",
            description = "A cozy, comforting oriental scent wrapped around dark melted chocolate, spicy incense smoke, and golden balsamic amber. Highly addictive, perfect for cool evenings.",
            price = 75.0,
            rating = 4.7,
            notesTop = "Mandarin Zest, Orange Blossom, Raw Cacao",
            notesHeart = "Balsamic Benzoin, Frankincense, Jasmine Petal",
            notesBase = "Sweet Golden Amber, Bourbon Vanilla, Patchouli",
            isOil = true,
            bottleColorHex = "#CD661D", // Dark Orange/Amber
            bottleGradientHex = "#5E2605" // Warm brown oud tint
        )
    )

    fun getProductById(id: String): Product? {
        return items.find { it.id == id }
    }
}
